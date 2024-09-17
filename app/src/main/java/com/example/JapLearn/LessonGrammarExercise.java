package com.example.JapLearn;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonGrammarExercise extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private TextView tvSentence, tvSecondSentence;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private Animation scaleUp, scaleDown;
    private int currentQuestionIndex = 1;
    private int correctCount = 0;
    private static final int TOTAL_QUESTIONS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_grammar_exercise);

        // Initialize Firebase database reference
        databaseRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Lessons").child("questions");

        // Initialize UI components
        tvSentence = findViewById(R.id.tvSentence);
        tvSecondSentence = findViewById(R.id.tvSecondSentence);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);

        // Initialize animations
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        // Load and display the first question
        loadQuestion(currentQuestionIndex);
    }

    private void loadQuestion(int questionIndex) {
        databaseRef.child(String.valueOf(questionIndex)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("LessonGrammarExercise", "Loading question " + questionIndex);

                    String sentence = snapshot.child("sentence").getValue(String.class);
                    String translation = snapshot.child("translation").getValue(String.class);
                    String choice1 = snapshot.child("choice1").getValue(String.class);
                    String choice2 = snapshot.child("choice2").getValue(String.class);
                    String choice3 = snapshot.child("choice3").getValue(String.class);
                    String choice4 = snapshot.child("choice4").getValue(String.class);
                    String correctAnswer = snapshot.child("correct").getValue(String.class);

                    // Populate UI elements with fetched data
                    tvSentence.setText(sentence);
                    tvSecondSentence.setText(translation);
                    btnOption1.setText(choice1);
                    btnOption2.setText(choice2);
                    btnOption3.setText(choice3);
                    btnOption4.setText(choice4);

                    // Attach click listeners to buttons
                    btnOption1.setOnClickListener(view -> checkAnswer(choice1, correctAnswer, snapshot.child(choice1).getValue(String.class)));
                    btnOption2.setOnClickListener(view -> checkAnswer(choice2, correctAnswer, snapshot.child(choice2).getValue(String.class)));
                    btnOption3.setOnClickListener(view -> checkAnswer(choice3, correctAnswer, snapshot.child(choice3).getValue(String.class)));
                    btnOption4.setOnClickListener(view -> checkAnswer(choice4, correctAnswer, snapshot.child(choice4).getValue(String.class)));
                } else {
                    Log.e("LessonGrammarExercise", "Question " + questionIndex + " does not exist in Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LessonGrammarExercise", "Firebase database error: " + error.getMessage());
            }
        });
    }

    private void checkAnswer(String selectedAnswer, String correctAnswer, String feedback) {
        boolean isCorrect = selectedAnswer.equals(correctAnswer);

        // Apply animation to button
        Button selectedButton = null;
        if (selectedAnswer.equals(btnOption1.getText().toString())) {
            selectedButton = btnOption1;
        } else if (selectedAnswer.equals(btnOption2.getText().toString())) {
            selectedButton = btnOption2;
        } else if (selectedAnswer.equals(btnOption3.getText().toString())) {
            selectedButton = btnOption3;
        } else if (selectedAnswer.equals(btnOption4.getText().toString())) {
            selectedButton = btnOption4;
        }

        if (selectedButton != null) {
            selectedButton.startAnimation(scaleUp);
            selectedButton.startAnimation(scaleDown);
        }

        // Show dialog for correct/incorrect answer
        showAnswerDialog(isCorrect, selectedAnswer, feedback);
    }

    private void showAnswerDialog(boolean isCorrect, String selectedAnswer, String feedback) {
        String message = isCorrect ? "You got the correct answer!" : feedback;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCorrect ? "Correct Answer!" : "Incorrect Answer");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Increment correct count if answer is correct
                if (isCorrect) {
                    correctCount++;
                }

                // Proceed to next question or show results
                if (currentQuestionIndex < TOTAL_QUESTIONS) {
                    currentQuestionIndex++;  // Increment index
                    loadQuestion(currentQuestionIndex); // Load next question
                } else {
                    showResults();
                }
            }
        });
        builder.setCancelable(false); // Prevent dialog from being dismissed on outside touch
        builder.show();
    }

    private void showResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Results");
        builder.setMessage("Congratulations! You got " + correctCount + " out of " + TOTAL_QUESTIONS + " right!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Navigate back to ChooseLessonActivity
                // Replace with your navigation logic
                // Intent intent = new Intent(LessonGrammarExercise.this, ChooseLessonActivity.class);
                // startActivity(intent);
                finish(); // Close current activity
            }
        });
        builder.setCancelable(false); // Prevent dialog from being dismissed on outside touch
        builder.show();
    }
}
