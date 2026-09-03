plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sampleAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val sampleBannerId = "ca-app-pub-3940256099942544/6300978111"
val configuredAdMobAppId = providers.gradleProperty("ADMOB_APP_ID").orElse(sampleAdMobAppId)
val configuredBannerId = providers.gradleProperty("ADMOB_BANNER_ID").orElse(sampleBannerId)

android {
    namespace = "com.n9nik.imagecompressor"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.n9nik.imagecompressor"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        manifestPlaceholders["admobAppId"] = configuredAdMobAppId.get()
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${configuredBannerId.get()}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore/tinypic-upload.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "TinyPic2026!"
            keyAlias = System.getenv("KEY_ALIAS") ?: "tinypic-upload"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "TinyPic2026!"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            val keystoreFile = rootProject.file("keystore/tinypic-upload.jks")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

val verifyReleaseAds by tasks.registering {
    group = "verification"
    description = "Prevents a production bundle from shipping with Google's sample ad IDs."
    doLast {
        val isCi = System.getenv("CI") == "true"
        if (!isCi) {
            check(configuredAdMobAppId.get() != sampleAdMobAppId) {
                "Set ADMOB_APP_ID in ~/.gradle/gradle.properties before building release."
            }
            check(configuredBannerId.get() != sampleBannerId) {
                "Set ADMOB_BANNER_ID in ~/.gradle/gradle.properties before building release."
            }
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseAds)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.exifinterface:exifinterface:1.4.1")
    implementation("com.google.android.gms:play-services-ads:25.3.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
