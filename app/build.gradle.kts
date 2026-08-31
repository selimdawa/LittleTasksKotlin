plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.flatcode.littletasks"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.flatcode.littletasks"
        minSdk = 24
        targetSdk = 37
        versionCode = 8
        versionName = "1.38"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    //signingConfigs {
    //    create("release") {
    //        storeFile = file("D:\\MyProjects\\Kotlin\\Little Tasks\\Little Tasks\\LittleTasks.jks")
    //        storePassword = "00000000"
    //        keyAlias = "LittleTasks"
    //        keyPassword = "00000000"
    //    }
    //}
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    //buildTypes {
    //    getByName("release") {
    //        signingConfig = signingConfigs.getByName("release")
    //        isMinifyEnabled = true
    //        isShrinkResources = true
    //        proguardFiles(
    //            getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
    //        )
    //    }
    //}
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)           //Shared Preference
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Layout
    implementation(libs.material)
    implementation(libs.multicolors)
    //Image
    implementation(libs.circleimageview)                //Circle Image
    implementation(libs.coil)                           //Coil Image
    api(libs.android.image.cropper)                     //Image Crop
    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    //Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    //implementation(libs.firebase.crashlytics)
    //Other's
    implementation(libs.bubblebottom)                   //Bottom Navigation
    implementation(libs.material.ripple)                //Ripple Effect
}