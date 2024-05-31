package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class LessonsActivity extends AppCompatActivity {

    private static final String TAG = "Lesson421";
    private TextView tvFront;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext;
    private Map<String, Integer> counters = new HashMap<>();
    private int currentLessonIndex = 0; // Start from Lesson1
    private int lessonCount; // Total lessons, will be dynamically set

    private DatabaseReference database;
    private String currentCharacter;
    private UserModel userModel;
    private String userId;
    private String currentLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        // Initialize views
        tvFront = findViewById(R.id.tvFront);
        txtUserInput = findViewById(R.id.txtUserInput);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Initialize UserModel
        userModel = new UserModel();
        userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        // Fetch current lesson for the user
        fetchCurrentLesson();

        // Set up click listener for btnNext
        btnNext.setOnClickListener(v -> {
            if (currentLessonIndex < lessonCount - 1) {
                currentLessonIndex++;
                loadLesson(currentLesson, currentLessonIndex);
            } else {
                updateDailyStreak();
            }
        });

        // Disable prev button click listener
        btnPrev.setOnClickListener(null);
    }

    private void fetchCurrentLesson() {
        userModel.getUserRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);
                    // Count the number of items under the current lesson
                    countLessonItems(currentLesson);
                } else {
                    Log.e(TAG, "User data does not exist for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data", databaseError.toException());
            }
        });
    }

    private void countLessonItems(String lesson) {
        database.child("Lessons").child(lesson).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lessonCount = (int) dataSnapshot.getChildrenCount();
                    // Load the first item of the current lesson
                    loadLesson(currentLesson, currentLessonIndex);
                } else {
                    Log.e(TAG, "Lesson does not exist: " + lesson);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error counting lesson items", databaseError.toException());
            }
        });
    }

    private void loadLesson(String lesson, int index) {
        // Construct the path based on the lesson number and item index
        String lessonPath = "Lessons/" + lesson + "/" + lesson + "_" + (index + 1);

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
        txtUserInput.setText(""); // Clear previous input

        // Check user input against the romaji
        txtUserInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String userInput = txtUserInput.getText().toString().trim();
                if (userInput.equalsIgnoreCase(romaji)) {
                    // If user input matches the romaji, set background color to green
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.green));
                    Log.d(TAG, "Correct answer: " + userInput);
                } else {
                    // If user input does not match the romaji, set background color to red
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));

                    Log.d(TAG, "Incorrect answer. User input: " + userInput + ", Correct answer: " + romaji);
                    Toast.makeText(this, "Next session will be in 3 hours", Toast.LENGTH_LONG).show();
                    saveIncorrectAnswer(currentCharacter, userInput);
                }
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

    private void updateDailyStreak() {
        userModel.getUserRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentTime = System.currentTimeMillis();
                    long lastLoginTime = dataSnapshot.child("lastLoginDate").getValue(Long.class);
                    int dailyStreak = dataSnapshot.child("dailyStreak").getValue(Integer.class);

                    if (currentTime - lastLoginTime < 24 * 60 * 60 * 1000) {
                        dailyStreak++;
                    } else if (currentTime - lastLoginTime > 48 * 60 * 60 * 1000) {
                        dailyStreak = 0;
                    }

                    dataSnapshot.getRef().child("dailyStreak").setValue(dailyStreak);
                    dataSnapshot.getRef().child("lastLoginDate").setValue(currentTime);

                    Log.d(TAG, "Daily streak updated: " + dailyStreak);
                } else {
                    Log.e(TAG, "User data does not exist for userId: " + userId);
                }

                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating daily streak", databaseError.toException());
                finish();
            }
        });
    }

    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Error checking progress", databaseError.toException());
    }
}