# Learning Dashboard (Android · Kotlin · Jetpack Compose)

**Run:** open in Android Studio (Koala+), run `app`. Login with any valid email and password `password123`.
**APK:** `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`. **Tests:** `./gradlew testDebugUnitTest`.
The mock API (`FakeLearningApi`) reads `assets/courses.json`, adds 800 ms latency and fails with `IOException` when the device is really offline, so the offline demo behaves like a real network.

### 1. Architecture
MVVM with a repository layer and unidirectional data flow: `Compose UI → ViewModel (StateFlow of sealed UI state) → CourseRepository → LearningApi + Room`. Each screen renders one immutable state, so loading/empty/error/success can't contradict each other. The repository is an interface, which is what makes the ViewModel testable with a fake (see `DashboardViewModelTest`). DI is a small manual `AppContainer` to keep the scope honest; Hilt is the next step in a real codebase. Progress is derived from lesson state (`Course.progress`), never stored separately, so it cannot drift.

### 2. Offline support
Offline-first with Room as the single source of truth. The UI only observes Room Flows; a refresh fetches courses + lessons and writes them in one transaction (`replaceCatalog`). If the refresh fails and a cache exists, the cached list is shown with an offline banner; with no cache, an error + retry. Marking a lesson complete writes locally first (`pendingSync = 1`), so it works offline and the UI updates instantly; pending completions are pushed on the next successful refresh, and a refresh never overwrites local completions. The session is persisted, so the app reopens offline straight into cached data.

### 3. Security
Production: short-lived access token plus a refresh token handled by an OkHttp `Authenticator`; both encrypted with an AES-GCM key held in the **Android Keystore** (non-exportable, StrongBox where available) and persisted in DataStore; cleared on logout; excluded from backups; never logged. The demo uses plain SharedPreferences behind the `SessionStore` interface, so the swap is one class.

### 4. Scale (1M users, hundreds of courses)
1. Paging 3 + `RemoteMediator` and a lightweight list endpoint returning progress summaries; load lessons per course on demand instead of prefetching everything.
2. Server-authoritative progress via idempotent "lesson completed" events, synced by WorkManager with backoff and network constraints.
3. HTTP caching (ETag / `If-None-Match`) and a CDN for the catalog; an eviction policy for local data.
4. Hilt + feature modularization for build times and team ownership.
5. Observability and safe rollout: Crashlytics, performance traces, feature flags, staged Play rollouts.

### 5. iOS / macOS
Same layering one-to-one: SwiftUI views → `@Observable` view models exposing an enum state → a `CourseRepository` protocol → a `URLSession` + async/await API client and SwiftData (or Core Data) as the cache and source of truth. Keychain for tokens, `NWPathMonitor` for connectivity, `NavigationStack` for navigation, and XCTest with a fake repository for the same ViewModel test.

**Note:** server-reported progress is converted to completed lessons (e.g. 40% of 16 → 6 lessons → shown as 38%), keeping a single source of truth.
