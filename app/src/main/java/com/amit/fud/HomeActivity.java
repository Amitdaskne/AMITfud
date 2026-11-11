package com.amit.fud;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.shimmer.ShimmerFrameLayout;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        ShimmerFrameLayout shimmerLayout = findViewById(R.id.shimmerLayout);
        View mainContent = findViewById(R.id.mainContent);

        // Start Skeleton Animation
        shimmerLayout.startShimmer();

        // Setup Sideways scrollable lists (RecyclerViews)
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        RecyclerView rvRestaurants = findViewById(R.id.rvRestaurants);
        
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Fake 2 second delay to show Skeleton (Later we will load Firebase data here)
        new Handler().postDelayed(() -> {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }, 2000);
    }
}
