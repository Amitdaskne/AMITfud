package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyALHFd8-PhTpNSn8ipHlLsQYeUjEiBDRMs")
                .setApplicationId("1:1052210817036:android:80674c39836371f46487e4")
                .setDatabaseUrl("https://chat2-6bd92-default-rtdb.firebaseio.com")
                .setProjectId("chat2-6bd92").build();
            FirebaseApp.initializeApp(this, options);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("name").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        startActivity(new Intent(this, HomeActivity.class));
                    } else {
                        startActivity(new Intent(this, OnboardingActivity.class));
                    }
                    finish();
                });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
