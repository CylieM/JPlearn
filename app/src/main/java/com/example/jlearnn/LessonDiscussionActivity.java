package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonDiscussionActivity extends AppCompatActivity {

    private static final String TAG = "LessonDiscussionActivity";
    private TextView tvFront, tvPronunciation, tvDesc, tvBtnName, tvBtnExample, tvRomaji;
    private ImageView ivArrow;
    private int currentLessonIndex = 0;
    private int lessonCount = 0;
    private boolean isRomajiName = true; // Flag to track if Romaji is currently set to Name or Examples
    private DatabaseReference database;
    private String currentRomaji = "";
    private String currentExamples = "";
    private boolean isReviewMode = false; // Flag to check if it's in review mode
    private int lessonItemIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_discussion);

        // Initialize views
        tvFront = findViewById(R.id.tvFront);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        tvRomaji = findViewById(R.id.tvRomaji);
        tvDesc = findViewById(R.id.tvDesc);
        tvBtnName = findViewById(R.id.tvBtnName);
        tvBtnExample = findViewById(R.id.tvBtnExample);
        ivArrow = findViewById(R.id.ivArrow);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Check if the activity was started in review mode
        isReviewMode = getIntent().getBooleanExtra("reviewMode", false);

        // Fetch current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch current lesson from user's data
        UserModel userModel = new UserModel();
        DatabaseReference userRef = userModel.getUserRef(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);
                    if (currentLesson != null && !currentLesson.isEmpty()) {
                        currentLessonIndex = Integer.parseInt(currentLesson);
                        // Load the first lesson item when loading the lesson initially
                        loadLesson(currentLessonIndex, 0); // Pass lessonItemIndex as 0
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting user data", databaseError.toException());
            }
        });




        // Set up click listener for btnNext
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (lessonItemIndex < lessonCount - 1) {
                lessonItemIndex++;
                loadLesson(currentLessonIndex, lessonItemIndex); // Load the next lesson item
            } else {
                // Handle case when there are no more lesson items
                if (isReviewMode) {
                    startLessonReviewActivity();
                } else {
                    startLessonsActivity();
                }
            }
        });


// Set up click listener for btnPrev
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if (lessonItemIndex > 0) {
                lessonItemIndex--;
                loadLesson(currentLessonIndex, lessonItemIndex); // Load the previous lesson item
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
        String pronunciation = dataSnapshot.child("pronunciation").getValue(String.class);
        currentExamples = dataSnapshot.child("dataExample").getValue(String.class);
        String description = dataSnapshot.child("dataDesc").getValue(String.class);

        Log.d(TAG, "Displaying lesson item: " + dataSnapshot.getKey());

        tvFront.setText(japaneseChar);
        tvPronunciation.setText(pronunciation);
        tvRomaji.setText(currentRomaji);
        tvDesc.setText(description);
        isRomajiName = true;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivArrow.getLayoutParams();
        marginLayoutParams.setMargins(0, 16, 0, 0);
        ivArrow.setLayoutParams(marginLayoutParams);

        tvBtnExample.setOnClickListener(v -> {
            if (isRomajiName) {
                tvRomaji.setText(currentExamples);
                isRomajiName = false;
            }
        });

        tvBtnName.setOnClickListener(v -> {
            if (!isRomajiName) {
                tvRomaji.setText(currentRomaji);
                isRomajiName = true;
            }
        });
    }

    private void startLessonsActivity() {
        // Start the LessonsActivity
        startActivity(new Intent(LessonDiscussionActivity.this, LessonsActivity.class));
    }

    private void startLessonReviewActivity() {
        // Start the LessonReviewActivity
        startActivity(new Intent(LessonDiscussionActivity.this, LessonReview.class));
    }
}
