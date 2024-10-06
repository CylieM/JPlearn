package com.example.JapLearn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MultiplayerGameActivity extends AppCompatActivity {

    private TextView timerDisplay;
    private TextView textDisplay;
    private EditText textInput;
    private LinearLayout raceTrackContainer;
    private DatabaseReference gameRoomRef;
    private String gameCode;
    private String userId;
    private String roomOwnerId;
    private String currentParagraph;
    private String currentRomaji;
    private int currentIndex = 0;
    private long startTimeMillis;

    private CountDownTimer timer;

    private long interval = 1000; // 1 second

    private ParagraphSQLiteDB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        timerDisplay = findViewById(R.id.timerDisplay);
        textDisplay = findViewById(R.id.textDisplay);
        textInput = findViewById(R.id.textInput);
        raceTrackContainer = findViewById(R.id.raceTrackContainer);

        gameCode = getIntent().getStringExtra("GAME_CODE");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        gameRoomRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("gameRooms").child(gameCode);

        dbHelper = new ParagraphSQLiteDB(this);
        getRoomOwnerId();

        initializePlayers();
        listenForPlayerProgressUpdates();
        listenForGameConfiguration(); // Changed to fetch game configuration

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTyping(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void getRoomOwnerId() {
        gameRoomRef.child("roomOwner").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomOwnerId = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void initializePlayers() {
        gameRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerId = playerSnapshot.getKey();

                    // Retrieve user information using UserModel
                    UserModel userModel = new UserModel();
                    userModel.getUserRef(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            // Check if the user data exists
                            if (userSnapshot.exists()) {
                                // Create a User object from the snapshot
                                UserModel.User user = userSnapshot.getValue(UserModel.User.class);

                                // Extract username and profile picture URL
                                String username = user.getUsername();
                                String profilePictureUrl = user.getProfilePicture();

                                PlayerView playerView = new PlayerView(MultiplayerGameActivity.this);
                                playerView.setPlayerId(playerId);
                                playerView.setUsername(username);

                                // Set the profile image
                                playerView.setProfileImage(profilePictureUrl, MultiplayerGameActivity.this);

                                // Add the PlayerView to the container
                                raceTrackContainer.addView(playerView);
                            } else {
                                Log.d("MultiplayerGameActivity", "User not found for player ID: " + playerId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("MultiplayerGameActivity", "Error retrieving user data: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MultiplayerGameActivity", "Error loading players: " + error.getMessage());
            }
        });
    }



    private void listenForPlayerProgressUpdates() {
        gameRoomRef.child("progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerId = playerSnapshot.getKey();
                    Double progress = playerSnapshot.getValue(Double.class);
                    if (progress != null) {
                        updatePlayerView(playerId, progress);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MultiplayerGameActivity.this, "Failed to load player progress.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForGameConfiguration() {
        gameRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long timerSeconds = snapshot.child("timer").getValue(Long.class);
                    String category = snapshot.child("characters").getValue(String.class); // Adjust this based on your data structure
                    int sentences = snapshot.child("sentences").getValue(Integer.class);
                    startGame(timerSeconds, category, sentences);
                } else {
                    Toast.makeText(MultiplayerGameActivity.this, "Game configuration not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MultiplayerGameActivity.this, "Failed to load game configuration.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame(long timerSeconds, String category, int sentences) {
        dbHelper.logAllData();
        String[] paragraph = getParagraph(category, sentences);
        currentParagraph = paragraph[0].replace(" ", "");
        currentRomaji = paragraph[1].trim();
        textDisplay.setText(currentParagraph);
        textDisplay.setTextSize(24);
        startTimeMillis = System.currentTimeMillis();
        long startTime = timerSeconds * 1000; // Convert to milliseconds

        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(startTime, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerDisplay.setText("Time remaining: " + millisUntilFinished / 1000);
                timerDisplay.setTextColor(Color.BLACK); // Set timer text color to black
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private String[] getParagraph(String category, int sentences) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ParagraphSQLiteDB.TABLE_PARAGRAPHS + " WHERE " + ParagraphSQLiteDB.COLUMN_CATEGORY + " = ? AND " + ParagraphSQLiteDB.COLUMN_SENTENCES + " = ? ORDER BY RANDOM() LIMIT 1", new String[]{category, String.valueOf(sentences)});

        if (cursor.moveToFirst()) {
            int columnIndexParagraph = cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_PARAGRAPH);
            int columnIndexRomaji = cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_ROMAJI);
            String paragraph = cursor.getString(columnIndexParagraph);
            String romaji = cursor.getString(columnIndexRomaji);
            cursor.close();
            return new String[]{paragraph, romaji};
        } else {
            cursor.close();
            return new String[]{"", ""}; // Handle case where no data is found
        }
    }

    private void handleTyping(String typedText) {
        String[] romajiCharacters = currentRomaji.split(" ");
        if (currentIndex < romajiCharacters.length && typedText.equals(romajiCharacters[currentIndex])) {
            currentIndex++;
            textInput.setText("");
            updateWPM();

            Spannable spannable = new SpannableString(textDisplay.getText());
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), currentIndex - 1, currentIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textDisplay.setText(spannable);

            float progress = (float) currentIndex / romajiCharacters.length;
            updatePlayerProgress(progress);

        }
        textInput.setTextColor(Color.BLACK);
    }

    private void updatePlayerProgress(double progress) {
        gameRoomRef.child("progress").child(userId).setValue(progress);
        updatePlayerView(userId, progress);
    }

    private void updatePlayerView(String playerId, double progress) {
        for (int i = 0; i < raceTrackContainer.getChildCount(); i++) {
            View view = raceTrackContainer.getChildAt(i);
            if (view instanceof PlayerView) {
                PlayerView playerView = (PlayerView) view;
                if (playerView.getPlayerId().equals(playerId)) {
                    playerView.updateProgress(progress);
                }
            }
        }
    }

    private void updateWPM() {
        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        double minutes = elapsedTimeMillis / 60000.0;
        int typedCharacters = currentIndex / 5;
        double wpm = typedCharacters / minutes;
    }

    private void endGame() {
        timer.cancel();
        showFinishedDialog();
        setGameStateFinished();
    }
    private void setGameStateFinished() {
        // Set the game state to finished in the database
        gameRoomRef.child("gameState").setValue("finished");
    }
    private boolean isRoomOwner() {
        return userId.equals(roomOwnerId);
    }
    private void showFinishedDialog() {
        AtomicReference<String> firstPlacePlayerId = new AtomicReference<>(null);

        gameRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PlayerResult> playerResults = new ArrayList<>();

                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerId = playerSnapshot.getKey();
                    String username = playerSnapshot.getValue(String.class);

                    gameRoomRef.child("progress").child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot progressSnapshot) {
                            Double progress = progressSnapshot.getValue(Double.class);
                            Log.e("MultiplayerGameActivity", "Player ID: " + playerId + ", Progress: " + progress);
                            Double wpm = calculateWPM(progress);
                            // Update the user's best WPM if applicable

                            if (progress == null) {
                                progress = 0.00; // Default value if progress is null
                            }


                            if (wpm == null) {
                                wpm = 0.00; // Default value if WPM is null
                            }
                            updateBestWPM(playerId, wpm);

                            playerResults.add(new PlayerResult(username, progress, wpm));

                            // Check if this player reached first place
                            if (firstPlacePlayerId.get() == null || progress > playerResults.get(playerResults.size() - 1).progress) {
                                firstPlacePlayerId.set(playerId);
                            }

                            if (playerResults.size() == snapshot.getChildrenCount()) {
                                // Increment first-place wins if applicable
                                if (firstPlacePlayerId.get() != null) {
                                    incrementFirstPlace(firstPlacePlayerId.get());
                                }

                                displayFinishedDialog(playerResults);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateBestWPM(String userId, double wpm) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double currentBestWPM = snapshot.child("nraceBestWPM").getValue(Double.class);
                    if (wpm > currentBestWPM) {
                        userRef.child("nraceBestWPM").setValue(wpm);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void incrementFirstPlace(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId);
        userRef.child("nraceFirstPlace").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long currentFirstPlaceWins = snapshot.getValue(Long.class);
                    userRef.child("nraceFirstPlace").setValue(currentFirstPlaceWins + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    private Double calculateWPM(Double progress) {
        if (progress == null || progress == 0.00) {
            return 0.00; // Return default WPM if progress is null or zero
        }
        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        double minutes = elapsedTimeMillis / 60000.0;
        double words = progress * currentRomaji.split(" ").length;
        return words / minutes;
    }
    private void displayFinishedDialog(List<MultiplayerGameActivity.PlayerResult> playerResults) {
        Collections.sort(playerResults, new Comparator<MultiplayerGameActivity.PlayerResult>() {
            @Override
            public int compare(MultiplayerGameActivity.PlayerResult o1, MultiplayerGameActivity.PlayerResult o2) {
                return Double.compare(o2.progress, o1.progress);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiplayerGameActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_finished, null);
        builder.setView(dialogView);

        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        for (MultiplayerGameActivity.PlayerResult result : playerResults) {
            TableRow row = new TableRow(this);

            TextView usernameTextView = new TextView(this);
            usernameTextView.setText(result.username);
            usernameTextView.setPadding(4, 4, 4, 4);
            usernameTextView.setTextColor(Color.BLACK);
            row.addView(usernameTextView);

            TextView progressTextView = new TextView(this);
            progressTextView.setText(String.format("%.2f%%", result.progress * 100));
            progressTextView.setPadding(4, 4, 4, 4);
            progressTextView.setTextColor(Color.BLACK);
            row.addView(progressTextView);

            TextView wpmTextView = new TextView(this);
            wpmTextView.setText(String.format("%.2f", result.wpm));
            wpmTextView.setPadding(4, 4, 4, 4);
            wpmTextView.setTextColor(Color.BLACK);
            row.addView(wpmTextView);

            tableLayout.addView(row);
        }

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnShowAllLeaderboards = dialogView.findViewById(R.id.btnShowAllLeaderboards);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the super method to handle back press
                MultiplayerGameActivity.super.onBackPressed();

                // Create an intent to start HomeActivity
                Intent intent = new Intent(MultiplayerGameActivity.this, HomeActivity.class);
                startActivity(intent); // Start the new activity
                finish(); // Optionally call finish() to remove this activity from the back stack
            }
        });


        btnShowAllLeaderboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Dialog", "Show All Leaderboards Button Clicked");
                Intent intent = new Intent(MultiplayerGameActivity.this, NihonBoardActivity.class);
                startActivity(intent);
            }
        });

        builder.create().show();
    }

    public class PlayerView extends LinearLayout {

        private String playerId;

        private ImageView profileImageView;
        private TextView usernameTextView;
        private View progressBar;

        public PlayerView(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setPadding(16, 16, 16, 16); // Optional: add some padding for better aesthetics

            // Create a horizontal layout for the profile image and username
            LinearLayout playerInfoLayout = new LinearLayout(context);
            playerInfoLayout.setOrientation(HORIZONTAL);
            playerInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            playerInfoLayout.setGravity(Gravity.CENTER_VERTICAL); // Center vertically

            // Initialize and configure the profile ImageView
            profileImageView = new ImageView(context);
            int imageSize = 100; // Set your desired size
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            imageLayoutParams.setMargins(0, 0, 16, 3); // Optional: add margin to separate image and text
            profileImageView.setLayoutParams(imageLayoutParams);
            profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            playerInfoLayout.addView(profileImageView);

            // Initialize and configure the username TextView
            usernameTextView = new TextView(context);
            usernameTextView.setTextColor(Color.BLACK); // Set text color to black
            usernameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            playerInfoLayout.addView(usernameTextView);

            // Add player info layout to PlayerView
            addView(playerInfoLayout);

            // Initialize and configure the progress bar
            progressBar = new View(context);
            progressBar.setBackgroundColor(Color.RED);
            LinearLayout.LayoutParams progressLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    15 // Set your desired height for the progress bar
            );
            progressBar.setLayoutParams(progressLayoutParams);
            addView(progressBar);
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setProfileImage(String imageUrl, Context context) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .override(120, 120) // Specify size
                            .placeholder(R.drawable.loading) // Placeholder image
                            .error(R.drawable.error) // Error image
                            .circleCrop()) // Crop into a circle
                    .into(profileImageView);
        }

        public void setUsername(String username) {
            usernameTextView.setText(username);
        }

        public void updateProgress(double progress) {
            int parentWidth = ((ViewGroup) getParent()).getWidth();
            int newWidth = (int) (parentWidth * progress);

            ViewGroup.LayoutParams params = progressBar.getLayoutParams();
            params.width = newWidth;
            progressBar.setLayoutParams(params);
        }
    }


    public static class PlayerResult {
        public String username;
        public double progress;
        public double wpm;

        public PlayerResult(String username, double progress, double wpm) {
            this.username = username;
            this.progress = progress;
            this.wpm = wpm;
        }
    }
    @Override
    public void onBackPressed() {
        // Create an AlertDialog to confirm exit
        new AlertDialog.Builder(this)
                .setTitle("Quit Game")
                .setMessage("Are you sure you want to quit the game?")
                .setCancelable(false) // Prevents dialog from being dismissed on outside touch
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User chose to exit the game
                        MultiplayerGameActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null) // Dismiss dialog on "No"
                .show();
    }

}