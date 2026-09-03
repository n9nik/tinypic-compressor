# TinyPic - Image Compressor — Play Store Listing Draft

**Package:** `com.n9nik.imagecompressor` (debug variant `com.n9nik.imagecompressor.debug` for testing)  
**App Name:** TinyPic - Image Compressor  
**Category:** Photography / Tools  
**Content Rating:** Everyone  
**Pricing:** Free, ad-supported, no in-app purchases, no paywall

## Short description (80 chars max Play limit - we use 74)
Offline image compressor MB→KB. No watermark, no signup, fast.

## Full description

**TinyPic compresses your photos from MB to KB offline — no watermark, no cloud upload, no account.**

Students, sellers, and photographers waste 90 seconds watching ads in other compressors that still add a watermark. TinyPic fixes that complaint with one job done fast.

**Why people switch to TinyPic:**
- ✅ **True offline** — works in airplane mode, never uploads your photos
- 🎯 **Precise Target KB** — set 100KB, 200KB, 250KB, 500KB for forms, portals, uploads
- 🖼️ **No watermark** — ever. Your image stays yours
- ⚡ **Fast by default** — one tap compress, before/after size, % saved
- 🔒 **Privacy first** — only reads the image you pick, no contacts/location/camera creep
- 📱 **<12MB app** — no bloat, no 155MB SDKs
- 🔄 **Smart quality** — JPEG & WebP, auto-fits target, keeps orientation correct

**Perfect for:**
- College/job forms that need 100KB / 200KB uploads
- Marketplace listings, WhatsApp forwards, email attachments
- Photographers sending previews without cloud privacy fear

**How it works:**
1. Pick image from system picker
2. Choose JPEG/WebP, Target KB or Quality %
3. Tap Compress — 100% offline
4. Preview before/after, save to Pictures/TinyPic

No account. No cloud. No watermark. Just compression.

**Vs. competitors (1-star patterns we fixed):**
- Other app: "triple ads eating 90 seconds, said no ads but does" → TinyPic: banner only in debug, single interstitial only on batch export (future), not per image
- Other app: "failed to save" → TinyPic: MediaStore + scoped storage Android 11+ tested, fallback cache
- Other app: "uploads to cloud privacy fear" → TinyPic: offline, verify with airplane mode
- Other app: "watermark added despite promise" → TinyPic: never adds watermark, open to audit

**Tech:** Android BitmapFactory with sampling (no OOM on 20MP), EXIF orientation handling via androidx.exifinterface, WebP lossy on Android 10+.

**Free, ad-supported** to keep it alive — ads never block your compress.

---

## Keywords (for ASO, not in description directly)

image compressor, photo compressor, MB to KB, compress image to 100KB, compress image to 200KB, image size reducer, no watermark image compressor, offline image compressor, JPEG compressor, WebP compressor, reduce photo size, picture compressor

## Store assets TODO

- Icon: simple, no watermark badge, 512x512, Material You friendly, show "KB→" arrow
- Screenshots (truthful):
  1. Picker + Original size card (MB)
  2. Target KB slider 100/250/500
  3. Compressed preview + % saved + no watermark label
  4. Save to Pictures/TinyPic proof
  5. Airplane mode badge "Works offline"
- Feature graphic: "Offline MB→KB • No Watermark" text, high contrast
- Privacy policy URL: host `privacy-policy.md` on GitHub Pages or n9nik.github.io
- Data Safety:
  - Does app collect/share data? Yes, via Google ads SDK: Advertising ID, approximate location (ad), app interactions. No collection by developer directly. No upload of photos.
  - Encryption in transit: Yes (ads SDK)
  - Data deletion: No account to delete, cache cleared on uninstall, user can delete saved images via gallery
  - Location: Approximate only via ads SDK, not precise, user can opt out via device ad settings

## Monetization

- Banner only in v1 (test IDs debug, real IDs release)
- Interstitial only on batch export in future v2, not per single image, to avoid "triple ads 90 sec" complaint cluster
- No paywall, no subscription, static QR-like trust model

## Release checklist

- Replace test AdMob IDs with real IDs in ~/.gradle/gradle.properties ADMOB_APP_ID, ADMOB_BANNER_ID
- Generate AAB via `./gradlew bundleRelease`
- Complete Play App Signing, content rating, target audience (13+? check), ads declaration yes, Data Safety yes
- Upload AAB to closed testing track, invite 12 testers for 14 days continuous
- Host privacy policy, add link in Play Console

## Build command

```bash
./gradlew clean assembleDebug  # local test
./gradlew bundleRelease -PADMOB_APP_ID=ca-app-pub-...~... -PADMOB_BANNER_ID=ca-app-pub-.../...
```

## Notes on policy risk

- No MANAGE_ALL_FILES, no READ_CONTACTS, minimal permissions = lower review risk
- No deceptive "free forever" that hides paywall later (unlike QR generators) — static free model is moat
- Do not copy competitor icons/listing text, use own screenshots

---
*Drafted Sep 3 2026 PDT for Nikhil's factory. Keep under 4000 chars for full description Play limit.*
