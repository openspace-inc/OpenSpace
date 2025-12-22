package com.example.iaso;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iaso.PersonalPage.PersonalPage;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.PersonalPage.DynamicHabit;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Workhorse Activity
 *
 * This is the main AI chat screen where users can talk to Claude.
 *
 * HOW IT WORKS:
 * 1. User types a message in the input field at the bottom
 * 2. User taps the send arrow or presses Enter/Send on keyboard
 * 3. We send the message to Claude via the Convex backend
 * 4. While waiting, cycling status messages are shown (e.g., "plotting timelines")
 * 5. Claude's response is displayed in the middle of the screen
 *
 * KEY COMPONENTS:
 * - userInput: EditText where user types their message
 * - enterArrow: ImageView button to send the message
 * - responseText: TextView that displays Claude's response
 * - loadingContainer: LinearLayout containing ion logo and status text
 * - loadingStatusText: TextView showing cycling loading messages
 * - convexApiHelper: Our helper class that handles the API calls
 */
public class workhorse extends AppCompatActivity {

    // ==================== UI ELEMENTS ====================

    /**
     * Input field where the user types their message
     */
    private EditText userInput;

    /**
     * Arrow button that sends the message when tapped
     */
    private ImageView enterArrow;

    /**
     * RecyclerView that displays the milestone list
     */
    private RecyclerView milestoneList;

    /**
     * Adapter for the milestone list
     */
    private MilestoneAdapter milestoneAdapter;

    /**
     * Container for the loading indicator (ion logo + status text)
     */
    private LinearLayout loadingContainer;

    /**
     * TextView showing cycling status messages during loading
     */
    private TextView loadingStatusText;

    /**
     * Container for timeline exceeded warning
     */
    private LinearLayout timelineWarningContainer;

    // ==================== ONBOARDING UI ELEMENTS ====================

    /**
     * Container for the time investment question
     */
    private LinearLayout questionTimeContainer;

    /**
     * Container for the completion date question
     */
    private LinearLayout questionDateContainer;

    /**
     * NumberPicker for selecting daily minutes
     */
    private NumberPicker minutesPicker;

    /**
     * NumberPicker for selecting day
     */
    private NumberPicker dayPicker;

    /**
     * NumberPicker for selecting month
     */
    private NumberPicker monthPicker;

    /**
     * NumberPicker for selecting year
     */
    private NumberPicker yearPicker;

    /**
     * Continue button for time question
     */
    private TextView continueTimeButton;

    /**
     * Continue button for date question
     */
    private TextView continueDateButton;

    /**
     * Bottom container with input box
     */
    private ConstraintLayout bottomContainer;

    /**
     * Continue button shown after milestones are displayed
     */
    private TextView continueMilestoneButton;

    /**
     * Container for the investment view
     */
    private LinearLayout investmentContainer;

    /**
     * EditText showing the investment amount (inline editable)
     */
    private EditText investmentAmount;

    /**
     * TextView showing the current worth value
     */
    private TextView currentWorthValue;

    /**
     * FrameLayout container for animated number
     */
    private FrameLayout investmentNumberContainer;

    /**
     * Finalize button for completing the project setup
     */
    private TextView finalizeButton;

    // ==================== USER DATA ====================

    /**
     * Daily minutes the user wants to invest (stored temporarily)
     */
    private int dailyMinutesInvestment = 0;

    /**
     * Ticker symbol for the user's project (generated by AI)
     */
    private String tickerSymbol = "";

    /**
     * User's original project description (what they typed in input_box)
     */
    private String projectDescription = "";

    /**
     * Target completion day
     */
    private int targetDay = 1;

    /**
     * Target completion month (1-12)
     */
    private int targetMonth = 1;

    /**
     * Target completion year
     */
    private int targetYear = 2025;

    /**
     * Current investment amount selected by user
     */
    private int currentInvestmentAmount = 0;

    /**
     * User's total points from SharedPreferences
     */
    private int userPoints = 0;

    // ==================== API HELPER ====================

    /**
     * Helper class that handles communication with Convex/Claude
     */
    private ConvexApiHelper convexApiHelper;

    // ==================== LOADING STATUS ANIMATION ====================

    /**
     * Handler for posting delayed tasks (used for cycling status text)
     */
    private Handler statusHandler;

    /**
     * Runnable that cycles through loading status messages
     */
    private Runnable statusCycleRunnable;

    /**
     * Flag to track if we're currently loading (to stop the cycling animation)
     */
    private boolean isLoading = false;

    /**
     * Random number generator for selecting status messages
     */
    private Random random = new Random();

    /**
     * Array of loading status messages to cycle through randomly
     * These give the user feedback that the AI is "thinking"
     */
    private static final String[] LOADING_MESSAGES = {
            "plotting timelines",
            "making decisions",
            "changing course",
            "common mistake",
            "analyzing years ahead",
            "recalculating",
            // Note: "analyzing X different paths" is handled separately with random number
            "creating benchmarks",
            "found misstep"
    };

    /**
     * Final messages shown before displaying the response
     */
    private static final String[] FINAL_MESSAGES = {
            "activating",
            "Pushing to @OpenSpace"
    };

    // ==================== LIFECYCLE METHODS ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workhorse);

        // ==================== FIND VIEWS ====================

        userInput = findViewById(R.id.user_input);
        enterArrow = findViewById(R.id.enter_arrow);
        milestoneList = findViewById(R.id.milestone_list);
        loadingContainer = findViewById(R.id.loading_container);
        loadingStatusText = findViewById(R.id.loading_status_text);
        bottomContainer = findViewById(R.id.bottom_container);
        timelineWarningContainer = findViewById(R.id.timeline_warning_container);
        ImageButton backButton = findViewById(R.id.back_button);

        // Setup RecyclerView for milestones
        milestoneAdapter = new MilestoneAdapter();
        milestoneList.setLayoutManager(new LinearLayoutManager(this));
        milestoneList.setAdapter(milestoneAdapter);

        // Set up click listener for editing milestones
        milestoneAdapter.setOnMilestoneClickListener((position, milestone) -> {
            showEditMilestoneDialog(position, milestone);
        });

        // Set up swipe-to-delete functionality
        setupSwipeToDelete();

        // Onboarding views
        questionTimeContainer = findViewById(R.id.question_time_container);
        questionDateContainer = findViewById(R.id.question_date_container);
        minutesPicker = findViewById(R.id.minutes_picker);
        dayPicker = findViewById(R.id.day_picker);
        monthPicker = findViewById(R.id.month_picker);
        yearPicker = findViewById(R.id.year_picker);
        continueTimeButton = findViewById(R.id.continue_time_button);
        continueDateButton = findViewById(R.id.continue_date_button);

        // Investment views
        continueMilestoneButton = findViewById(R.id.continue_milestone_button);
        investmentContainer = findViewById(R.id.investment_container);
        investmentAmount = findViewById(R.id.investment_amount);
        currentWorthValue = findViewById(R.id.current_worth_value);
        investmentNumberContainer = findViewById(R.id.investment_number_container);
        finalizeButton = findViewById(R.id.finalize_button);

        // Load user points from SharedPreferences
        loadUserPoints();

        // ==================== INITIALIZE ====================

        convexApiHelper = new ConvexApiHelper();
        statusHandler = new Handler(Looper.getMainLooper());

        // Setup NumberPickers for onboarding
        setupNumberPickers();

        // Create the runnable that cycles through status messages
        statusCycleRunnable = new Runnable() {
            @Override
            public void run() {
                if (isLoading && loadingStatusText != null) {
                    // Pick a random message to display
                    String message = getRandomLoadingMessage();
                    loadingStatusText.setText(message);

                    // Schedule the next message change (every 800-1500ms for variety)
                    int delay = 800 + random.nextInt(700);
                    statusHandler.postDelayed(this, delay);
                }
            }
        };

        // ==================== KEYBOARD HANDLING ====================

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Apply system bars to root padding
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom);

            // Apply keyboard (IME) insets to bottom_container's bottom margin
            // This moves input box up when keyboard appears
            if (bottomContainer != null) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) bottomContainer.getLayoutParams();
                params.bottomMargin = imeInsets.bottom;
                bottomContainer.setLayoutParams(params);
            }

            return insets;
        });

        // ==================== CLICK LISTENERS ====================

        // Back button - navigate to PersonalPage
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(workhorse.this, PersonalPage.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Onboarding continue buttons
        if (continueTimeButton != null) {
            continueTimeButton.setOnClickListener(v -> onContinueTimeClicked());
        }

        if (continueDateButton != null) {
            continueDateButton.setOnClickListener(v -> onContinueDateClicked());
        }

        if (continueMilestoneButton != null) {
            continueMilestoneButton.setOnClickListener(v -> onContinueMilestoneClicked());
        }

        if (investmentAmount != null) {
            setupInvestmentAmountWatcher();
        }

        if (finalizeButton != null) {
            finalizeButton.setOnClickListener(v -> onFinalizeClicked());
        }

        if (enterArrow != null) {
            enterArrow.setOnClickListener(v -> sendMessage());
        }

        if (userInput != null) {
            userInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
            userInput.setOnEditorActionListener((v, actionId, event) -> {
                boolean isSend = actionId == EditorInfo.IME_ACTION_SEND;
                boolean isEnter = event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN;

                if (isSend || isEnter) {
                    sendMessage();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the status cycling animation
        stopLoadingAnimation();

        // Clean up the API helper
        if (convexApiHelper != null) {
            convexApiHelper.shutdown();
        }
    }

    // ==================== MESSAGE HANDLING ====================

    /**
     * Sends the user's message to Claude via the Convex backend
     */
    private void sendMessage() {
        if (userInput == null) return;

        String text = userInput.getText() != null ? userInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        // Store the project description for later use in DynamicHabit creation
        projectDescription = text;

        // ==================== SHOW LOADING STATE ====================

        // Show the loading container with ion logo and cycling text
        startLoadingAnimation();

        // Clear previous milestones
        if (milestoneAdapter != null) {
            milestoneAdapter.setMilestones(new ArrayList<>());
        }

        // Disable input to prevent duplicate sends
        setInputEnabled(false);

        // Clear the input field
        userInput.getText().clear();

        // ==================== BUILD PROMPT AND SEND TO CLAUDE ====================

        // Build the formatted prompt with user's project details
        String prompt = buildMilestonePrompt(text);

        convexApiHelper.sendMessageToClaude(prompt, new ConvexApiHelper.ClaudeResponseCallback() {
            @Override
            public void onSuccess(String response) {
                // Show final messages before displaying response
                showFinalMessagesAndResponse(response);
            }

            @Override
            public void onError(String errorMessage) {
                // Stop loading animation
                stopLoadingAnimation();

                // Show error
                Toast.makeText(workhorse.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();

                // Re-enable input
                setInputEnabled(true);
            }
        });
    }

    // ==================== LOADING ANIMATION ====================

    /**
     * Starts the loading animation with cycling status messages
     */
    private void startLoadingAnimation() {
        isLoading = true;

        // Show the loading container
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }

        // Start cycling through messages
        statusHandler.post(statusCycleRunnable);
    }

    /**
     * Stops the loading animation
     */
    private void stopLoadingAnimation() {
        isLoading = false;

        // Remove any pending status updates
        statusHandler.removeCallbacks(statusCycleRunnable);

        // Hide the loading container
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Gets a random loading message to display
     * Sometimes generates "analyzing X different paths" with a random number
     *
     * @return A random loading status message
     */
    private String getRandomLoadingMessage() {
        // 20% chance to show the "analyzing paths" message with random number
        if (random.nextInt(5) == 0) {
            int paths = 1 + random.nextInt(25000); // Random number between 1-25000
            return "analyzing " + paths + " different paths";
        }

        // Otherwise pick from the standard messages
        return LOADING_MESSAGES[random.nextInt(LOADING_MESSAGES.length)];
    }

    /**
     * Shows the final messages ("activating", "Pushing to @OpenSpace")
     * before displaying Claude's response
     *
     * @param response The response from Claude to display after final messages
     */
    private void showFinalMessagesAndResponse(String response) {
        // Stop the random cycling
        isLoading = false;
        statusHandler.removeCallbacks(statusCycleRunnable);

        // Show "activating" first
        if (loadingStatusText != null) {
            loadingStatusText.setText(FINAL_MESSAGES[0]); // "activating"
        }

        // After 500ms, show "Pushing to @OpenSpace"
        statusHandler.postDelayed(() -> {
            if (loadingStatusText != null) {
                loadingStatusText.setText(FINAL_MESSAGES[1]); // "Pushing to @OpenSpace"
            }

            // After another 500ms, hide loading and show the response
            statusHandler.postDelayed(() -> {
                // Hide loading container
                if (loadingContainer != null) {
                    loadingContainer.setVisibility(View.GONE);
                }

                // Hide input box when milestones are shown
                if (bottomContainer != null) {
                    bottomContainer.setVisibility(View.GONE);
                }

                // Parse and display milestones
                List<Milestone> milestones = parseMilestones(response);
                if (milestoneAdapter != null) {
                    milestoneAdapter.setMilestones(milestones);
                }

                // Calculate total days from milestones and check against user's timeline
                int totalMilestoneDays = calculateTotalMilestoneDays(milestones);
                int userTargetDays = calculateDaysUntilCompletion();

                // Show warning if milestones exceed user's timeline
                if (timelineWarningContainer != null) {
                    if (totalMilestoneDays > userTargetDays) {
                        timelineWarningContainer.setVisibility(View.VISIBLE);
                    } else {
                        timelineWarningContainer.setVisibility(View.GONE);
                    }
                }

                // Scroll to top of list
                if (milestoneList != null) {
                    milestoneList.scrollToPosition(0);
                }

                // Show continue button after milestones are displayed
                if (continueMilestoneButton != null) {
                    continueMilestoneButton.setVisibility(View.VISIBLE);
                }

            }, 500);

        }, 500);
    }

    /**
     * Parses the AI response into a list of Milestone objects.
     * Expected format:
     * Line 1: Ticker symbol (no comma)
     * Line 2+: "milestone name, X days" per line
     *
     * @param response The raw response from Claude
     * @return List of Milestone objects
     */
    private List<Milestone> parseMilestones(String response) {
        List<Milestone> milestones = new ArrayList<>();

        if (response == null || response.isEmpty()) {
            return milestones;
        }

        // Split response by lines
        String[] lines = response.split("\n");

        boolean firstLineProcessed = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // First non-empty line is the ticker symbol (no comma)
            if (!firstLineProcessed) {
                // Check if this line contains a comma (milestone format)
                // If no comma, it's the ticker symbol
                if (!line.contains(",")) {
                    tickerSymbol = line;
                    firstLineProcessed = true;
                    continue;
                }
                firstLineProcessed = true;
            }

            // Find the last comma to split name and time
            int lastCommaIndex = line.lastIndexOf(",");
            if (lastCommaIndex > 0 && lastCommaIndex < line.length() - 1) {
                String name = line.substring(0, lastCommaIndex).trim();
                String time = line.substring(lastCommaIndex + 1).trim();

                if (!name.isEmpty() && !time.isEmpty()) {
                    milestones.add(new Milestone(name, time));
                }
            }
        }

        return milestones;
    }

    /**
     * Calculates the total number of days from all milestones
     * @param milestones List of milestones to sum
     * @return Total days across all milestones
     */
    private int calculateTotalMilestoneDays(List<Milestone> milestones) {
        int totalDays = 0;
        for (Milestone milestone : milestones) {
            totalDays += extractDaysFromTime(milestone.getTime());
        }
        return totalDays;
    }

    /**
     * Extracts the number of days from a time string like "55 days" or "30 days"
     * @param timeString The time string to parse
     * @return Number of days, or 0 if parsing fails
     */
    private int extractDaysFromTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        // Extract numbers from the string
        String numbersOnly = timeString.replaceAll("[^0-9]", "");
        if (numbersOnly.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(numbersOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Shows a bottom sheet dialog to edit a milestone's name and time
     * @param position The position of the milestone in the list
     * @param milestone The milestone to edit
     */
    private void showEditMilestoneDialog(int position, Milestone milestone) {
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_milestone, null);
        bottomSheetDialog.setContentView(dialogView);

        // Make background transparent to show rounded corners
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        // Get input fields
        EditText nameInput = dialogView.findViewById(R.id.edit_milestone_name);
        EditText timeInput = dialogView.findViewById(R.id.edit_milestone_time);
        ImageView saveButton = dialogView.findViewById(R.id.save_milestone_button);

        // Pre-fill current values
        nameInput.setText(milestone.getName());
        timeInput.setText(milestone.getTime());

        // Set up save button click listener
        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newTime = timeInput.getText().toString().trim();

            if (!newName.isEmpty() && !newTime.isEmpty()) {
                Milestone updatedMilestone = new Milestone(newName, newTime);
                milestoneAdapter.updateMilestone(position, updatedMilestone);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    /**
     * Sets up swipe-to-delete functionality for the milestone list
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                milestoneAdapter.removeMilestone(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(milestoneList);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Enables or disables the input controls
     */
    private void setInputEnabled(boolean enabled) {
        if (userInput != null) {
            userInput.setEnabled(enabled);
        }
        if (enterArrow != null) {
            enterArrow.setEnabled(enabled);
            enterArrow.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    /**
     * Converts dp to pixels
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // ==================== ONBOARDING METHODS ====================

    /**
     * Sets up the NumberPickers with appropriate min/max values
     */
    private void setupNumberPickers() {
        // Minutes picker: 1-180 minutes (3 hours max), default 30
        if (minutesPicker != null) {
            minutesPicker.setMinValue(1);
            minutesPicker.setMaxValue(180);
            minutesPicker.setValue(30);
            minutesPicker.setWrapSelectorWheel(true);
        }

        // Get current date for setting defaults
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Day picker: 1-31
        if (dayPicker != null) {
            dayPicker.setMinValue(1);
            dayPicker.setMaxValue(31);
            dayPicker.setValue(currentDay);
            dayPicker.setWrapSelectorWheel(true);
        }

        // Month picker: 1-12 with display values
        if (monthPicker != null) {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            monthPicker.setMinValue(1);
            monthPicker.setMaxValue(12);
            monthPicker.setDisplayedValues(months);
            monthPicker.setValue(currentMonth);
            monthPicker.setWrapSelectorWheel(true);
        }

        // Year picker: current year to current year + 10
        if (yearPicker != null) {
            yearPicker.setMinValue(currentYear);
            yearPicker.setMaxValue(currentYear + 10);
            yearPicker.setValue(currentYear);
            yearPicker.setWrapSelectorWheel(false);
        }
    }

    /**
     * Called when user presses continue on the time investment question
     */
    private void onContinueTimeClicked() {
        // Store the selected minutes
        if (minutesPicker != null) {
            dailyMinutesInvestment = minutesPicker.getValue();
        }

        // Hide time question, show date question
        if (questionTimeContainer != null) {
            questionTimeContainer.setVisibility(View.GONE);
        }
        if (questionDateContainer != null) {
            questionDateContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when user presses continue on the completion date question
     */
    private void onContinueDateClicked() {
        // Store the selected date
        if (dayPicker != null) {
            targetDay = dayPicker.getValue();
        }
        if (monthPicker != null) {
            targetMonth = monthPicker.getValue();
        }
        if (yearPicker != null) {
            targetYear = yearPicker.getValue();
        }

        // Hide date question, show main input interface
        if (questionDateContainer != null) {
            questionDateContainer.setVisibility(View.GONE);
        }
        if (milestoneList != null) {
            milestoneList.setVisibility(View.VISIBLE);
        }
        if (bottomContainer != null) {
            bottomContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the stored daily minutes investment
     * @return Minutes per day the user wants to invest
     */
    public int getDailyMinutesInvestment() {
        return dailyMinutesInvestment;
    }

    /**
     * Gets the stored target completion date as a formatted string
     * @return Target date in format "DD/MM/YYYY"
     */
    public String getTargetCompletionDate() {
        return String.format("%02d/%02d/%04d", targetDay, targetMonth, targetYear);
    }

    /**
     * Gets the ticker symbol for the current project
     * @return Ticker symbol generated by AI
     */
    public String getTickerSymbol() {
        return tickerSymbol;
    }

    /**
     * Calculates the number of days from today until the target completion date
     * @return Number of days until completion (minimum 1)
     */
    private int calculateDaysUntilCompletion() {
        // Get today's date
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Create target date calendar
        Calendar target = Calendar.getInstance();
        target.set(Calendar.YEAR, targetYear);
        target.set(Calendar.MONTH, targetMonth - 1); // Calendar months are 0-based
        target.set(Calendar.DAY_OF_MONTH, targetDay);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // Calculate difference in milliseconds and convert to days
        long diffMillis = target.getTimeInMillis() - today.getTimeInMillis();
        int days = (int) (diffMillis / (1000 * 60 * 60 * 24));

        // Return at least 1 day
        return Math.max(1, days);
    }

    /**
     * Builds the formatted prompt to send to Claude with user's project details
     * @param projectDescription The user's description of their project/goal
     * @return The formatted prompt string
     */
    private String buildMilestonePrompt(String projectDescription) {
        int daysUntilCompletion = calculateDaysUntilCompletion();

        return "Context: The user wants to invest a maximum of " + dailyMinutesInvestment +
                " mins everyday and wants to accomplish " + projectDescription +
                " in " + daysUntilCompletion + " days. Provide a list of milestones (2-15) " +
                "(1-2 goals for under 25 days, 2-5 for goals under 100 days, 6-8 for goals between 100-365, and 9-12 for goals that take years) to break down " +
                "this goal that the user must hit along with the amount of days each milestone will take.\n" +
                "Your response:\n" +
                "Only provide a list and no other text which follows the below example, additionally, using the projectDescripton create a ticker Symbol for it:\n" +
                "Tickersymbol\n" +
                "milestone name, X days\n" +
                "milestone name, X days";
    }

    // ==================== INVESTMENT METHODS ====================

    /**
     * Flag to prevent recursive text change events during animation
     */
    private boolean isAnimating = false;

    /**
     * Loads the user's points from SharedPreferences
     */
    private void loadUserPoints() {
        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userPoints = userData.getInt("userPoints", 0);
    }

    /**
     * Called when user presses continue on the milestone view
     */
    private void onContinueMilestoneClicked() {
        // Hide milestone list, warning container, and continue button
        if (milestoneList != null) {
            milestoneList.setVisibility(View.GONE);
        }
        if (timelineWarningContainer != null) {
            timelineWarningContainer.setVisibility(View.GONE);
        }
        if (continueMilestoneButton != null) {
            continueMilestoneButton.setVisibility(View.GONE);
        }

        // Show investment container
        if (investmentContainer != null) {
            investmentContainer.setVisibility(View.VISIBLE);
        }

        // Update current worth display
        if (currentWorthValue != null) {
            currentWorthValue.setText(String.valueOf(userPoints));
        }
    }

    /**
     * Sets up the TextWatcher for inline editing of investment amount
     */
    private void setupInvestmentAmountWatcher() {
        investmentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isAnimating) return;

                String text = s.toString();

                // If text is empty, reset to 0
                if (text.isEmpty()) {
                    isAnimating = true;
                    s.clear();
                    s.append("0");
                    investmentAmount.setSelection(1);
                    isAnimating = false;

                    int oldAmount = currentInvestmentAmount;
                    currentInvestmentAmount = 0;
                    if (oldAmount != 0) {
                        animateNumberChangeOnly(0, oldAmount);
                    }
                    updateFinalizeButtonState();
                    return;
                }

                // Remove leading zeros (except for just "0")
                if (text.length() > 1 && text.startsWith("0")) {
                    isAnimating = true;
                    String newText = text.replaceFirst("^0+", "");
                    if (newText.isEmpty()) newText = "0";
                    s.clear();
                    s.append(newText);
                    investmentAmount.setSelection(newText.length());
                    isAnimating = false;
                    // Don't return - continue processing with the cleaned text
                    text = newText;
                }

                int newAmount = 0;
                try {
                    newAmount = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    newAmount = 0;
                }

                // Update the value and animate if changed
                int oldAmount = currentInvestmentAmount;
                if (newAmount != oldAmount) {
                    currentInvestmentAmount = newAmount;
                    animateNumberChangeOnly(newAmount, oldAmount);
                }

                // Always update finalize button and color
                updateInvestmentColor();
                updateFinalizeButtonState();
            }
        });
    }

    /**
     * Animates the number change with a wheel effect (without updating currentInvestmentAmount)
     * @param newAmount The new amount
     * @param oldAmount The old amount
     */
    private void animateNumberChangeOnly(int newAmount, int oldAmount) {
        if (investmentAmount == null || investmentNumberContainer == null) return;

        // Determine animation direction
        float startY;
        if (newAmount > oldAmount) {
            // New number is larger - animate downward (old goes down, new comes from top)
            startY = -investmentAmount.getHeight();
        } else if (newAmount < oldAmount) {
            // New number is smaller - animate upward (old goes up, new comes from bottom)
            startY = investmentAmount.getHeight();
        } else {
            // Same number - no animation needed
            return;
        }

        // Animate old number out
        ObjectAnimator oldAnimator;
        if (newAmount > oldAmount) {
            oldAnimator = ObjectAnimator.ofFloat(investmentAmount, "translationY", 0, investmentAmount.getHeight());
        } else {
            oldAnimator = ObjectAnimator.ofFloat(investmentAmount, "translationY", 0, -investmentAmount.getHeight());
        }
        oldAnimator.setDuration(100);

        oldAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Position for animation in
                investmentAmount.setTranslationY(startY);

                // Animate new number in
                ObjectAnimator newAnimator = ObjectAnimator.ofFloat(investmentAmount, "translationY", startY, 0);
                newAnimator.setDuration(100);
                newAnimator.start();
            }
        });

        oldAnimator.start();
    }

    /**
     * Animates the investment amount change with a wheel effect
     * @param newAmount The new amount to display
     * @param displayText The text to display (preserves user's input format)
     */
    private void animateNumberChange(int newAmount, String displayText) {
        if (investmentAmount == null || investmentNumberContainer == null) return;

        int oldAmount = currentInvestmentAmount;
        currentInvestmentAmount = newAmount;

        animateNumberChangeOnly(newAmount, oldAmount);
        updateInvestmentColor();
    }

    /**
     * Updates the investment amount color based on validation against user points
     */
    private void updateInvestmentColor() {
        if (investmentAmount == null) return;

        // Check if amount exceeds user points and change color accordingly
        if (currentInvestmentAmount > userPoints) {
            // Red color for exceeding user points
            investmentAmount.setTextColor(0xFFBA524F);
        } else {
            // White color for valid amount
            investmentAmount.setTextColor(0xFFFFFFFF);
        }
    }

    /**
     * Gets the current investment amount from the EditText
     */
    private int getCurrentInvestmentFromField() {
        if (investmentAmount == null) return 0;
        String text = investmentAmount.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Updates the finalize button state based on whether the investment is valid
     */
    private void updateFinalizeButtonState() {
        if (finalizeButton == null) return;

        // Read directly from field to ensure accuracy
        int amount = getCurrentInvestmentFromField();
        boolean canFinalize = amount >= 1 && amount <= userPoints;

        finalizeButton.setEnabled(canFinalize);
        finalizeButton.setAlpha(canFinalize ? 1.0f : 0.5f);
    }

    /**
     * Called when user presses the finalize button
     */
    private void onFinalizeClicked() {
        // Read current value directly from field
        int amount = getCurrentInvestmentFromField();

        // Validate that the investment amount is valid
        if (amount < 1 || amount > userPoints) {
            Toast.makeText(this, "Please enter a valid investment amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update currentInvestmentAmount to match field value
        currentInvestmentAmount = amount;

        // Subtract investment from user points and save
        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int newPoints = userPoints - currentInvestmentAmount;
        userData.edit().putInt("userPoints", newPoints).apply();

        // Create and save the DynamicHabit
        createAndSaveDynamicHabit();

        // Navigate to PersonalPage with fade animation
        Intent intent = new Intent(workhorse.this, PersonalPage.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Creates a DynamicHabit and saves it to SharedPreferences
     */
    private void createAndSaveDynamicHabit() {
        SharedPreferences dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dynamicHabits.getString("personalHabitList", null);

        ArrayList<DynamicHabit> habitList;

        if (json == null) {
            habitList = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
            habitList = gson.fromJson(json, type);
            if (habitList == null) {
                habitList = new ArrayList<>();
            }
        }

        // Get a random unused image
        String imageName = HabitImageHelper.getRandomUnusedImage(habitList);

        // Create the new DynamicHabit
        // Using constructor: (name, streak, type, description, time, imageName, timeInvested, blocks)
        DynamicHabit newHabit = new DynamicHabit(
                tickerSymbol,           // name: AI-generated ticker symbol
                0,                      // streak: 0
                "Personal",             // type: Personal
                projectDescription,     // description: what user typed originally
                dailyMinutesInvestment, // time: daily minutes from the beginning
                imageName,              // imageName: randomly selected
                0,                      // timeInvested: 0
                currentInvestmentAmount // blocks: investment amount
        );

        habitList.add(newHabit);

        // Save back to SharedPreferences
        String updatedJson = gson.toJson(habitList);
        SharedPreferences.Editor editor = dynamicHabits.edit();
        editor.putString("personalHabitList", updatedJson);
        editor.apply();
    }
}
