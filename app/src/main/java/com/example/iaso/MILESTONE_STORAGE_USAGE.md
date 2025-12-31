# Milestone Storage Usage Guide

## Overview
The milestone storage system provides a clean way to store and retrieve milestone data associated with habits throughout the app.

## Classes

### 1. `Milestone.java`
- Represents a single milestone
- **Fields:**
  - `name`: The milestone description (e.g., "Complete basic tutorial")
  - `time`: The time string (e.g., "30 days")
  - `days`: Parsed integer value of days (automatically extracted from time string)

### 2. `HabitMilestones.java`
- Wrapper class that associates a habit name with its milestones
- **Fields:**
  - `habitName`: The habit/project name (ticker symbol)
  - `milestones`: List of Milestone objects
- **Methods:**
  - `getTotalDays()`: Returns sum of all milestone days
  - `getMilestoneCount()`: Returns number of milestones

### 3. `MilestoneStorage.java`
- Helper class for storing/retrieving from SharedPreferences
- **Storage Location:** SharedPreferences file named "HabitMilestones"
- **Storage Key:** "habitMilestonesList"

## Usage Examples

### Saving Milestones (Already implemented in workhorse.java)
```java
// After parsing milestones from AI response
MilestoneStorage storage = new MilestoneStorage(context);
storage.saveMilestones(tickerSymbol, milestones);
```

### Retrieving Milestones for a Habit
```java
MilestoneStorage storage = new MilestoneStorage(context);
List<Milestone> milestones = storage.getMilestonesForHabit("WEBAPP");

// Use the milestones
for (Milestone milestone : milestones) {
    String name = milestone.getName();      // e.g., "Set up development environment"
    String timeStr = milestone.getTime();   // e.g., "10 days"
    int days = milestone.getDays();         // e.g., 10
}
```

### Getting All Habits with Milestones
```java
MilestoneStorage storage = new MilestoneStorage(context);
List<HabitMilestones> allHabits = storage.getAllHabitMilestones();

for (HabitMilestones habit : allHabits) {
    String habitName = habit.getHabitName();
    int totalDays = habit.getTotalDays();
    int milestoneCount = habit.getMilestoneCount();
    List<Milestone> milestones = habit.getMilestones();
}
```

### Checking if Milestones Exist
```java
MilestoneStorage storage = new MilestoneStorage(context);
if (storage.hasMilestonesForHabit("WEBAPP")) {
    // Load and display milestones
}
```

### Deleting Milestones
```java
MilestoneStorage storage = new MilestoneStorage(context);
boolean deleted = storage.deleteMilestonesForHabit("WEBAPP");
```

## Data Structure in SharedPreferences

The data is stored as a JSON array of `HabitMilestones` objects:

```json
[
  {
    "habitName": "WEBAPP",
    "milestones": [
      {
        "name": "Set up development environment",
        "time": "10 days",
        "days": 10
      },
      {
        "name": "Build authentication system",
        "time": "25 days",
        "days": 25
      }
    ]
  },
  {
    "habitName": "GUITAR",
    "milestones": [
      {
        "name": "Learn basic chords",
        "time": "15 days",
        "days": 15
      }
    ]
  }
]
```

## Integration Points

### Current Usage:
- **workhorse.java**: Saves milestones after AI generates them (line 594-600)

### Potential Future Usage:
- **PersonalPage.java**: Display milestone progress for each habit
- **Progress tracking**: Mark milestones as complete
- **Timeline visualization**: Show milestone timeline on a graph
- **Notifications**: Remind users about upcoming milestones
- **Analytics**: Track which milestones take longer than estimated

## Notes
- Milestones are automatically linked to habits via the ticker symbol
- The `days` field is automatically calculated when creating/updating milestones
- Storage uses Gson for JSON serialization
- All operations are synchronous (use background thread for large operations)
