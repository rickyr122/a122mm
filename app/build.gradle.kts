plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.a122mm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.a122mm"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.navigation.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Retrofit and Coil
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material:1.6.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation("androidx.palette:palette:1.0.0")

    implementation("androidx.paging:paging-runtime:3.3.0")
    implementation("androidx.paging:paging-compose:3.3.0")

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // FFmpeg software decoders (GPL-3.0)
    implementation("org.jellyfin.media3:media3-ffmpeg-decoder:1.3.1+2")

    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.material:material:1.12.0")



    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("androidx.core:core-splashscreen:1.0.1")


}

// === AUTO-GENERATE version.json AFTER BUILD ===
tasks.register("generateVersionJson") {
    doLast {
        val versionName = android.defaultConfig.versionName
        val versionCode = android.defaultConfig.versionCode
        val outputDir = file("${buildDir}/outputs/version")
        outputDir.mkdirs()

        val jsonFile = file("${outputDir}/version.json")
        jsonFile.writeText(
            """
            {
              "versionName": "$versionName",
              "versionCode": $versionCode,
              "apkUrl": "https://videos.122movies.my.id/app/app-release-$versionCode.apk"
            }
            """.trimIndent()
        )
        println("✅ Generated version.json at: ${jsonFile.absolutePath}")
    }
}

// Hook this task to run after the release build
gradle.projectsEvaluated {
    tasks.named("assembleRelease") {
        finalizedBy("generateVersionJson")
    }
}
