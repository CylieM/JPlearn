package com.example.jlearnn;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonReview extends AppCompatActivity {

    private static final String TAG = "LessonReview";
    private TextView tvFront;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext;
    private int currentLessonIndex = 0; // Start from Lesson1
    private int lessonCount = 5; // Total lessons
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
        loadLesson(currentLessonIndex);

        // Set up click listener for btnNext
        btnNext.setOnClickListener(v -> {
            if (currentLessonIndex < lessonCount - 1) {
                currentLessonIndex++;
                loadLesson(currentLessonIndex);
            }
        });

        // Disable prev button click listener
        btnPrev.setOnClickListener(null);
    }

    private void loadLesson(int index) {
        // Construct the lesson path based on the lesson index
        String lessonPath = "Lessons/Lesson 1/1_" + (index + 1);

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
                } else {
                    // If user input does not match the romaji, set background color to red
                    txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));
                    Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
