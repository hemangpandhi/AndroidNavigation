# AAOS Navigation & Task Management

This project demonstrates how to handle a complex, non-standard navigation requirement in Android Automotive OS (AAOS) using a single-activity architecture (Jetpack Navigation).

## The Use-Case
- When the app is launched from the **Car Launcher / Home Screen**, it must default to the `RootFragment`.
- When the app is launched from **Recents** or **other apps (like Car Settings)**, it must remember its state and resume on the last viewed fragment (e.g., `SecondFragment`).
- The transition must not show a "flicker" of the previous fragment when launching from the Car Launcher.

---

## Architectural Challenges in AAOS 14

In a standard Android environment, managing task resets is handled by the framework using Intent flags (`FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`) and Manifest properties (`clearTaskOnLaunch`). 

However, in this specific AAOS environment, the Car Launcher fails to send the proper Intent flags, and aggressively caches component states. This forces the OS to treat every launch as a standard "Hot Start" (bringing the background task to the front). During a Hot Start, the Android 14 `WindowManager` enforces an unskippable zoom-in animation using a historical snapshot of the app's last known state (e.g., `SecondFragment`).

Because we cannot fix the Car Launcher directly, we must rely on app-level workarounds. Below are the three primary architectural approaches to solving this, from most standard to most aggressive.

---

## Approach 1: The Google-Recommended Standard (Fails on custom AAOS)

If you were building this for standard Android (or an AAOS build that strictly follows Android Intent task management), Google recommends relying on OS-level manifest flags.

### Implementation:
1. Create an invisible routing Activity (`SplashActivity`).
2. Put your Fragments in a separate Activity (`ContentActivity`).
3. Set `android:clearTaskOnLaunch="true"` on the `SplashActivity` in the Manifest.

### Why it fails here:
The specific AAOS emulator's Car Launcher heavily caches component names and bypasses standard Intent flags. Because the Car Launcher does not send `FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`, the Android Framework ignores `clearTaskOnLaunch`, causing the recommended approach to fail entirely.

---

## Approach 2: Snapshot Forgery Cover (Current Implementation)

Since we are forced to use a Single-Activity architecture and cannot rely on standard task clearing, we must "trick" the OS snapshot mechanism.

### Implementation:
1. We place a hidden `FrameLayout` over our UI in `activity_main.xml` that looks exactly like `RootFragment`.
2. In `MainActivity.onPause()`, right before the OS takes its background screenshot, we make this cover visible.
3. The OS takes a screenshot of the fake `RootFragment`.
4. In `MainActivity.onResume()`, we instantly hide the cover.

### Pros & Cons:
- **Pros:** The Car Launcher zoom-in animation perfectly displays `RootFragment`, eliminating the `SecondFragment` flicker. It does not interfere with `onUserLeaveHint` or task lifecycles.
- **Cons:** Android only has *one* snapshot slot. Because the snapshot is now `RootFragment`, launching the app from **Recents** or **Car Settings** will show a brief flash of `RootFragment` before revealing the actual `SecondFragment` underneath. Furthermore, Android's UI thread does not always guarantee the cover will draw in time before the snapshot is composited.

---

## Approach 3: The Multi-Task Router Architecture (The Ultimate Workaround)

If Approach 2's Recents flash is unacceptable, or the OS fails to draw the cover in time, this is the final, most robust way to defeat a misbehaving CarLauncher from the inside without killing the task on leave.

### Implementation:
We split the app into two separate OS Tasks using `android:taskAffinity`.
1. **`RouterActivity` (Transparent Theme):** The main entry point for the Launcher. Runs in the default task.
2. **`AppActivity` (Your UI):** Runs in a separate background task (e.g., `taskAffinity=".app"`).

### How it works:
1. When you tap the Car Launcher, the OS blindly animates the `RouterActivity`. Because it is transparent, there is **no flicker**.
2. The invisible `RouterActivity` instantly analyzes the intent. If it came from the Launcher, it sends an explicit `Intent.FLAG_ACTIVITY_CLEAR_TASK` command to `AppActivity` and brings it to the front. `AppActivity` restarts cleanly on `RootFragment`.
3. If launched from Recents or Car Settings, we bypass the clear command and just bring `AppActivity` to the front, preserving `SecondFragment`.

### Conclusion:
If you cannot fix the Car Launcher code, **Approach 3 (Multi-Task Router)** is the ultimate architectural solution to guarantee perfect transitions on a custom, non-compliant AAOS build.
