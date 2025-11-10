package com.example.parkfinder

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.room.Room
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random
import android.os.Handler
import android.os.Looper



class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var db: AppDatabase
    private lateinit var dao: ParkingSpotDao
    private lateinit var mMap: GoogleMap
    private val markerMap = mutableMapOf<String, Marker>()
    private var selectedLatLng: LatLng? = null
    private val channelId = "navigation_reminder" // ✅ fixed naming warning

    private val baseLat = 40.640063
    private val baseLng = 22.944419

    private val parkingSpotData = mutableMapOf<String, Triple<String, String, LatLng>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "ParkFinder"
        supportActionBar?.subtitle = "Find open parking near you"

        //  Initialize Room Database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "parking-db"
        ).build()

        dao = db.parkingSpotDao()

        //  Filter buttons
        findViewById<Button>(R.id.btn_all).setOnClickListener { filterMarkers("all") }
        findViewById<Button>(R.id.btn_available).setOnClickListener { filterMarkers("available") }
        findViewById<Button>(R.id.btn_full).setOnClickListener { filterMarkers("full") }

        //  Navigate button
        findViewById<Button>(R.id.btn_navigate).setOnClickListener {
            selectedLatLng?.let { latLng ->
                Toast.makeText(
                    this,
                    "🚗 Navigating... press back or tap notification to return",
                    Toast.LENGTH_LONG
                ).show()
                showReturnNotification()

                val userLat = 40.640063  // Ag. Dimitriou
                val userLng = 22.944419
                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$userLat,$userLng&destination=${latLng.latitude},${latLng.longitude}&travelmode=driving")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Load map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //  Generate parking spots from DB
        generateParkingSpots()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //  Hardcoded user location in Thessaloniki (Ag. Dimitriou 55)
        val userLocation = LatLng(40.640063, 22.944419)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        //  Add a large blue marker for user location
        mMap.addMarker(
            MarkerOptions()
                .position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        //  Add markers
        parkingSpotData.forEach { (title, triple) ->
            val (status, _, location) = triple
            val color = if (status.contains("✅")) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
            marker?.let { markerMap[title] = it }
        }

        //  Marker click
        mMap.setOnMarkerClickListener { marker ->
            parkingSpotData[marker.title]?.let { (status, distance, latLng) ->
                findViewById<TextView>(R.id.spot_title).text = marker.title
                findViewById<TextView>(R.id.spot_status).text = status
                findViewById<TextView>(R.id.spot_distance).text = distance
                selectedLatLng = latLng
                findViewById<Button>(R.id.btn_navigate).visibility = View.VISIBLE

                // If spot is available, allow booking
                if (status.contains("✅")) {
                    showBookingDialog(marker.title!!)
                }
            }
            marker.showInfoWindow()
            true
        }


        //  Zoom buttons
        findViewById<Button>(R.id.btn_zoom_in).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        findViewById<Button>(R.id.btn_zoom_out).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }

        //  Default marker info
        parkingSpotData["Parking Spot #1"]?.let {
            findViewById<TextView>(R.id.spot_title).text = "Parking Spot #1"
            findViewById<TextView>(R.id.spot_status).text = it.first
            findViewById<TextView>(R.id.spot_distance).text = it.second
        }
    }

    private fun generateParkingSpots() {
        lifecycleScope.launch {
            val spotsFromDb = dao.getAllSpots()

            // If DB is empty, generate dummy spots and save them to DB
            if (spotsFromDb.isEmpty()) {
                val newSpots = mutableListOf<ParkingSpot>()
                val rand = Random(System.currentTimeMillis())

                for (i in 1..100) {
                    val latOffset = (rand.nextDouble() - 0.5) / 200
                    val lngOffset = (rand.nextDouble() - 0.5) / 200
                    val lat = baseLat + latOffset
                    val lng = baseLng + lngOffset
                    val isAvailable = rand.nextBoolean()
                    val status = if (isAvailable) "✅ Available" else "❌ Full"
                    val distance = "📍 ${(250..900).random()}m from you"

                    newSpots.add(
                        ParkingSpot(
                            title = "Parking Spot #$i",
                            status = status,
                            distance = distance,
                            latitude = lat,
                            longitude = lng
                        )
                    )
                }

                dao.insertAll(newSpots)
            }

            // Load data from DB
            val finalSpots = dao.getAllSpots()
            parkingSpotData.clear()
            finalSpots.forEach { spot ->
                val location = LatLng(spot.latitude, spot.longitude)
                parkingSpotData[spot.title] = Triple(spot.status, spot.distance, location)
            }

            // Refresh map
            if (::mMap.isInitialized) {
                mMap.clear()
                onMapReady(mMap)
            }
        }
    }

    private fun showBookingDialog(spotName: String) {
        val durations = arrayOf("1 hour", "2 hours","3 hours","4 hours","5 hours","6 hours","7 hours")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Book this spot")
        builder.setItems(durations) { dialog: android.content.DialogInterface, which: Int ->
            val selectedDuration = durations[which]
            showPaymentDialog(spotName, selectedDuration)


            // 🔁 Update the database
            lifecycleScope.launch {
                val spot = dao.getSpotByName(spotName)
                if (spot != null) {
                    val updatedSpot = spot.copy(
                        status = "❌ Full",
                        distance = "⏱ Booked for $selectedDuration"
                    )
                    dao.insertAll(listOf(updatedSpot))

                    // ✅ Refresh UI
                    generateParkingSpots()
                    Toast.makeText(this@MainActivity, "Spot booked!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showPaymentDialog(spotName: String, duration: String) {
        val input = TextView(this).apply {
            text = "💳 Payment processed (mock)"
            setPadding(50, 50, 50, 50)
        }

        AlertDialog.Builder(this)
            .setTitle("Payment for $duration")
            .setView(input)
            .setPositiveButton("Confirm & Pay") { _, _ ->
                completeBooking(spotName, duration)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun completeBooking(spotName: String, duration: String) {
        lifecycleScope.launch {
            val spot = dao.getSpotByName(spotName)
            if (spot != null) {
                val updatedSpot = spot.copy(
                    status = "❌ Full",
                    distance = "⏱ Booked for $duration"
                )
                dao.insertAll(listOf(updatedSpot))
                generateParkingSpots()
                Toast.makeText(this@MainActivity, "✅ Booking complete!", Toast.LENGTH_SHORT).show()

                //  Convert duration to milliseconds
                val delayMillis = when (duration) {
                    "30 minutes" -> 30 * 60 * 1000L
                    "1 hour" -> 60 * 60 * 1000L
                    "2 hours" -> 2 * 60 * 60 * 1000L
                    else -> 60 * 60 * 1000L
                }

                //  Automatically mark spot as available again
                Handler(mainLooper).postDelayed({
                    lifecycleScope.launch {
                        val refreshedSpot = dao.getSpotByName(spotName)
                        if (refreshedSpot != null) {
                            val resetSpot = refreshedSpot.copy(
                                status = "✅ Available",
                                distance = "📍 500m from you"
                            )
                            dao.insertAll(listOf(resetSpot))
                            generateParkingSpots()
                            Toast.makeText(this@MainActivity, "🟢 Spot available again!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, delayMillis)
            }
        }
    }


    private fun filterMarkers(type: String) {
        markerMap.forEach { (title, marker) ->
            val status = parkingSpotData[title]?.first ?: ""
            marker.isVisible = when (type) {
                "available" -> status.contains("✅")
                "full" -> status.contains("❌")
                else -> true
            }
        }
    }

    private fun showReturnNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Navigation Reminder"
            val descriptionText = "Tap to return to ParkFinder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_directions)
            .setContentTitle("🚗 Navigating in Google Maps")
            .setContentText("Tap to return to ParkFinder")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notify(101, builder.build())
            }
        }
    }
}
