package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnAction;
    private TextView tvTitle, tvToggleAuth;
    private LottieAnimationView eggAnimation;
    private boolean isLoginMode = true;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnAction = findViewById(R.id.btnAction);
        tvTitle = findViewById(R.id.tvTitle);
        tvToggleAuth = findViewById(R.id.tvToggleAuth);
        eggAnimation = findViewById(R.id.eggAnimation);
        
        // Animation file set karo
        eggAnimation.setAnimation("egg_breaking.json");

        btnAction.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) return;

            // Egg Animation Start
            btnAction.setVisibility(View.GONE);
            eggAnimation.setVisibility(View.VISIBLE);
            eggAnimation.playAnimation();

            if (isLoginMode) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) checkOnboarding();
                    else showError(task.getException().getMessage());
                });
            } else {
                if (!password.equals(etConfirmPassword.getText().toString())) {
                    showError("Passwords do not match"); return;
                }
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) checkOnboarding();
                    else showError(task.getException().getMessage());
                });
            }
        });

        tvToggleAuth.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            etConfirmPassword.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
            btnAction.setText(isLoginMode ? "LOGIN" : "REGISTER");
            tvTitle.setText(isLoginMode ? "Login to AMITfud" : "Create Account");
            tvToggleAuth.setText(isLoginMode ? "New here? Create Account" : "Already have an account? Login");
        });
    }

    private void checkOnboarding() {
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name").get()
            .addOnCompleteListener(task -> {
                eggAnimation.cancelAnimation();
                if (task.isSuccessful() && task.getResult().exists()) {
                    startActivity(new Intent(this, HomeActivity.class));
                } else {
                    startActivity(new Intent(this, OnboardingActivity.class));
                }
                finish();
            });
    }

    private void showError(String msg) {
        eggAnimation.cancelAnimation();
        eggAnimation.setVisibility(View.GONE);
        btnAction.setVisibility(View.VISIBLE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
