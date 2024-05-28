package com.example.jlearnn;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TestYourself extends AppCompatActivity {

    private static final String TAG = "TestYourself";

    private List<String> words = new ArrayList<>();
    private List<String> answers = new ArrayList<>();
    private List<String> incorrectWords = new ArrayList<>();
    private List<String> incorrectAnswers = new ArrayList<>();
    private int currentWordIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private int roundCount = 1;
    private boolean answerSubmitted = false;

    private TextView tvCorrect;
    private TextView tvIncorrect;
    private TextView tvRound;
    private ImageButton btnPrevTest;
    private ImageButton btnNextTest;
    private EditText txtUserInputTest;
    private TextView tvFrontTest;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_yourself);

        // Initialize views
        tvCorrect = findViewById(R.id.tvCorrect);
        tvRound = findViewById(R.id.tvRound);
        tvIncorrect = findViewById(R.id.tvIncorrect);
        btnPrevTest = findViewById(R.id.btnPrevTest);
        btnNextTest = findViewById(R.id.btnNextTest);
        txtUserInputTest = findViewById(R.id.txtUserInputTest);
        tvFrontTest = findViewById(R.id.tvFrontTest);

        // Disable prev button initially
        btnPrevTest.setEnabled(false);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference();

        // Load data from Firebase Realtime Database
        loadDataFromFirebase();

        // Set click listener for Next button
        btnNextTest.setOnClickListener(v -> {
            txtUserInputTest.setText("");
            txtUserInputTest.setBackgroundColor(ContextCompat.getColor(TestYourself.this, android.R.color.white));
            answerSubmitted = false;

            // Check if the current word is the last one
            if (currentWordIndex < words.size() - 1) {
                currentWordIndex++;
                displayWord();
            } else {
                Toast.makeText(TestYourself.this, "End of Test", Toast.LENGTH_SHORT).show();
                // Show incorrect answers if any
                if (!incorrectWords.isEmpty()) {
                    words = new ArrayList<>(incorrectWords);
                    answers = new ArrayList<>(incorrectAnswers);
                    incorrectWords.clear();
                    incorrectAnswers.clear();
                    currentWordIndex = 0;
                    displayWord();
                }
            }

            if (roundCount == 5) {
                roundCount = 1;
                currentWordIndex = 0;
                resetCounters();
            } else {
                roundCount++;
            }
            tvRound.setText("Round " + roundCount + " / 5");
        });

        // Disable prev button click listener
        btnPrevTest.setOnClickListener(null);

        // Set Enter key listener for txtUserInputTest
        txtUserInputTest.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) && !answerSubmitted) {
                String userAnswer = txtUserInputTest.getText().toString();
                submitAnswer(userAnswer);
                return true;
            }
            return false;
        });
    }

    private void loadDataFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child("users").child(userId).child("incorrectAnswers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                words.clear();
                answers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String word = snapshot.getKey();
                    String answer = snapshot.getValue(String.class);
                    words.add(word);
                    answers.add(answer);
                }
                // Display the first word
                displayWord();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadDataFromFirebase:onCancelled", databaseError.toException());
                Toast.makeText(TestYourself.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void displayWord() {
        tvFrontTest.setText(words.get(currentWordIndex));
    }

    private void resetCounters() {
        correctCount = 0;
        incorrectCount = 0;
        updateCounters();
    }

    private void updateCounters() {
        tvCorrect.setText("Correct: " + correctCount);
        tvIncorrect.setText("Incorrect: " + incorrectCount);
    }

    private void submitAnswer(String userAnswer) {
        String correctAnswer = answers.get(currentWordIndex);
        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);
        checkAnswer(isCorrect);

        if (isCorrect) {
            txtUserInputTest.setBackgroundColor(ContextCompat.getColor(TestYourself.this, R.color.green));
        } else {
            txtUserInputTest.setBackgroundColor(ContextCompat.getColor(TestYourself.this, R.color.red));
            incorrectWords.add(words.get(currentWordIndex));
            incorrectAnswers.add(answers.get(currentWordIndex));
        }

        answerSubmitted = true;
    }

    private void checkAnswer(boolean isCorrect) {
        if (isCorrect) {
            correctCount++;
        } else {
            incorrectCount++;
        }
        updateCounters();
    }
}

