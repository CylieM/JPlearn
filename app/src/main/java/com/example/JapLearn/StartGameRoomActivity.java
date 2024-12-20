package com.example.JapLearn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartGameRoomActivity extends AppCompatActivity {

    private TextView lobbyStatusTextView;
    private ListView usersListView;
    private ArrayAdapter<String> usersAdapter;
    private List<String> usersList = new ArrayList<>();
    private DatabaseReference lobbyRef;
    private DatabaseReference userRef;
    private String userId;
    private CountDownTimer lobbyTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game_room);

        initializeUI();
        initializeFirebase();

        String lobbyId = getIntent().getStringExtra("LOBBY_ID");
        if (lobbyId != null) {
            joinLobby(lobbyId);
        } else {
            checkOrCreateLobby();
        }
        showDirectionsDialog();
    }

    private void initializeUI() {
        lobbyStatusTextView = findViewById(R.id.lobbyStatusTextView);
        usersListView = findViewById(R.id.usersListView);

        // Create a custom ArrayAdapter to set text color
        usersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usersList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the default view
                View view = super.getView(position, convertView, parent);
                // Get the TextView from the view and set its color
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK); // Set the text color to black
                return view;
            }
        };

        usersListView.setAdapter(usersAdapter);
    }


    private void initializeFirebase() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        lobbyRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("lobbies");
        userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId);
    }

    private void checkOrCreateLobby() {
        lobbyRef.orderByChild("gameState").equalTo("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean waitingLobbyFound = false;

                if (snapshot.exists()) {
                    for (DataSnapshot lobbySnapshot : snapshot.getChildren()) {
                        String lobbyId = lobbySnapshot.getKey();
                        String gameState = lobbySnapshot.child("gameState").getValue(String.class);

                        if (lobbyId != null && "waiting".equals(gameState)) {
                            joinLobby(lobbyId);
                            waitingLobbyFound = true;
                            break;
                        }
                    }
                }

                if (!waitingLobbyFound) {
                    createLobby();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StartGameRoomActivity", "Error checking lobbies: " + error.getMessage());
            }
        });
    }

    private void joinLobby(String lobbyId) {
        lobbyRef = lobbyRef.child(lobbyId);
        lobbyRef.child("gameState").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String gameState = snapshot.getValue(String.class);
                if ("started".equals(gameState) || "ongoing".equals(gameState) || "finished".equals(gameState)) {
                    // Lobby has already started, is ongoing, or finished, create a new lobby
                    // Reset lobbyRef to the main lobbies reference
                    lobbyRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("lobbies");
                    createLobby();
                } else {
                    // Lobby is waiting, join it
                    lobbyStatusTextView.setText("Joined lobby: " + lobbyId);
                    addUserToLobby(lobbyId);
                    listenForLobbyUpdates();
                    syncLobbyTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StartGameRoomActivity", "Error joining lobby: " + error.getMessage());
            }
        });
    }


    private void createLobby() {
        String newLobbyId = lobbyRef.push().getKey();
        if (newLobbyId != null) {
            lobbyRef = lobbyRef.child(newLobbyId);
            lobbyRef.child("creator").setValue(userId);
            lobbyRef.child("gameState").setValue("waiting"); // Set default game state

            // Randomly select values for characters and sentences
            String[] charactersOptions = {"Hiragana", "Mixed"};
            int[] sentencesOptions = {1, 2};

            Random random = new Random();
            String randomCharacter = charactersOptions[random.nextInt(charactersOptions.length)];
            int randomSentence = sentencesOptions[random.nextInt(sentencesOptions.length)];

            // Store the randomly selected values in the lobby
            lobbyRef.child("characters").setValue(randomCharacter);
            lobbyRef.child("sentences").setValue(randomSentence);

            lobbyStatusTextView.setText("Created lobby: " + newLobbyId);
            lobbyStatusTextView.setTextColor(Color.BLACK);
            addUserToLobby(newLobbyId);
            listenForLobbyUpdates();
            lobbyRef.child("startTime").setValue(System.currentTimeMillis());
            syncLobbyTimer();
            // Start a timer to delete the lobby after 2 minutes
            new CountDownTimer(120000, 1000) { // 120000 ms = 2 minutes
                @Override
                public void onTick(long millisUntilFinished) {
                    // Optional: Update status if needed (e.g., countdown timer)
                }

                @Override
                public void onFinish() {
                    lobbyRef.removeValue(); // Automatically delete the lobby
                    Log.d("StartGameRoomActivity", "Lobby " + newLobbyId + " has been destroyed due to inactivity.");
                }
            }.start();
        }
    }

    private void addUserToLobby(String lobbyId) {
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                if (username != null) {
                    lobbyRef.child("players").child(userId).setValue(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StartGameRoomActivity", "Error adding user to lobby: " + error.getMessage());
            }
        });
    }

    private void listenForLobbyUpdates() {
        lobbyRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getValue(String.class);
                    if (username != null) {
                        usersList.add(username);
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StartGameRoomActivity", "Error listening for lobby updates: " + error.getMessage());
            }
        });
    }

    private void syncLobbyTimer() {
        lobbyRef.child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long startTime = snapshot.getValue(Long.class);
                if (startTime != null) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long remainingTime = 30000 - elapsedTime;
                    if (remainingTime > 0) {
                        startLobbyTimer(remainingTime);
                    } else {
                        startMultiplayerGame();
                    }
                } else {
                    startLobbyTimer(30000); // Default to 30 seconds if start time is not set
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StartGameRoomActivity", "Error syncing lobby timer: " + error.getMessage());
            }
        });
    }

    private void startLobbyTimer(long durationMillis) {
        if (lobbyTimer != null) {
            lobbyTimer.cancel();
        }
        lobbyTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                lobbyStatusTextView.setText("Game starting in: " + millisUntilFinished / 1000 + " seconds");
                lobbyStatusTextView.setTextColor(Color.BLACK);
            }

            @Override
            public void onFinish() {
                startMultiplayerGame();
            }
        }.start();
    }

    private void startMultiplayerGame() {
        lobbyRef.child("gameState").setValue("started");
        Intent intent = new Intent(StartGameRoomActivity.this, StartMultiplayerGameActivity.class);
        intent.putExtra("LOBBY_ID", lobbyRef.getKey());
        startActivity(intent);
        finish();
    }
    private void showDirectionsDialog() {
        SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        boolean dontShowAgain = prefs.getBoolean("dont_show_directions", false);

        if (!dontShowAgain) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialogue_direction, null);
            CheckBox dontShowAgainCheckbox = dialogView.findViewById(R.id.checkbox_dont_show_again);

            new AlertDialog.Builder(this)
                    .setTitle("Directions")
                    .setView(dialogView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (dontShowAgainCheckbox.isChecked()) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("dont_show_directions", true);
                                editor.apply();
                            }
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }
}
