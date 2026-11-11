package com.amit.fud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private String appMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MapLibre Initialize
        MapLibre.getInstance(this);
        
        setContentView(R.layout.activity_main);

        appMode = getIntent().getStringExtra("MODE");
        Toast.makeText(this, "Welcome " + appMode, Toast.LENGTH_SHORT).show();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            
            // Free OpenStreetMap Style URL
            String styleUrl = "https://demotiles.maplibre.org/style.json";
            
            map.setStyle(styleUrl, style -> {
                enableLocation(style);
            });
        });
    }

    private void enableLocation(Style style) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // MapLibre LocationComponent Setup
        LocationComponent locationComponent = mapLibreMap.getLocationComponent();
        
        // Activate with default options
        LocationComponentActivationOptions options = LocationComponentActivationOptions
                .builder(this, style)
                .useDefaultLocationEngine(true)
                .build();
        
        locationComponent.activateLocationComponent(options);
        
        // Enable location visibility
        locationComponent.setLocationComponentEnabled(true);
        
        // Set camera mode to tracking and render mode to compass
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        
        // Default camera position (India)
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20.5937, 78.9629), 4.0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Re-fetch style to enable location
            if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                enableLocation(mapLibreMap.getStyle());
            }
        }
    }

    // MapView Lifecycle methods
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
}
