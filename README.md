# Postly for Android

Postly is a native Android app for discovering, searching, and publishing posts. It is built with Kotlin and Jetpack Compose and connects to a separate HTTP API for accounts, posts, likes, comments, and notifications.

## Features

- Browse a paginated feed and search posts.
- Open post details, like posts, and add or delete comments.
- Register with email and password or sign in with Google.
- Create, edit, and delete your posts, including drafts and published posts.
- Search Pexels for cover-image suggestions in the post editor.
- View in-app notifications and receive Firebase Cloud Messaging push notifications.
- Open a post from a push notification.

Some features require optional third-party credentials. See [Configuration](#configuration).

## Requirements

- Android Studio with Android SDK Platform 37 installed.
- JDK 21. The Gradle daemon is configured to use Java 21; the Gradle Wrapper can provision it through Foojay when needed.
- An Android emulator or device running Android 8.1 (API 27) or later.
- Access to a compatible Postly API. The mobile project does not include or start the backend.

The project uses Gradle 9.3.1, Android Gradle Plugin 9.1.1, Kotlin 2.2.10, and compile SDK 37. Dependency resolution requires network access on the first Gradle sync/build.

## Quick start

1. Clone the repository and open its root directory in Android Studio, or use the command line:

   ```bash
   git clone <repository-url>
   cd Postly
   ```

2. Create a local secrets file from the checked-in template:

   ```bash
   cp secrets.properties.example secrets.properties
   ```

3. Edit `secrets.properties` and set at least `API_BASE_URL` to the base URL of your running API. The URL should include the API prefix used by your server, for example `https://api.example.com/api/`.

4. Sync Gradle in Android Studio, or build and install from a terminal:

   ```bash
   ./gradlew :app:installDebug
   ```

   To run from Android Studio, select the `app` run configuration and a device or emulator, then press **Run**.

## Configuration

The app reads values from `secrets.properties` at the repository root. Gradle generates Android `BuildConfig` constants from these values during the build. The real secrets file is git-ignored; keep it local and do not commit credentials.

| Property | Required | Used for |
| --- | --- | --- |
| `API_BASE_URL` | Yes | Base URL of the Postly API, including any API prefix such as `/api/`. |
| `PEXELS_API_KEY` | No | Image suggestions in the post editor. If blank, Pexels suggestions are unavailable. |
| `GOOGLE_WEB_CLIENT_ID` | No | Google sign-in. Set this to the OAuth **Web application** client ID used as the server client ID. |
| `FIREBASE_API_KEY` | No | Firebase app initialization for push messaging. |
| `FIREBASE_APP_ID` | No | Firebase Android app ID. |
| `FIREBASE_PROJECT_ID` | No | Firebase project ID. |
| `FIREBASE_SENDER_ID` | No | Firebase Cloud Messaging sender ID (the project number). |

For example, `secrets.properties` can be filled in like this:

```properties
API_BASE_URL=https://api.example.com/api/
PEXELS_API_KEY=your-pexels-api-key
GOOGLE_WEB_CLIENT_ID=your-web-oauth-client-id.apps.googleusercontent.com
FIREBASE_API_KEY=your-firebase-api-key
FIREBASE_APP_ID=your-firebase-android-app-id
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_SENDER_ID=your-firebase-sender-id
```

Only `API_BASE_URL` is required to compile and use the core app. The API URL must be reachable from the emulator or device. For the standard Android emulator, `localhost` refers to the emulator itself, not your development computer; use `10.0.2.2` to reach a server running on the host, for example `http://10.0.2.2:3000/api/`. Android blocks cleartext HTTP by default for this app, so that HTTP example also requires a debug-only network security policy. Prefer an HTTPS development URL when available. On a physical device, use a host name or LAN IP reachable from that device.

### Google sign-in

1. Configure OAuth credentials for the Android application ID `com.example.postly`, including the signing certificate fingerprints for the builds you use.
2. Create or identify the OAuth Web application client ID and set it as `GOOGLE_WEB_CLIENT_ID`.
3. Configure the backend to verify the Google ID token sent by the app at `POST /auth/login`.

Without this property the app can still be built and email/password authentication can still be used, but Google sign-in reports a configuration error.

### Pexels image search

Create a Pexels API key and set `PEXELS_API_KEY`. The app requests up to six matching images from the Pexels search API when the user searches from the post editor. Leaving the key blank disables those suggestions.

### Firebase push notifications

Create a Firebase project and register an Android app with application ID `com.example.postly`. Copy the API key, Android app ID, project ID, and sender/project number into the four `FIREBASE_*` properties above. This app initializes Firebase Messaging from those properties; it does not load a `google-services.json` file.

The app asks for notification permission on Android 13 and later. Push token registration also requires an authenticated user and the API's device-token endpoints. If Firebase properties are omitted, ordinary app and in-app API functionality remains available, but push messaging is disabled.

## API contract

The app uses JSON over HTTP(S). Unless stated otherwise, authenticated requests send `Authorization: Bearer <accessToken>`. The API base URL is combined with the paths below; for example, `https://api.example.com/api/` plus `posts` becomes `https://api.example.com/api/posts`.

| Method | Path | Authentication | Purpose |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | No | Register with `{ "name", "email", "password" }`. |
| `POST` | `/auth/login/email` | No | Sign in with `{ "email", "password" }`. |
| `POST` | `/auth/login` | No | Verify Google sign-in with `{ "token" }`. |
| `GET` | `/auth/check` | Yes | Validate the saved session and return the current user. |
| `GET` | `/posts?page={page}&limit={limit}` | No | Load a paginated feed. |
| `GET` | `/posts/search?q={query}` | No | Search posts. The client accepts either a JSON array or an object containing `posts` and optional `pagination`. |
| `GET` | `/posts/{postId}` | No | Load a post. |
| `POST` | `/posts` | Yes | Create a post with `{ "title", "content", "status", "coverImage" }`. |
| `PUT` | `/posts/{postId}` | Yes | Update a post with the same fields as create. |
| `DELETE` | `/posts/{postId}` | Yes | Delete a post. |
| `GET` | `/users/me/posts` | Yes | Load the signed-in user's posts. |
| `GET`, `POST`, `DELETE` | `/posts/{postId}/like` | Yes | Check, add, or remove a like. The check response contains `liked`. |
| `POST` | `/posts/{postId}/comments` | Yes | Add a comment with `{ "content" }`. |
| `DELETE` | `/posts/{postId}/comments/{commentId}` | Yes | Delete a comment. |
| `GET` | `/notifications` | Yes | Load notifications and optionally `unreadCount`. |
| `PATCH` | `/notifications/{notificationId}/read` | Yes | Mark a notification as read. |
| `PATCH` | `/notifications/read-all` | Yes | Mark all notifications as read. |
| `POST` | `/notifications/device-token` | Yes | Register a push token with `{ "token" }`. |
| `DELETE` | `/notifications/device-token` | Yes | Unregister a push token with `{ "token" }`. |

Successful authentication responses must contain `accessToken` and `user`. Paginated post responses use a `posts` array and `pagination` object (`total`, `page`, `limit`, `totalPages`). Post and comment objects use `_id` identifiers; post records include `title`, `content`, `author`, `createdAt`, `status`, and may include `coverImage`, `readTime`, `views`, `likes`, and `comments`. The app currently expects the API to return meaningful JSON for the fields it displays and treats non-2xx responses as errors.

## Build and run

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Install the debug app on a connected device or running emulator:

```bash
./gradlew :app:installDebug
```

On Windows, use `gradlew.bat` in place of `./gradlew`.

## Run checks

Run local JVM unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run instrumented tests on a connected Android device or emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Project structure

```text
app/src/main/java/com/example/postly/
├── managers/       # Authentication and session state
├── models/         # API data models
├── networking/     # API client and endpoint definitions
├── services/       # Auth, posts, comments, Pexels, and notifications
├── ui/             # Compose app, navigation, components, and screens
└── viewmodels/     # Feed and notification state
```

The main entry point is `MainActivity`; `PostlyApplication` initializes Firebase when all Firebase settings are present and creates the notification channel. `SessionManager` wires the authenticated API services together. The UI and app data live in the `app` module.

## Troubleshooting

- **`API_BASE_URL is missing`**: create `secrets.properties` from the example and set a non-empty URL.
- **Requests fail on the emulator**: confirm the API is running, the URL is reachable from the device, and that it includes the correct API prefix. Do not use the host computer's `localhost` address from the emulator.
- **HTTP connection is blocked**: use HTTPS or add a development-only cleartext network policy for your local build.
- **Google sign-in is unavailable or rejected**: check `GOOGLE_WEB_CLIENT_ID`, the OAuth Android package name and signing certificate fingerprints, and backend Google token verification.
- **Pexels image suggestions are empty**: verify `PEXELS_API_KEY` and network connectivity. An empty key intentionally returns no suggestions.
- **Push notifications do not arrive**: verify all four Firebase values, notification permission, a signed-in session, and that the backend implements device-token registration and push delivery.
- **Gradle cannot find Java 21 or SDK 37**: install JDK 21 and Android SDK Platform 37, then sync Gradle again. Android Studio can manage both from its SDK and Gradle settings.

## Security notes

`secrets.properties` is ignored by Git, but values inserted into the Android app are packaged in the client and can be extracted. Do not put privileged server credentials or secrets that must remain private in this file. Treat client API keys as public, restrict them through their provider where possible, and enforce authorization and validation on the backend.
