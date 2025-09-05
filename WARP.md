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

## 🚀 Project Status

**Currently in Development** - Following an incremental 4-week roadmap:
- Week 1: Basic class tracking ✅ (In Progress)
- Week 2: Templates & better UX
- Week 3: Invoice generation
- Week 4: Polish & production ready

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
cd YogaKnete/android

# Build with Gradle
gradle build

# Run tests
gradle test

# Install on device
gradle installDebug
```

## 📱 Demo

Check out the [interactive HTML prototype](prototype/yoga-app-demo.html) to see the user flow in action.

## 🤝 Contributing

This is currently a personal project, but suggestions and feedback are welcome!

## 📄 License

MIT License - Feel free to use this code for your own projects.

---

*Built with ❤️ for yoga instructors who just want to focus on teaching*
