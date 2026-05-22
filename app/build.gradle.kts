import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.agcoding.cartrackingapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.agcoding.cartrackingapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 14
        versionName = "1.0.13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Load API key from local.properties (not committed to git)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // Add OpenAI API key to BuildConfig
        val openAiKey = localProperties.getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/cariboo_key.jks")
            storePassword = "CaribooKey!"
            keyAlias = "cariboo"
            keyPassword = "CaribooKey!"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = true
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // Custom output file naming for APKs
    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val buildType = variant.buildType.name
            val versionName = variant.versionName

            // Set custom APK file name
            output.outputFileName = "cariboo_${buildType}_${versionName}.apk"
        }
    }

    // Custom bundle naming (AAB files)
    tasks.whenTaskAdded {
        if (name.startsWith("bundle")) {
            doLast {
                val variantName = name.removePrefix("bundle").replaceFirstChar { it.lowercase() }
                val buildType = if (variantName.contains("release", ignoreCase = true)) "release" else "debug"
                val versionName = defaultConfig.versionName

                val bundleDir = file("${layout.buildDirectory.get()}/outputs/bundle/${variantName}")
                val defaultBundleFile = File(bundleDir, "app-${variantName}.aab")
                val customBundleFile = File(bundleDir, "cariboo_${buildType}_${versionName}.aab")

                if (defaultBundleFile.exists()) {
                    // Copy instead of rename so Android Studio's Locate button still works
                    defaultBundleFile.copyTo(customBundleFile, overwrite = true)
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += setOf("NullSafeMutableLiveData", "RememberInComposition")
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Force a consistent log4j-api version across all configurations so any transitive
// dependency that pulls an older version (e.g. POI 5.2.3 → 2.17.2) is upgraded.
configurations.all {
    resolutionStrategy {
        force("org.apache.logging.log4j:log4j-api:${libs.versions.log4jApiVersion.get()}")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Compose
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Location
    implementation(libs.play.services.location)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Apache POI for Excel files
    // Exclude old log4j-api (2.17.2) bundled by POI — uses deprecated Class.newInstance()
    // which crashes on Android API 31+ with InstantiationException for DefaultFlowMessageFactory.
    implementation(libs.apache.poi) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    implementation(libs.apache.poi.ooxml) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    // Force newer log4j-api (2.21.1+) that uses getDeclaredConstructor().newInstance()
    implementation(libs.log4j.api)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Glance (Widgets)
    implementation(libs.bundles.glance)

    // Retrofit & OkHttp for API calls (Voice LLM integration)
    implementation(libs.bundles.networking)

    // Moshi for JSON parsing
    implementation(libs.bundles.moshi.bundle)
    ksp(libs.moshi.kotlin.codegen)

    // Timber
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}