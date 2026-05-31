# PanelSena Android Client

Android digital-signage player for the PanelSena platform. Any Android display, phone, or
tablet running this app can be linked to a PanelSena account from the dashboard, then be
monitored and controlled centrally (play / pause / stop / skip / volume / brightness, plus
scheduled playback) — exactly like the Raspberry Pi player.

## How linking works

The app uses the same device-link protocol as the Raspberry Pi player:

1. On first launch the app generates a **Device ID** (`DEVICE_<timestamp>_<random>`) and a
   secret **Device Key**, persisted in DataStore. Both are shown under **Profile & Settings**
   and on the **Display Info** tab.
2. The user signs in with Google (their PanelSena account).
3. The app registers itself in Realtime DB `device_registry/{deviceId}`.
4. In the dashboard: **Displays → Add Display → Link Device**, the user enters the Device ID
   and Device Key. The dashboard verifies the key and writes `device_links/{deviceId}`.
5. The app observes `device_links/{deviceId}`, discovers its `{userId, displayId}`, then:
   - heartbeats `users/{userId}/displays/{displayId}/status` every 10s, and
   - listens for `users/{userId}/displays/{displayId}/commands`, executing and acking each.
6. `play` commands resolve content from Firestore (`schedules/{id}.contentIds` →
   `content/{id}.url`, or a single `content/{id}`) and play it fullscreen.

## ⚠️ Required: google-services.json

`app/google-services.json` in this repo is a **placeholder** that targets the correct
Firebase project (`panelsena-r2`) with the real public Web API key and RTDB URL, but the
Android-specific fields (`mobilesdk_app_id`, OAuth client IDs) are placeholders. The app will
build with it, but Google Sign-In and Firebase will only work once you replace it with the
real file:

1. Firebase Console → project **panelsena-r2** → Project Settings.
2. Under **Your apps**, add an **Android** app with package name `com.panelsena.app`
   (if it does not already exist).
3. Add your signing certificate **SHA-1** (required for Google Sign-In):
   ```bash
   # debug keystore SHA-1
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```
   Paste the SHA-1 into the Android app's settings in the Firebase Console.
4. Download the generated `google-services.json` and replace `app/google-services.json`.
5. In Firebase Console → Authentication → Sign-in method, ensure **Google** is enabled.

`AuthScreen` reads the web client ID from `R.string.default_web_client_id`, which the
google-services Gradle plugin generates from this file, so no client ID is hardcoded.

## Build

```bash
./gradlew assembleDebug
```
