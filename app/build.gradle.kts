import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// ---------------------------------------------------------------------------
// Centralized versioning (see version.properties in the project root).
//
// The development and production tracks each have their OWN versionName and
// versionCode. The profile is selected with -Pcaribou.profile=<development|
// production> (defaults to "development" so local/IDE builds keep working).
//
// The convenience tasks assembleDevelopmentBump / bundleProductionBump bump the
// matching track (+1 versionName patch, +1 versionCode) BEFORE the build. The
// bump runs at configuration time — while the requested-task list is known but
// before the version below is read — so a single command bumps and then builds,
// in one Gradle process, with no bash or nested invocation. It only edits
// version.properties locally (no git commit). CI does the same via
// scripts/bump-version.sh.
// ---------------------------------------------------------------------------

// Rewrites version.properties, bumping only the requested track.
fun bumpCaribouVersion(profile: String) {
    val file = rootProject.file("version.properties")
    val props = Properties().apply { if (file.exists()) file.inputStream().use { load(it) } }

    fun bumpPatch(name: String): String {
        val parts = name.split(".")
        require(parts.size == 3) { "versionName '$name' is not in MAJOR.MINOR.PATCH form" }
        return "${parts[0]}.${parts[1]}.${parts[2].trim().toInt() + 1}"
    }

    // Fall back to a shared legacy "versionName" if a per-track name is absent.
    val legacyName = props.getProperty("versionName")
    var devName = props.getProperty("developmentVersionName") ?: legacyName ?: "1.0.0"
    var devCode = props.getProperty("developmentVersionCode")?.trim()?.toInt() ?: 1
    var prodName = props.getProperty("productionVersionName") ?: legacyName ?: "1.0.0"
    var prodCode = props.getProperty("productionVersionCode")?.trim()?.toInt() ?: 1

    if (profile == "production") {
        prodName = bumpPatch(prodName); prodCode += 1
    } else {
        devName = bumpPatch(devName); devCode += 1
    }

    file.writeText(
        """
        # Centralized version management for Caribou.
        #
        # developmentVersionName / developmentVersionCode -> bumped ONLY on development builds.
        # productionVersionName  / productionVersionCode  -> bumped ONLY on production builds.
        #
        # The development and production tracks are fully independent: each has its own
        # versionName and versionCode counter. This file is updated by
        # scripts/bump-version.sh (manual CI workflow) and by the local Gradle tasks
        # (assembleDevelopmentBump / bundleProductionBump). Edit with care.
        developmentVersionName=$devName
        developmentVersionCode=$devCode
        productionVersionName=$prodName
        productionVersionCode=$prodCode
        """.trimIndent() + "\n"
    )
    val active = if (profile == "production") "$prodName (code $prodCode)" else "$devName (code $devCode)"
    logger.lifecycle("Caribou: bumped $profile version -> $active")
}

// Which convenience task (if any) was requested on the command line.
val requestedCaribouTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
val caribouBumpDevelopment = requestedCaribouTasks.contains("assembleDevelopmentBump")
val caribouBumpProduction = requestedCaribouTasks.contains("bundleProductionBump")

// Perform the bump now, before the version is read below.
if (caribouBumpDevelopment) bumpCaribouVersion("development")
if (caribouBumpProduction) bumpCaribouVersion("production")

val versionProps = Properties().apply {
    val versionPropsFile = rootProject.file("version.properties")
    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { load(it) }
    }
}
// A requested bump task also selects its profile so the build uses the right track.
val caribouProfile = when {
    caribouBumpProduction -> "production"
    caribouBumpDevelopment -> "development"
    else -> (project.findProperty("caribou.profile") as String?) ?: "development"
}
val caribouVersionName = when (caribouProfile) {
    "production" -> versionProps.getProperty("productionVersionName")
    else -> versionProps.getProperty("developmentVersionName")
} ?: versionProps.getProperty("versionName") ?: "1.0.0"
val caribouVersionCode = when (caribouProfile) {
    "production" -> versionProps.getProperty("productionVersionCode")?.trim()?.toInt() ?: 1
    else -> versionProps.getProperty("developmentVersionCode")?.trim()?.toInt() ?: 1
}

android {
    namespace = "com.agcoding.cartrackingapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.agcoding.cartrackingapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = caribouVersionCode
        versionName = caribouVersionName

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
            // The Caribou signing key is committed in the project so BOTH debug and
            // release builds are always signed with the same key. This lets a new
            // development APK be installed over a previous one and needs no CI secrets.
            // Environment variables override the committed values when provided (e.g. to
            // rotate the key via GitHub Secrets without changing the source).
            storeFile = file(System.getenv("CARIBOU_KEYSTORE_FILE") ?: "${rootProject.projectDir}/cariboo_key.jks")
            storePassword = System.getenv("CARIBOU_KEYSTORE_PASSWORD") ?: "CaribooKey!"
            keyAlias = System.getenv("CARIBOU_KEY_ALIAS") ?: "cariboo"
            keyPassword = System.getenv("CARIBOU_KEY_PASSWORD") ?: "CaribooKey!"
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
            // Sign debug/development builds with the same key as release, so a new
            // development APK can be installed over a previous one.
            signingConfig = signingConfigs.getByName("release")
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

// ---------------------------------------------------------------------------
// Convenience tasks: bump the version AND build, in a single command.
//
//   ./gradlew assembleDevelopmentBump  ->  bumps developmentVersionName +
//       developmentVersionCode (each +1), then assembles the development (debug) APK.
//   ./gradlew bundleProductionBump     ->  bumps productionVersionName +
//       productionVersionCode (each +1), then builds the production (release) AAB bundle.
//
// The version bump happens above, at configuration time, when either task is
// requested (see bumpCaribouVersion). These tasks just wire the bump to the
// matching build output. The bump edits version.properties locally only — it
// does NOT create a git commit.
// ---------------------------------------------------------------------------
tasks.register("assembleDevelopmentBump") {
    group = "caribou"
    description = "Bump the development version (versionName + versionCode +1) and assemble the development (debug) APK."
    dependsOn("assembleDebug")
}

tasks.register("bundleProductionBump") {
    group = "caribou"
    description = "Bump the production version (versionName + versionCode +1) and build the production (release) AAB bundle."
    dependsOn("bundleRelease")
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