# Learning Dashboard – Android App

**APK:** [Download app-debug.apk](https://github.com/<your-username>/<your-repo>/releases/tag/v1.0)
**Demo video:** [Watch](https://drive.google.com/file/d/1ZHlNbE4cIdOlLrlbxQlIidsC86GzCSt5/view?usp=sharing)

Built with **Kotlin**, **Jetpack Compose** (UI) and **Room** (local database).

## How to run
1. Open the project in Android Studio and press **Run ▶**.
2. Login with any valid email and password **`password123`**.
3. Run tests: `./gradlew testDebugUnitTest`

The login and course API are **mocked**. Courses come from `assets/courses.json`.
The mock API fails when the phone has no internet, so offline mode can be tested for real.

## 1. Architecture
I used **MVVM with a Repository**:

```
Screen  →  ViewModel  →  Repository  →  API + Local Database
```

- **Screen** only shows data.
- **ViewModel** holds the screen state (Loading, Success, Empty, Error).
- **Repository** decides whether to use the API or the database.

Each part has one job, so the code is easy to read, change and test.

## 2. Offline Support
- Courses and lessons are saved in a **Room database** on the phone.
- The screens **always read from the database**, never directly from the API.
- When online, the app downloads courses and saves them in the database.
- When offline, the API call fails, but the saved courses are still shown with an "offline" message.
- Marking a lesson complete is saved on the phone first, so it also works offline. It is sent to the server on the next refresh.

## 3. Security
In a real app I would:
- Save the login token **encrypted**, using the **Android Keystore**.
- Use a short-lived token and refresh it when it expires.
- Delete the token and the user's saved data on logout.

(In this demo the token is kept in SharedPreferences to keep it simple.)

## 4. Scale (1 million users, hundreds of courses)
1. **Load courses page by page** instead of all at once.
2. **Load lessons only when a course is opened.**
3. **Sync progress in the background** with WorkManager, retrying if it fails.
4. **Cache API responses** and use a CDN to reduce server load.
5. **Add crash reporting and monitoring** (e.g. Firebase Crashlytics).

## 5. Building it on iOS
I would use the same structure:
- **SwiftUI** for screens
- **ViewModel** classes for state
- **URLSession** for API calls
- **SwiftData / Core Data** for the offline database
- **Keychain** for storing the token

## Note
Progress is calculated from completed lessons.
Example: Generative AI has 6 of 16 lessons done, so it shows **38%**.
