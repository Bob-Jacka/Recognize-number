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
        create("py312") { dimension = "pyVersion" }
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
            getByName("py312") { version = "3.12" }
        }
        defaultConfig {
            val path: String = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                "C:\\Users\\user\\AppData\\Local\\Programs\\Python\\Python312\\python.exe"
            } else {
                "/home/kirill/"
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

                val keras =
                    "keras-3.6.0-py3-none-any.whl"
                val matplotlib =
                    "matplotlib-3.8.2-0-cp312-cp312-android_21_arm64_v8a.whl"
                val ml_dtypes =
                    "ml_dtypes-0.4.0-cp312-cp312-macosx_10_9_universal2.whl"
                val h5py =
                    "h5py-3.10.0-cp312-cp312-macosx_11_0_arm64.whl"
                val optree =
                    "optree-0.13.1-cp312-cp312-manylinux_2_17_aarch64.manylinux2014_aarch64.whl"
                val numpy =
                    "numpy-1.26.2-0-cp312-cp312-android_21_arm64_v8a.whl"
                val tensorflow =
                    "tensorflow-2.17.0-cp312-cp312-macosx_12_0_arm64.whl "


                install((path_to_libs + keras))
                install((path_to_libs + matplotlib))
                install((path_to_libs + ml_dtypes))
                install((path_to_libs + h5py))
                install((path_to_libs + optree))
                install((path_to_libs + tensorflow))
                install((path_to_libs + numpy))
            }
            pyc {
                src = true
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