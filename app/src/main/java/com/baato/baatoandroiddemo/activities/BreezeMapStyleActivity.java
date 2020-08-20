package com.baato.baatoandroiddemo.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baatoandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

/**
 * input the baato monochrome map style url and load with mapbox
 */
public class BreezeMapStyleActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView bottomInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_styles);

        mapView = findViewById(R.id.mapView);
        bottomInfoLayout = findViewById(R.id.bottomInfoLayout);

        Mapbox.getInstance(this, null);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap ->
        {
            //remove mapbox attribute
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);
            //add you map style url here
            mapboxMap.setStyle(getString(R.string.base_url) + "styles/breeze?key=" + getString(R.string.baato_access_token),
                    style -> {
                        bottomInfoLayout.setText(Html.fromHtml("<b>Breeze Map</b>" + " from Baato.io"));
                        bottomInfoLayout.setVisibility(View.VISIBLE);
                    });
        });

    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
