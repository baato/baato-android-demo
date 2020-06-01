package com.example.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.example.baatoandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
/**
 * input the baato retro map style url and load with mapbox
 */
public class RetroMapStyleActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView bottomInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_styles);

        mapView = findViewById(R.id.mapView);
        bottomInfoLayout = findViewById(R.id.bottomInfoLayout);

        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap ->
                //add your map style url here
                mapboxMap.setStyle("http://baato.io/api/v1/styles/retro?key=" + getString(R.string.baato_access_token),
                        style -> {
                            bottomInfoLayout.setText(Html.fromHtml("<b>Retro Map</b>" + " from Baato.io"));
                            bottomInfoLayout.setVisibility(View.VISIBLE);
                        }));

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
