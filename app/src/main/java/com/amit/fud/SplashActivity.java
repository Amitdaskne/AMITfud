package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);

        Button btnUser = findViewById(R.id.btnUser);
        Button btnRider = findViewById(R.id.btnRider);

        btnUser.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("MODE", "USER");
            startActivity(i);
        });

        btnRider.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("MODE", "RIDER");
            startActivity(i);
        });
    }
}
