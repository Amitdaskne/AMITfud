package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnAction, btnForgot;
    private TextView tvTitle, tvToggleAuth;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnAction = findViewById(R.id.btnAction);
        btnForgot = findViewById(R.id.btnForgot);
        tvTitle = findViewById(R.id.tvTitle);
        tvToggleAuth = findViewById(R.id.tvToggleAuth);
        progressBar = findViewById(R.id.progressBar);

        btnAction.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnAction.setEnabled(false);

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                String confirmPass = etConfirmPassword.getText().toString().trim();
                if (!password.equals(confirmPass)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnAction.setEnabled(true);
                    return;
                }
                registerUser(email, password);
            }
        });

        tvToggleAuth.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            if (isLoginMode) {
                tvTitle.setText("Login to AMITfud");
                btnAction.setText("LOGIN");
                etConfirmPassword.setVisibility(View.GONE);
                btnForgot.setVisibility(View.GONE);
                tvToggleAuth.setText("New here? Create Account");
            } else {
                tvTitle.setText("Create Account");
                btnAction.setText("REGISTER");
                etConfirmPassword.setVisibility(View.VISIBLE);
                btnForgot.setVisibility(View.GONE);
                tvToggleAuth.setText("Already have an account? Login");
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                btnAction.setEnabled(true);
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                btnAction.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
