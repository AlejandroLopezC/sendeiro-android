plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "gal.sendeiro.rutas"
    compileSdk = 35
    defaultConfig {
        applicationId = "gal.sendeiro.rutas"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release {
            isMinifyEnabled = false
            // Firma configurada únicamente mediante propiedades locales externas al repositorio.
            val storePath = providers.gradleProperty("SENDEIRO_STORE").orNull
            val storePass = providers.gradleProperty("SENDEIRO_PASS").orNull
            val keyAliasValue = providers.gradleProperty("SENDEIRO_ALIAS").orNull
            val keyPassValue = providers.gradleProperty("SENDEIRO_KEY_PASS").orNull
            if (!storePath.isNullOrBlank() && !storePass.isNullOrBlank() && !keyAliasValue.isNullOrBlank() && !keyPassValue.isNullOrBlank()) {
                signingConfig = signingConfigs.create("externalRelease") {
                    storeFile = file(storePath)
                    storePassword = storePass
                    keyAlias = keyAliasValue
                    keyPassword = keyPassValue
                }
            }
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = false }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    kapt(libs.androidx.room.compiler)
}
