package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.fud.adapters.ProductAdapter;
import com.amit.fud.adapters.RestaurantAdapter;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private View loadingOverlay;
    private List<Map<String, Object>> restaurantList = new ArrayList<>();
    private List<Map<String, Object>> productList = new ArrayList<>();
    private RestaurantAdapter restaurantAdapter;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        loadingOverlay = findViewById(R.id.loadingOverlay);
        ImageView ivDp = findViewById(R.id.ivUserDp);
        TextView tvName = findViewById(R.id.tvUserName);
        TextView tvAddress = findViewById(R.id.tvUserAddress);
        ImageView btnCart = findViewById(R.id.btnCart);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        RecyclerView rvRestaurants = findViewById(R.id.rvRestaurants);
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        restaurantAdapter = new RestaurantAdapter(restaurantList);
        productAdapter = new ProductAdapter(productList);
        rvRestaurants.setAdapter(restaurantAdapter);
        rvProducts.setAdapter(productAdapter);

        // Show Full Screen Blur Loader
        loadingOverlay.setVisibility(View.VISIBLE);

        // 1. Fetch User Data
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

        // 2. Fetch Restaurants
        FirebaseDatabase.getInstance().getReference("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                restaurantList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Map<String, Object> res = (Map<String, Object>) postSnapshot.getValue();
                    restaurantList.add(res);
                }
                restaurantAdapter.notifyDataSetChanged();
                checkLoadingStatus();
            }
            @Override public void onCancelled(DatabaseError error) { checkLoadingStatus(); }
        });

        // 3. Fetch Products
        FirebaseDatabase.getInstance().getReference("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Map<String, Object> prod = (Map<String, Object>) postSnapshot.getValue();
                    productList.add(prod);
                }
                productAdapter.notifyDataSetChanged();
                checkLoadingStatus();
            }
            @Override public void onCancelled(DatabaseError error) { checkLoadingStatus(); }
        });

        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) return true;
            else if (item.getItemId() == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); return true; }
            else if (item.getItemId() == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); return true; }
            return false;
        });
    }

    private void checkLoadingStatus() {
        // Data fetch hone ke baad Blur Overlay hata do (1 second delay for smooth transition)
        new android.os.Handler().postDelayed(() -> {
            if (loadingOverlay.getVisibility() == View.VISIBLE) {
                loadingOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> loadingOverlay.setVisibility(View.GONE)).start();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
