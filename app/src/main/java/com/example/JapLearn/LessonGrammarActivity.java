package com.example.JapLearn;

// Import necessary Firebase libraries
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LessonGrammarActivity extends AppCompatActivity {

    private List<Lesson> lessonList = new ArrayList<>();
    private int currentIndex = 0;
    private Animation scaleUp, scaleDown;
    private TextView Particle, dataRomaji, dataDesc;
    private Button btndataExample;
    private ImageButton btnNext, btnPrev;
    private MediaPlayer mediaPlayer;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_grammar);

        Particle = findViewById(R.id.Particle);
        dataRomaji = findViewById(R.id.dataRomaji);
        dataDesc = findViewById(R.id.dataDesc);
        btndataExample = findViewById(R.id.btndataExample);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        mediaPlayer = new MediaPlayer();
        storage = FirebaseStorage.getInstance();

        fetchLessons();

        btnNext.setOnClickListener(v -> showNextLesson());
        btnPrev.setOnClickListener(v -> showPreviousLesson());
        btndataExample.setOnClickListener(v -> {
            playAudioExample();
            applyButtonAnimation(btndataExample);
        });
    }

    private void fetchLessons() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference ref = database.getReference("Lessons/4");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lessonList.clear();
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()) {
                    Lesson lesson = lessonSnapshot.getValue(Lesson.class);
                    lessonList.add(lesson);
                }
                if (!lessonList.isEmpty()) {
                    displayLesson(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("LessonGrammarActivity", "Error fetching data", databaseError.toException());
            }
        });
    }

    private void displayLesson(int index) {
        if (index >= 0 && index < lessonList.size()) {
            Lesson lesson = lessonList.get(index);
            Log.d("LessonGrammarActivity", "Particle: " + lesson.getParticle());
            Particle.setText(lesson.getParticle());
            dataRomaji.setText(lesson.getDataRomaji());
            dataDesc.setText(lesson.getDataDesc());
            String exampleText = lesson.getDataExampleJp() + "\n" + lesson.getDataExampleEn();
            btndataExample.setText(exampleText);
        }
    }


    private void showNextLesson() {
        if (currentIndex < lessonList.size() - 1) {
            currentIndex++;
            displayLesson(currentIndex);
        } else {
            // Redirect to the exercise activity
            Intent intent = new Intent(LessonGrammarActivity.this, LessonGrammarExercise.class);
            startActivity(intent);
        }
    }

    private void showPreviousLesson() {
        if (currentIndex > 0) {
            currentIndex--;
            displayLesson(currentIndex);
        }
    }

    private void playAudioExample() {
        if (currentIndex >= 0 && currentIndex < lessonList.size()) {
            String audioFileName = "4_" + (currentIndex + 1) + ".m4a";
            StorageReference audioRef = storage.getReference().child("grammar/" + audioFileName);

            audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(uri.toString());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(LessonGrammarActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(exception -> {
                // Handle errors
                Toast.makeText(LessonGrammarActivity.this, "Audio file not found", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void applyButtonAnimation(Button button) {
        button.startAnimation(scaleUp);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                button.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    // Define the Lesson class as an inner class
    public static class Lesson {
        private String Particle;
        private String dataRomaji;
        private String dataDesc;
        private String dataExampleJp;
        private String dataExampleEn;

        public Lesson() {
            // Default constructor required for calls to DataSnapshot.getValue(Lesson.class)
        }

        public String getParticle() {
            return Particle;
        }

        public void setParticle(String Particle) {
            this.Particle = Particle;
        }

        public String getDataRomaji() {
            return dataRomaji;
        }

        public void setDataRomaji(String dataRomaji) {
            this.dataRomaji = dataRomaji;
        }

        public String getDataDesc() {
            return dataDesc;
        }

        public void setDataDesc(String dataDesc) {
            this.dataDesc = dataDesc;
        }

        public String getDataExampleJp() {
            return dataExampleJp;
        }

        public void setDataExampleJp(String dataExampleJp) {
            this.dataExampleJp = dataExampleJp;
        }

        public String getDataExampleEn() {
            return dataExampleEn;
        }

        public void setDataExampleEn(String dataExampleEn) {
            this.dataExampleEn = dataExampleEn;
        }
    }
}
