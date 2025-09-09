import java.util.Properties
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "de.yogaknete.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.yogaknete.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Check for CI/CD environment variables first
            val signingKeyBase64 = System.getenv("SIGNING_KEY_BASE64")
            val keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProperties["keyAlias"]?.toString()
            val keyStorePassword = System.getenv("KEY_STORE_PASSWORD") ?: keystoreProperties["storePassword"]?.toString()
            val keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProperties["keyPassword"]?.toString()
            
            if (signingKeyBase64 != null && keyAlias != null && keyStorePassword != null && keyPassword != null) {
                // CI/CD signing using base64 encoded keystore
                val keystoreFile = File.createTempFile("keystore", ".jks")
                keystoreFile.writeBytes(Base64.getDecoder().decode(signingKeyBase64))
                storeFile = keystoreFile
                storePassword = keyStorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else if (keystoreProperties.containsKey("storeFile")) {
                // Local development signing using keystore.properties
                storeFile = file(keystoreProperties["storeFile"].toString())
                storePassword = keystoreProperties["storePassword"].toString()
                this.keyAlias = keystoreProperties["keyAlias"].toString()
                this.keyPassword = keystoreProperties["keyPassword"].toString()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Use release signing configuration
            signingConfig = signingConfigs.getByName("release")

            // Performance optimizations
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3

            // Packaging options for smaller APK
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    excludes += "META-INF/DEPENDENCIES"
                    excludes += "META-INF/LICENSE"
                    excludes += "META-INF/LICENSE.txt"
                    excludes += "META-INF/license.txt"
                    excludes += "META-INF/NOTICE"
                    excludes += "META-INF/NOTICE.txt"
                    excludes += "META-INF/notice.txt"
                    excludes += "META-INF/ASL2.0"
                    excludes += "META-INF/*.kotlin_module"
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    // Configure APK naming
    applicationVariants.all {
        val buildType = this.buildType.name
        val versionName = this.versionName
        
        this.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            
            when (buildType) {
                "debug" -> {
                    output.outputFileName = "yogaknete-debug-${versionName}.apk"
                }
                "release" -> {
                    output.outputFileName = "yogaknete-${versionName}.apk"
                }
            }
        }
    }
    
    // Configure AAB naming for bundle tasks
    tasks.whenTaskAdded {
        if (name.startsWith("bundle") && name.contains("Release")) {
            doLast {
                val bundleDir = layout.buildDirectory.dir("outputs/bundle/release").get().getAsFile()
                if (bundleDir.exists()) {
                    bundleDir.listFiles()?.forEach { file ->
                        if (file.name.endsWith(".aab")) {
                            // Get version from defaultConfig
                            val versionName = android.defaultConfig.versionName ?: "1.0"
                            
                            val newName = "yogaknete-${versionName}.aab"
                            val newFile = File(bundleDir, newName)
                            if (file.renameTo(newFile)) {
                                println("Renamed AAB: ${file.name} -> $newName")
                            }
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM - ensures all compose libraries use compatible versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    // Additional Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Date/Time handling
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Serialization for backup/restore
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Testing
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
