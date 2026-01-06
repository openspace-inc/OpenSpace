# Focus Timer System Implementation

## Overview
A comprehensive time recording system has been implemented that allows users to start, pause, resume, and stop focus timers for their personal habits. The system persists across app restarts and correctly handles edge cases like pausing and closing the app.

## Features Implemented

### 1. **Timer Activation**
- **Long-press** any habit icon in the Row on MainActivity to start a focus timer
- Toast notification confirms timer has started
- Bottom navigation bar height increases dynamically to accommodate timer UI (base 90dp + timer addition 56dp = 146dp total)
- Height calculation is automatic and adapts to the timer container size
- Timer elements are positioned at the top of the CardView with proper padding to avoid the rounded corners

### 2. **Timer UI Components**
The timer UI displays above the navigation buttons with the following elements:

#### a. Habit Image (24dp x 24dp, left side)
- Shows the image of the habit being timed
- Circular crop applied for consistency
- **Tap to stop timer**

#### b. Progress Bar (center, fills remaining space)
- **Grey background rectangle**: Represents the full daily time commitment goal
- **Black foreground rectangle**: Grows proportionally as time passes
  - Width = (elapsed time / daily goal time) * 100%
  - Stops growing at 100% (when goal is reached)
  - Timer can continue past goal, but bar stays at 100%
- Rounded edges (4dp corner radius)
- 16dp padding between image and bar, and bar and timer

#### c. Live Time Counter (right side)
- Uses Robinhood TickerView for smooth animated number transitions
- Displays time in **MM:SS format** (e.g., "5:42", "23:08")
- For 1000+ minutes, displays only minutes (e.g., "1234")
- Updates every second while timer is running
- Accurately calculates seconds even after:
  - App is closed and reopened
  - Timer is paused and resumed
  - Multiple pause/resume cycles
- **Tap to pause/resume timer**
- **Note**: When timer is stopped, only minutes are saved (seconds are for display only)

### 3. **Timer States**

#### Active (Running)
- Timer is counting up
- UI updates every second
- Progress bar grows proportionally
- Minutes counter increments

#### Paused
- Timer stops counting
- UI stops updating
- Paused time is tracked separately
- Tap minutes counter to resume

#### Stopped
- Timer is cleared from memory
- UI animates away (fade out + height decrease)
- Elapsed time (in minutes) is saved to userStorage

### 4. **Data Persistence**

#### Timer State Storage (SharedPreferences: "FocusTimerState")
The timer state persists across app restarts with the following fields:
- `isActive`: Boolean indicating if timer is running
- `habitName`: Name of the habit being timed
- `habitImageName`: Image resource name for the habit
- `dailyGoalMinutes`: Daily time commitment goal
- `startTimeMillis`: Timestamp when timer started
- `isPaused`: Boolean indicating if timer is paused
- `pausedTimeMillis`: Timestamp when timer was paused
- `totalPausedDurationMillis`: Total time spent paused (accumulated)

#### Time Calculation
Elapsed time is calculated with millisecond precision:
```
elapsed_millis = (current_time - start_time) - total_paused_duration
elapsed_seconds = elapsed_millis / 1000
minutes = elapsed_seconds / 60
seconds = elapsed_seconds % 60

Display format:
  if minutes < 1000: "MM:SS"
  else: "MMMM" (minutes only)
```

This ensures:
- Timer continues correctly after app restart
- Paused time is excluded from elapsed time
- Multiple pause/resume cycles are handled correctly
- Seconds are accurately calculated and displayed
- When timer is paused, the pause duration is dynamically added to total paused time

#### Data Recording (SharedPreferences: "userStorage")
When timer is stopped, a `dataStorage` object is created and saved with:
- `name`: Habit name
- `type`: "Personal" (verified from habit data)
- `hours`: Elapsed **minutes only** (rounded down, seconds are not saved)
- `day`: Current day of year (Calendar.DAY_OF_YEAR)

**Note**: Seconds are displayed for user feedback but only minutes are recorded in the data storage, as per the original specification.

### 5. **Animations**

#### Showing Timer UI
1. Bottom navigation bar height animates dynamically from base height (90dp) to expanded height (146dp) over 300ms
2. Timer container fades in with alpha 0→1 (300ms, 150ms delay)
3. Height calculation breakdown:
   - Base height: 90dp (navigation buttons at bottom)
   - Container top padding: 12dp
   - Timer container internal padding: 16dp (8dp top + 8dp bottom)
   - Timer content height: ~24dp
   - Container bottom padding: 4dp
   - **Total: 90dp + 56dp = 146dp**

#### Hiding Timer UI
1. Timer container fades out with alpha 1→0 (300ms)
2. Bottom navigation bar height animates back to base height (90dp) over 300ms with 150ms delay

#### Layout Structure
- Timer container is constrained to the **top** of the CardView (not to navigation buttons)
- Navigation buttons are constrained to the **bottom** of the CardView
- This prevents timer elements from being hidden by the rounded corners
- Padding ensures proper spacing within the curved CardView shape

#### Progress Bar Growth
- Smooth width constraint percentage update every second
- No animation on individual updates for real-time feel

#### Minutes Counter
- Robinhood TickerView provides smooth vertical scroll animation
- 800ms animation duration for number changes

## Architecture

### FocusTimerManager.java
**Location**: `/app/src/main/java/com/example/iaso/FocusTimer/FocusTimerManager.java`

**Responsibilities**:
- Timer state management
- SharedPreferences persistence
- Time calculations (handling pause states)
- Thread-safe state queries

**Key Methods**:
- `startTimer(habitName, habitImageName, dailyGoalMinutes)`: Starts new timer
- `pauseTimer()`: Pauses active timer
- `resumeTimer()`: Resumes paused timer
- `stopTimer()`: Stops timer and returns elapsed minutes
- `getElapsedMinutes()`: Returns current elapsed time in minutes (excluding paused time)
- `getElapsedSeconds()`: Returns current elapsed time in seconds (excluding paused time)
- `getElapsedMillis()`: Returns current elapsed time in milliseconds for precise calculation
- `getFormattedElapsedTime()`: Returns formatted time string ("MM:SS" or minutes only for 1000+)
- `getProgressRatio()`: Returns progress (0.0 to 1.0) toward daily goal
- `getState()`: Returns immutable FocusTimerState object for UI

### BottomNavigationHelper.java (Enhanced)
**Location**: `/app/src/main/java/com/example/iaso/BottomNavigationHelper.java`

**New Responsibilities**:
- Timer UI initialization and updates
- Animation management
- Click listener setup (tap image to stop, tap minutes to pause/resume)
- Periodic UI updates (1 second interval)
- Data persistence integration

**Key Methods**:
- `setupBottomNavigation()`: Initializes nav and timer UI
- `startFocusTimer()`: Public method to start timer from activities
- `showTimerUI() / hideTimerUI()`: Handles UI animations
- `startTimerUpdates() / stopTimerUpdates()`: Manages update loop
- `updateTimerUI()`: Updates progress bar and minutes counter
- `stopFocusTimer()`: Stops timer and saves data
- `saveTimerData()`: Persists elapsed time to userStorage
- `cleanup()`: Stops updates and cleans up resources

### MainActivity.java (Enhanced)
**Location**: `/app/src/main/java/com/example/iaso/Home/MainActivity.java`

**Changes**:
- Added `setOnLongClickListener` to habit buttons in `populateProjectRow()`
- Calls `BottomNavigationHelper.startFocusTimer()` on long-press
- Added `onDestroy()` to call `BottomNavigationHelper.cleanup()`

### PersonalPage.java (Enhanced)
**Location**: `/app/src/main/java/com/example/iaso/PersonalPage/PersonalPage.java`

**Changes**:
- Added `onDestroy()` to call `BottomNavigationHelper.cleanup()`
- Ensures timer updates stop when leaving the activity

### bottom_navigation_bar.xml (Enhanced)
**Location**: `/app/src/main/res/layout/bottom_navigation_bar.xml`

**New Elements**:
- `focusTimerContainer`: ConstraintLayout for timer UI (initially GONE)
- `focusTimerHabitImage`: ImageView (24dp x 24dp) for habit image
- `progressBarContainer`: Container for progress bars
  - `progressBarBackground`: Grey background bar
  - `progressBarForeground`: Black foreground bar (width controlled by constraint percentage)
- `focusTimerMinutesText`: TickerView for animated minutes counter

### Drawable Resources
**Location**: `/app/src/main/res/drawable/`

- `rounded_progress_background.xml`: Grey (#E0E0E0) rounded rectangle (4dp radius)
- `rounded_progress_foreground.xml`: Black (#000000) rounded rectangle (4dp radius)

## Edge Case Handling

### ✅ App Closed and Reopened
- Timer state is read from SharedPreferences on app start
- If active timer exists, UI is shown immediately
- Elapsed time is calculated correctly from start time and accumulated paused duration
- Timer updates resume automatically

### ✅ Paused and App Closed
- Pause timestamp is saved
- On reopen, paused state is restored
- Current pause duration is included in elapsed time calculation
- UI shows correct elapsed time without updates (paused)
- Tap minutes to resume - works correctly

### ✅ Multiple Pause/Resume Cycles
- Each pause cycle's duration is accumulated in `totalPausedDurationMillis`
- Elapsed time calculation subtracts total paused time
- No time is lost or duplicated across cycles

### ✅ Timer Continues Past Daily Goal
- Progress bar stops growing at 100%
- Minutes counter continues incrementing
- Full elapsed time is saved when stopped

### ✅ No Active Timer
- Timer UI remains hidden
- Bottom nav height stays at 90dp
- No background updates running

## Usage Flow

1. **Start Timer**: Long-press habit icon in MainActivity Row
2. **Monitor Progress**: Watch minutes counter and progress bar
3. **Pause Timer** (optional): Tap minutes counter
4. **Resume Timer** (optional): Tap minutes counter again
5. **Stop Timer**: Tap habit image
6. **Data Saved**: Elapsed minutes automatically saved to userStorage with habit name, type "Personal", and current date

## Code Quality

### ✅ Commenting
- Comprehensive JavaDoc comments on all classes and methods
- Inline comments explaining complex logic
- Clear section headers in code

### ✅ Readability
- Descriptive variable and method names
- Logical code organization
- Consistent formatting

### ✅ Stability
- Null checks before accessing views
- Safe SharedPreferences operations
- Proper resource cleanup in onDestroy
- Handler callback removal to prevent memory leaks
- Thread-safe timer calculations

## Testing Checklist

- [ ] Long-press habit in MainActivity starts timer
- [ ] Timer UI appears with animation
- [ ] Habit image loads correctly
- [ ] Progress bar is visible (grey background)
- [ ] Time counter displays "0:00" initially
- [ ] Seconds increment correctly (0:01, 0:02, etc.)
- [ ] Minutes increment correctly (0:59 → 1:00)
- [ ] Format shows MM:SS up to 999:59
- [ ] Format switches to minutes only at 1000 (displays "1000", "1001", etc.)
- [ ] Tap time counter to pause - display freezes with accurate time
- [ ] Tap time counter again to resume - continues from exact paused time
- [ ] Progress bar grows proportionally based on minutes
- [ ] Progress bar stops at 100% when goal is reached
- [ ] Timer continues past goal (counter still updates)
- [ ] Tap habit image to stop timer
- [ ] Timer UI disappears with animation
- [ ] Data is saved to userStorage (minutes only, no seconds)
- [ ] Toast shows confirmation with minutes
- [ ] Close app with active timer - reopens with correct MM:SS time
- [ ] Close app with paused timer - reopens in paused state with correct time
- [ ] Seconds are accurate after app restart (not reset to :00)
- [ ] Multiple pause/resume cycles maintain accurate seconds
- [ ] Navigation between MainActivity and PersonalPage preserves timer with seconds
- [ ] Timer updates stop after navigating away

## Future Enhancements (Not Implemented)

- Notification showing timer status when app is in background
- Timer history/analytics view
- Multiple concurrent timers
- Custom timer sounds/alerts at goal completion
- Widget support for home screen timer display

