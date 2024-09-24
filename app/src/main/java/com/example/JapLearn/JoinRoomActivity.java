package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private DatabaseReference gameRoomRef;
    private String gameCode;

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
                gameCode = gameCodeEditText.getText().toString().trim();
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
                    // Game code exists, check the game state
                    String gameState = dataSnapshot.child("gameState").getValue(String.class);
                    Boolean gameStarted = dataSnapshot.child("gameStarted").getValue(Boolean.class);

                    if ("waiting".equals(gameState) && (gameStarted == null || !gameStarted)) {
                        // Game is waiting and not started
                        String creatorUsername = dataSnapshot.child("creator").getValue(String.class);
                        if (creatorUsername != null) {
                            String userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
                            userModel.getUserRef(userId).child("teacherEmail").setValue(creatorUsername).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Join the game room and start listening for game start
                                    gameRoomRef = gameRoomsReference.child(gameCode);
                                    addUserToGameRoom(userId);
                                    listenForGameStart();

                                    // Start the CreateRoomActivity
                                    Intent intent = new Intent(JoinRoomActivity.this, CreateRoomActivity.class);
                                    intent.putExtra("GAME_CODE", gameCode);
                                    startActivity(intent);
                                    finish(); // Optional: Finish the JoinRoomActivity if you don't want to keep it in the back stack
                                } else {
                                    Toast.makeText(JoinRoomActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(JoinRoomActivity.this, "Failed to retrieve creator username.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Game has already started or is in a state that does not allow joining
                        Toast.makeText(JoinRoomActivity.this, "Sorry, the game has already started.", Toast.LENGTH_SHORT).show();
                    }
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

    private void addUserToGameRoom(String userId) {
        gameRoomRef.child("players").child(userId).setValue(userModel.getFirebaseAuth().getCurrentUser().getDisplayName()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User added to game room successfully
                Toast.makeText(JoinRoomActivity.this, "Joined game room successfully.", Toast.LENGTH_SHORT).show();
            } else {
                // Failed to add user to game room
                Toast.makeText(JoinRoomActivity.this, "Failed to join game room.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForGameStart() {
        gameRoomRef.child("gameStarted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean gameStarted = snapshot.getValue(Boolean.class);
                if (gameStarted != null && gameStarted) {
                    Intent intent = new Intent(JoinRoomActivity.this, MultiplayerGameActivity.class);
                    intent.putExtra("GAME_CODE", gameCode);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JoinRoomActivity", "Error listening for game start: " + error.getMessage());
            }
        });
    }
}

