# AAOS Navigation & Task Management

This project demonstrates how to handle a complex, non-standard navigation requirement in Android Automotive OS (AAOS) using a single-activity architecture (Jetpack Navigation).

## The Use-Case
- When the app is launched from the **Car Launcher / Home Screen**, it must default to the `RootFragment`.
- When the app is launched from **Recents** or **other apps (like Car Settings)**, it must remember its state and resume on the last viewed fragment (e.g., `SecondFragment`).
- The transition must not show a "flicker" of the previous fragment when launching from the Car Launcher.

---

## Is the current implementation recommended by Google?

**No.** The current implementation (killing the Activity in `onUserLeaveHint` and manually parceling the `NavController` state to `SharedPreferences`) is a **"Nuclear Workaround."** 

It is not officially recommended by Google. Google's architectural guidance generally assumes that single-activity apps either always resume their state (standard behavior) or always reset their state when launched from a launcher. 

### Why did we use a Nuclear Workaround?
We were forced into this workaround due to a physical limitation in how specific AAOS Car Launchers interact with the Android 14 `WindowManager`.

When an app goes into the background, the OS takes a **Snapshot** (a screenshot) of the current UI. When the app is brought back to the foreground (a "Hot Start"), the OS forcefully animates this snapshot on the screen. Because the Car Launcher in this specific environment ignores standard Android manifest flags (`clearTaskOnLaunch`, `finishOnTaskLaunch`) and modern snapshot-disabling APIs (`setRecentsScreenshotEnabled(false)`), it was mathematically impossible to prevent the OS from flashing a picture of `SecondFragment` on the screen before our code could jump to `RootFragment`.

By aggressively calling `finish()` when the app goes to the background, we force the OS to completely destroy the task and drop the snapshot. The next launch becomes a clean **Cold Start**, entirely bypassing the unskippable OS animation flicker.

---

## The Google-Recommended Approach

If you were building this for standard Android (or an AAOS build that strictly follows standard Android Intent task management), Google recommends relying on OS-level manifest flags and avoiding manual state hacking.

Here is the officially recommended approach to achieve this use-case natively:

### 1. Use `clearTaskOnLaunch` with a Routing Activity
Instead of hacking the Jetpack Navigation backstack inside `onNewIntent`, use the Android Manifest to tell the OS to automatically clear the task when launched from the Launcher.

**Step A:** Create an invisible routing Activity (e.g., `SplashActivity`).
**Step B:** Put your Fragments in a separate Activity (e.g., `ContentActivity`).
**Step C:** Configure your `AndroidManifest.xml` like this:

```xml
<!-- This is the entry point from the Launcher -->
<activity
    android:name=".SplashActivity"
    android:exported="true"
    android:clearTaskOnLaunch="true"
    android:theme="@style/Theme.NoDisplay">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- This holds your actual Jetpack Navigation Graph -->
<activity
    android:name=".ContentActivity"
    android:exported="false">
</activity>
```

### How the Recommended Approach Works:
1. When the user taps the **Launcher**, the OS looks at `SplashActivity`. Because it has `clearTaskOnLaunch="true"`, the Android Framework *automatically* destroys `ContentActivity` in the background. `SplashActivity` then wakes up and starts a fresh `ContentActivity` (showing `RootFragment`) natively, with zero flickers.
2. When the user taps **Recents**, the OS bypasses `SplashActivity` entirely, ignores the `clearTaskOnLaunch` flag, and directly resumes `ContentActivity` (showing `SecondFragment`).

### Why didn't we use it?
We actually tried this approach during our testing phases! However, your specific AAOS emulator's Car Launcher heavily caches component names and bypasses standard Intent flags (specifically `FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`). Because the Car Launcher wasn't sending the proper system flags to the framework, Android ignored `clearTaskOnLaunch`, causing the recommended approach to fail and forcing us to rely on the manual SharedPreferences workaround.

---

## Conclusion
If your AAOS platform eventually strictly adheres to standard Android task management, you should revert the code to use the **Google-Recommended Approach** (`clearTaskOnLaunch` with a router activity). 

However, if you are forced to deploy on a customized AAOS environment with an aggressive, non-standard Car Launcher that forces Hot Start snapshots regardless of manifest properties, the current **Cold Start / Manual State Restoration** workaround is the only technically viable way to guarantee a flicker-free transition.
