package com.amit.fud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Marker;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;
    private String appMode;
    
    private DatabaseReference riderLocationRef;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker riderMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_main);

        // 🔥 MANUAL FIREBASE INITIALIZATION 🔥
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyALHFd8-PhTpNSn8ipHlLsQYeUjEiBDRMs")
                .setApplicationId("1:1052210817036:android:80674c39836371f46487e4") // Android format
                .setDatabaseUrl("https://chat2-6bd92-default-rtdb.firebaseio.com")
                .setProjectId("chat2-6bd92")
                .setStorageBucket("chat2-6bd92.appspot.com")
                .build();
                
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Firebase Init Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        appMode = getIntent().getStringExtra("MODE");
        Toast.makeText(this, "Logged in as " + appMode, Toast.LENGTH_SHORT).show();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        FloatingActionButton btnMyLocation = findViewById(R.id.btnMyLocation);

        // Firebase Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        riderLocationRef = database.getReference("rider_location");

        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            String styleUrl = "https://tiles.openfreemap.org/styles/bright";
            map.setStyle(styleUrl, style -> {
                enableLocation(style);
                
                if ("USER".equals(appMode)) {
                    listenToRiderLocation(style);
                }
            });
        });

        btnMyLocation.setOnClickListener(v -> centerToMyLocation());
    }

    private void enableLocation(Style style) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationComponent = mapLibreMap.getLocationComponent();
        LocationComponentActivationOptions options = LocationComponentActivationOptions
                .builder(this, style).useDefaultLocationEngine(true).build();
        
        locationComponent.activateLocationComponent(options);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);

        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20.5937, 78.9629), 15.0));

        if ("RIDER".equals(appMode)) {
            startSendingLocation();
        }
    }

    // 🏍️ RIDER: Apni location Firebase me bhejna
    private void startSendingLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Latitude aur Longitude ko alag alag save karo taaki read karna aasan ho
                        riderLocationRef.child("lat").setValue(location.getLatitude());
                        riderLocationRef.child("lng").setValue(location.getLongitude());
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Toast.makeText(this, "Broadcasting location to users...", Toast.LENGTH_LONG).show();
        }
    }

    // 👤 USER: Rider ki location sunna aur map par icon move karna
    private void listenToRiderLocation(Style style) {
        riderLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);

                    if (lat != null && lng != null) {
                        LatLng riderLoc = new LatLng(lat, lng);
                        
                        if (riderMarker == null) {
                            riderMarker = mapLibreMap.addMarker(
                                    new MarkerOptions()
                                            .position(riderLoc)
                                            .title("Rider is here")
                            );
                            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(riderLoc, 16.0));
                        } else {
                            riderMarker.setPosition(riderLoc);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void centerToMyLocation() {
        if (locationComponent != null && locationComponent.getLastKnownLocation() != null) {
            Location loc = locationComponent.getLastKnownLocation();
            LatLng userLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 16.0), 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                enableLocation(mapLibreMap.getStyle());
            }
        }
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
}
