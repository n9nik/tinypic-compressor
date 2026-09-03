# TinyPic - Image Compressor - Build Notes

**Built from:** UtilityAppTemplate (green build proven via GitHub Actions run 33717337925)
**Date:** Sep 3 2026
**Package:** com.n9nik.imagecompressor
**App Name:** TinyPic - Image Compressor

## What changed from template

- Domain logic replaced with ImageCompressor.kt: offline Bitmap compress with EXIF orientation, sampleSize to avoid OOM, binary search quality 95→5 to hit target KB, downscale fallback 20-90% if still over target.
- UI: Full Compose screen with PickVisualMedia + GetContent fallback, original preview, JPEG/WebP chips, Target KB slider (50-1500) + presets 100/250/500/1000, Quality mode toggle, offline compress button, compressed preview + saved % + save to MediaStore Pictures/TinyPic (Android Q+) or legacy + MediaScanner, cache fallback.
- Deps: Added androidx.exifinterface:exifinterface:1.4.1
- Tests: Kept transform test + formatBytes test.
- Strings: app_name updated.
- Settings: rootProject.name = TinyPic-ImageCompressor.

## How to build locally (Android SDK required)

```bash
cd /path/to/TinyPic
./gradlew clean assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

If gradle wrapper not present (template intentionally omitted binaries), run once after sync in Android Studio:
```
gradle wrapper --gradle-version 9.3.1
```

## GitHub Actions

Copy the existing working workflow from n9nik-first-app/.github/workflows/android-cloud-build.yml - it already handles:
- unzip Base App Template.zip
- locate UtilityAppTemplate (adjust path to TinyPic root or keep same extraction logic)
- setup-java 17, setup-android, setup-gradle 9.7.1, clean assembleDebug, upload artifact

For new repo, create n9nik/tinypic-compressor repo and push this folder as root.

## Release

- Put real AdMob IDs in ~/.gradle/gradle.properties:
  ADMOB_APP_ID=ca-app-pub-...~...
  ADMOB_BANNER_ID=ca-app-pub-.../...
- ./gradlew bundleRelease -PADMOB... to get AAB
- Play App Signing, Data Safety (ads SDK collects Ad ID, approximate location, app interactions), privacy policy hosted.

## Offline guarantee

Core compress works airplane mode. Internet permission only for ads. Verified by disabling network before tap Compress.

## Next (v2 batch)

- Multi-select picker, parallel compress 100 images <60 sec
- ZIP export, keep EXIF toggle, strip GPS option
- Batch interstitial (once) not per image to avoid "90 sec ads" complaint
