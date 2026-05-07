🚗 ParkFinder – Android Parking Application for Thessaloniki
ParkFinder is an Android application that helps users find parking facilities around Thessaloniki, view them on a map, book a parking space for a selected duration, and navigate to the chosen location using Google Maps.

The application was developed as a lightweight prototype with an offline-first approach. It uses local data to simulate parking availability across the city and presents the information in a simple and easy-to-use way. Each parking facility contains multiple parking positions, and availability is shown using a three-colour system:

🟢 Green – all positions available  
🟠 Orange – some positions available  
🔴 Red – no positions available

✨ Features

🗺️ Google Maps Integration
View parking facilities on an interactive Google Map
Display facilities with colour-coded markers based on availability
See parking locations across Thessaloniki at a glance.

🕒 Booking System
Tap on an available parking facility
Enter the duration needed for parking
Reserve a parking space locally through the app
Update availability immediately after booking

🧭 Navigation Support
Open Google Maps with turn-by-turn driving directions
Use the user’s current location as the starting point

🗄️ Local Database (Room)
Store parking facilities, positions, bookings, and tariff information locally on the device
Use multiple tables to model the parking data in a more realistic way
Keep booking and availability information persistent

📍 Location and Distance Handling
Show the user’s current location on the map when permission is granted
Display a more realistic distance estimate to the selected facility

---

## 🏗️ Architecture Overview

com.example.parkfinder
├── MainActivity.kt        # Main screen logic: map, filters, booking, navigation
├── data/
│   ├── AppDatabase.kt     # Room database setup
│   ├── Facility.kt        # Parking facility entity
│   ├── Position.kt        # Parking position entity
│   ├── Booking.kt         # Booking entity
│   ├── Tariff.kt          # Tariff/rules entity
│   └── DAO classes        # Data access objects for queries
├── ui/
│   └── activity_main.xml  # Layout with toolbar, buttons, map and info panel


UI layer: handles the map, filters, booking input, and navigation actions
Data layer: manages local persistence using Room
Map layer: displays facilities and availability using Google Maps
Location layer: handles current position and distance estimation

---

## 🧪 How It Works

The app loads parking facilities for Thessaloniki from the local database.
Each facility contains multiple parking positions.
Availability is calculated from the number of free positions and shown with the green / orange / red marker system.
When the user selects a facility, the app displays:
-the facility name
-the address
-the number of free and total positions
-the estimated distance from the user
The user can book an available facility for the required time.
Booking updates the local database and refreshes the marker colour immediately.
Pressing Navigate opens Google Maps with directions to the selected facility.

---

## 🛠️ Setup Instructions

### 1. Clone the repository
bash
git clone https://github.com/CSD-CPM/dissertation-code-submission-nickXenos.git

### 2. Open with Android Studio

### 3. Enable Google Maps API
Go to Google Cloud Console
Enable Maps SDK for Android
Generate an API key

### 4. Add your API key
Insert inside AndroidManifest.xml:
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE"/>

### 5. Run the app
On a physical Android device
Or an emulator with Google Play Services

📦 Requirements
Android Studio
Android API level compatible with the project
Google Play Services
Internet access for Google Maps and navigation features

### 🔮 Future Improvements

Real-time parking availability using sensors or live APIs
User accounts and booking history
Cloud synchronisation with Firebase
Real payment integration with Stripe or Google Pay
Marker clustering for dense areas
Improved route and distance accuracy

### 👨‍💻 Author
Xenos Nikolaos Sarantis — Developer
