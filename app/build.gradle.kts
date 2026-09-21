plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.kotlinParcelize)
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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
    //Layout
    implementation(libs.material)
    implementation(libs.multicolors)
    //Image
    implementation(libs.coil)                           //Coil Image
    implementation(libs.coil.network.okhttp)
    api(libs.android.image.cropper)                     //Image Crop
    //Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    //Cloudinary
    implementation(libs.cloudinary.android)
    //MVVM
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    //Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    //Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    //Coroutines
    implementation(libs.kotlinx.coroutines.android)
    //Other
    implementation(libs.material.ripple)                //Ripple Effect
    implementation(libs.bubblebottom)                   //Bottom Navigation
    implementation(libs.timber)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}