package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class JoinRoomActivity extends AppCompatActivity {

    private EditText gameCodeEditText;
    private Button clearButton, enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        gameCodeEditText = findViewById(R.id.editTextGameCode);
        clearButton = findViewById(R.id.btnClear);
        enterButton = findViewById(R.id.btnEnter);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameCodeEditText.setText("");
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gameCode = gameCodeEditText.getText().toString().trim();
                if (!gameCode.isEmpty()) {
                    Intent intent = new Intent(JoinRoomActivity.this, GameRoomActivity.class);
                    intent.putExtra("GAME_CODE", gameCode);
                    startActivity(intent);
                }
            }
        });
    }
}
