package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinRoomActivity extends AppCompatActivity {

    private EditText gameCodeEditText;
    private Button clearButton, enterButton;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        gameCodeEditText = findViewById(R.id.editTextGameCode);
        clearButton = findViewById(R.id.btnClear);
        enterButton = findViewById(R.id.btnEnter);

        userModel = new UserModel();

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
                    checkGameCodeExists(gameCode);
                }
            }
        });
    }

    private void checkGameCodeExists(String gameCode) {
        DatabaseReference gameRoomsReference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("gameRooms");

        gameRoomsReference.child(gameCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Game code exists
                    String userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
                    userModel.getUserRef(userId).child("joinedRoomCode").setValue(1).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(JoinRoomActivity.this, CreateRoomActivity.class);
                            intent.putExtra("GAME_CODE", gameCode);
                            startActivity(intent);
                        } else {
                            Toast.makeText(JoinRoomActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Game code does not exist
                    Toast.makeText(JoinRoomActivity.this, "Game code does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(JoinRoomActivity.this, "Error checking game code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
