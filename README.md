# 🍳 Cooking Game App (Kotlin - Android)

A multiplayer cooking game where players complete recipe tasks using drag-and-drop, motion gestures, and real-time room-based sessions. Developed entirely in Kotlin for Android with Firebase integration.

---

## 📁 Project Structure

```
CookingGameApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cookinggameapp/
│   │   │   ├── res/layout/
│   │   │   ├── res/drawable/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### 🧰 Prerequisites

- Android Studio Hedgehog (or later)
- Android SDK 35+
- Kotlin Plugin
- Emulator or Android phone (API 24+)
- Firebase project (with Firestore enabled)

---

## 🧪 Steps to Run the Project

### 1. Clone the Repository

```bash
git clone https://github.com/traanf-thuw/cooking-game
cd cooking-game/cooking-game/CookingGameApp
```

### 2. Open in Android Studio

- Open Android Studio
- File → Open → Select the `CookingGameApp` folder
- Wait for Gradle sync to complete

### 3. Add Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project (e.g., `CookingGameApp`)
3. Register an Android app with package name: `com.example.cookinggameapp`
4. Download the `google-services.json` file
5. Place it in:

```
app/src/main/google-services.json
```

6. Enable Firestore in Firebase Console

### 4. Build and Run

- Connect a physical Android device or start an emulator
- Run the app from Android Studio

---

## 🔑 Permissions Required

The game requests:

- `ACCESS_FINE_LOCATION` – Needed for Nearby communication
- `INTERNET` – For Firebase
- `CAMERA` – (optional) for future features

Make sure you grant permissions when asked.

---

## 🎮 Game Flow

### MainActivity.kt

- Loads `activity_menu.xml`
- Lets player **create** or **join** rooms
- Room creation triggers `SelectDifficultyActivity.kt`
- Multiplayer sync is done using Firebase Firestore

### PlayGameActivity.kt

- Displays animated cooking tools
- Implements drag-and-drop gestures
- Responds to device shake, touch, and motion

### Room Logic

- `CreateRoomActivity` creates Firestore doc with timestamp
- `JoinRoomActivity` reads from Firestore using room code
- Shared room ID and timer used to synchronize gameplay

---

## 🧩 Important Files

| File                                  | Description                         |
|---------------------------------------|-------------------------------------|
| `MainActivity.kt`                     | Entry screen with room options      |
| `PlayGameActivity.kt`                 | Main gameplay logic                 |
| `SelectDifficultyActivity.kt`         | Difficulty selection                |
| `CreateRoomActivity.kt`, `JoinRoomActivity.kt` | Multiplayer session handling |
| `activity_playgame.xml`              | Core UI layout for the game screen  |
| `drawable/`                           | Game assets: food, tools, backgrounds |

---

## 📦 Dependencies

From `app/build.gradle.kts`:

- `androidx.compose.material3`
- `androidx.constraintlayout`
- `androidx.camera:camera-core`
- `com.google.firebase:firebase-firestore`
- `com.google.android.gms:play-services-nearby`

Firebase BOM ensures version alignment.

---

## 🔬 Testing

Testing is stubbed. Tests can be added in:

```
app/src/test/java/
```

Use:

```bash
./gradlew test
```

---

## 📝 Notes

- All assets are stored in `res/drawable/`
- Room code is alphanumeric, auto-generated
- Game logic is centralized in `PlayGameActivity`
- Device shake = sync action trigger (e.g. drop item)

---


## 👨‍🍳 Contributors

- Yaroslav Oleinychenko 
- Bocheng Peng 
- Daryl Genove 
- Thu Tran 
- Derakhshan Radbareh 

