# 🚀 Build Daily - Smart Productivity App

<div align="center">

![Build Daily Logo](https://img.shields.io/badge/Build_Daily-Purple-8B5CF6?style=for-the-badge&logo=android)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A modern, time-based productivity companion for Android**

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Tech Stack](#-tech-stack) • [Contributing](#-contributing)

</div>

---

## ✨ About

**Build Daily** is a premium productivity application designed to help you take control of your time through intelligent scheduling, comprehensive analytics, and beautiful design. Unlike traditional to-do apps, Build Daily focuses on **time-based task management**, helping you plan your day with precision and track your progress with powerful insights.

### 🎯 Key Philosophy

- **Time-First Approach**: Schedule tasks by time slots, not just lists
- **Full Device Adaptivity**: Fully responsive UI for phones, tablets, and foldables
- **Premium Security Vault**: Multi-method biometric and PIN-based protection
- **Data-Driven Insights**: Understand your productivity patterns with detailed analytics
- **Beautiful Experience**: Enjoy a premium galactic dark theme with smooth animations
- **Privacy-First**: Device-based identity with no mandatory account creation

---

## 🌟 Features

### 🛡️ Premium Security Vault
- **Multi-Method Lock**: Choose between 4-Digit PIN, 6-Digit PIN, Pattern, or Alphanumeric Password
- **Biometric Integration**: Seamless Fingerprint and Face Unlock support
- **Secure Onboarding**: Refined security setup flow with no forced defaults
- **Dynamic Lock Screen**: Beautiful, interactive lock UI with glassmorphism effects

### 📱 Full Device Adaptivity
- **Responsive Layouts**: Adaptive UI using `BoxWithConstraints` and `FlowRow`
- **Tablet & Foldable Ready**: Intelligent multi-column grids for large screen optimization
- **Keyboard Awareness**: Zero UI overlap with smart `imePadding` management
- **Orientation Support**: Optimized layouts for both Portrait and Landscape modes

### 📅 Smart Time Scheduling
- **Time-Based Tasks**: Schedule tasks with precise time slots
- **Conflict Detection**: Automatic warning for overlapping tasks
- **Drag & Drop**: Intuitive task reordering
- **Recurring Tasks**: Set daily, weekly, or custom recurrence patterns

### 🔔 Intelligent Notification System
- **Hydration Daisy-Chain**: Smart water reminders that adapt to your intake
- **Context-Aware Reminders**: Smart notifications based on task timing
- **Quiet Hours**: Respect your sleep schedule (10 PM - 7 AM)
- **Notification History**: Track all your past notifications
- **Permission Guard**: Seamless Android 13+ permission management

### 💰 Buy List & Budget Manager
- **Financial Precision**: High-accuracy `Double` based currency tracking
- **Savings Tracker**: Log incremental savings towards specific goals
- **Priority Logic**: Organize your wishlist by "Must Haves" vs "Wants"
- **Affordability Forecast**: Instant calculation of days remaining to hit your goals

### 📚 Book Library Vault
- **Reading Progress**: Track pages read vs total pages with real-time percentages
- **Dynamic Stats**: Automatically calculated "Books Read This Year" and yearly goals
- **Favorite System**: Curate a premium vault of your top reads
- **Archive Logic**: Keep your main library clean while preserving history

### 📊 Powerful Analytics
- **Adaptive Charts**: Responsive data visualization that scales with screen size
- **Daily Completion Rate**: Track your daily progress
- **Weekly Trends**: Visualize your weekly productivity
- **Category Breakdown**: See performance by task type
- **Streak Tracking**: Build consistent habits with streaks

### ⏱️ Pomodoro Timer
- **Built-in Timer**: 25/5 minute work/break cycles
- **Background Service**: Timer continues even when app is closed
- **Focus Mode**: Minimize distractions during sessions

---

## 📸 Screenshots

### Home Screen
*Your daily command center*
- Timeline view of today's tasks
- Quick stats and progress indicators

### Buy List & Budget
*Strategic financial planning*
- Item prioritization and cost tracking
- Progress bars for savings goals
- High-precision financial calculations

### Book Library
*Your intellectual sanctuary*
- Visual library layout with cover-style cards
- Detailed progress tracking for every book
- Yearly reading goal analytics

---

## 🛠️ Tech Stack

### Frontend & UI
- **Kotlin**: Modern, type-safe programming language
- **Jetpack Compose**: Declarative UI toolkit for premium animations
- **Material Design 3**: Latest design tokens and components
- **Coroutines & StateFlow**: Reactive, thread-safe state management

### Architecture
- **Advanced MVVM**: Clean separation of UI and business logic
- **ViewModel Orchestration**: Centralized state derivation for maximum performance
- **Repository Pattern**: Robust data access and persistence layer
- **High-Precision Persistence**: Bit-level `Double` storage for financial data

### Libraries
- **kotlinx-datetime**: High-precision time and date management
- **kotlinx-serialization**: Modern JSON parsing and object mapping
- **Vico Charts**: Premium data visualizations
- **SharedPreferences**: Encrypted-ready local storage

---

## 📱 App Structure

```
app/
├── data/
│   ├── model/              # Data models (Task, Book, BuyItem, Hydration)
│   ├── repository/         # Data access layer with precision persistence
│   └── local/              # Local storage management
├── ui/
│   ├── home/               # Adaptive Home screen with grid/list support
│   ├── security/           # Multi-method security vault & biometric logic
│   ├── buylist/            # Wishlist & Budget Manager
│   ├── booklibrary/        # Smart Reading Vault
│   ├── hydration/          # Daisy-chain hydration module
│   ├── stats/              # Advanced Analytics with responsive charts
│   ├── profile/            # Unified Settings & Utilities hub
│   ├── pomodoro/           # Pomodoro focus timer
│   └── components/         # Reusable premium UI components
├── util/                   # Utility classes (Schedulers, Formatters, BiometricHelper)
└── BuildDailyApp.kt        # Application navigation & dependency hub
```

---

## 🤝 Contributing

We welcome contributions! Please follow the Kotlin coding conventions and ensure all new features are implemented using the established **MVVM** pattern with proper **StateFlow** exposure.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ using Kotlin and Jetpack Compose**

[⬆ Back to Top](#-build-daily---smart-productivity-app)

</div>
