package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        ImageView ivDp = findViewById(R.id.ivUserDp);
        TextView tvName = findViewById(R.id.tvUserName);
        TextView tvAddress = findViewById(R.id.tvUserAddress);
        ShimmerFrameLayout shimmerLayout = findViewById(R.id.shimmerLayout);
        View mainContent = findViewById(R.id.mainContent);
        ImageView btnCart = findViewById(R.id.btnCart);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        shimmerLayout.startShimmer();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvName.setText(snapshot.child("name").getValue(String.class));
                    tvAddress.setText(snapshot.child("address").getValue(String.class));
                    String dpUrl = snapshot.child("dp").getValue(String.class);
                    if (dpUrl != null) Glide.with(HomeActivity.this).load(dpUrl).circleCrop().into(ivDp);
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        new Handler().postDelayed(() -> {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }, 1500);

        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        // 🔥 Bottom Navigation Navigation Logic 🔥
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
