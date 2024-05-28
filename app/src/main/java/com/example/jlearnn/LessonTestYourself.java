package com.example.jlearnn;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LessonTestYourself extends AppCompatActivity {

    private static final String TAG = "Lesson421";
    private TextView tvFront, tvCounter;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext;
    private Map<String, Integer> counters = new HashMap<>();
    private int currentLessonIndex = 0; // Start from Lesson1
    private int lessonCount = 5; // Total lessons

    private DatabaseReference database;
    private String currentCharacter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_yourself);

        // Initialize views
        tvFront = findViewById(R.id.tvFront);
        txtUserInput = findViewById(R.id.txtUserInput);
        tvCounter = findViewById(R.id.tvCounter);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        // Load the current lesson
        loadLesson(currentLessonIndex);

        // Set up click listener for btnNext
        btnNext.setOnClickListener(v -> {
            if (currentLessonIndex < lessonCount - 1) {
                currentLessonIndex++;
                loadLesson(currentLessonIndex);
            } else {
                finish();
            }
        });

        // Disable prev button click listener
        btnPrev.setOnClickListener(null);
    }
    private void loadLesson(int index) {
        // Construct the path based on the lesson index
        String lessonPath = "Lessons/Lesson 1/1_" + (index + 1);

        // Fetch the lesson data from Realtime Database
        database.child(lessonPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    displayLesson(dataSnapshot);
                } else {
                    Log.e(TAG, "Lesson does not exist: " + lessonPath);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting lesson data", databaseError.toException());
            }
        });
    }
    private void displayLesson(DataSnapshot dataSnapshot) {
        // Get data from Firebase Realtime Database
        currentCharacter = dataSnapshot.child("japaneseChar").getValue(String.class);
        String romaji = dataSnapshot.child("dataRomaji").getValue(String.class);

        // Display the data in the appropriate TextViews
        tvFront.setText(currentCharacter);
        tvCounter.setText(currentCharacter + "="  + "/5");
        txtUserInput.setText(""); // Clear previous input

        // Check user input against the romaji
        txtUserInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String userInput = txtUserInput.getText().toString().trim();
                if (userInput.equalsIgnoreCase(romaji)) {
                    // If user input matches the romaji, set background color to green and update counter
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.green));
                    updateCounter(currentCharacter, 1);
                    Log.d(TAG, "Correct answer: " + userInput);
                } else {
                    // If user input does not match the romaji, set background color to red
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));
                    saveIncorrectAnswer(currentCharacter, userInput);
                    Log.d(TAG, "Incorrect answer. User input: " + userInput + ", Correct answer: " + romaji);
                    Toast.makeText(this, "Next session will be in 3 hours", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void updateCounter(String lessonKey, int increment) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userLessonRef = database.child("users").child(userId).child("itemProgress").child(currentCharacter);
        userLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer counter = dataSnapshot.getValue(Integer.class);
                if (counter != null && counter < 5) {
                    userLessonRef.setValue(counter + increment);
                    tvCounter.setText(currentCharacter + "=" + (counter + increment) + "/5");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating counter", databaseError.toException());
            }
        });
    }
    private void saveIncorrectAnswer(String character, String userInput) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userIncorrectAnswersRef = database.child("users").child(userId).child("incorrectAnswers");
        userIncorrectAnswersRef.child(character).setValue(userInput)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Incorrect answer saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving incorrect answer", e));
    }
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Error checking progress", databaseError.toException());
    }
}

