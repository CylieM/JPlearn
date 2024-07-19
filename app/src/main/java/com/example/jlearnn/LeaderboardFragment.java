package com.example.jlearnn;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
        headerLastWave = view.findViewById(R.id.lastColumnHeader); // Correct TextView ID

        // Fetch leaderboard data from Firebase for Daily Streak initially
        fetchLeaderboardData("DailyStreak");

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
        int viewId = view.getId(); // Get the view's ID

        if (viewId == R.id.buttonDailyStreak) {
            fetchLeaderboardData("DailyStreak");
        } else if (viewId == R.id.buttonKanaShoot) {
            fetchLeaderboardData("KanaShoot");
        } else if (viewId == R.id.buttonNihongoRace) {
            fetchLeaderboardData("NihongoRace");
        } else {
            // Handle other cases if needed
        }
    }

    private void fetchLeaderboardData(final String category) {
        Log.d("Leaderboard", "Fetching data for category: " + category);
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        // Update header text based on the category
        switch (category) {
            case "DailyStreak":
                headerLastWave.setText("Daily Streak");
                break;
            case "KanaShoot":
                headerLastWave.setText("Wave");
                break;
            case "NihongoRace":
                headerLastWave.setText("WPM");
                break;
            default:
                headerLastWave.setText("Score");
                break;
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear all rows including header
                tableLayout.removeAllViews();

                // Add header row back

                // Collect and sort user data
                List<RegistrationActivity.User> userList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    RegistrationActivity.User user = data.getValue(RegistrationActivity.User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Sort users based on the selected category
                userList.sort((user1, user2) -> {
                    int value1 = getCategoryValue(user1, category);
                    int value2 = getCategoryValue(user2, category);
                    return Integer.compare(value2, value1); // Sort in descending order
                });

                // Add sorted users to the table
                int rank = 1;
                for (RegistrationActivity.User user : userList) {
                    int lastWave = getCategoryValue(user, category);
                    addTableRow(rank, user.getUsername(), lastWave, user.getProfilePicture());
                    rank++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });
    }

    private int getCategoryValue(RegistrationActivity.User user, String category) {
        switch (category) {
            case "DailyStreak":
                return user.getDailyStreak();
            case "KanaShoot":
                return user.getKSWaves();
            case "NihongoRace":
                return user.getNRaceBestWPM();
            default:
                return 0; // Default value if category is not recognized
        }
    }

    private void addTableRow(int rank, String username, int lastWave, String profilePictureUrl) {
        Log.d("Leaderboard", "Adding row for user: " + username);
        TableRow tableRow = new TableRow(getContext());

        TextView rankTextView = new TextView(getContext());
        rankTextView.setText(String.valueOf(rank));
        rankTextView.setPadding(40, 8, 40, 8); // Increase left and right padding
        tableRow.addView(rankTextView);

        // ImageView for profile picture
        ImageView profileImageView = new ImageView(getContext());
        profileImageView.setPadding(40, 8, 40, 8); // Increase left and right padding
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(getContext()).load(profilePictureUrl).apply(RequestOptions.circleCropTransform()).into(profileImageView);
        tableRow.addView(profileImageView, new TableRow.LayoutParams(100, 100)); // Set layout parameters when adding to TableRow

        TextView usernameTextView = new TextView(getContext());
        usernameTextView.setText(username);
        usernameTextView.setPadding(40, 8, 240, 8); // Increase left and right padding
        usernameTextView.setMaxLines(1); // Limit to a single line
        usernameTextView.setEllipsize(TextUtils.TruncateAt.END); // Add ellipsis at the end if the text is too long
        tableRow.addView(usernameTextView);

        TextView lastWaveTextView = new TextView(getContext());
        lastWaveTextView.setText(String.valueOf(lastWave));
        lastWaveTextView.setPadding(24, 8, 24, 8); // Increase left and right padding
        tableRow.addView(lastWaveTextView);

        tableLayout.addView(tableRow);
    }





}
