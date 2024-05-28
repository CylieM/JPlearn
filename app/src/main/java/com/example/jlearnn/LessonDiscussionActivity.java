package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

        // Fetch data from Firebase Realtime Database
        database.child("Lessons").child("Lesson 1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lessonCount = (int) dataSnapshot.getChildrenCount();
                if (lessonCount > 0) {
                    loadLesson(currentLessonIndex);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error getting documents.", databaseError.toException());
            }
        });

        // Set up click listener for btnNext
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (currentLessonIndex < lessonCount - 1) {
                currentLessonIndex++;
                if (currentLessonIndex == 4) { // Transition to next activity when reaching Lesson1.5
                    if (isReviewMode) {
                        startLessonReviewActivity();
                    } else {
                        startLessonsActivity();
                    }
                } else {
                    loadLesson(currentLessonIndex);
                }
            }
        });

        // Set up click listener for btnPrev
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if (currentLessonIndex > 0) {
                currentLessonIndex--;
                loadLesson(currentLessonIndex);
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

    private void loadLesson(int index) {
        String lessonPath = "Lessons/Lesson 1/1_" + (index + 1);

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
        String japaneseChar = dataSnapshot.child("japaneseChar").getValue(String.class);
        currentRomaji = dataSnapshot.child("dataRomaji").getValue(String.class);
        String pronunciation = dataSnapshot.child("pronunciation").getValue(String.class);
        currentExamples = dataSnapshot.child("dataExample").getValue(String.class);
        String description = dataSnapshot.child("dataDesc").getValue(String.class);

        // Display the data in the appropriate TextViews
        tvFront.setText(japaneseChar);
        tvPronunciation.setText(pronunciation);
        tvRomaji.setText(currentRomaji);
        tvDesc.setText(description);
        isRomajiName = true; // Reset flag when loading new lesson

        // Dynamically update the layout parameters to avoid hardcoded values
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivArrow.getLayoutParams();
        marginLayoutParams.setMargins(0, 16, 0, 0);
        ivArrow.setLayoutParams(marginLayoutParams);

        // Handle the click event for the button to change Romaji between Name and Examples
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

