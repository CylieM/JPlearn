package com.example.JapLearn;

import android.app.AlertDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class StartMultiplayerGameActivity extends AppCompatActivity {

    private TextView timerDisplay;
    private TextView textDisplay;
    private EditText textInput;
    private LinearLayout raceTrackContainer;
    private DatabaseReference lobbyRef;
    private String lobbyCode;
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

        lobbyCode = getIntent().getStringExtra("LOBBY_ID");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        lobbyRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("lobbies").child(lobbyCode);

        dbHelper = new ParagraphSQLiteDB(this);

        initializePlayers();
        listenForPlayerProgressUpdates();
        startGame();

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

    private void initializePlayers() {
        lobbyRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String firstPlacePlayerId = null;
                    String playerId = playerSnapshot.getKey();
                    String username = playerSnapshot.getValue(String.class);

                    PlayerView playerView = new PlayerView(StartMultiplayerGameActivity.this);
                    playerView.setPlayerId(playerId);
                    playerView.setUsername(username);

                    raceTrackContainer.addView(playerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }



    private void listenForPlayerProgressUpdates() {
        lobbyRef.child("progress").addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(StartMultiplayerGameActivity.this, "Failed to load player progress.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame() {
        dbHelper.logAllData();
        Log.e("MultiplayerGameActivity", "Current Paragraph: " + currentParagraph);
        String[] paragraph = getRandomParagraph();
        currentParagraph = paragraph[0].replace(" ", "");
        currentRomaji = paragraph[1].trim();
        textDisplay.setText(currentParagraph);
        lobbyRef.child("gameState").setValue("finished");
        startTimeMillis = System.currentTimeMillis();

        // Retrieve the timer value from the intent extras
        int timerSeconds = getIntent().getIntExtra("TIMER", 30); // Default to 30 if not found
        long startTime = timerSeconds * 1000; // Convert to milliseconds

        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(startTime, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerDisplay.setText("Time remainingg: " + millisUntilFinished / 1000);
                timerDisplay.setTextColor(Color.BLACK);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
        Log.e("MultiplayerGameActivity", "Current Paragraph (after retrieval): " + currentParagraph);
    }


    private String[] getRandomParagraph() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ParagraphSQLiteDB.TABLE_PARAGRAPHS, null);
        int count = cursor.getCount();
        if (count == 0) {
            return new String[] {"", ""}; // Handle case with no paragraphs in the database
        }
        int randomIndex = new Random().nextInt(count);
        cursor.moveToPosition(randomIndex);
        String paragraph = cursor.getString(cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_PARAGRAPH));
        String romaji = cursor.getString(cursor.getColumnIndexOrThrow(ParagraphSQLiteDB.COLUMN_ROMAJI));
        cursor.close();
        return new String[] {paragraph, romaji};
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
        lobbyRef.child("progress").child(userId).setValue(progress);
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
        lobbyRef.child("gameState").setValue("finished");
    }

    private boolean isRoomOwner() {
        return userId.equals(roomOwnerId);
    }
    private void showFinishedDialog() {
        AtomicReference<String> firstPlacePlayerId = new AtomicReference<>(null);

        lobbyRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PlayerResult> playerResults = new ArrayList<>();

                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerId = playerSnapshot.getKey();
                    String username = playerSnapshot.getValue(String.class);

                    lobbyRef.child("progress").child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot progressSnapshot) {
                            Double progress = progressSnapshot.getValue(Double.class);
                            Log.e("StartMultiplayerGameActivity", "Player ID: " + playerId + ", Progress: " + progress);
                            Double wpm = calculateWPM(progress);
                            if (progress == null) {
                                progress = 0.00; // Default value if progress is null
                            }


                            if (wpm == null) {
                                wpm = 0.00; // Default value if WPM is null
                            }
                            // Update the user's best WPM if applicable
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
    private void displayFinishedDialog(List<PlayerResult> playerResults) {
        Collections.sort(playerResults, new Comparator<PlayerResult>() {
            @Override
            public int compare(PlayerResult o1, PlayerResult o2) {
                return Double.compare(o2.progress, o1.progress);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(StartMultiplayerGameActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_finished, null);
        builder.setView(dialogView);

        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        for (PlayerResult result : playerResults) {
            TableRow row = new TableRow(this);

            TextView usernameTextView = new TextView(this);
            usernameTextView.setText(result.username);
            usernameTextView.setPadding(4, 4, 4, 4);
            row.addView(usernameTextView);

            TextView progressTextView = new TextView(this);
            progressTextView.setText(String.format("%.2f%%", result.progress * 100));
            progressTextView.setPadding(4, 4, 4, 4);
            row.addView(progressTextView);

            TextView wpmTextView = new TextView(this);
            wpmTextView.setText(String.format("%.2f", result.wpm));
            wpmTextView.setPadding(4, 4, 4, 4);
            row.addView(wpmTextView);

            tableLayout.addView(row);
        }

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnShowAllLeaderboards = dialogView.findViewById(R.id.btnShowAllLeaderboards);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace fragment with NihongoRaceFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NihongoRaceFragment())
                        .addToBackStack(null)  // Optional: add to back stack if you want to enable back navigation
                        .commit();
            }
        });

        btnShowAllLeaderboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Dialog", "Show All Leaderboards Button Clicked");
                Intent intent = new Intent(StartMultiplayerGameActivity.this, NihonBoardActivity.class);
                startActivity(intent);
            }
        });

        builder.create().show();
    }

    public class PlayerView extends LinearLayout {

        private String playerId;

        private TextView usernameTextView;
        private View progressBar;

        public PlayerView(Context context) {
            super(context);
            setOrientation(VERTICAL);

            // Initialize and configure the username TextView
            usernameTextView = new TextView(context);
            usernameTextView.setTextColor(Color.BLACK); // Set text color to black
            addView(usernameTextView);

            // Initialize and configure the progress bar
            progressBar = new View(context);
            progressBar.setBackgroundColor(Color.BLUE);
            LinearLayout.LayoutParams progressLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    10 // Set your desired height for the progress bar
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
}