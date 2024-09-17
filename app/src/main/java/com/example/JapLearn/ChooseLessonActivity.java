package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseLessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lesson);

        Button btnLesson1 = findViewById(R.id.btnLesson1);
        Button btnLesson2 = findViewById(R.id.btnLesson2);
        Button btnLesson3 = findViewById(R.id.btnLesson3);



        btnLesson3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLessonActivity.this, LessonGrammarIntroActivity.class);
                startActivity(intent);
            }
        });
    }
}