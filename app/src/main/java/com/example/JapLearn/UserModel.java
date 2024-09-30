package com.example.JapLearn;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserModel {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    public UserModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public DatabaseReference getUserRef(String userId) {
        return userRef.child(userId);
    }

    // Define the User class as a static inner class
    public static class User {
        private String username;
        private String email;
        private String profilePicture;
        private String role;
        private String userId;
        private String currentLesson;
        private int KSWaves;
        private int KSHighScore;
        private int HiraganaProgress;
        private int KatakanaProgress;
        private int VocabularyProgress;
        private int NRaceBestWPM;
        private int NRaceFirstPlace;
        private int DailyStreak;
        private long lastLoginDate;

        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        public User() {}

        public User(String username, String email, String profilePicture, String role,
                    int KSWaves, int KSHighScore, int HiraganaProgress,
                    int KatakanaProgress, int VocabularyProgress,
                    int NRaceBestWPM, int NRaceFirstPlace,
                    int DailyStreak, long lastLoginDate, String currentLesson) {
            this.username = username;
            this.email = email;
            this.profilePicture = profilePicture;
            this.role = role;
            this.KSWaves = KSWaves;
            this.KSHighScore = KSHighScore;
            this.HiraganaProgress = HiraganaProgress;
            this.KatakanaProgress = KatakanaProgress;
            this.VocabularyProgress = VocabularyProgress;
            this.NRaceBestWPM = NRaceBestWPM;
            this.NRaceFirstPlace = NRaceFirstPlace;
            this.DailyStreak = DailyStreak;
            this.lastLoginDate = lastLoginDate;
            this.currentLesson = currentLesson;
        }

        // Getter methods
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getUserId() { return userId; }
        public String getProfilePicture() { return profilePicture; }
        public String getRole() { return role; }
        public int getKSWaves() { return KSWaves; }
        public int getKSHighScore() { return KSHighScore; }
        public int getHiraganaProgress() { return HiraganaProgress; }
        public int getKatakanaProgress() { return KatakanaProgress; }
        public int getVocabularyProgress() { return VocabularyProgress; }
        public int getNRaceBestWPM() { return NRaceBestWPM; }
        public int getNRaceFirstPlace() { return NRaceFirstPlace; }
        public int getDailyStreak() { return DailyStreak; }
        public String getCurrentLesson() { return currentLesson; }
        public long getLastLoginDate() { return lastLoginDate; }

        // Setter methods
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
        public void setRole(String role) { this.role = role; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setKSWaves(int KSWaves) { this.KSWaves = KSWaves; }
        public void setKSHighScore(int KSHighScore) { this.KSHighScore = KSHighScore; }
        public void setHiraganaProgress(int HiraganaProgress) { this.HiraganaProgress = HiraganaProgress; }
        public void setKatakanaProgress(int KatakanaProgress) { this.KatakanaProgress = KatakanaProgress; }
        public void setVocabularyProgress(int VocabularyProgress) { this.VocabularyProgress = VocabularyProgress; }
        public void setNRaceBestWPM(int NRaceBestWPM) { this.NRaceBestWPM = NRaceBestWPM; }
        public void setNRaceFirstPlace(int NRaceFirstPlace) { this.NRaceFirstPlace = NRaceFirstPlace; }
        public void setDailyStreak(int DailyStreak) { this.DailyStreak = DailyStreak; }
        public void setLastLoginDate(long lastLoginDate) { this.lastLoginDate = lastLoginDate; }
        public void setCurrentLesson(String currentLesson) { this.currentLesson = currentLesson; }

        public Object getJoinedRoomCode() {
            return null; // Modify if needed
        }
    }
}
