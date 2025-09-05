# 🚀 Android Project Setup Guide

## Current Repository Structure

We've successfully cleaned up the repository and created a new `android-main` branch:
- ✅ Removed old web app code (6.1GB of React + Rust)
- ✅ Kept important documentation and analysis
- ✅ Created Android-focused README
- ✅ Added Android-specific .gitignore

## Next Steps to Complete Setup

### 1. Install Android Studio
If you haven't already, download Android Studio from: https://developer.android.com/studio

### 2. Open the Project in Android Studio
```bash
# Navigate to the android folder
cd /mnt/quickstuff/git/Vereinsknete/android

# Open with Android Studio
android-studio .
```

### 3. Create New Android Project via Android Studio
Since we need the proper Android project structure, use Android Studio's wizard:

1. **File → New → New Project**
2. **Select "Empty Activity"** (we'll use Compose later)
3. **Configure:**
   - Name: `YogaKnete`
   - Package: `de.yogaknete.app`
   - Save location: `/mnt/quickstuff/git/Vereinsknete/android`
   - Language: `Kotlin`
   - Minimum SDK: `API 26 (Android 8.0)`
   - Build configuration language: `Kotlin DSL`

### 4. Add Dependencies
Once the project is created, update `app/build.gradle.kts`:

```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Hilt for DI
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### 5. Enable Required Features
In `app/build.gradle.kts`, ensure these are enabled:

```kotlin
android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

### 6. Create Initial Project Structure

```bash
# Create the package structure
mkdir -p app/src/main/java/de/yogaknete/app/{core,data,domain,presentation}
mkdir -p app/src/main/java/de/yogaknete/app/core/{di,utils,navigation}
mkdir -p app/src/main/java/de/yogaknete/app/data/{local,repository}
mkdir -p app/src/main/java/de/yogaknete/app/domain/{model,repository,usecase}
mkdir -p app/src/main/java/de/yogaknete/app/presentation/{theme,components,screens}
```

## Git Workflow

### Current Status
- We're on `android-main` branch
- Old `main` branch still has the web app for reference
- All changes are committed

### Recommended Git Strategy
```bash
# Push the new android-main branch
git push -u origin android-main

# When ready, make android-main the new default
# (Can be done in GitHub settings)
```

### Alternative: Fresh Repository
If you prefer a completely fresh start:
```bash
# Create new repo
git init YogaKnete-Android
cd YogaKnete-Android

# Copy over documentation
cp ../Vereinsknete/ANDROID_*.md .
cp ../Vereinsknete/MOCKUPS_DEUTSCH.md .
cp -r ../Vereinsknete/prototype .
```

## Week 1 Development Checklist

Once Android Studio project is set up:

### Day 1-2: Foundation
- [ ] Create Room database entities (UserProfile, Studio, YogaClass)
- [ ] Set up Hilt dependency injection
- [ ] Create base theme with yoga colors

### Day 3-4: Onboarding
- [ ] Create onboarding screens with Compose
- [ ] User profile setup form
- [ ] Studio addition screen
- [ ] Save to database

### Day 5: Week View
- [ ] Basic week view layout
- [ ] Display classes from database
- [ ] Add class functionality
- [ ] Mark as completed/cancelled

## Questions Resolved

Based on your feedback:
- ✅ Studios are dynamically added (not hardcoded)
- ✅ Cancellation of past classes is supported
- ✅ Invoice format includes all required fields
- ✅ Development is incremental (weekly releases)
- ✅ Yoga-themed purple colors for UI

## Ready to Code! 🚀

The repository is now clean and ready for Android development. The next step is to:

1. Open Android Studio
2. Create the project structure using the wizard
3. Start implementing Week 1 features

Need help with any of these steps? Let me know!
