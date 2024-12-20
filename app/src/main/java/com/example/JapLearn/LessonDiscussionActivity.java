package com.example.JapLearn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class LessonDiscussionActivity extends AppCompatActivity {


    private MediaPlayer mediaPlayer;
    private Button playAudio;
    private static final String TAG = "LessonDiscussionActivity";
    private TextView tvFront, tvBtnName, tvBtnExample, tvRomaji, tv_op_1, tv_op_2, tv_desc_1, tv_desc_2, tvTitle;
    private ImageView audioIcon;
    private int currentLessonIndex = 0;
    private int lessonCount = 0;
    private boolean isRomajiName = true; // Flag to track if Romaji is currently set to Name or Examples
    private DatabaseReference database;
    private String currentRomaji = "";
    private String currentExamples = "";
    private String dataPronun = "";
    private String dataDesc = "";
    private String exampleEn = "";
    private String exampleJp = "";
    private boolean isReviewMode = false; // Flag to check if it's in review mode
    private int lessonItemIndex = 0;
    private FirebaseStorage storage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_discussion);

        // Initialize views
        tv_desc_1 = findViewById(R.id.Tv_desc_1);
        tv_desc_2 = findViewById(R.id.Tv_desc_2);
        tvFront = findViewById(R.id.tvFront);
        tvRomaji = findViewById(R.id.tvRomaji);
        tvBtnName = findViewById(R.id.tvBtnName);
        tvBtnExample = findViewById(R.id.tvBtnExample);
        tv_op_1 = findViewById(R.id.Tv_op_1);
        tv_op_2 = findViewById(R.id.Tv_op_2);
        audioIcon = findViewById(R.id.btn_lesson_audio);
        tvTitle = findViewById(R.id.tvTitle);

        storage = FirebaseStorage.getInstance();


        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Check if the activity was started in review mode
        isReviewMode = getIntent().getBooleanExtra("reviewMode", false);

        // Get the lesson number from the intent
        int lessonNumber = getIntent().getIntExtra("lessonNumber", 1);

        // Fetch current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch current lesson from user's data
        UserModel userModel = new UserModel();
        DatabaseReference userRef = userModel.getUserRef(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Load the lesson based on the passed lesson number
                    loadLesson(lessonNumber, 0); // Pass lessonItemIndex as 0
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting user data", databaseError.toException());
            }
        });

        // Set default values for the Name option
        tv_op_1.setText("Pronunciation : ");
        tv_op_2.setText("Description : ");
        tv_desc_1.setText("");
        tv_desc_2.setText("");
        audioIcon.setVisibility(View.GONE); // Hide the audio icon by default

        // Set up click listener for Name button
        tvBtnName.setOnClickListener(v -> {
            setNameOptionActive();
            loadNameOptionData();
        });

        // Set up click listener for Example button
        tvBtnExample.setOnClickListener(v -> {
            setExampleOptionActive();
            loadExampleOptionData();
        });

        // Set up click listener for btnNext
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (lessonItemIndex < lessonCount - 1) {
                lessonItemIndex++;
                loadLesson(lessonNumber, lessonItemIndex); // Load the next lesson item

            } else {
                showEndOfLessonPopup(); // Show end of lesson popup
            }
        });

        // Set up click listener for btnPrev
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if (lessonItemIndex > 0) {
                lessonItemIndex--;
                loadLesson(lessonNumber, lessonItemIndex); // Load the previous lesson item
            }
        });

        // Set up click listener for tvRomaji
        tvRomaji.setOnClickListener(v -> {
            if (isRomajiName) {
                tvRomaji.setText(currentExamples);
                isRomajiName = false;
            } else {
                tvRomaji.setText(currentRomaji);
                isRomajiName = true;
            }
        });

        // Set up click listener for the audio icon
        audioIcon.setOnClickListener(v -> playLessonAudio(tvFront.getText().toString())); // Implement playLessonAudio method
    }

    private void setNameOptionActive() {
        tvBtnName.setTextAppearance(R.style.BoldText); // Use a style for bold text
        tvBtnExample.setTextAppearance(R.style.NormalText); // Use a style for normal text
        tv_op_1.setText("Pronunciation : ");
        tv_op_2.setText("Description : ");
        tv_desc_1.setText(dataPronun);
        tv_desc_2.setText(dataDesc);
        audioIcon.setVisibility(View.GONE); // Hide the audio icon
        tvTitle.setText("Name"); // Update title text
    }

    private void setExampleOptionActive() {
        tvBtnName.setTextAppearance(R.style.NormalText); // Use a style for normal text
        tvBtnExample.setTextAppearance(R.style.BoldText); // Use a style for bold text
        tv_op_1.setText("English Example :");
        tv_op_2.setText("Japanese Example :");
        tv_desc_1.setText(exampleEn);
        tv_desc_2.setText(exampleJp);
        audioIcon.setVisibility(View.VISIBLE); // Show the audio icon
        tvTitle.setText("Example"); // Update title text
    }

    private void loadLesson(int lessonNumber, int lessonItemIndex) {
        String lessonItemPath = "Lessons/" + lessonNumber;
        Log.d(TAG, "Loading lesson items for lesson: " + lessonNumber + ", index: " + lessonItemIndex);

        database.child(lessonItemPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lessonCount = (int) dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Total lesson items found: " + lessonCount);

                    String specificLessonItemPath = lessonItemPath + "/" + lessonNumber + "_" + (lessonItemIndex + 1);
                    Log.d(TAG, "Loading specific lesson item: " + specificLessonItemPath);

                    database.child(specificLessonItemPath).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d(TAG, "Lesson item found: " + dataSnapshot.getKey());
                                displayLesson(dataSnapshot);
                            } else {
                                Log.e(TAG, "Lesson item does not exist: " + specificLessonItemPath);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Error getting specific lesson item data", databaseError.toException());
                        }
                    });
                } else {
                    Log.e(TAG, "Lesson node does not exist: " + lessonItemPath);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting lesson data", databaseError.toException());
            }
        });
    }

    private void displayLesson(DataSnapshot dataSnapshot) {
        String japaneseChar = dataSnapshot.child("japaneseChar").getValue(String.class);
        currentRomaji = dataSnapshot.child("dataRomaji").getValue(String.class);
        dataPronun = dataSnapshot.child("dataPronun").getValue(String.class);
        dataDesc = dataSnapshot.child("dataDesc").getValue(String.class);
        exampleEn = dataSnapshot.child("dataExampleEn").getValue(String.class);
        exampleJp = dataSnapshot.child("dataExampleJp").getValue(String.class);

        Log.d(TAG, "Displaying lesson item: " + dataSnapshot.getKey());

        tvFront.setText(japaneseChar);
        tvRomaji.setText(currentRomaji);

        // Set the active option based on isRomajiName
        if (isRomajiName) {
            setNameOptionActive();
        } else {
            setExampleOptionActive();
        }
    }


    private void loadNameOptionData() {
        // This method can be used to load additional data specific to the Name option if required
    }

    private void loadExampleOptionData() {
        // This method can be used to load additional data specific to the Example option if required
    }

    private void playLessonAudio(String tvFront) {
        // Get lesson number from intent
        int lessonNumber = getIntent().getIntExtra("lessonNumber", 1);

        // Define the path based on the lesson number
        String lessonNumberPath = lessonNumber == 1 ? "audios/" : "audioKana/";

        // Define the audio file name with or without .mp3 based on the lesson number
        String audioFileName = lessonNumber == 2 ? tvFront + ".mp3" : tvFront;

        // Get a reference to the audio file in Firebase Storage
        StorageReference audioRef = storage.getReference().child(lessonNumberPath + audioFileName);

        // Check if MediaPlayer is already in use
        if (mediaPlayer != null) {
            mediaPlayer.reset();  // Reset it to reuse the same player
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
                    Toast.makeText(LessonDiscussionActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                    return true;  // Return true to indicate the error has been handled
                });
                mediaPlayer.prepareAsync();  // Use async preparation to avoid blocking the main thread
            } catch (IOException e) {
                Log.e(TAG, "Error setting data source for MediaPlayer", e);
                Toast.makeText(LessonDiscussionActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error fetching audio file URL", exception);
            Toast.makeText(LessonDiscussionActivity.this, "Audio file not found", Toast.LENGTH_SHORT).show();
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




    private void showEndOfLessonPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations!")
                .setMessage("You have completed this lesson.")
                .setPositiveButton("Back to Lesson List", (dialog, which) -> {
                    Intent intent = new Intent(LessonDiscussionActivity.this, LessonList.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Restart Lesson", (dialog, which) -> {
                    lessonItemIndex = 0;
                    loadLesson(getIntent().getIntExtra("lessonNumber", 1), lessonItemIndex);
                })
                .show();
    }
}