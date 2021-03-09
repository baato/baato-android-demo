package com.baato.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

        Mapbox.getInstance(this, null);
        //add your map style url here
        mapView.setStyleUrl(getString(R.string.base_url)+ "styles/retro?key=" + getString(R.string.baato_access_token));
        mapView.getMapAsync(mapboxMap ->
        {
            //remove mapbox attribute
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);

            //add your baato logo attribution here
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(250, 104);
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            params.setMargins(12, 12, 12, 12);
            ImageView imageview = new ImageView(this);
            imageview.setImageResource(R.drawable.baato_logo);
            imageview.setLayoutParams(params);
            mapView.addView(imageview);


            //add your map style url here
//            mapboxMap.setStyle(getString(R.string.base_url)+ "styles/retro?key=" + getString(R.string.baato_access_token),
//                    style -> {
//                        bottomInfoLayout.setText(Html.fromHtml("<b>Retro Map</b>" + " from Baato.io"));
//                        bottomInfoLayout.setVisibility(View.VISIBLE);
//                    });
        });
        mapView.onCreate(savedInstanceState);

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
