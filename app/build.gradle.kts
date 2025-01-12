import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.chaquo.python")
}

android {
    namespace = "com.example.recognizenumber"
    compileSdk = 34

    flavorDimensions += "pyVersion"
    productFlavors {
        create("py38") { dimension = "pyVersion" }
    }

    defaultConfig {
        applicationId = "com.example.recognizenumber"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a")
        }
    }
    chaquopy {
        productFlavors {
            getByName("py38") { version = "3.8" }
        }
        defaultConfig {
            val path: String = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                "C:\\Users\\user\\AppData\\Local\\Programs\\Python\\Python312\\python.exe"
            } else {
                "/home/kirill/miniconda3/bin/python"
            }
            buildPython(path)
            pip {
                val path_to_libs =
                    "" + projectDir +
                            File.separator + "src" +
                            File.separator + "main" +
                            File.separator + "java" +
                            File.separator + "com" +
                            File.separator + "example" +
                            File.separator + "recognizenumber" +
                            File.separator + "neuro_libraries" +
                            File.separator

                val matplotlib =
                    "matplotlib-3.6.0-0-cp38-cp38-android_21_arm64_v8a.whl"
                val numpy =
                    "numpy-1.19.5-0-cp38-cp38-android_21_arm64_v8a.whl"
                val torch = "torch-1.8.1-3-cp38-cp38-android_21_arm64_v8a.whl"
                val torch_vision = "torchvision-0.9.1-1-cp38-cp38-android_21_arm64_v8a.whl"
                val pillow = "Pillow-9.2.0-0-cp38-cp38-android_21_arm64_v8a.whl"

                install((path_to_libs + torch))
                install((path_to_libs + matplotlib))
                install((path_to_libs + torch_vision))
                install((path_to_libs + pillow))
                install((path_to_libs + numpy))
            }
            pyc {
                src = false
            }
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            multiDexEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            multiDexEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val camerax_version = "1.2.2"
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
}