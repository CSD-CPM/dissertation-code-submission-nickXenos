package com.example.parkfinder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var db: AppDatabase
    private lateinit var mMap: GoogleMap

    private val baseLat = 40.640063
    private val baseLng = 22.944419
    private var selectedLocation: ParkingLocation? = null

    private val markerMap = mutableMapOf<Int, Marker>()
    private val channelId = "navigation_reminder"
    private val ORANGE_HUE = 30f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "ParkFinder"
        supportActionBar?.subtitle = "Find parking near you"

        db = AppDatabase.getDatabase(applicationContext)

        findViewById<Button>(R.id.btn_all).setOnClickListener { filterMarkers("all") }
        findViewById<Button>(R.id.btn_available).setOnClickListener { filterMarkers("available") }
        findViewById<Button>(R.id.btn_full).setOnClickListener { filterMarkers("full") }
        findViewById<Button>(R.id.btn_partial).setOnClickListener { filterMarkers("partial") }
        findViewById<Button>(R.id.btn_navigate).setOnClickListener {
            selectedLocation?.let { loc ->
                showReturnNotification()
                navigateToLocation(loc)
            }
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        seedDatabaseIfEmpty()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val userLoc = LatLng(baseLat, baseLng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 15f))

        mMap.addMarker(
            MarkerOptions()
                .position(userLoc)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        loadMarkers()
        setupMarkerClick()
        setupZoomButtons()
    }

    // --------------------------- DATABASE SEEDING ---------------------------
    private fun seedDatabaseIfEmpty() {
        lifecycleScope.launch {
            val locations = db.locationDao().getAllLocations()
            if (locations.isNotEmpty()) return@launch

            val random = Random(System.currentTimeMillis())

            // Thessaloniki neighborhoods with realistic coordinates
            val neighborhoods = listOf(

                Pair(40.6401, 22.9444),
                Pair(40.6389, 22.9456),
                Pair(40.6415, 22.9432),
                Pair(40.6395, 22.9438),
                Pair(40.6408, 22.9460),
                Pair(40.6378, 22.9448),
                Pair(40.6420, 22.9420),
                Pair(40.6385, 22.9465),
                Pair(40.6410, 22.9475),
                Pair(40.6398, 22.9418),
                Pair(40.6425, 22.9450),
                Pair(40.6372, 22.9440),
                Pair(40.6405, 22.9430),
                Pair(40.6418, 22.9408),
                Pair(40.6392, 22.9470),
                // ---- ARISTOTELOUS / WATERFRONT ----
                Pair(40.6350, 22.9350),
                Pair(40.6335, 22.9370),
                Pair(40.6360, 22.9340),
                Pair(40.6345, 22.9360),
                Pair(40.6325, 22.9380),
                // ---- ANO POLI (Upper Town) ----
                Pair(40.6480, 22.9510),
                Pair(40.6510, 22.9490),
                Pair(40.6495, 22.9530),
                // ---- TOUMBA ----
                Pair(40.6210, 22.9650),
                Pair(40.6190, 22.9680),
                Pair(40.6230, 22.9620),
                // ---- KALAMARIA ----
                Pair(40.5980, 22.9630),
                Pair(40.5950, 22.9660),
                Pair(40.6010, 22.9600),
                // ---- STAVROUPOLI ----
                Pair(40.6620, 22.9200),
                Pair(40.6650, 22.9180),
                Pair(40.6600, 22.9220),
                // ---- EVOSMOS ----
                Pair(40.6720, 22.8900),
                Pair(40.6750, 22.8870),
                Pair(40.6700, 22.8930),
                // ---- PYLAIA ----
                Pair(40.5850, 22.9980),
                Pair(40.5820, 23.0010),
                Pair(40.5880, 22.9950),
                // ---- SIKIES ----
                Pair(40.6550, 22.9380),
                Pair(40.6580, 22.9350),
                Pair(40.6530, 22.9410),
                // ---- NEAPOLI ----
                Pair(40.6450, 22.9600),
                Pair(40.6470, 22.9580),
                Pair(40.6430, 22.9620),
                // ---- PANORAMA ----
                Pair(40.5920, 22.9780),
                Pair(40.5900, 22.9810),
                Pair(40.5940, 22.9750),
                // ---- AMPELOKIPOI ----
                Pair(40.6300, 22.9550),
                Pair(40.6280, 22.9580),
                Pair(40.6320, 22.9520),
                // ---- TRIANDRIA ----
                Pair(40.6150, 22.9720),
                Pair(40.6130, 22.9750),
                Pair(40.6170, 22.9690),
                // ---- ORAIOKASTRO ----
                Pair(40.7020, 22.9050),
                Pair(40.7050, 22.9020),
                Pair(40.6990, 22.9080),
                // ---- THERMI ----
                Pair(40.5480, 23.0200),
                Pair(40.5450, 23.0230),
                Pair(40.5510, 23.0170),
                // ---- POLICHNI ----
                Pair(40.6680, 22.9100),
                Pair(40.6710, 22.9070),
                Pair(40.6650, 22.9130),
                // ---- KORDELIO ----
                Pair(40.6580, 22.8980),
                Pair(40.6610, 22.8950),
                Pair(40.6550, 22.9010)
            )


            neighborhoods.forEachIndexed { index, (lat, lng) ->
                // Add small random offset so spots don't stack exactly
                val jitterLat = lat + (random.nextDouble() - 0.5) * 0.002
                val jitterLng = lng + (random.nextDouble() - 0.5) * 0.002

                val totalSpaces = (2..10).random()

                val loc = ParkingLocation(
                    name = "Parking #${index + 1} - ${getNeighborhoodName(index)}",
                    latitude = jitterLat,
                    longitude = jitterLng,
                    totalSpaces = totalSpaces
                )

                val id = db.locationDao().insertLocation(loc).toInt()

                val spaces = (1..totalSpaces).map {
                    ParkingSpace(locationId = id, isAvailable = (1..100).random() > 55)
                }
                db.spaceDao().insertSpaces(spaces)
            }

            loadMarkers()
        }
    }

    private fun getNeighborhoodName(index: Int): String {
        val names = listOf(
            // City Center (15 spots)
            "City Center", "City Center", "City Center",
            "City Center", "City Center", "City Center",
            "City Center", "City Center", "City Center",
            "City Center", "City Center", "City Center",
            "City Center", "City Center", "City Center",
            // Waterfront (5 spots)
            "Waterfront", "Waterfront", "Waterfront",
            "Waterfront", "Waterfront",
            // Ano Poli (3 spots)
            "Ano Poli", "Ano Poli", "Ano Poli",
            // Toumba (3 spots)
            "Toumba", "Toumba", "Toumba",
            // Kalamaria (3 spots)
            "Kalamaria", "Kalamaria", "Kalamaria",
            // Stavroupoli (3 spots)
            "Stavroupoli", "Stavroupoli", "Stavroupoli",
            // Evosmos (3 spots)
            "Evosmos", "Evosmos", "Evosmos",
            // Pylaia (3 spots)
            "Pylaia", "Pylaia", "Pylaia",
            // Sikies (3 spots)
            "Sikies", "Sikies", "Sikies",
            // Neapoli (3 spots)
            "Neapoli", "Neapoli", "Neapoli",
            // Panorama (3 spots)
            "Panorama", "Panorama", "Panorama",
            // Ampelokipoi (3 spots)
            "Ampelokipoi", "Ampelokipoi", "Ampelokipoi",
            // Triandria (3 spots)
            "Triandria", "Triandria", "Triandria",
            // Oraiokastro (3 spots)
            "Oraiokastro", "Oraiokastro", "Oraiokastro",
            // Thermi (3 spots)
            "Thermi", "Thermi", "Thermi",
            // Polichni (3 spots)
            "Polichni", "Polichni", "Polichni",
            // Kordelio (3 spots)
            "Kordelio", "Kordelio", "Kordelio"
        )
        return if (index < names.size) names[index] else "Thessaloniki"
    }


    // --------------------------- MAP MARKERS ---------------------------
    private fun loadMarkers() {
        lifecycleScope.launch {
            val locations = db.locationDao().getAllLocations()
            mMap.clear()
            markerMap.clear()

            val userLoc = LatLng(baseLat, baseLng)
            mMap.addMarker(
                MarkerOptions()
                    .position(userLoc)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            for (loc in locations) {
                val available = db.spaceDao().getAvailableSpaces(loc.id)
                val total = loc.totalSpaces

                val color = when {
                    available == total -> BitmapDescriptorFactory.HUE_GREEN
                    available == 0 -> BitmapDescriptorFactory.HUE_RED
                    else -> ORANGE_HUE
                }

                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(loc.latitude, loc.longitude))
                        .title(loc.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                )

                if (marker != null) {
                    marker.tag = loc.id
                    markerMap[loc.id] = marker
                }
            }
        }
    }

    private fun setupMarkerClick() {
        mMap.setOnMarkerClickListener { marker ->
            val locationId = marker.tag as? Int ?: return@setOnMarkerClickListener false

            lifecycleScope.launch {
                val loc = db.locationDao().getAllLocations().firstOrNull { it.id == locationId }
                if (loc != null) {
                    selectedLocation = loc
                    updateInfoPanel(loc)
                    if (db.spaceDao().getAvailableSpaces(loc.id) > 0) {
                        showBookingDialog(loc)
                    }
                }
            }
            true
        }
    }

    // --------------------------- INFO PANEL ---------------------------
    private fun updateInfoPanel(loc: ParkingLocation) {
        lifecycleScope.launch {
            val available = db.spaceDao().getAvailableSpaces(loc.id)
            val (distanceKm, minutes) = calculateDistance(baseLat, baseLng, loc.latitude, loc.longitude)

            val formattedDistance = if (distanceKm < 1.0) {
                "${(distanceKm * 1000).toInt()} m"
            } else {
                String.format("%.1f km", distanceKm)
            }

            val timeText = if (minutes < 60) {
                "$minutes min"
            } else {
                val h = minutes / 60
                val m = minutes % 60
                if (m == 0) "${h}h" else "${h}h ${m}min"
            }

            findViewById<TextView>(R.id.spot_title).text = loc.name

            val statusText = when {
                available == loc.totalSpaces -> "🟢 All spaces free"
                available == 0 -> "🔴 Fully booked"
                else -> "🟠 Partially available"
            }

            findViewById<TextView>(R.id.spot_status).text =
                "$statusText ($available / ${loc.totalSpaces})"

            findViewById<TextView>(R.id.spot_distance).text =
                "📍 $formattedDistance away  🚗 $timeText"

            findViewById<Button>(R.id.btn_navigate).visibility = View.VISIBLE
        }
    }


    //----------PAYMENT COUNT--------
    private fun calculatePrice(hours: Int, minutes: Int): Double {
        val totalMinutes = (hours * 60) + minutes
        return totalMinutes * (2.0 / 60.0) // 2 euros per hour = 0.0333 per minute
    }


    // --------------------------- BOOKING ---------------------------
    private fun showBookingDialog(location: ParkingLocation) {
        val dialogView = layoutInflater.inflate(android.R.layout.activity_list_item, null)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        val hoursInput = android.widget.EditText(this).apply {
            hint = "Hours (0-23)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("1")
        }

        val minutesInput = android.widget.EditText(this).apply {
            hint = "Minutes (0-59)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("0")
        }

        val hoursLabel = TextView(this).apply { text = "Hours:" }
        val minutesLabel = TextView(this).apply { text = "Minutes:" }

        layout.addView(hoursLabel)
        layout.addView(hoursInput)
        layout.addView(minutesLabel)
        layout.addView(minutesInput)

        AlertDialog.Builder(this)
            .setTitle("Select booking duration for ${location.name}")
            .setView(layout)
            .setPositiveButton("Continue") { _, _ ->
                val hours = hoursInput.text.toString().toIntOrNull() ?: 0
                val minutes = minutesInput.text.toString().toIntOrNull() ?: 0

                if (hours == 0 && minutes == 0) {
                    Toast.makeText(this, "Duration must be at least 1 minute", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (hours > 23) {
                    Toast.makeText(this, "Hours cannot exceed 23", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (minutes > 59) {
                    Toast.makeText(this, "Minutes cannot exceed 59", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                showPaymentDialog(location, hours, minutes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showPaymentDialog(location: ParkingLocation, hours: Int, minutes: Int) {
        val price = calculatePrice(hours, minutes)
        val formattedPrice = String.format("%.2f", price)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        val details = TextView(this).apply {
            text = "📍 Location: ${location.name}\n" +
                    "⏱ Duration: ${hours}h ${minutes}m\n" +
                    "💶 Total price: €$formattedPrice"
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }

        layout.addView(details)

        AlertDialog.Builder(this)
            .setTitle("Confirm Booking")
            .setView(layout)
            .setPositiveButton("Confirm & Pay €$formattedPrice") { _, _ ->
                completeBooking(location, hours, minutes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun completeBooking(location: ParkingLocation, hours: Int, minutes: Int) {
        lifecycleScope.launch {
            val spaces = db.spaceDao().getSpacesForLocation(location.id)
            val freeSpace = spaces.firstOrNull { it.isAvailable }

            if (freeSpace == null) {
                Toast.makeText(this@MainActivity, "No available spaces!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Mark space as occupied
            db.spaceDao().updateSpace(freeSpace.copy(isAvailable = false))

            // Calculate duration correctly (hours + minutes)
            val durationMillis = (hours * 60L * 60L * 1000L) + (minutes * 60L * 1000L)
            val endTime = System.currentTimeMillis() + durationMillis

            db.bookingDao().insertBooking(
                Booking(
                    locationId = location.id,
                    startTime = System.currentTimeMillis(),
                    endTime = endTime,
                    spacesBooked = 1
                )
            )

            Toast.makeText(this@MainActivity, "Booking complete!", Toast.LENGTH_SHORT).show()
            loadMarkers()
            updateInfoPanel(location)

            // Auto-release after duration
            Handler(Looper.getMainLooper()).postDelayed({
                lifecycleScope.launch {
                    db.spaceDao().updateSpace(freeSpace.copy(isAvailable = true))
                    loadMarkers()
                }
            }, durationMillis)
        }
    }

    // --------------------------- DISTANCE CALC ---------------------------
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Pair<Double, Int> {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceMeters = R * c

        val distanceKm = distanceMeters / 1000.0
        // Average driving speed in city = 30 km/h
        val minutes = ((distanceKm / 30.0) * 60).toInt().coerceAtLeast(1)

        return Pair(distanceKm, minutes)
    }


    // --------------------------- NAVIGATION ---------------------------
    private fun navigateToLocation(loc: ParkingLocation) {
        val originLat = baseLat
        val originLng = baseLng
        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=$originLat,$originLng" +
                    "&destination=${loc.latitude},${loc.longitude}" +
                    "&travelmode=driving"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    // --------------------------- NOTIFICATION ---------------------------
    private fun showReturnNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Navigation Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
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
            .setContentTitle("🚗 Navigation Started")
            .setContentText("Tap to return to ParkFinder")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notify(101, builder.build())
            }
        }
    }

    // --------------------------- ZOOM / FILTER ---------------------------
    private fun setupZoomButtons() {
        findViewById<Button>(R.id.btn_zoom_in).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        findViewById<Button>(R.id.btn_zoom_out).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    private fun filterMarkers(type: String) {
        lifecycleScope.launch {
            val locations = db.locationDao().getAllLocations()
            for ((id, marker) in markerMap) {
                val loc = locations.first { it.id == id }
                val available = db.spaceDao().getAvailableSpaces(id)
                val total = loc.totalSpaces
                marker.isVisible = when (type) {
                    "available" -> available == total
                    "partial" -> available in 1 until total
                    "full" -> available == 0
                    else -> true
                }
            }
        }
    }
}
