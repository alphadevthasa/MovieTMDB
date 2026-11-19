plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.thasa.moviedb.core.domain"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    api(libs.androidx.paging.runtime)
    api(libs.kotlinx.coroutines.core)
}

// This module has no instrumented tests.
androidComponents {
    beforeVariants(selector().all()) { it.enableAndroidTest = false }
}
