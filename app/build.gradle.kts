import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
} else {
    throw GradleException("local.properties file not found at project root.")
}

val apiKeyGoogleMaps = localProperties.getProperty("API_KEY_GOOGLE_MAPS")
    ?: throw GradleException("API_KEY is missing in local.properties")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ktlint)
    kotlin("kapt")
}

android {
    namespace = "com.ualachallenge"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ualachallenge"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY_GOOGLE_MAPS", "\"$apiKeyGoogleMaps\"")
        manifestPlaceholders["apiKeyGoogleMaps"] = apiKeyGoogleMaps
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-Xsuppress-version-warnings")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ktlint {
    version.set("0.50.0")
    enableExperimentalRules.set(true)
    filter {
        exclude { element -> element.file.path.contains("build/") }
    }
    // Use .editorconfig for configuration
    android.set(true)
    verbose.set(true)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Pull to refresh
    implementation(libs.androidx.material)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.material)

    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))

    // Unit tests (JUnit 5)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.androidx.hilt.compiler)

    // Google Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
