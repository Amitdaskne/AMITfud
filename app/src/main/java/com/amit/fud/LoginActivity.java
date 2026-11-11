package com.amit.fud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhone, etOtp;
    private Button btnGetOtp, btnVerifyOtp;
    private LinearLayout otpLayout;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnGetOtp = findViewById(R.id.btnGetOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        otpLayout = findViewById(R.id.otpLayout);
        progressBar = findViewById(R.id.progressBar);

        btnGetOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.length() != 10) {
                Toast.makeText(this, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            sendOtp("+91" + phone);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.isEmpty()) return;
            progressBar.setVisibility(View.VISIBLE);
            verifyOtp(code);
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Auto-verification (instant)
                        signInWithCredential(credential);
                    }
                    @Override
                    public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        progressBar.setVisibility(View.GONE);
                        otpLayout.setVisibility(View.VISIBLE);
                        btnGetOtp.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "OTP Sent!", Toast.LENGTH_SHORT).show();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
