package com.example.iaso;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class workhorse extends AppCompatActivity {

    private EditText userInput;
    private ImageView enterArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workhorse);

        // Views
        userInput = findViewById(R.id.user_input);
        enterArrow = findViewById(R.id.enter_arrow);
        ConstraintLayout bottomContainer = findViewById(R.id.bottom_container);

        // Keyboard-safe behavior (edge-to-edge):
        // 1) Apply left/top/right system bar insets to the root.
        // 2) Apply bottom padding to the bottom container based on IME (keyboard) height.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, 0);

            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int basePadding = dpToPx(16);
            int bottom = Math.max(sysBars.bottom, imeInsets.bottom) + basePadding;

            // Keep existing left/top/right padding from XML; only control the bottom.
            bottomContainer.setPadding(
                    bottomContainer.getPaddingLeft(),
                    bottomContainer.getPaddingTop(),
                    bottomContainer.getPaddingRight(),
                    bottom
            );

            return insets;
        });

        // Tap arrow to submit
        if (enterArrow != null) {
            enterArrow.setOnClickListener(v -> sendMessage());
        }

        // Press keyboard "Send" to submit
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

    private void sendMessage() {
        if (userInput == null) return;

        String text = userInput.getText() != null ? userInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        // TODO: Submit 'text' to your backend / Convex / Claude and render the response.

        userInput.getText().clear();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}