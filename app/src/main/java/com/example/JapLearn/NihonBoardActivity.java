package com.example.JapLearn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
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

public class NihonBoardActivity extends AppCompatActivity {

    private TableLayout tableLayout;

    private Button buttonBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nihonboard);

        tableLayout = findViewById(R.id.tableLayout);

        buttonBackToHome = findViewById(R.id.buttonBackToHome);

        fetchLeaderboardData();

        buttonBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start HomeActivity
                Intent intent = new Intent(NihonBoardActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Optionally call finish() if you want to remove this activity from the back stack
            }
        });
    }

    private void fetchLeaderboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableLayout.removeAllViews();

                List<UserModel.User> userList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel.User user = data.getValue(UserModel.User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                userList.sort((user1, user2) -> Integer.compare(user2.getNRaceBestWPM(), user1.getNRaceBestWPM()));

                int rank = 1;
                for (UserModel.User user : userList) {
                    int categoryValue = user.getNRaceBestWPM();
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
        LayoutInflater inflater = LayoutInflater.from(this);
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.table_row_leaderboard, null);

        TextView rankTextView = tableRow.findViewById(R.id.rankTextView);
        ImageView profileImageView = tableRow.findViewById(R.id.profileImageView);
        TextView usernameTextView = tableRow.findViewById(R.id.usernameTextView);
        TextView categoryValueTextView = tableRow.findViewById(R.id.categoryValueTextView);

        rankTextView.setText(String.valueOf(rank));
        usernameTextView.setText(username);
        categoryValueTextView.setText(String.valueOf(categoryValue));

        Glide.with(this)
                .load(profilePictureUrl)
                .apply(new RequestOptions()
                        .override(100, 100) // Specify the size of the ImageView
                        .placeholder(R.drawable.loading) // Set a placeholder image
                        .error(R.drawable.loading) // Set an error image if loading fails
                        .circleCrop()) // Crop the image into a circle
                .into(profileImageView);

        tableLayout.addView(tableRow);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Disable back button handling when the activity is visible
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }
}

