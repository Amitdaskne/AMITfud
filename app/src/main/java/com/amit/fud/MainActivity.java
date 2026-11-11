package com.amit.fud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;
    private String appMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_main);

        appMode = getIntent().getStringExtra("MODE");
        Toast.makeText(this, "Welcome " + appMode, Toast.LENGTH_SHORT).show();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        FloatingActionButton btnMyLocation = findViewById(R.id.btnMyLocation);

        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            String styleUrl = "https://demotiles.maplibre.org/style.json";
            map.setStyle(styleUrl, style -> {
                enableLocation(style);
            });
        });

        // Current Location Button Click
        btnMyLocation.setOnClickListener(v -> {
            if (locationComponent != null && locationComponent.getLastKnownLocation() != null) {
                Location loc = locationComponent.getLastKnownLocation();
                LatLng userLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
                // Smoothly animate camera to current location
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 16.0), 1000);
                Toast.makeText(this, "Centered to your location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not available yet. Wait for GPS.", Toast.LENGTH_SHORT).show();
                enableLocation(mapLibreMap.getStyle()); // Retry enabling
            }
        });
    }

    private void enableLocation(Style style) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationComponent = mapLibreMap.getLocationComponent();
        
        LocationComponentActivationOptions options = LocationComponentActivationOptions
                .builder(this, style)
                .useDefaultLocationEngine(true)
                .build();
        
        locationComponent.activateLocationComponent(options);
        locationComponent.setLocationComponentEnabled(true);
        
        // --- LIVE TRACKING (Local) ---
        // TRACKING mode: Map aapke phone ke sath sath move karega (Live)
        // GPS mode: Jab phone ghumega, map bhi ghumega
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20.5937, 78.9629), 4.0));
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
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
}
