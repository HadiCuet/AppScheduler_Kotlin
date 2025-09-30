# Material 3 Redesign - Create/Edit Schedule Screen

## Overview

This PR completely redesigns the Create/Edit Schedule screen using modern Material 3 components, improved UX patterns, and modular architecture.

## Key Features Implemented

### 🎨 Modern Material 3 Design
- **LargeTopAppBar** with scroll behavior and dynamic titles
- **ElevatedCard** components for clear section organization  
- **Material 3 color scheme** with dynamic colors support
- **Proper spacing** (16dp horizontal, 12-16dp vertical)
- **48dp minimum touch targets** for accessibility

### ⚡ Enhanced User Experience
- **Quick time presets**: +5 min, Morning 9:00, Afternoon 13:00, Evening 18:00, Tonight 21:00
- **Human-readable summaries**: "Fires in 1h 12m on Tue, Oct 1"
- **Inline validation** with animated error states
- **Haptic feedback** on important actions
- **Bottom sheet pickers** for date/time selection

### 🔍 Improved App Selection
- **Bottom sheet** with search functionality
- **Recent apps** (up to 3 most recently selected)
- **App icons** with fallback to first letter
- **Smooth animations** with `animateItemPlacement`

### 🔧 Technical Improvements  
- **Modular component architecture**
- **Comprehensive unit tests** for utility functions
- **UI tests** for component validation
- **Accessibility** with proper content descriptions
- **Type-safe state management**

## Components Architecture

### Core Components

1. **EditScheduleScreen.kt** - Main screen orchestration
2. **AppPickerSheet.kt** - Bottom sheet with search and app list
3. **DateTimeSection.kt** - Quick presets and Material 3 pickers
4. **BottomActionBar.kt** - Sticky save/cancel bar
5. **Banners.kt** - Permission warnings and error messages

### Utilities

1. **TimePresets.kt** - Time preset management and formatting
2. **TimePresetsTest.kt** - Comprehensive unit tests

### UI/UX Highlights

```kotlin
// Quick preset example
TimePresets.applyPreset(TimePresets.Preset.MORNING) // Returns 9:00 today or tomorrow

// Human-friendly formatting
TimePresets.getHumanFriendlySummary(triggerTime) // "Fires in 1h 12m on Tue, Oct 1"

// Accessibility support
Modifier.semantics {
    contentDescription = "Select ${app.label}"
}
```

## Visual Design Patterns

### Card Layout
```
┌─────────────────────────────────────┐
│  📱  App Name                [Change] │
│      com.package.name               │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Date & Time                        │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐    │
│  │+5min│ │ 9:00│ │13:00│ │21:00│    │
│  └─────┘ └─────┘ └─────┘ └─────┘    │
│  ┌──────────────┐ ┌──────────────┐   │
│  │📅 Tue, Oct 1 │ │🕐 14:30      │   │
│  └──────────────┘ └──────────────┘   │
│  💡 Fires in 2h 15m on Tue, Oct 1   │
└─────────────────────────────────────┘
```

### Bottom Action Bar
```
┌─────────────────────────────────────┐
│                    [Cancel] [Save] │
└─────────────────────────────────────┘
```

## Animation & Feedback

- **Card elevation changes** on error states
- **Color transitions** for validation feedback  
- **Content size animations** for expanding/collapsing content
- **Haptic feedback** on save success/error
- **List item animations** in app picker

## Accessibility Features

- **48dp minimum touch targets**
- **Content descriptions** for all interactive elements
- **TalkBack support** with meaningful labels
- **Contrast compliant** error states
- **Semantic markup** for form elements

## Testing Strategy

### Unit Tests
- Time preset calculations
- Date/time formatting
- Validation logic
- Edge case handling

### UI Tests  
- Save button enabling/disabling
- Preset chip interactions
- Error message display
- App selection flow

## Responsive Design

- **WindowInsets** handling for system bars
- **Adaptive spacing** based on screen size  
- **Flexible layouts** that work on different form factors
- **Proper keyboard navigation** support

## Error Handling

### Inline Validation
- Past time detection with immediate feedback
- Conflict detection with existing schedules
- Permission validation with actionable guidance

### Error States
```kotlin
when {
    isPastTime -> "Time must be in the future."
    hasConflict -> "A schedule already exists at this time."
    !hasPermission -> "Exact alarm permission required."
}
```

## Performance Optimizations

- **LazyColumn** for app lists
- **remember** for expensive calculations
- **State hoisting** for efficient recomposition
- **Minimal recomposition** scope

## Code Quality

- **File-level OptIn** for experimental APIs
- **Type-safe** state management
- **Separation of concerns** between UI and business logic
- **Comprehensive documentation** and comments
- **Consistent naming** conventions

## Migration Notes

### Preserved Functionality
- All existing business logic maintained
- Database schema unchanged  
- Repository/DAO patterns intact
- Permission handling preserved

### Enhanced Features
- Better UX with quick presets
- Improved accessibility
- Modern Material 3 design
- Enhanced validation feedback

This redesign significantly improves the user experience while maintaining all existing functionality and adding modern Material 3 design patterns.