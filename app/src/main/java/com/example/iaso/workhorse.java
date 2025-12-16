package com.example.iaso;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Workhorse Activity
 *
 * This is the main AI chat screen where users can talk to Claude.
 *
 * HOW IT WORKS:
 * 1. User types a message in the input field at the bottom
 * 2. User taps the send arrow or presses Enter/Send on keyboard
 * 3. We send the message to Claude via the Convex backend
 * 4. Claude's response is displayed in the middle of the screen
 *
 * KEY COMPONENTS:
 * - userInput: EditText where user types their message
 * - enterArrow: ImageView button to send the message
 * - responseText: TextView that displays Claude's response
 * - loadingIndicator: TextView that shows "Thinking..." while waiting
 * - convexApiHelper: Our helper class that handles the API calls
 */
public class workhorse extends AppCompatActivity {

    // ==================== UI ELEMENTS ====================
    // These are references to the views defined in activity_workhorse.xml

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
     * Located in the middle of the screen
     */
    private TextView responseText;

    /**
     * Loading indicator shown while waiting for Claude
     * Displays "Thinking..." during API calls
     */
    private TextView loadingIndicator;

    /**
     * ScrollView that contains the response text
     * Allows scrolling for long responses
     */
    private ScrollView responseScroll;

    // ==================== API HELPER ====================

    /**
     * Helper class that handles communication with Convex/Claude
     * This does all the heavy lifting of making network requests
     */
    private ConvexApiHelper convexApiHelper;

    // ==================== LIFECYCLE METHODS ====================

    /**
     * Called when the activity is first created
     *
     * This is where we:
     * 1. Set up the layout
     * 2. Find all our views
     * 3. Configure keyboard behavior
     * 4. Set up click listeners
     * 5. Initialize the API helper
     *
     * @param savedInstanceState Previous state (if activity was recreated)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display (content extends behind system bars)
        EdgeToEdge.enable(this);

        // Set the layout for this activity
        setContentView(R.layout.activity_workhorse);

        // ==================== FIND VIEWS ====================
        // Connect our Java variables to the XML views

        userInput = findViewById(R.id.user_input);
        enterArrow = findViewById(R.id.enter_arrow);
        responseText = findViewById(R.id.response_text);
        loadingIndicator = findViewById(R.id.loading_indicator);
        responseScroll = findViewById(R.id.response_scroll);
        ConstraintLayout bottomContainer = findViewById(R.id.bottom_container);

        // ==================== INITIALIZE API HELPER ====================
        // Create our helper that will talk to Convex/Claude

        convexApiHelper = new ConvexApiHelper();

        // ==================== KEYBOARD HANDLING ====================
        // Handle edge-to-edge layout with keyboard appearing/disappearing

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            // Get the system bar insets (status bar, navigation bar)
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, 0);

            // Get the keyboard (IME) insets
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int basePadding = dpToPx(16);

            // Adjust bottom padding based on keyboard visibility
            // This pushes the input field up when the keyboard appears
            int bottom = Math.max(sysBars.bottom, imeInsets.bottom) + basePadding;

            // Apply the padding to the bottom container
            bottomContainer.setPadding(
                    bottomContainer.getPaddingLeft(),
                    bottomContainer.getPaddingTop(),
                    bottomContainer.getPaddingRight(),
                    bottom
            );

            return insets;
        });

        // ==================== SEND BUTTON CLICK LISTENER ====================
        // Send message when user taps the arrow button

        if (enterArrow != null) {
            enterArrow.setOnClickListener(v -> sendMessage());
        }

        // ==================== KEYBOARD SEND ACTION ====================
        // Send message when user presses "Send" on their keyboard

        if (userInput != null) {
            // Configure the keyboard to show a "Send" button instead of Enter
            userInput.setImeOptions(EditorInfo.IME_ACTION_SEND);

            // Listen for keyboard actions
            userInput.setOnEditorActionListener((v, actionId, event) -> {
                // Check if user pressed "Send" button
                boolean isSend = actionId == EditorInfo.IME_ACTION_SEND;

                // Also check for physical Enter key press
                boolean isEnter = event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN;

                // If either send action occurred, send the message
                if (isSend || isEnter) {
                    sendMessage();
                    return true; // We handled this event
                }
                return false; // Let the system handle other events
            });
        }
    }

    /**
     * Called when the activity is being destroyed
     *
     * IMPORTANT: We must clean up the API helper to prevent memory leaks
     * and properly shut down the background thread.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up the API helper's resources
        if (convexApiHelper != null) {
            convexApiHelper.shutdown();
        }
    }

    // ==================== MESSAGE HANDLING ====================

    /**
     * Sends the user's message to Claude via the Convex backend
     *
     * This method:
     * 1. Gets the text from the input field
     * 2. Validates that it's not empty
     * 3. Shows a loading indicator
     * 4. Disables input while waiting
     * 5. Calls the API
     * 6. Displays the response (or error)
     * 7. Re-enables input
     */
    private void sendMessage() {
        // Safety check: make sure input field exists
        if (userInput == null) return;

        // Get the text the user typed, trimmed of whitespace
        String text = userInput.getText() != null ? userInput.getText().toString().trim() : "";

        // Don't send empty messages
        if (text.isEmpty()) return;

        // ==================== SHOW LOADING STATE ====================
        // Let the user know we're processing their request

        // Show the "Thinking..." indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // Hide the previous response while loading
        if (responseText != null) {
            responseText.setText("");
        }

        // Disable the input and send button to prevent duplicate sends
        setInputEnabled(false);

        // Clear the input field
        userInput.getText().clear();

        // ==================== SEND TO CLAUDE ====================
        // Use our API helper to send the message and handle the response

        convexApiHelper.sendMessageToClaude(text, new ConvexApiHelper.ClaudeResponseCallback() {
            /**
             * Called when we successfully get a response from Claude
             *
             * @param response The text response from Claude
             */
            @Override
            public void onSuccess(String response) {
                // Hide the loading indicator
                if (loadingIndicator != null) {
                    loadingIndicator.setVisibility(View.GONE);
                }

                // Display Claude's response
                if (responseText != null) {
                    responseText.setText(response);
                }

                // Scroll to the top of the response
                if (responseScroll != null) {
                    responseScroll.scrollTo(0, 0);
                }

                // Re-enable input so user can send another message
                setInputEnabled(true);
            }

            /**
             * Called when something goes wrong
             *
             * @param errorMessage Description of what went wrong
             */
            @Override
            public void onError(String errorMessage) {
                // Hide the loading indicator
                if (loadingIndicator != null) {
                    loadingIndicator.setVisibility(View.GONE);
                }

                // Show error message to the user
                // Toast is a small popup message at the bottom of the screen
                Toast.makeText(workhorse.this, errorMessage, Toast.LENGTH_LONG).show();

                // Also display error in the response area for clarity
                if (responseText != null) {
                    responseText.setText("Error: " + errorMessage);
                }

                // Re-enable input so user can try again
                setInputEnabled(true);
            }
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Enables or disables the input controls
     *
     * We disable input while waiting for a response to prevent
     * the user from sending multiple messages at once.
     *
     * @param enabled true to enable input, false to disable
     */
    private void setInputEnabled(boolean enabled) {
        if (userInput != null) {
            userInput.setEnabled(enabled);
        }
        if (enterArrow != null) {
            enterArrow.setEnabled(enabled);
            // Optionally dim the arrow when disabled
            enterArrow.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    /**
     * Converts density-independent pixels (dp) to actual pixels
     *
     * Android uses dp for consistent sizing across different screen densities.
     * This method converts dp values to pixel values for the current device.
     *
     * @param dp The value in density-independent pixels
     * @return The equivalent value in actual pixels
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
