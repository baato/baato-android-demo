package com.example.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baatoandroiddemo.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kathmandulivinglabs.baatolibrary.models.LatLon;
import com.kathmandulivinglabs.baatolibrary.models.Place;
import com.kathmandulivinglabs.baatolibrary.models.PlaceAPIResponse;
import com.kathmandulivinglabs.baatolibrary.services.BaatoReverse;
import com.mapbox.core.constants.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static android.view.View.GONE;

public class ReverseGeoCodeActivity extends AppCompatActivity {
    private static String TAG = "ReverseGeoCodeActivity";
    private MapView mapView;
    private TextView bottomInfoLayout;
    private MapboxMap mapboxMap;
    private Icon selectedMarkerIcon;
    private MarkerOptions marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_geo_code);
        mapView = findViewById(R.id.mapView);
        bottomInfoLayout = findViewById(R.id.bottomInfoLayout);

        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyleUrl("http://baato.io/api/v1/styles/retro?key=" + getString(R.string.baato_access_token), style -> {
            mapView.setVisibility(View.VISIBLE);
            setupMap(mapboxMap);
            bottomInfoLayout.setVisibility(View.VISIBLE);
        }));
    }

    private void setupMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        Drawable drawabled = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
        Bitmap dBitmap = BitmapUtils.getBitmapFromDrawable(drawabled);
        assert dBitmap != null;
        IconFactory mIconFactory = IconFactory.getInstance(this);
        selectedMarkerIcon = mIconFactory.fromBitmap(dBitmap);

        mapboxMap.addOnMapClickListener(point -> {
            updateMarkerPosition(point);
        });
    }

    private void updateMarkerPosition(LatLng point) {
        if (marker == null) {
            marker = new MarkerOptions().icon(selectedMarkerIcon).position(point);
            mapboxMap.addMarker(marker);
        } else if (!mapboxMap.getMarkers().isEmpty()) {
            Marker marker = mapboxMap.getMarkers().get(0);
            marker.setPosition(point);
            mapboxMap.updateMarker(marker);
        }

        getAddressFromLibrary(point);
    }

    private void getAddressFromLibrary(LatLng position) {
        new BaatoReverse(this)
                .setLatLon(new LatLon(position.getLatitude(), position.getLongitude()))
                .setAccessToken(getString(R.string.baato_access_token))
                .setRadius(2)
                .withListener(new BaatoReverse.BaatoReverseRequestListener() {
                    @Override
                    public void onSuccess(PlaceAPIResponse places) {
                        if (!places.getData().isEmpty()) {
                            Place place=places.getData().get(0);
                            bottomInfoLayout.setText(Html.fromHtml("<b><h6>"+place.getName()+"</h6></b>"+place.getAddress()));
                        } else {
                            bottomInfoLayout.setText("No address found!");
                        }
                    }

                    @Override
                    public void onFailed(Throwable error) {
                        bottomInfoLayout.setText(error.getMessage());
                    }
                })
                .doRequest();
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
