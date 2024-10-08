package com.example.JapLearn;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class LessonsActivity extends AppCompatActivity {

    private static final String TAG = "Lesson421";
    private MediaPlayer mediaPlayer;

    private TextView tvFront, tvOp1, tvOp2, tvDesc1, tvDesc2, tvTitle;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext, btnTestName, btnTestExample, btnAudio;
    private int currentLessonIndex = 1;
    private int currentItemIndex = 1;
    private int lessonCount;
    private int itemsPerLesson = 43;
    private int correctAnswers = 0; // Track correct answers
    private int totalItems = 0; // Track total items for score calculation
    private boolean answerVerified = false;

    private DatabaseReference database;
    private String currentCharacter;
    private UserModel userModel;
    private String userId;
    private String currentLesson;
    private String romaji;
    private String dataPronun;
    private String dataDesc;
    private String exampleEn;
    private String exampleJp;
    private FirebaseStorage storage;
    private FirebaseAuth auth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        // Initialize views
        tvFront = findViewById(R.id.tvFront);
        txtUserInput = findViewById(R.id.txtUserInput);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvOp1 = findViewById(R.id.Tv_op_1);
        tvOp2 = findViewById(R.id.Tv_op_2);
        tvDesc1 = findViewById(R.id.Tv_desc_1);
        tvDesc2 = findViewById(R.id.Tv_desc_2);
        tvTitle = findViewById(R.id.tvTitle);
        btnTestName = findViewById(R.id.btnTestName);
        btnTestExample = findViewById(R.id.btnTestExample);
        btnAudio = findViewById(R.id.btn_lesson_audio);
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initially hide the TextViews and disable the buttons
        resetViewsForNewQuestion();

        userModel = new UserModel();
        userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference();

        // Fetch current lesson for the user
        fetchCurrentLesson();

        btnNext.setOnClickListener(v -> {
            if (!answerVerified) {
                verifyAnswer();
            } else {
                loadNextQuestion();
            }
        });

        btnPrev.setOnClickListener(null); // Placeholder for "previous question" logic

        // Find EditText and set input type to allow pressing enter
        txtUserInput = findViewById(R.id.txtUserInput);
        txtUserInput.setImeOptions(EditorInfo.IME_ACTION_DONE);  // Change keyboard "Done" icon to submit answer
        txtUserInput.setInputType(InputType.TYPE_CLASS_TEXT);    // Set input to normal text

        // Listen for the "Enter" or "Done" key press
        txtUserInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAnswer(); // Call the verification when Enter is pressed
                return true;
            }
            return false;
        });

        // Set up click listeners for buttons
        btnTestName.setOnClickListener(v -> updateTextViewsForTestName());
        btnTestExample.setOnClickListener(v -> updateTextViewsForTestExample());
        btnAudio.setOnClickListener(v -> {
            playLessonAudio(tvFront.getText().toString());
        });

    }

    private void resetViewsForNewQuestion() {
        // Disable the buttons initially, to only allow their usage after incorrect answer
        btnTestName.setEnabled(false);
        btnTestExample.setEnabled(false);
        btnAudio.setEnabled(false);

        // Enable user input for a new question and reset the background
        txtUserInput.setEnabled(true);
        txtUserInput.setBackgroundColor(getResources().getColor(R.color.white));

        // Initially hide all the tips
        tvTitle.setText(" ");
        tvOp1.setText(" ");
        tvOp2.setText(" ");
        tvDesc1.setText(" ");
        tvDesc2.setText( "");
        tvTitle.setVisibility(View.INVISIBLE);
        tvOp1.setVisibility(View.INVISIBLE);
        tvOp2.setVisibility(View.INVISIBLE);
        tvDesc1.setVisibility(View.INVISIBLE);
        tvDesc2.setVisibility(View.INVISIBLE);
    }

    private void fetchCurrentLesson() {
        userModel.getUserRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);
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
                    totalItems = (int) dataSnapshot.getChildrenCount();
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
        String lessonPath = "Lessons/" + lesson + "/" + lesson + "_" + index;

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
        currentCharacter = dataSnapshot.child("japaneseChar").getValue(String.class);
        romaji = dataSnapshot.child("dataRomaji").getValue(String.class);
        dataPronun = dataSnapshot.child("dataPronun").getValue(String.class);
        dataDesc = dataSnapshot.child("dataDesc").getValue(String.class);
        exampleEn = dataSnapshot.child("dataExampleEn").getValue(String.class);
        exampleJp = dataSnapshot.child("dataExampleJp").getValue(String.class);


        tvFront.setText(currentCharacter);
        txtUserInput.setText(""); // Clear previous input

        // Set default values for test name
        resetViewsForNewQuestion();
    }

    private void updateTextViewsForTestName() {
        tvTitle.setText("Item Name");
        tvOp1.setText("Pronunciation");
        tvOp2.setText("Description");
        tvDesc1.setText(dataPronun);
        tvDesc2.setText(dataDesc);

        tvTitle.setVisibility(View.VISIBLE);
        tvOp1.setVisibility(View.VISIBLE);
        tvOp2.setVisibility(View.VISIBLE);
        tvDesc1.setVisibility(View.VISIBLE);
        tvDesc2.setVisibility(View.VISIBLE);
    }

    private void updateTextViewsForTestExample() {
        tvTitle.setText("Item Example");
        tvOp1.setText("English Example");
        tvOp2.setText("Japanese Example");
        tvDesc1.setText(exampleEn);
        tvDesc2.setText(exampleJp);

        tvTitle.setVisibility(View.VISIBLE);
        tvOp1.setVisibility(View.VISIBLE);
        tvOp2.setVisibility(View.VISIBLE);
        tvDesc1.setVisibility(View.VISIBLE);
        tvDesc2.setVisibility(View.VISIBLE);
    }

    private void verifyAnswer() {
        String userInput = txtUserInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            if (userInput.equalsIgnoreCase(romaji)) {
                txtUserInput.setBackgroundColor(getResources().getColor(R.color.green));
                correctAnswers++;
                answerVerified = true;

                txtUserInput.postDelayed(() -> {
                    txtUserInput.setBackgroundColor(getResources().getColor(android.R.color.white));
                    loadNextQuestion();
                }, 1000);

            } else {
                handleIncorrectAnswer(userInput);
            }
        }
    }

    private void playLessonAudio(String tvFront) {
        // Get lesson number from the currentLesson variable
        int lessonNumber = Integer.parseInt(currentLesson);

        // Define the path based on the lesson number
        String lessonNumberPath;
        switch (lessonNumber) {
            case 1:
                lessonNumberPath = "audios/";
                break;
            case 2:
                lessonNumberPath = "audioKana/";
                break;
            case 3:
                lessonNumberPath = "audioVocab/";
                break;
            case 4:
                lessonNumberPath = "grammar/";
                break;
            default:
                lessonNumberPath = "audios/";  // Default fallback
                break;
        }

        // Define the audio file name with .mp3 for lessons 2 and 3
        String audioFileName = (lessonNumber == 2 || lessonNumber == 3) ? tvFront + ".mp3" : tvFront;

        // Get a reference to the audio file in Firebase Storage
        StorageReference audioRef = storage.getReference().child(lessonNumberPath + audioFileName);

        // Check if MediaPlayer is already in use
        if (mediaPlayer != null) {
            mediaPlayer.reset();  // Reset to reuse the same player
        } else {
            mediaPlayer = new MediaPlayer();  // Initialize new MediaPlayer if it's null
        }

        // Fetch the download URL for the audio file
        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                mediaPlayer.setDataSource(uri.toString());  // Set the data source to Firebase URL
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();  // Start playback when it's prepared
                    Log.d(TAG, "Playing audio from: " + uri.toString());
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    Toast.makeText(LessonsActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                    return true;  // Return true to indicate the error has been handled
                });
                mediaPlayer.prepareAsync();  // Use async preparation to avoid blocking the main thread
            } catch (IOException e) {
                Log.e(TAG, "Error setting data source for MediaPlayer", e);
                Toast.makeText(LessonsActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error fetching audio file URL", exception);
            Toast.makeText(LessonsActivity.this, "Audio file not found", Toast.LENGTH_SHORT).show();
        });

        // Log the audio path for debugging purposes
        Log.d(TAG, "Audio path: " + lessonNumberPath + audioFileName);
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release the media player to free up resources
            mediaPlayer = null;
        }
        super.onDestroy();
    }



    private void handleIncorrectAnswer(String userInput) {
        txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));

        btnTestName.setEnabled(true);
        btnTestExample.setEnabled(true);
        btnAudio.setEnabled(true);
//        audioIcon.setOnClickListener(v -> playLessonAudio(tvFront.getText().toString())); // Implement playLessonAudio method


        btnNext.setEnabled(true); // Enable next button to proceed after wrong answer
        answerVerified = true; // Keep answer verification false to allow retry
        txtUserInput.setEnabled(false); // Disable input after incorrect answer

    }

    private void loadNextQuestion() {
        currentItemIndex++;
        if (currentItemIndex <= totalItems) {
            resetViewsForNewQuestion();
            loadLesson(currentLesson, currentItemIndex);
            answerVerified = false; // Reset verification status
        } else {
            showScoreAndRedirect();
        }
    }
    private void incrementCurrentLesson() {
        // Path to the user's current lesson in the database
        String lessonPath = "/users/" + userId + "/currentLesson";

        database.child(lessonPath).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the current lesson value as String
                String currentLesson = task.getResult().getValue(String.class);

                if (currentLesson != null) {
                    try {
                        // Parse currentLesson to integer, increment by 1
                        int currentLessonInt = Integer.parseInt(currentLesson);
                        String newLesson = String.valueOf(currentLessonInt + 1);  // Convert back to String

                        // Update the currentLesson for the current user
                        database.child(lessonPath).setValue(newLesson)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("LessonActivity", "Current lesson incremented successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LessonActivity", "Failed to increment current lesson.", e);
                                });
                    } catch (NumberFormatException e) {
                        Log.e("LessonActivity", "Error parsing currentLesson", e);
                    }
                }
            } else {
                Log.e("LessonActivity", "Failed to retrieve current lesson.", task.getException());
            }
        });
    }



    private void showScoreAndRedirect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LessonsActivity.this);
        builder.setMessage("You scored " + correctAnswers + " out of " + totalItems)
                .setPositiveButton("OK", (dialog, id) -> {
                    // Check if the user scored all answers correctly
                    if (correctAnswers == totalItems) {
                        incrementCurrentLesson();
                    }
                    Intent intent = new Intent(LessonsActivity.this, LessonList.class);
                    startActivity(intent);
                    finish();
                });
        builder.create().show();
    }
}