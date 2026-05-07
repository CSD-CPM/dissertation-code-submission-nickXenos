# 🚗 ParkFinder – Android Parking Application for Thessaloniki

ParkFinder is an Android application that helps users find available parking spots around Thessaloniki, view them on a map, book a spot for a selected time, and navigate to it using Google Maps.

---

## ✨ Features

### 🗺️ Google Maps Integration
- View all nearby parking spots with color-coded markers  
  - 🟢 **Available**  
  - 🔴 **Full**

### 🕒 Booking System
- Tap an available spot
- Choose booking duration (1h, 2h, 3h, etc.)
- Spot becomes **Full** during the booking time
- Automatic reset: spots return to **Available** after time passes

### 🧭 Navigation Support
- Opens **Google Maps** with turn-by-turn driving directions

### 🗄️ Local Database (Room)
- Stores spot availability and booking states persistently on the device
- DAO, Entities, and Database setup included

### 🔔 Notifications
- Reminder sent when navigating
- Useful alerts during map navigation

---

## 🏗️ Architecture Overview

com.example.parkfinder
├── MainActivity.kt # Main logic: map, UI, booking, navigation
├── data/
│ ├── AppDatabase.kt # Room database setup
│ ├── ParkingSpot.kt # Entity class
│ └── ParkingSpotDao.kt# DAO for database queries
├── ui/
│ └── activity_main.xml # Layout with buttons, map, status panel


- MVVM-lite structure
- Google Maps API + Room DB
- Clean separation of UI, Data, and Logic

---

## 🧪 How It Works

- The app generates **100 dummy parking spots** around Thessaloniki.
- Each spot has:
  - Title  
  - Availability (🟢 / 🔴)  
  - Coordinates  
  - Distance from user  
- Booking updates the DB and UI instantly.
- Pressing **Navigate** opens Google Maps with navigation from the user’s location to the selected spot.

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
Android Studio Bumblebee or newer
Android API Level 29+
Google Play Services
Internet access for map & navigation

### 🔮 Future Improvements

Firebase for cloud syncing
User login & account history
Payment integration (Stripe, Google Pay)
Real-time availability using sensors or live data

### 👨‍💻 Author
Xenos Nikolaos Sarantis — Developer
