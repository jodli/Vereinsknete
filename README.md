# 🧘‍♀️ YogaKnete - Simple Yoga Class Tracker & Invoice Generator

## 📱 Android App for Yoga Instructors

**YogaKnete** is a simplified Android app designed specifically for yoga instructors to track their classes and generate professional invoices with minimal effort. Built with modern Android technologies and a focus on user experience.

## ✨ Key Features

- **Quick Class Entry**: Log a class in under 10 seconds with smart templates
- **Week View**: See all your classes at a glance with color-coded status
- **Flexible Cancellations**: Edit or cancel classes even from past weeks
- **Professional Invoices**: Generate PDF invoices with all required German tax information
- **Studio Management**: Add unlimited studios with individual hourly rates
- **WhatsApp Integration**: Share invoices directly via WhatsApp or email
- **Offline-First**: Everything works without internet connection
- **German Language**: Fully localized for German users

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Architecture**: MVVM with Clean Architecture
- **PDF Generation**: iText7
- **Design**: Material Design 3 (Material You)
- **DI**: Hilt
- **Testing**: JUnit, MockK, Compose UI Testing

## 📚 Documentation

- [Implementation Plan](ANDROID_IMPLEMENTATION_PLAN.md) - Detailed technical architecture
- [Incremental Roadmap](ANDROID_INCREMENTAL_ROADMAP.md) - Week-by-week development plan
- [Test Strategy](ANDROID_TEST_PLAN.md) - Comprehensive testing approach
- [Mockups (German)](MOCKUPS_DEUTSCH.md) - UI mockups and workflows
- [Analysis](ANDROID_PORT_ANALYSIS.md) - Original web app analysis and learnings

## 🎯 Design Goals

1. **Simplicity First**: Focused on one user's specific workflow
2. **Mobile-Optimized**: Designed for one-thumb operation
3. **Fast Data Entry**: Templates and smart defaults
4. **Professional Output**: Clean, compliant German invoices
5. **Reliability**: Offline-first with automatic backups

## 🚦 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Kotlin 1.9+

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/YogaKnete.git
cd YogaKnete

# Build with Gradle
./gradlew build

# Run tests
./gradlew test

# Install debug version on device
./gradlew installDebug

# Build production release (requires signing)
./gradlew assembleRelease
```

## 📱 Installation & Demo

### For Users

Download the latest APK from the [Releases](https://github.com/yourusername/YogaKnete/releases) page and install on your Android device.

### Demo

Check out the [interactive HTML prototype](prototype/yoga-app-demo.html) to see the user flow in action, or build and install the actual Android app using the instructions above.

## 🎆 What's New in v1.0

- **Complete Feature Set**: All planned features are now implemented
- **Full German Localization**: UI completely translated for German yoga instructors
- **Professional Invoice Generation**: PDF invoices with proper German tax formatting
- **Robust Offline Support**: Works completely without internet connection
- **Comprehensive Testing**: Full test suite ensuring reliability
- **Material Design 3**: Modern, accessible UI following latest Android design guidelines

## 🤝 Contributing

While YogaKnete v1.0 is feature-complete, contributions are welcome for:

- Bug fixes and performance improvements
- Additional language translations
- New export formats
- Enhanced accessibility features

Please open an issue first to discuss any major changes!

## 📄 License

MIT License - Feel free to use this code for your own projects.

---

_Built with ❤️ for yoga instructors who just want to focus on teaching_
