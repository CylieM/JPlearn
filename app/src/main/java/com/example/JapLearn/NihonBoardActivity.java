package com.example.JapLearn;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
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

public class NihonBoardActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView headerLastWave;
    private Button buttonBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nihonboard);

        tableLayout = findViewById(R.id.tableLayout);
        headerLastWave = findViewById(R.id.lastColumnHeader);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);

        fetchLeaderboardData();

    }

    private void fetchLeaderboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        headerLastWave.setText("WPM");
        headerLastWave.setTextColor(Color.BLACK);
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
        TableRow tableRow = new TableRow(this);

        TextView rankTextView = new TextView(this);
        rankTextView.setText(String.valueOf(rank));
        rankTextView.setPadding(40, 8, 40, 8);
        rankTextView.setTextColor(Color.BLACK);
        tableRow.addView(rankTextView);

        ImageView profileImageView = new ImageView(this);
        profileImageView.setPadding(20, 8, 20, 8); // Adjust padding as needed
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileImageView.setAdjustViewBounds(true); // Allow the ImageView to adjust its bounds

// Set a larger size for the ImageView
        int imageSize = 120; // Set desired size (e.g., 150dp)
        TableRow.LayoutParams params = new TableRow.LayoutParams(imageSize, imageSize);
        params.gravity = Gravity.CENTER; // Center the image in the TableRow
        profileImageView.setLayoutParams(params);

        Glide.with(this)
                .load(profilePictureUrl)
                .apply(new RequestOptions()
                        .override(imageSize, imageSize) // Specify the size of the ImageView
                        .placeholder(R.drawable.loading) // Set a placeholder image
                        .error(R.drawable.loading) // Set an error image if loading fails
                        .circleCrop()) // Crop the image into a circle
                .into(profileImageView);

        tableRow.addView(profileImageView); // Add ImageView to the row


        TextView usernameTextView = new TextView(this);
        usernameTextView.setText(username);
        usernameTextView.setPadding(45, 8, 140, 8);
        usernameTextView.setMaxLines(1);
        usernameTextView.setEllipsize(TextUtils.TruncateAt.END);
        usernameTextView.setTextColor(Color.BLACK);
        tableRow.addView(usernameTextView);

        TextView categoryValueTextView = new TextView(this);
        categoryValueTextView.setText(String.valueOf(categoryValue));
        categoryValueTextView.setPadding(1, 8, 24, 8);
        categoryValueTextView.setTextColor(Color.BLACK);
        tableRow.addView(categoryValueTextView);

        tableLayout.addView(tableRow);
    }



}
