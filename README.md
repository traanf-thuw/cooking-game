
# 🍳 Cooking Game App (Kotlin - Android)

A multiplayer cooking game where players complete recipe tasks using drag-and-drop, motion gestures, and real-time communication. Built in Kotlin using Android SDK and Firebase Firestore for multiplayer sync.

---

## 📁 Project Structure

```
CookingGameApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cookinggameapp/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── CreateRoomActivity.kt
│   │   │   │   ├── JoinRoomActivity.kt
│   │   │   │   ├── SelectDifficultyActivity.kt
│   │   │   │   └── PlayGameActivity.kt
│   │   │   ├── res/layout/
│   │   │   │   └── activity_playgame.xml
│   │   │   └── res/drawable/
│   │   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### 🧰 Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 35+
- Kotlin plugin
- Emulator or physical device (API 24+)
- Firebase Project with Firestore enabled

### 🧪 Steps to Run the Project

1. Clone the repository:
```bash
git clone https://github.com/traanf-thuw/cooking-game.git
cd cooking-game/cooking-game/CookingGameApp
```

2.  Open in Android Studio

- Open Android Studio
- File → Open → Select the `CookingGameApp` folder
- Wait for Gradle sync to complete

3. Build and Run

- Connect a physical Android device or start an emulator
- Run the app from Android Studio
- Or Build the APK:

  In Android Studio:

  Go to Build > Generate App Bundles or APKs

  After the build finishes, click “locate” to find the APK

---


## 🎮 Game Flow

### MainActivity.kt
- Loads the main menu layout (`activity_menu.xml`)
- Lets players **create** or **join** rooms
- Hosts proceed to difficulty selection

### SelectDifficultyActivity.kt
- Lets host pick between Easy, Medium, or Hard
- Difficulty affects countdown timer in gameplay

### CreateRoomActivity.kt & JoinRoomActivity.kt
- `CreateRoomActivity` sets up a new room in Firestore
- `JoinRoomActivity` connects to existing room via 6-letter code
- Shared room ID + timestamp sync all players in real time

### PlayGameActivity.kt
- Main game screen logic
- Implements:
  - Drag-and-drop interaction
  - Motion gesture input (device shake via SensorManager)
  - Cooking animations
  - Chopping and stirring detection
  - Countdown timer using difficulty
  - Firestore syncing for game state
  - Vibration feedback

---

## 📦 Important Files

| File                                      | Description                                |
|-------------------------------------------|--------------------------------------------|
| `MainActivity.kt`                         | Entry point with options to host/join      |
| `SelectDifficultyActivity.kt`             | Game difficulty selection                  |
| `CreateRoomActivity.kt`, `JoinRoomActivity.kt` | Multiplayer session logic             |
| `PlayGameActivity.kt`                     | Core gameplay, UI + game loop              |
| `activity_playgame.xml`                   | Layout for PlayGame screen                 |
| `drawable/`                               | Game graphics: ingredients, tools, etc.    |

---

## 🔐 Permissions Required

This app requires the following permissions for full functionality:

- **Internet** – For multiplayer and network communication.
- **Camera** – Used for the game feature of taking photo.
- **Vibration** – Haptic feedback during gameplay.
- **Wake Lock** – Keeps screen active during game sessions.
- **Network State Access** – Checks online status before connecting.
- **Read/Write External Storage** – Load or save user-generated content or assets.

Make sure you grant permissions when asked.

---

## 📚 Libraries Used

- **Firebase Firestore** – Realtime database
- **AndroidX** – UI components and backward compatibility
- **Kotlin Coroutines** – Background threading (if added)
- **SensorManager** – Shake and motion detection
- **VibratorManager** – Device feedback during gameplay

---

## 🛠️ Authors

Thu Tran - Yaroslav Oleinychenko - Derakhshan Radbare - Bocheng Peng - Daryl Genove 

