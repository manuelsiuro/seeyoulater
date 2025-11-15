# See You Later

An Android app to save links from all other apps and social networks and easily find them for later viewing.

## Features

- 📎 Receive and save links via Android share intent
- 🗂️ Organize links with titles and descriptions
- ⭐ Mark links as favorites
- 👁️ Track read/unread status
- 🔍 Search, sort, and filter your saved links
- ✏️ Edit link details with custom titles and descriptions
- 📊 Bulk operations with selection mode
- 🔄 Pull-to-refresh functionality

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil

## Setup & Build

### Prerequisites
- Android Studio Ladybug or later
- Android SDK API 35
- Kotlin 2.2.20
- Gradle 8.9

### Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

```bash
./gradlew assembleDebug
```

## Project Structure

- `app/src/main/java/com/msa/seeyoulater/`
  - `data/` - Data layer (Room database, repositories)
  - `ui/` - UI layer (Compose screens and components)
  - `ui/navigation/` - Navigation graph
  - `ui/screens/` - Screen composables
  - `ui/theme/` - Material 3 theming

## License

Copyright © 2025 Manuel Siuro
