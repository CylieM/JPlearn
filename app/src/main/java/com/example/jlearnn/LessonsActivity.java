package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class LessonsActivity extends AppCompatActivity {

    private static final String TAG = "Lesson421";
    private TextView tvFront;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext;
    private int currentLessonIndex = 1; // Start from Lesson1
    private int currentItemIndex = 1; // Start from item_1
    private int lessonCount; // Total lessons, will be dynamically set
    private int itemsPerLesson = 5; // Assuming 5 items per lesson
    private boolean answerVerified = false; // Flag to track if answer has been verified

    private DatabaseReference database;
    private String currentCharacter;
    private UserModel userModel;
    private String userId;
    private String currentLesson;
    private String romaji;

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
            if (!answerVerified) {
                // If the answer has not been verified, verify the answer
                verifyAnswer();
            } else {
                // If the answer has already been verified, proceed to the next question
                loadNextQuestion();
            }
        });

        // Disable prev button click listener
        btnPrev.setOnClickListener(null);

        // Set up text change listener for txtUserInput
        txtUserInput.addTextChangedListener(new TextWatcher() {
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
                // Automatically verify the answer when the user finishes typing
                if (!answerVerified) {
                    verifyAnswer();
                }
            }
        });
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
                    loadLesson(lesson, currentItemIndex);
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
        String lessonPath = "Lessons/" + lesson + "/" + lesson + "_" + index;

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
        currentCharacter= dataSnapshot.child("japaneseChar").getValue(String.class);
        romaji = dataSnapshot.child("dataRomaji").getValue(String.class);

        // Display the data in the appropriate TextViews
        tvFront.setText(currentCharacter);
        txtUserInput.setText(""); // Clear previous input
    }

    private void verifyAnswer() {
        String userInput = txtUserInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            if (userInput.equalsIgnoreCase(romaji)) {
                txtUserInput.setBackgroundColor(getResources().getColor(R.color.green));
                Log.d(TAG, "Correct answer: " + userInput);
                txtUserInput.postDelayed(() -> {
                    txtUserInput.setBackgroundColor(getResources().getColor(android.R.color.white));
                    if (currentItemIndex == itemsPerLesson) {
                        returnToLessonDiscussion();
                    } else {
                        loadNextQuestion();
                    }
                }, 1000);
            } else {
                txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));
                Log.d(TAG, "Incorrect answer. User input: " + userInput + ", Correct answer: " + romaji);

                saveIncorrectAnswer(currentCharacter, userInput);
                if (currentItemIndex == itemsPerLesson) {
                    btnNext.setEnabled(true); // Enable the next button if it's the last question and the answer is incorrect
                }
            }
            answerVerified = false;
            updateProgress(); // Increment count after verifying answer
        }
    }

    private void loadNextQuestion() {
        if (currentItemIndex < itemsPerLesson + 1) {
            currentItemIndex++;
            loadLesson(currentLesson, currentItemIndex);
        } else {
            returnToLessonDiscussion(); // Return to lesson discussion after completing all questions
        }
    }




    private void saveIncorrectAnswer(String character, String userInput) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userIncorrectAnswersRef = database.child("users").child(userId).child("incorrectAnswers");
        userIncorrectAnswersRef.child(character).setValue(userInput)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Incorrect answer saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving incorrect answer", e));
    }

    private void updateProgress() {
        // Get the user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Construct the path to the user's progress for the current item
        String itemProgressPath = "users/" + userId + "/itemProgress/lesson" + currentLessonIndex + "/item_" + currentItemIndex + "/counter";

        // Increment the progress for the corresponding Lesson
        incrementProgress(itemProgressPath);
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

                // Check if all items in the current lesson are completed
                checkAllItemsProgress();
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
                        Integer currentProgress = lessonItemSnapshot.child("counter").getValue(Integer.class);

                        // Check if the progress has reached 5 for all items
                        if (currentProgress == null || currentProgress < 5) {
                            allItemsCompleted = false;
                            break;
                        }
                    }

                    if (allItemsCompleted) {
                        // After completing all items in the lesson, return to lesson discussion
                        returnToLessonDiscussion();
                    } else {
                        // If not all items are completed, reload the lesson
                        loadLesson(currentLesson, currentItemIndex);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking progress", databaseError.toException());
            }
        });
    }



    private void returnToLessonDiscussion() {
        // Implement the logic to return to the lesson discussion
        // For example, you might start a new activity for the lesson discussion
        Intent intent = new Intent(this, LessonDiscussionActivity.class);
        startActivity(intent);
    }

    private void proceedToNextLesson() {
        if (currentLessonIndex < lessonCount) {
            currentLessonIndex++;
            currentItemIndex = 1; // Reset the item index
            fetchCurrentLesson(); // Fetch current lesson again
        } else {
            // Update the daily streak before returning to the Lesson Discussion
            updateDailyStreak();
        }
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

                    // Return to lesson discussion after updating the daily streak
                    returnToLessonDiscussion();
                } else {
                    Log.e(TAG, "User data does not exist for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating daily streak", databaseError.toException());
                finish();
            }
        });
    }
}
