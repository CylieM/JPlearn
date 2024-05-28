package com.example.jlearnn;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.Random;
import android.text.TextWatcher;
import android.text.Editable;

public class PracticeNihongoRaceActivity extends AppCompatActivity {

    private TextView timerDisplay;
    private TextView wpmDisplay;
    private TextView textDisplay;
    private EditText textInput;
    private Button resetButton;
    private ImageView playerIcon;
    private ParagraphSQLiteDB dbHelper;
    private CountDownTimer timer;
    private long startTime = 30 * 1000; // 30 seconds
    private long interval = 1000; // 1 second
    private long startTimeMillis;
    private String currentParagraph;
    private String currentRomaji;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_nihongorace);

        // Initialize views
        timerDisplay = findViewById(R.id.timerDisplay);
        wpmDisplay = findViewById(R.id.wpmDisplay);
        textDisplay = findViewById(R.id.textDisplay);
        textInput = findViewById(R.id.textInput);
        resetButton = findViewById(R.id.resetButton);
        playerIcon = findViewById(R.id.playerIcon);

        // Initialize database helper
        dbHelper = new ParagraphSQLiteDB(this);

        // Set up reset button click listener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        // Start game
        startGame();
    }

    private void startGame() {
        // Fetch a random paragraph from the database
        String[] paragraph = getRandomParagraph();
        currentParagraph = paragraph[0].replace(" ", ""); // remove spaces for display
        currentRomaji = paragraph[1].trim();

        // Display the Japanese text to type
        textDisplay.setText(currentParagraph);

        // Record the start time in milliseconds
        startTimeMillis = System.currentTimeMillis();

        // Start the timer
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(startTime, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerDisplay.setText("Time remaining: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();

        // Check the user's input against the correct Romaji translation
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] romajiCharacters = currentRomaji.split(" ");
                if (currentIndex < romajiCharacters.length && s.toString().equals(romajiCharacters[currentIndex])) {
                    // User got it correct, move on to the next character
                    currentIndex++;
                    textInput.setText("");
                    updateWPM();

                    // Highlight the correctly typed character in green
                    Spannable spannable = new SpannableString(textDisplay.getText());
                    spannable.setSpan(new ForegroundColorSpan(Color.GREEN), currentIndex - 1, currentIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textDisplay.setText(spannable);

                    // Move the player icon to the right
                    float progress = (float) currentIndex / romajiCharacters.length;
                    movePlayerIcon(progress);

                    // Check if the user has finished typing the entire text
                    if (currentIndex == romajiCharacters.length) {
                        endGame();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void endGame() {
        // Handle the end of the game when the timer runs out or the user completes the sentence
        if (timer != null) {
            timer.cancel();
        }
        String message;
        if (currentIndex == currentRomaji.split(" ").length) {
            // User completed the sentence
            message = "Finished! Your final WPM is: " + calculateWPM();
        } else {
            // Timer ran out
            message = "Time's up! Your final WPM is: " + calculateWPM();
        }

        // Show a pop-up that says "Finished!" or "Time's up!" and the user's final WPM
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> resetGame())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void resetGame() {
        // Reset all elements back to their initial state
        if (timer != null) {
            timer.cancel();
        }
        currentIndex = 0;
        textInput.setText("");
        textInput.setEnabled(true);
        timerDisplay.setText("Time remaining: 30");
        wpmDisplay.setText("WPM: 0");
        playerIcon.clearAnimation();
        movePlayerIcon(0); // Reset player icon to the start position

        // Fetch and display a new random paragraph
        String[] paragraph = getRandomParagraph();
        currentParagraph = paragraph[0].replace(" ", ""); // remove spaces for display
        currentRomaji = paragraph[1].trim();
        textDisplay.setText(currentParagraph);

        // Start the timer
        startGame();
    }

    private String[] getRandomParagraph() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ParagraphSQLiteDB.TABLE_PARAGRAPHS, null);
        int count = cursor.getCount();
        if (count == 0) {
            return new String[] {"", ""}; // Handle case with no paragraphs in the database
        }
        int randomIndex = new Random().nextInt(count);
        cursor.moveToPosition(randomIndex);
        String paragraph = cursor.getString(cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_PARAGRAPH));
        String romaji = cursor.getString(cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_ROMAJI));
        cursor.close();
        return new String[] {paragraph, romaji};
    }

    private void updateWPM() {
        // Calculate the elapsed time in minutes
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        double minutes = elapsedMillis / (1000.0 * 60.0);
        int wpm = (int) (minutes > 0 ? currentIndex / minutes : 0);
        wpmDisplay.setText("WPM: " + wpm);
    }

    private int calculateWPM() {
        // Calculate the WPM based on the number of characters typed and the time taken
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        double minutes = elapsedMillis / (1000.0 * 60.0);
        return (int) (minutes > 0 ? currentIndex / minutes : 0);
    }

    private void movePlayerIcon(float progress) {
        // Get the width of the racetrack
        int trackWidth = ((ViewGroup) playerIcon.getParent()).getWidth();
        // Calculate the new position of the player icon
        int newPosition = Math.round((trackWidth - playerIcon.getWidth() - wpmDisplay.getWidth()) * progress);

        // Update the position of the player icon
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerIcon.getLayoutParams();
        params.leftMargin = newPosition;
        playerIcon.setLayoutParams(params);
    }
}



