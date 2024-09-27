package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class KanaBoardActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView headerLastWave;
    private Button buttonPlayAgain, buttonBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanaboard);

        tableLayout = findViewById(R.id.tableLayout);
        headerLastWave = findViewById(R.id.lastColumnHeader);
        buttonPlayAgain = findViewById(R.id.buttonPlayAgain);


        fetchLeaderboardData();

        buttonPlayAgain.setOnClickListener(v -> restartKanaShoot());

    }

    private void fetchLeaderboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        headerLastWave.setText("Wave");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableLayout.removeAllViews();

                List<RegistrationActivity.User> userList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    RegistrationActivity.User user = data.getValue(RegistrationActivity.User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                userList.sort((user1, user2) -> Integer.compare(user2.getKSWaves(), user1.getKSWaves()));

                int rank = 1;
                for (RegistrationActivity.User user : userList) {
                    int categoryValue = user.getKSWaves();
                    if (categoryValue > 0) {
                        addTableRow(rank, user.getUsername(), categoryValue, user.getProfilePicture());
                        rank++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void addTableRow(int rank, String username, int categoryValue, String profilePictureUrl) {
        TableRow tableRow = new TableRow(this);

        TextView rankTextView = new TextView(this);
        rankTextView.setText(String.valueOf(rank));
        rankTextView.setPadding(40, 8, 40, 8);
        tableRow.addView(rankTextView);

        ImageView profileImageView = new ImageView(this);
        profileImageView.setPadding(40, 8, 40, 8);
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(profilePictureUrl).apply(RequestOptions.circleCropTransform()).into(profileImageView);
        tableRow.addView(profileImageView, new TableRow.LayoutParams(100, 100));

        TextView usernameTextView = new TextView(this);
        usernameTextView.setText(username);
        usernameTextView.setPadding(40, 8, 240, 8);
        usernameTextView.setMaxLines(1);
        usernameTextView.setEllipsize(TextUtils.TruncateAt.END);
        tableRow.addView(usernameTextView);

        TextView categoryValueTextView = new TextView(this);
        categoryValueTextView.setText(String.valueOf(categoryValue));
        categoryValueTextView.setPadding(24, 8, 24, 8);
        tableRow.addView(categoryValueTextView);

        tableLayout.addView(tableRow);
    }

    private void restartKanaShoot() {
        Intent intent = new Intent(this, KanaShootActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity
    }


}