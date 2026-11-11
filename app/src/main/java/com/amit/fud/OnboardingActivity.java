package com.amit.fud;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnboardingActivity extends AppCompatActivity {
    private ImageView ivProfile;
    private EditText etName, etAddress;
    private Button btnSave;
    private ProgressBar progressBar;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_onboarding);
        
        ivProfile = findViewById(R.id.ivProfile);
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivProfile);
                }
            });

        ivProfile.setOnClickListener(v -> pickImage.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Name and Address required", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            if (selectedImageUri != null) {
                uploadToCloudinary(selectedImageUri, name, address);
            } else {
                saveToFirebase(name, address, "https://res.cloudinary.com/dlsiskxua/image/upload/v1/sample.jpg");
            }
        });
    }

    private void uploadToCloudinary(Uri imageUri, String name, String address) {
        new Thread(() -> {
            try {
                String cloudName = "dlsiskxua";
                String uploadPreset = "ml_default";
                String urlStr = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
                
                String boundary = "*****" + UUID.randomUUID().toString() + "*****";
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream os = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
                
                // Upload Preset
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                writer.append(uploadPreset).append("\r\n");
                
                // File
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"dp.jpg\"\r\n");
                writer.append("Content-Type: image/jpeg\r\n\r\n");
                writer.flush();

                InputStream is = getContentResolver().openInputStream(imageUri);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                writer.append("\r\n--").append(boundary).append("--\r\n");
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    // Simple JSON parse for secure_url
                    String resp = response.toString();
                    String secureUrl = resp.split("\"secure_url\":\"")[1].split("\"")[0];
                    runOnUiThread(() -> saveToFirebase(name, address, secureUrl));
                } else {
                    runOnUiThread(() -> { progressBar.setVisibility(View.GONE); btnSave.setEnabled(true); Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show(); });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progressBar.setVisibility(View.GONE); btnSave.setEnabled(true); Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); });
            }
        }).start();
    }

    private void saveToFirebase(String name, String address, String dpUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("address", address);
        user.put("dp", dpUrl);

        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(user)
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "DB Error", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
