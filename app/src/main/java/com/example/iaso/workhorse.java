package com.example.iaso;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
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
     * TextView that displays Claude's response
     */
    private TextView responseText;

    /**
     * Container for the loading indicator (ion logo + status text)
     */
    private LinearLayout loadingContainer;

    /**
     * TextView showing cycling status messages during loading
     */
    private TextView loadingStatusText;

    /**
     * ScrollView that contains the response text
     */
    private ScrollView responseScroll;

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

    // ==================== USER DATA ====================

    /**
     * Daily minutes the user wants to invest (stored temporarily)
     */
    private int dailyMinutesInvestment = 0;

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
        responseText = findViewById(R.id.response_text);
        loadingContainer = findViewById(R.id.loading_container);
        loadingStatusText = findViewById(R.id.loading_status_text);
        responseScroll = findViewById(R.id.response_scroll);
        bottomContainer = findViewById(R.id.bottom_container);

        // Onboarding views
        questionTimeContainer = findViewById(R.id.question_time_container);
        questionDateContainer = findViewById(R.id.question_date_container);
        minutesPicker = findViewById(R.id.minutes_picker);
        dayPicker = findViewById(R.id.day_picker);
        monthPicker = findViewById(R.id.month_picker);
        yearPicker = findViewById(R.id.year_picker);
        continueTimeButton = findViewById(R.id.continue_time_button);
        continueDateButton = findViewById(R.id.continue_date_button);

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
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, 0);

            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int basePadding = dpToPx(16);
            int bottom = Math.max(sysBars.bottom, imeInsets.bottom) + basePadding;

            bottomContainer.setPadding(
                    bottomContainer.getPaddingLeft(),
                    bottomContainer.getPaddingTop(),
                    bottomContainer.getPaddingRight(),
                    bottom
            );

            return insets;
        });

        // ==================== CLICK LISTENERS ====================

        // Onboarding continue buttons
        if (continueTimeButton != null) {
            continueTimeButton.setOnClickListener(v -> onContinueTimeClicked());
        }

        if (continueDateButton != null) {
            continueDateButton.setOnClickListener(v -> onContinueDateClicked());
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

        // ==================== SHOW LOADING STATE ====================

        // Show the loading container with ion logo and cycling text
        startLoadingAnimation();

        // Clear previous response
        if (responseText != null) {
            responseText.setText("");
        }

        // Disable input to prevent duplicate sends
        setInputEnabled(false);

        // Clear the input field
        userInput.getText().clear();

        // ==================== SEND TO CLAUDE ====================

        convexApiHelper.sendMessageToClaude(text, new ConvexApiHelper.ClaudeResponseCallback() {
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
                Toast.makeText(workhorse.this, errorMessage, Toast.LENGTH_LONG).show();

                if (responseText != null) {
                    responseText.setText("Error: " + errorMessage);
                }

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

                // Display Claude's response
                if (responseText != null) {
                    responseText.setText(response);
                }

                // Scroll to top of response
                if (responseScroll != null) {
                    responseScroll.scrollTo(0, 0);
                }

                // Re-enable input
                setInputEnabled(true);

            }, 500);

        }, 500);
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
        if (responseScroll != null) {
            responseScroll.setVisibility(View.VISIBLE);
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
}
