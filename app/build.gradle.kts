plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.walhero.myteacher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.walhero.myteacher"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        // The real My Teacher App ID is required so the published UMP message
        // can be retrieved in both debug and release builds.
        manifestPlaceholders["adMobAppId"] = "ca-app-pub-2807969310197931~7450138818"

        buildConfigField(
            "String",
            "ADMOB_REWARDED_AD_UNIT_ID",
            "\"ca-app-pub-2807969310197931/8753134707\""
        )
        buildConfigField(
            "String",
            "PRIVACY_POLICY_URL",
            "\"https://princejour.github.io/my-teacher-privacy.html\""
        )
    }

    buildTypes {
        debug {
            // Google test rewarded unit protects the AdMob account during testing.
            buildConfigField(
                "String",
                "ADMOB_REWARDED_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5224354917\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
