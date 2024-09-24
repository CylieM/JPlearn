package com.example.JapLearn;

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

public class LessonReview extends AppCompatActivity {

    private static final String TAG = "LessonReview";
    private TextView tvFront;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext;
    private int currentLessonIndex = 1; // Start from Lesson1
    private int currentItemIndex = 1; // Start from item_1
    private int lessonCount = 4; // Total lessons
    private int itemsPerLesson = 5; // Total items per lesson
    private DatabaseReference database;
    private String currentCharacter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        // Initialize views
        tvFront = findViewById(R.id.tvFront);
        txtUserInput = findViewById(R.id.txtUserInput);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Load the current lesson
        loadLesson(currentLessonIndex, currentItemIndex);

        // Set up click listener for btnNext
        btnNext.setOnClickListener(v -> {
            if (currentItemIndex < itemsPerLesson) {
                currentItemIndex++;
                loadLesson(currentLessonIndex, currentItemIndex);
            } else if (currentLessonIndex < lessonCount) {
                currentLessonIndex++;
                currentItemIndex = 1;
                loadLesson(currentLessonIndex, currentItemIndex);
            } else {
                finish();
            }
        });

        // Disable prev button click listener
        btnPrev.setOnClickListener(null);
    }

    private void loadLesson(int lessonIndex, int itemIndex) {
        // Construct the lesson path based on the lesson and item index
        String lessonPath = "Lessons/Lesson " + lessonIndex + "/1_" + itemIndex;

        // Fetch the lesson data
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
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();

                    // Update progress in Firebase
                    updateProgress();
                } else {
                    // If user input does not match the romaji, set background color to red
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));
                    Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProgress() {
        // Get the user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Construct the path to the user's progress for the current item
        String itemProgressPath = "users/" + userId + "/itemProgress/lesson" + currentLessonIndex + "/item_" + currentItemIndex + "/counter";

        // Increment the progress for the corresponding Lesson
        incrementProgress(itemProgressPath);

        // Check if all items in the current lesson are completed
        checkAllItemsProgress();
    }

    private void incrementProgress(String userProgressPath) {
        // Update the progress in the database for the Lesson Item
        database.child(userProgressPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentProgress = dataSnapshot.getValue(Integer.class);

                // Increment the progress
                if (currentProgress != null) {
                    currentProgress++;
                } else {
                    currentProgress = 1; // Start from 1 if it's null
                }

                // Update the progress in the database
                database.child(userProgressPath).setValue(currentProgress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating user progress", databaseError.toException());
            }
        });
    }

    private void checkAllItemsProgress() {
        // Get the user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Construct the path to the user's progress for the current lesson
        String lessonProgressPath = "users/" + userId + "/itemProgress/lesson" + currentLessonIndex;

        // Fetch the user's progress data
        database.child(lessonProgressPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean allItemsCompleted = true;
                if (dataSnapshot.exists()) {
                    // Iterate through each child (lesson item key)
                    for (DataSnapshot lessonItemSnapshot : dataSnapshot.getChildren()) {
                        // Get the current progress count
                        int currentProgress = lessonItemSnapshot.child("counter").getValue(Integer.class);

                        // Check if the progress has reached 5 for all items
                        if (currentProgress < 5) {
                            allItemsCompleted = false;
                            break;
                        }
                    }
                    if (allItemsCompleted) {
                        // If all items have reached 5, proceed to the next lesson
                        proceedToNextLesson();
                    }
                } else {
                    Log.e(TAG, "User progress data not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting user progress data", databaseError.toException());
            }
        });
    }

    private void proceedToNextLesson() {
        // Increment the lesson progress and reset item counters for the new lesson
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int nextLessonIndex = currentLessonIndex + 1;
        String nextLessonPath = "users/" + userId + "/itemProgress/lesson" + nextLessonIndex;
        Map<String, Integer> newLessonProgress = new HashMap<>();
        for (int i = 1; i <= itemsPerLesson; i++) {
            newLessonProgress.put("item_" + i + "/counter", 0);
        }
        database.child(nextLessonPath).setValue(newLessonProgress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentLessonIndex++;
                        currentItemIndex = 1;
                        loadLesson(currentLessonIndex, currentItemIndex);
                    } else {
                        Log.e(TAG, "Error resetting progress for new lesson");
                    }
                });
    }
}
