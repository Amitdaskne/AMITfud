package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView ivDp;
    private TextView tvEmail;
    private EditText etName, etAddress;
    private Button btnUpdate, btnLogout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_profile);

        ivDp = findViewById(R.id.ivProfileDp);
        tvEmail = findViewById(R.id.tvEmail);
        etName = findViewById(R.id.etProfileName);
        etAddress = findViewById(R.id.etProfileAddress);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBarProfile);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tvEmail.setText("Email: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etAddress.setText(snapshot.child("address").getValue(String.class));
                    String dpUrl = snapshot.child("dp").getValue(String.class);
                    if (dpUrl != null) Glide.with(ProfileActivity.this).load(dpUrl).circleCrop().into(ivDp);
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        btnUpdate.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();
            if (newName.isEmpty() || newAddress.isEmpty()) return;

            progressBar.setVisibility(View.VISIBLE);
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("address", newAddress);

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}
