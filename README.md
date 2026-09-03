# Utility App Template

A small, reusable Android foundation for one-job utility apps. The Compose compiler plugin is pinned to AGP 9.1.1's built-in Kotlin version (2.2.10) to keep the toolchain coherent. It uses Kotlin, Jetpack Compose, Material 3, Google Mobile Ads banner ads, and Google's UMP consent flow. Debug builds use Google's sample ad IDs; release builds fail until real AdMob IDs are supplied.

## What is included

- Clean one-screen utility shell with dark mode and responsive scrolling
- Isolated `UtilityLogic` so each cloned app can replace one small unit
- Banner ad loaded only after UMP says ads may be requested
- Privacy choices entry point when UMP requires one
- Release guard against accidentally shipping Google's sample ad IDs
- Clone script, static preflight, unit-test example, privacy-policy starter, and launch checklist
- No account, analytics, database, backend, or unnecessary permissions

## First run

1. Install current stable Android Studio with JDK 17 and Android SDK 37.
2. Open this folder as a project. If Android Studio asks to configure Gradle, choose Gradle 9.3.1.
3. Let Gradle sync, then run the `app` debug configuration on an emulator or Android phone.
4. The demo collapses extra whitespace. Replace `UtilityLogic` and the screen for each real utility.

This archive intentionally omits generated Gradle wrapper binaries. After the first successful sync, run `gradle wrapper --gradle-version 9.3.1` once if you want command-line `./gradlew` builds or CI.

## Ad configuration

Development uses Google's official sample app and banner IDs. Before a release, add real IDs to your user-level `~/.gradle/gradle.properties`:

```properties
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
```

Do not put signing keys or passwords in this project. The AdMob IDs are publisher identifiers, not authentication secrets, but keeping per-app IDs outside the template makes cloning safer.

## Factory workflow

Use `tools/clone_app.py` to create each candidate, then keep each app narrowly scoped. Start with a useful offline core, add only permissions the feature truly needs, test with Google's sample ads, and supply each app's real AdMob IDs only for the release bundle. Follow `APP_FACTORY_CHECKLIST.md` every time.

## Important policy notes

Ads are not passive plumbing. Before production, configure Privacy & messaging in AdMob, host a truthful privacy policy, complete Play's Data safety form, declare that the app contains ads, and verify the final implementation against current Google Play and AdMob policies. Do not copy competitors' code, branding, listing text, or screenshots. Similar functionality is fine; deceptive clones and repetitive low-quality content are not.

## Verified references

- Google Mobile Ads Android setup: https://developers.google.com/admob/android/quick-start
- Google UMP Android setup: https://developers.google.com/admob/ump/android/quick-start
- Official banner test-ad guidance: https://developers.google.com/admob/cpp/banner
- Compose BOM guidance: http://developer.android.com/develop/ui/compose/bom
- Android Gradle Plugin 9.1 compatibility: http://developer.android.com/build/releases/agp-9-1-0-release-notes

Dependency versions are pinned as of September 2, 2026. Re-check them before a later production launch.
