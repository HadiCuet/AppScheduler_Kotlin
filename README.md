# Android App Scheduler

A modern Android application built with Kotlin and Jetpack Compose that allows users to schedule any installed app to launch at specific times, with comprehensive schedule management and execution tracking.

## 📱 Features

### Core Functionality
- **Schedule App Launches**: Select any installed app and schedule it to launch at a specific date and time
- **Edit Schedules**: Modify the time of existing schedules with conflict detection
- **Cancel Schedules**: Cancel any upcoming schedule before it executes
- **Multiple Schedules**: Support for multiple future schedules with conflict prevention
- **Execution Tracking**: Complete record of schedule execution with status tracking
- **Boot Persistence**: Schedules survive device reboots and are automatically restored

### Schedule Status Tracking
- **SCHEDULED**: Schedule is set and waiting to fire
- **FIRED**: Alarm has triggered and notification is being processed
- **LAUNCH_INTENT_SENT**: High-priority notification sent to user
- **LAUNCHED_CONFIRMED**: App launch verified (requires Usage Access permission)
- **CANCELLED**: Schedule cancelled by user
- **MISSED**: Schedule failed to execute (app uninstalled, permission issues, etc.)

### Android Compliance
- **Android 12+ Ready**: Full support for exact alarm permissions
- **Android 13+ Compatible**: Runtime notification permissions
- **Background Launch Compliant**: Uses notification-based approach for Android 10+ restrictions
- **Usage Stats Verification**: Optional verification of successful app launches

## 🏗️ Architecture

### Modern Android Stack
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room for local persistence
- **Concurrency**: Kotlin Coroutines and Flow
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

### Project Structure
```
app/
├── src/main/java/com/hadi/appscheduler/
│   ├── data/               # Room database, entities, DAOs
│   │   ├── AppDatabase.kt
│   │   ├── Schedule.kt
│   │   ├── ScheduleDao.kt
│   │   └── ScheduleStatus.kt
│   ├── domain/             # Business logic services
│   │   ├── AppScheduler.kt      # AlarmManager wrapper
│   │   └── LaunchVerifier.kt    # Usage stats verification
│   ├── alarm/              # System broadcast receivers
│   │   ├── AlarmReceiver.kt     # Handles scheduled alarms
│   │   └── BootReceiver.kt      # Restores alarms after reboot
│   ├── ui/                 # Jetpack Compose UI
│   │   ├── screens/             # Screen composables and ViewModels
│   │   ├── theme/               # Material 3 theming
│   │   ├── MainActivity.kt
│   │   ├── AppSchedulerApp.kt   # Navigation and app structure
│   │   ├── ScheduleRepository.kt
│   │   └── AppListProvider.kt
│   └── util/               # Utility classes
│       ├── PermissionHelper.kt
│       └── DateTimeUtils.kt
└── src/test/               # Unit tests
    └── java/com/hadi/appscheduler/
        ├── data/           # Room DAO tests
        ├── domain/         # Service logic tests
        └── util/           # Utility tests
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- Android SDK API 35
- JDK 8 or higher

### Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/HadiCuet/AppScheduler_Kotlin.git
   cd AppScheduler_Kotlin
   ```

2. Open the project in Android Studio

3. Sync project with Gradle files

4. Build and run:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### Permission Setup
The app requires several permissions for full functionality:

1. **Notification Permission (Android 13+)**:
   - Granted automatically on first run
   - Required to show schedule notifications

2. **Exact Alarm Permission (Android 12+)**:
   - The app will prompt you to enable this in Settings
   - Navigate to: Settings → Apps → App Scheduler → Set Alarms and Reminders → Allow

3. **Usage Access (Optional)**:
   - For launch verification functionality
   - Navigate to: Settings → Privacy → Device admin apps → Usage access → App Scheduler → Allow

## 📋 How to Use

### Creating a Schedule
1. Tap the **+** floating action button
2. Select an app from the list
3. Choose date and time using the date/time pickers
4. Tap **Save Schedule**

### Managing Schedules
- **Edit**: Tap the menu (⋮) on any upcoming schedule and select "Edit"
- **Cancel**: Use the menu to cancel schedules before they fire
- **Delete**: Remove schedules from history using the delete option

### Viewing Execution Logs
- Switch to the "Logs" tab to view execution history
- Filter by "All", "Success", or "Failed" status
- See detailed information about each execution attempt

## 🔍 Testing & Validation

### Demo Script
To validate all features work correctly:

1. **Create Multiple Schedules**:
   ```
   - Schedule Calculator to open in 2 minutes
   - Schedule Camera to open in 4 minutes
   - Verify no conflicts when selecting same time
   ```

2. **Edit a Schedule**:
   ```
   - Change Calculator schedule to 3 minutes
   - Verify old alarm is cancelled and new one set
   ```

3. **Cancel a Schedule**:
   ```
   - Cancel the Camera schedule
   - Verify it shows as CANCELLED in logs
   ```

4. **Wait for Execution**:
   ```
   - Let Calculator schedule fire
   - Tap the notification to launch the app
   - Check logs for LAUNCH_INTENT_SENT status
   ```

5. **Reboot Test**:
   ```
   - Create schedule 10 minutes in future
   - Reboot device/emulator
   - Verify schedule still fires after boot
   ```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Generate test report
./gradlew testDebugUnitTest --continue
```

## 🔧 Technical Implementation

### Alarm Scheduling
- Uses `AlarmManager.setExactAndAllowWhileIdle()` with `RTC_WAKEUP`
- Stable request codes derived from schedule IDs
- Immutable PendingIntents for Android 12+ compatibility

### Notification System
- High-priority notification channel for urgent alerts
- Tap-to-launch functionality with proper intent handling
- Graceful degradation when notification permission is denied

### Data Persistence
- Room database with automatic migrations
- Coroutines and Flow for reactive data
- Proper transaction handling for data consistency

### Time Handling
- All times stored as UTC epoch milliseconds
- UI displays times in local timezone
- Proper DST handling through epoch-based scheduling

## 🚨 Platform Restrictions Research

### Background Activity Launch Restrictions (Android 10+)

Starting with Android 10 (API 29), Google implemented strict restrictions on background activity starts to improve user experience and prevent malicious apps from disrupting users.

#### The Problem
- Apps cannot directly start activities from the background
- Traditional alarm-triggered activity launches are blocked
- Users see no indication when scheduled apps should launch

#### Our Solution: Notification-Based Approach
We implement a compliant solution that:
1. **Alarm Fires**: `AlarmReceiver` triggers at the scheduled time
2. **High-Priority Notification**: Shows prominent notification to user
3. **User Action Required**: User must tap notification to launch app
4. **Status Tracking**: Records whether user acted on notification

#### Alternative: Full-Screen Intent (Optional)
```kotlin
// Full-screen intent option (requires justification)
val fullScreenIntent = PendingIntent.getActivity(
    context, requestCode, launchIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

notification.setFullScreenIntent(fullScreenIntent, true)
```

**Why we don't use this by default**:
- Requires `USE_FULL_SCREEN_INTENT` permission
- Google Play Store scrutinizes this permission heavily
- Only appropriate for alarm clock apps and incoming calls
- May be rejected during app review process

#### References
- [Background Activity Launch Restrictions](https://developer.android.com/guide/components/activities/background-starts)
- [Exact Alarm Permission](https://developer.android.com/about/versions/12/features/exact-alarms)
- [Full-Screen Intent Best Practices](https://developer.android.com/training/notify-user/time-sensitive)

### Exact Alarm Changes (Android 12+)
- `SCHEDULE_EXACT_ALARM` permission required for exact timing
- Users must explicitly grant permission in Settings
- Fallback to inexact alarms if permission denied
- Apps can check `AlarmManager.canScheduleExactAlarms()`

## 🧪 Testing Strategy

### Unit Tests
- **DAO Tests**: Room database operations with in-memory DB
- **Service Tests**: AppScheduler alarm management logic
- **Utility Tests**: Date/time formatting and calculations
- **Repository Tests**: Data layer integration

### Integration Tests
- **AlarmReceiver Tests**: Broadcast receiver logic with Robolectric
- **Permission Tests**: Permission flow validation
- **Notification Tests**: Notification creation and handling

### Manual Test Cases
1. **Permission Flows**: Test each permission grant/deny scenario
2. **Edge Cases**: App uninstall, time zone changes, device reboot
3. **Conflict Detection**: Same-time schedule validation
4. **Notification Handling**: Various notification states

## 🔒 Privacy & Security

- **Minimal Permissions**: Only requests necessary permissions
- **Local Data**: All data stored locally, no cloud sync
- **Usage Stats**: Optional permission, clearly explained to users
- **No Network**: App works completely offline

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

If you encounter any issues or have questions:
- Create an issue on GitHub
- Check the demo script for validation steps
- Review the permission setup instructions

---

**Built with ❤️ using Kotlin and Jetpack Compose**