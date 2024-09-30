package com.example.JapLearn;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment implements View.OnClickListener {

    private TableLayout tableLayout;
    private TextView headerLastWave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        tableLayout = view.findViewById(R.id.tableLayout);
        headerLastWave = view.findViewById(R.id.lastColumnHeader);

        // Fetch leaderboard data based on the category from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String category = arguments.getString("CATEGORY");
            if (category != null) {
                fetchLeaderboardData(category);
            }
        } else {
            // Default category if no arguments are passed
            fetchLeaderboardData("DailyStreak");
        }

        // Set onClickListener for buttons
        view.findViewById(R.id.buttonDailyStreak).setOnClickListener(this);
        view.findViewById(R.id.buttonKanaShoot).setOnClickListener(this);
        view.findViewById(R.id.buttonNihongoRace).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        onLeaderboardButtonClick(view);
    }

    private void onLeaderboardButtonClick(View view) {
        int viewId = view.getId();
        String category = "DailyStreak";

        if (viewId == R.id.buttonDailyStreak) {
            category = "DailyStreak";
        } else if (viewId == R.id.buttonKanaShoot) {
            category = "KanaShoot";
        } else if (viewId == R.id.buttonNihongoRace) {
            category = "NihongoRace";
        }

        fetchLeaderboardData(category);
    }

    private void fetchLeaderboardData(final String category) {
        Log.d("Leaderboard", "Fetching data for category: " + category);
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        switch (category) {
            case "DailyStreak":
                headerLastWave.setText("Daily Streak");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            case "KanaShoot":
                headerLastWave.setText("Wave");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            case "NihongoRace":
                headerLastWave.setText("WPM");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            default:
                headerLastWave.setText("Score");
                headerLastWave.setTextColor(Color.BLACK);
                break;
        }

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

                // Sort users by category value in descending order
                userList.sort((user1, user2) -> {
                    int value1 = getCategoryValue(user1, category);
                    int value2 = getCategoryValue(user2, category);
                    return Integer.compare(value2, value1);
                });

                // Display only the top 10 users
                int rank = 1;
                for (UserModel.User user : userList) {
                    if (rank > 10) break; // Limit to top 10
                    int categoryValue = getCategoryValue(user, category);
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

    private int getCategoryValue(UserModel.User user, String category) {
        switch (category) {
            case "DailyStreak":
                return user.getDailyStreak();
            case "KanaShoot":
                return user.getKSWaves();
            case "NihongoRace":
                return user.getNRaceBestWPM();
            default:
                return 0;
        }
    }

    private void addTableRow(int rank, String username, int categoryValue, String profilePictureUrl) {
        TableRow tableRow = new TableRow(getContext());

        TextView rankTextView = new TextView(getContext());
        rankTextView.setText(String.valueOf(rank));
        rankTextView.setPadding(40, 8, 40, 8);
        rankTextView.setTextColor(Color.BLACK);
        tableRow.addView(rankTextView);

        ImageView profileImageView = new ImageView(getContext());
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
                        .error(R.drawable.error) // Set an error image if loading fails
                        .circleCrop()) // Crop the image into a circle
                .into(profileImageView);

        tableRow.addView(profileImageView); // Add ImageView to the row


        TextView usernameTextView = new TextView(getContext());
        usernameTextView.setText(username);
        usernameTextView.setPadding(45, 8, 140, 8);
        usernameTextView.setMaxLines(1);
        usernameTextView.setEllipsize(TextUtils.TruncateAt.END);
        usernameTextView.setTextColor(Color.BLACK);
        tableRow.addView(usernameTextView);

        TextView categoryValueTextView = new TextView(getContext());
        categoryValueTextView.setText(String.valueOf(categoryValue));
        categoryValueTextView.setPadding(1, 8, 24, 8);
        categoryValueTextView.setTextColor(Color.BLACK);
        tableRow.addView(categoryValueTextView);

        tableLayout.addView(tableRow);
    }
}
