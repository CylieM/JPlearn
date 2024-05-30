package com.example.jlearnn;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.widget.EditText;



public class CreateRoomActivity extends AppCompatActivity {

    private TextView gameCodeTextView;
    private Button startButton;
    private ListView usersListView;
    private ArrayAdapter<String> usersAdapter;
    private List<String> usersList = new ArrayList<>();
    private DatabaseReference gameRoomRef;
    private String gameCode;
    private String userId;


    private EditText timerInput;
    private Spinner sentencesSpinner;
    private Spinner charactersSpinner;
    private Button randomButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        gameCodeTextView = findViewById(R.id.gameCodeTextView);
        startButton = findViewById(R.id.startButton);
        usersListView = findViewById(R.id.usersListView);
        usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        usersListView.setAdapter(usersAdapter);
        timerInput = findViewById(R.id.timerInput);
        sentencesSpinner = findViewById(R.id.sentencesSpinner);
        charactersSpinner = findViewById(R.id.charactersSpinner);
        randomButton = findViewById(R.id.randomButton);
        gameCode = getIntent().getStringExtra("GAME_CODE");
        if (gameCode == null) {
            gameCode = generateGameCode();
        }
        gameCodeTextView.setText("Game Code: " + gameCode);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        gameRoomRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("gameRooms").child(gameCode);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        // Fetch and add the current user to the game room
        fetchAndAddCurrentUser();

        // Listen for game room updates
        listenForGameRoomUpdates();


        EditText timerInput = findViewById(R.id.timerInput);
        timerInput.setFilters(new InputFilter[]{ new CreateRoomInputfilter("1", "300") });

        // Initialize the sentences spinner
        Spinner sentencesSpinner = findViewById(R.id.sentencesSpinner);
        ArrayAdapter<CharSequence> sentencesAdapter = ArrayAdapter.createFromResource(this, R.array.sentences_array, android.R.layout.simple_spinner_item);
        sentencesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sentencesSpinner.setAdapter(sentencesAdapter);

        // Initialize the characters spinner
        Spinner charactersSpinner = findViewById(R.id.charactersSpinner);
        ArrayAdapter<CharSequence> charactersAdapter = ArrayAdapter.createFromResource(this, R.array.characters_array, android.R.layout.simple_spinner_item);
        charactersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        charactersSpinner.setAdapter(charactersAdapter);

        // Initialize the random button
        Button randomButton = findViewById(R.id.randomButton);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Randomize the timer, sentences, and characters
                Random random = new Random();
                timerInput.setText(String.valueOf(random.nextInt(300) + 1));
                sentencesSpinner.setSelection(random.nextInt(sentencesSpinner.getCount()));
                charactersSpinner.setSelection(random.nextInt(charactersSpinner.getCount()));
            }
        });
    }

    private void fetchAndAddCurrentUser() {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                if (username != null) {
                    addUserToGameRoom(username);

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
      
            }
        });
    }

    private void addUserToGameRoom(String username) {
        gameRoomRef.child("players").child(userId).setValue(username);
  
    }
    // Initialize the timer input


    private void listenForGameRoomUpdates() {
        gameRoomRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Existing code to update usersList
                usersList.clear(); // Clear the list to prevent duplication
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getValue(String.class);
                    if (username != null) {
                        usersList.add(username);
                    }
                }
                usersAdapter.notifyDataSetChanged();

                // Check if the game has started
                gameRoomRef.child("gameStarted").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot gameStartedSnapshot) {
                        Boolean gameStarted = gameStartedSnapshot.getValue(Boolean.class);
                        if (gameStarted != null && gameStarted) {
                            // Navigate to the game activity
                            Intent intent = new Intent(CreateRoomActivity.this, MultiplayerGameActivity.class);
                            intent.putExtra("GAME_CODE", gameCode);
                            // Pass any other necessary data
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
           
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
        
            }
        });
    }


    private void startGame() {
        // Apply the options here
        int timer = Integer.parseInt(timerInput.getText().toString());
        int sentences = Integer.parseInt(sentencesSpinner.getSelectedItem().toString());
        String characters = charactersSpinner.getSelectedItem().toString();

        // Set the game state to "started"
        gameRoomRef.child("gameState").setValue("started");

        // Broadcast a message to all players (e.g., set a "gameStarted" flag)
        gameRoomRef.child("gameStarted").setValue(true);

        // Navigate to the game activity
        Intent intent = new Intent(CreateRoomActivity.this, MultiplayerGameActivity.class);
        intent.putExtra("GAME_CODE", gameCode);
        // Pass the options to the game activity
        intent.putExtra("TIMER", timer);
        intent.putExtra("CATEGORY", characters);
        intent.putExtra("SENTENCES", sentences);
        startActivity(intent);
    }



    private String generateGameCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder gameCode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            gameCode.append(characters.charAt(random.nextInt(characters.length())));
        }
        return gameCode.toString();
    }

    private void checkUserRole() {
        DatabaseReference userRoleRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId)
                .child("role");

        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userRole = snapshot.getValue(String.class);
                if ("student".equals(userRole)) {
                    // Hide UI elements for students
                    startButton.setVisibility(View.GONE);
                    timerInput.setVisibility(View.GONE);
                    sentencesSpinner.setVisibility(View.GONE);
                    charactersSpinner.setVisibility(View.GONE);
                    randomButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameRoomActivity", "Error fetching user role: " + error.getMessage());
            }
        });
    }



}


