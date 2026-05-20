# AAOS Navigation & Task Management

This project demonstrates how to handle a complex, non-standard navigation requirement in Android Automotive OS (AAOS) using a single-activity architecture (Jetpack Navigation).

## The Use-Case
- When the app is launched from the **Car Launcher / Home Screen**, it must default to the `RootFragment`.
- When the app is launched from **Recents** or **other apps (like Car Settings)**, it must remember its state and resume on the last viewed fragment (e.g., `SecondFragment`).
- The transition must not show a "flicker" of the previous fragment when launching from the Car Launcher.

---

## The Current Architecture: Cold Start / Manual State Restoration

We are currently using the **"Nuclear Workaround"** (`onUserLeaveHint` + `finish()` + `SharedPreferences`). 

When the user presses the Home button, we save the Jetpack Navigation state to `SharedPreferences` and explicitly call `finish()`. This completely destroys the OS task and clears the OS WindowManager's visual snapshot. Every launch of the app is therefore a "Cold Start".

- If launched from the Launcher, we ignore the saved state and start fresh on `RootFragment`.
- If launched from Recents, we manually parse the `SharedPreferences` and reconstruct the Jetpack Navigation backstack to restore `SecondFragment`.

### Why this aggressive workaround?
In a standard Android ecosystem, you would use `android:clearTaskOnLaunch="true"` or a Multi-Task router to handle this gracefully. However, your specific AAOS Car Launcher heavily caches the exact ComponentName (`MainActivity`) and ignores standard intent flags (`FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`). Because the Launcher physically forces `MainActivity` to resume on top, the OS enforces an unskippable zoom-in animation using a historical snapshot of the app's last known state (`SecondFragment`).

The only mathematical way to eliminate the flicker on this specific build is to ensure the OS never has a snapshot to begin with, which requires destroying the task.

---

## How to Test Both Use-Cases

The app is now configured to use the Android 12+ Splash Screen API to ensure the Cold Start animation is completely black, hiding the app icon for a seamless transition.

### Test 1: Car Launcher (Flicker-Free Reset)
1. Open the app and navigate to the **Second Fragment**.
2. Press the **Home** button on your AAOS emulator (this triggers `onUserLeaveHint` and kills the app, saving state).
3. Tap the **App Icon** in the Car Launcher.
4. **Expected Result:** You will see a perfectly black screen (Cold Start), which instantly transitions directly into `RootFragment`. There should be absolutely no flicker of `SecondFragment`.

### Test 2: Recents / Car Settings (State Preservation)
1. Open the app and navigate to the **Second Fragment**.
2. Press the **Home** button.
3. Open your AAOS **Recents** menu and select the app.
4. **Expected Result:** You will see a perfectly black screen, and the app will manually reconstruct your history, instantly dropping you back into `SecondFragment`.

---

## Rejected Architectures (For Reference)

1. **The Google-Recommended Standard (`clearTaskOnLaunch`):** Failed because the Car Launcher does not send standard reset flags, causing the framework to ignore the command entirely.
2. **Snapshot Forgery (`onPause` Cover View):** Failed because Android's UI thread does not guarantee the fake cover will draw in time before the OS takes the background screenshot.
3. **Multi-Task Router (Separate `taskAffinity`):** Failed because the Car Launcher has hardcoded the `.MainActivity` component in its cache, completely bypassing the invisible Router Activity.
