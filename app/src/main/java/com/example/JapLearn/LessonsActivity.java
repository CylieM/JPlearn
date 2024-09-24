package com.example.JapLearn;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonsActivity extends AppCompatActivity {

    private static final String TAG = "Lesson421";
    private TextView tvFront, tvOp1, tvOp2, tvDesc1, tvDesc2, tvTitle;
    private EditText txtUserInput;
    private ImageButton btnPrev, btnNext, btnTestName, btnTestExample;
    private int currentLessonIndex = 1;
    private int currentItemIndex = 1;
    private int lessonCount;
    private int itemsPerLesson = 5;
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

        // Initially hide the TextViews and disable the buttons
        tvOp1.setVisibility(View.GONE);
        tvOp2.setVisibility(View.GONE);
        tvDesc1.setVisibility(View.GONE);
        tvDesc2.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);

        btnTestName.setEnabled(false);
        btnTestExample.setEnabled(false);

        userModel = new UserModel();
        userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        // Fetch current lesson for the user
        fetchCurrentLesson();

        btnNext.setOnClickListener(v -> {
            if (!answerVerified) {
                verifyAnswer();
            } else {
                loadNextQuestion();
            }
        });

        btnPrev.setOnClickListener(null); // Placeholder

        txtUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!answerVerified) {
                    verifyAnswer();
                }
            }
        });

        // Set up click listeners for buttons
        btnTestName.setOnClickListener(v -> updateTextViewsForTestName());
        btnTestExample.setOnClickListener(v -> updateTextViewsForTestExample());
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
                    lessonCount = (int) dataSnapshot.getChildrenCount();
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


    private void setNameOptionActive() {
        tvOp1.setText("Pronunciation : ");
        tvOp2.setText("Description : ");
        tvDesc1.setText(dataPronun);
        tvDesc2.setText(dataDesc);
        tvTitle.setText("Item Name"); // Update title text

        tvOp2.setVisibility(View.VISIBLE);     // Dont show option 2
        tvDesc2.setVisibility(View.VISIBLE);   // Dont Show description 2
        tvTitle.setVisibility(View.VISIBLE);   // Show title
        tvOp1.setVisibility(View.VISIBLE);     // Show option 1
        tvDesc1.setVisibility(View.VISIBLE);   // Show description 1

        btnTestName.setEnabled(true);         // Disable after clicked

    }

    private void setExampleOptionActive() {
        tvOp1.setText("English Example : ");
        tvOp2.setText("Japanese Example : ");
        tvDesc1.setText(exampleEn);
        tvDesc2.setText(exampleJp);
        tvTitle.setText("Item Example"); // Update title text

        tvOp1.setVisibility(View.VISIBLE);     // Show option 1
        tvDesc1.setVisibility(View.VISIBLE);   // Show description 1
        tvTitle.setVisibility(View.VISIBLE);   // Show title
        tvOp2.setVisibility(View.VISIBLE);     // Show option 2
        tvDesc2.setVisibility(View.VISIBLE);   // Show description 2

        btnTestExample.setEnabled(true);      // Disable after clicked

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

        // Set default values
        updateTextViewsForTestName();
    }

    private void updateTextViewsForTestName() {
        tvTitle.setText("Item Name");
        tvOp1.setText("Pronunciation");
        tvOp2.setText("Description");
        tvDesc1.setText(dataPronun);
        tvDesc2.setText(dataDesc);
    }

    private void updateTextViewsForTestExample() {
        tvTitle.setText("Item Example");
        tvOp1.setText("English Example");
        tvOp2.setText("Japanese Example");
        tvDesc1.setText(exampleEn);
        tvDesc2.setText(exampleJp);
    }

    private void verifyAnswer() {
        String userInput = txtUserInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            if (userInput.equalsIgnoreCase(romaji)) {
                txtUserInput.setBackgroundColor(getResources().getColor(R.color.green));
                Log.d(TAG, "Correct answer: " + userInput);

                txtUserInput.postDelayed(() -> {
                    txtUserInput.setBackgroundColor(getResources().getColor(android.R.color.white));
                    txtUserInput.setText(""); // Clear input field for next question
                    resetViewForNextQuestion(); // Reset the views

                    if (currentItemIndex == itemsPerLesson) {
                        showCompletionPopup(); // Show completion popup
                    } else {
                        loadNextQuestion(); // Load next question
                    }
                }, 1000);
            } else {
                handleIncorrectAnswer(userInput); // Handle the incorrect answer
            }
        }
    }

    private void handleIncorrectAnswer(String userInput) {
        txtUserInput.setBackgroundColor(getResources().getColor(R.color.red));
        Log.d(TAG, "Incorrect answer. User input: " + userInput + ", Correct answer: " + romaji);

        saveIncorrectAnswer(currentCharacter, userInput);


        // Disable further input entirely
        txtUserInput.setEnabled(false);
        txtUserInput.setFocusable(false);  // Make sure the input is not focusable
        txtUserInput.setInputType(0);  // Disable input type (prevents typing)

        // Enable the buttons to reveal information
        btnTestName.setEnabled(true);
        btnTestExample.setEnabled(true);

        // Enable btnNext to allow the user to proceed to the next question


        // Set click listeners to reveal the hidden TextViews when buttons are clicked
        btnTestName.setOnClickListener(v -> {
            setNameOptionActive();
        });

        btnTestExample.setOnClickListener(v -> {
            setExampleOptionActive();
        });

        btnNext.setEnabled(true);
        btnNext.setOnClickListener(v->{
            txtUserInput.postDelayed(() -> {
                txtUserInput.setBackgroundColor(getResources().getColor(android.R.color.white));
                txtUserInput.setText(""); // Clear input field for next question
                resetViewForNextQuestion(); // Reset the views
                txtUserInput.setEnabled(true);
                txtUserInput.setFocusable(true);  // Make sure the input is focusable
                txtUserInput.setInputType(1);  // Enable input type (prevents typing)

                if (currentItemIndex == itemsPerLesson) {
                    showCompletionPopup(); // Show completion popup
                } else {
                    loadNextQuestion(); // Load next question
                }
            }, 1000);

        });

    }

    private void resetViewForNextQuestion() {
        // Reset the TextViews to hidden
        tvOp1.setVisibility(View.GONE);
        tvOp2.setVisibility(View.GONE);
        tvDesc1.setVisibility(View.GONE);
        tvDesc2.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);

        // Disable buttons
        btnTestName.setEnabled(false);
        btnTestExample.setEnabled(false);

        // Enable input for next question
        txtUserInput.setEnabled(true);
        txtUserInput.setBackgroundColor(getResources().getColor(android.R.color.white));

        // Disable btnNext until the answer is verified
        btnNext.setEnabled(false);
    }

    private void loadNextQuestion() {
        if (currentItemIndex < itemsPerLesson + 1) {
            currentItemIndex++;
            resetViewForNextQuestion(); // Reset the UI for the new question
            loadLesson(currentLesson, currentItemIndex);
        } else {
            showCompletionPopup(); // Show completion popup
        }
    }


    private void showCompletionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Congrats! You've completed all items!")
                .setPositiveButton("OK", (dialog, id) -> finish());
        builder.create().show();
    }

    private void saveIncorrectAnswer(String character, String userInput) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userIncorrectAnswersRef = database.child("users").child(userId).child("incorrectAnswers");
        userIncorrectAnswersRef.child(character).setValue(userInput)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Incorrect answer saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving incorrect answer", e));
    }
}
