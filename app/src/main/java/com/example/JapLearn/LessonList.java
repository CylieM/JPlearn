package com.example.JapLearn;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class LessonList extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private DatabaseReference userRef;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    // Lesson buttons
    private LinearLayout lessonHiragana, lessonKatakana, lessonVocabulary, lessonGrammar;
    private ImageView lockKata, lockVocab, lockGrammar;
    private ImageButton btnInstruction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lesson_list);


        // Find the instruction button by its ID
        btnInstruction = findViewById(R.id.btn_instruction);

        // Set an OnClickListener for the instruction button
        btnInstruction.setOnClickListener(v -> showInstructionDialog());

        // Initialize lesson layouts
        lessonHiragana = findViewById(R.id.lessonHiragana);
        lessonKatakana = findViewById(R.id.lessonKatakana);
        lessonVocabulary = findViewById(R.id.lessonVocabulary);
        lessonGrammar = findViewById(R.id.lessonGrammar);

        // Initialize lock icons
        lockKata = findViewById(R.id.lock_kata);
        lockVocab = findViewById(R.id.lock_vocab);
        lockGrammar = findViewById(R.id.lock_grammar);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance(); // FirebaseStorage instance
        String userId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Initialize lesson buttons
        lessonHiragana = findViewById(R.id.lessonHiragana);
        lessonKatakana = findViewById(R.id.lessonKatakana);
        lessonVocabulary = findViewById(R.id.lessonVocabulary);
        lessonGrammar = findViewById(R.id.lessonGrammar);

        // Fetch user data from Firebase Realtime Database
        checkUserRole();
    }

    private void checkUserRole() {
        // Fetch the user's role from Firebase
        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    // If Teacher or Admin, enable all lessons immediately
                    if (role.equals("Teacher") || role.equals("Admin")) {
                        enableAllLessons();
                    } else if (role.equals("Student")) {
                        // If Student, fetch current lesson and enable accordingly
                        fetchCurrentLesson();
                    }
                } else {
                    Log.e("LessonList", "No role found for user.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LessonList", "Error fetching user role", databaseError.toException());
            }
        });
    }

    private void fetchCurrentLesson() {
        // Fetch current lesson from the user's data in the database (only for students)
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the current lesson from the database
                    String currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);
                    if (currentLesson != null) {
                        countLessonItems(currentLesson);
                    } else {
                        // Handle the case where the currentLesson is not found
                        Log.e("LessonList", "No current lesson found.");
                    }
                } else {
                    Log.e("LessonList", "User data does not exist for userId: " + auth.getCurrentUser().getUid());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
                Log.e("LessonList", "Error fetching user data", databaseError.toException());
            }
        });
    }

    private void countLessonItems(String currentLesson) {
        // Depending on the current lesson, update access to lessons accordingly
        updateLessonAccess("Student", Integer.parseInt(currentLesson));
    }

//    private void updateLessonAccess(String role, int currentLesson) {
//        if (role.equals("Teacher") || role.equals("Admin")) {
//            // This block is no longer needed, handled in checkUserRole() directly
//            enableAllLessons();
//        } else if (role.equals("Student")) {
//            // If the user is a student, check the current lesson progress
//            if (currentLesson == 1) {
//                enableLessons(lessonHiragana);
//                disableLessons(lessonKatakana, lessonVocabulary, lessonGrammar);
//            } else if (currentLesson == 2) {
//                enableLessons(lessonHiragana, lessonKatakana);
//                disableLessons(lessonVocabulary, lessonGrammar);
//            } else if (currentLesson == 3) {
//                enableLessons(lessonHiragana, lessonKatakana, lessonVocabulary);
//                disableLessons(lessonGrammar);
//            } else {
//                enableAllLessons();
//            }
//        }
//    }


    // Update to handle lesson access and dynamic UI changes
    private void updateLessonAccess(String role, int currentLesson) {
        if (role.equals("Teacher") || role.equals("Admin")) {
            enableAllLessons();  // Enable click events for all lessons
        } else if (role.equals("Student")) {
            // Update based on current lesson progress
            if (currentLesson == 1) {
                updateLessonUI(lessonHiragana, null, null, null, "In-Progress");
                updateLessonUI(lessonKatakana, lockKata, lessonKatakana, null, "Locked");
                updateLessonUI(lessonVocabulary, lockVocab, lessonVocabulary, null, "Locked");
                updateLessonUI(lessonGrammar, lockGrammar, lessonGrammar, null, "Locked");

                lessonHiragana.setEnabled(true);
                lessonKatakana.setEnabled(false);
                lessonVocabulary.setEnabled(false);
                lessonGrammar.setEnabled(false);

                setOnClickListeners();

            } else if (currentLesson == 2) {
                updateLessonUI(lessonHiragana, null, null, null, "Completed");
                updateLessonUI(lessonKatakana, null, lessonKatakana, lockKata, "In-Progress");
                updateLessonUI(lessonVocabulary, lockVocab, lessonVocabulary, null, "Locked");
                updateLessonUI(lessonGrammar, lockGrammar, lessonGrammar, null, "Locked");

                lessonHiragana.setEnabled(true);
                lessonKatakana.setEnabled(true);
                lessonVocabulary.setEnabled(false);
                lessonGrammar.setEnabled(false);

                setOnClickListeners();

            } else if (currentLesson == 3) {
                updateLessonUI(lessonHiragana, null, null, null, "Completed");
                updateLessonUI(lessonKatakana, null, lessonKatakana, lockKata, "Completed");
                updateLessonUI(lessonVocabulary, null, lessonVocabulary, lockVocab, "In-Progress");
                updateLessonUI(lessonGrammar, lockGrammar, lessonGrammar, null, "Locked");

                lessonHiragana.setEnabled(true);
                lessonKatakana.setEnabled(true);
                lessonVocabulary.setEnabled(true);
                lessonGrammar.setEnabled(false);

                setOnClickListeners();

            } else {
                updateLessonUI(lessonHiragana, null, null, null, "Completed");
                updateLessonUI(lessonKatakana, null, lessonKatakana, lockKata, "Completed");
                updateLessonUI(lessonVocabulary, null, lessonVocabulary, lockVocab, "Completed");
                updateLessonUI(lessonGrammar, null, lessonGrammar, lockGrammar, "In-Progress");

                lessonHiragana.setEnabled(true);
                lessonKatakana.setEnabled(true);
                lessonVocabulary.setEnabled(true);
                lessonGrammar.setEnabled(true);

                setOnClickListeners();
            }
        }
    }

    private void updateLessonUI(LinearLayout lessonLayout, ImageView lockIcon, LinearLayout lockLayout, ImageView unlockIcon, String status) {
        if ("Locked".equals(status)) {
            // Set locked background and show lock icon
            lessonLayout.setBackgroundResource(R.drawable.locked_content_roundbg);
            lessonLayout.setClickable(false);  // Disable click for locked lessons
            if (lockIcon != null) lockIcon.setVisibility(View.VISIBLE);

            // Remove margin if Locked
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lessonLayout.getLayoutParams();
            params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin); // Remove left margin
            lessonLayout.setLayoutParams(params);
        } else {
            // Set completed/in-progress background and hide lock icon
            lessonLayout.setBackgroundResource(R.drawable.custom_round_background);
            lessonLayout.setClickable(true);  // Enable click for unlocked lessons
            if (unlockIcon != null) unlockIcon.setVisibility(View.GONE);

            // Add marginLeft for Completed or In-progress status
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lessonLayout.getLayoutParams();
            params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin); // Add 35dp margin on the left
            lessonLayout.setLayoutParams(params);

            // Set click listener for In-Progress or Completed lessons
            lessonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle lesson click event here (navigate to lesson details, etc.)
                    // Example: goToLessonDetails(lessonLayout);
                    Toast.makeText(lessonLayout.getContext(), "Lesson clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void enableAllLessons() {
        lessonHiragana.setEnabled(true);
        lessonKatakana.setEnabled(true);
        lessonVocabulary.setEnabled(true);
        lessonGrammar.setEnabled(true);

        setOnClickListeners();
    }

    private void enableLessons(LinearLayout... lessons) {
        for (LinearLayout lesson : lessons) {
            lesson.setEnabled(true);
        }

        setOnClickListeners();
    }

    private void disableLessons(LinearLayout... lessons) {
        for (LinearLayout lesson : lessons) {
            lesson.setEnabled(false);
        }
    }

    private void setOnClickListeners() {
        lessonHiragana.setOnClickListener(v -> startHiraganaIntro());
        lessonKatakana.setOnClickListener(v -> startKatakanaIntro());
        lessonVocabulary.setOnClickListener(v -> startVocabIntro());
        lessonGrammar.setOnClickListener(v -> startGrammarIntro());
    }

    private void startHiraganaIntro() {
        Intent intent = new Intent(LessonList.this, HiraganaIntro.class);
        startActivity(intent);
    }

    private void startKatakanaIntro() {
        Intent intent = new Intent(LessonList.this, KatakanaIntro.class);
        startActivity(intent);
    }

    private void startVocabIntro() {
        Intent intent = new Intent(LessonList.this, VocabIntro.class);
        startActivity(intent);
    }

    private void startGrammarIntro() {
        Intent intent = new Intent(LessonList.this, LessonGrammarIntroActivity.class);
        startActivity(intent);
    }


    private void showInstructionDialog() {
        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LessonList.this);

        // Set the message for the dialog
        builder.setMessage("You need to have a perfect score in the Test yourself to unlock the next lesson.\n\nTest yourself automatically tests you on your latest unlocked lesson.")
                .setCancelable(false)  // Make it non-cancellable (user can only dismiss by pressing the "Got it!" button)
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Close the dialog when the user clicks "Got it!"
                        dialog.dismiss();
                    }
                });

        // Create the dialog and show it
        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize the dialog's appearance
        dialog.getWindow().setLayout(700, 800);  // Set the dialog's size (can adjust as necessary)

        // Set rounded corners for the dialog
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.lesson_instruction));

        // Optionally change the message text color to gray
        ((android.widget.TextView) dialog.findViewById(android.R.id.message)).setTextColor(getResources().getColor(android.R.color.darker_gray));
    }
}