import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val minhaChaveMaps = localProperties.getProperty("MAPS_API_KEY") ?: ""

android {
    namespace = "com.example.ambulncia_atividade"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ambulncia_atividade"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        manifestPlaceholders["MAPS_API_KEY"] = minhaChaveMaps

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
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}