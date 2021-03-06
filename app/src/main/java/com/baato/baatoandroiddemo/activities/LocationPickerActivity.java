package com.baato.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.baatoandroiddemo.R;
import com.baato.baatolibrary.models.LatLon;
import com.baato.baatolibrary.models.Place;
import com.baato.baatolibrary.models.PlaceAPIResponse;
import com.baato.baatolibrary.services.BaatoReverse;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

/**
 * Click and put a marker at a specific location and then perform
 * reverse geocoding to retrieve and display the location's address
 */
public class LocationPickerActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView bottomInfoLayout;
    private MapboxMap mapboxMap;
    private Icon selectedMarkerIcon;
    private MarkerOptions marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);
        mapView = findViewById(R.id.mapView);
        bottomInfoLayout = findViewById(R.id.bottomInfoLayout);

        Mapbox.getInstance(this,null);
        //set your map style url here
        mapView.setStyleUrl(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token));
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
            mapboxMap.setStyleUrl(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token),
                    style -> {
                        this.mapboxMap = mapboxMap;
                        mapView.setVisibility(View.VISIBLE);
                        initClickedMarker(mapboxMap);
                        bottomInfoLayout.setVisibility(View.VISIBLE);
                    });
        });
        mapView.onCreate(savedInstanceState);
    }

    // Initialize, but don't show, a symbol for the marker icon which will represent a selected location.
    private void initClickedMarker(MapboxMap mapboxMap) {
        Drawable drawabled = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
        Bitmap dBitmap = BitmapUtils.getBitmapFromDrawable(drawabled);
        assert dBitmap != null;
        IconFactory mIconFactory = IconFactory.getInstance(this);
        selectedMarkerIcon = mIconFactory.fromBitmap(dBitmap);

        //listen on map tapped events
        mapboxMap.addOnMapClickListener(point ->
                updateMarkerPosition(point));
    }

    //update tapped marker position
    private void updateMarkerPosition(LatLng point) {
        if (marker == null) {
            marker = new MarkerOptions().icon(selectedMarkerIcon).position(point);
            mapboxMap.addMarker(marker);
        } else if (!mapboxMap.getMarkers().isEmpty()) {
            Marker marker = mapboxMap.getMarkers().get(0);
            marker.setPosition(point);
            mapboxMap.updateMarker(marker);
        }
        moveCameraTo(point);
        // Use the tapped coordinates to make a reverse geocoding search
        getAddressFromLibrary(point);
    }

    private void moveCameraTo(LatLng point) {
        double zoom = mapboxMap.getCameraPosition().zoom;
        if (zoom < 10)
            zoom = 13;
        else if (zoom < 13)
            zoom = 15;
        else if (zoom < 15)
            zoom = zoom + 1;
        else if (zoom < 18)
            zoom = zoom + 1;
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom), 300);
    }

    /**
     * This method is used to perform reverse geocode where the user has tapped on the map.
     *
     * @param point The location to use for the search
     */
    private void getAddressFromLibrary(LatLng point) {
        new BaatoReverse(this)
                .setLatLon(new LatLon(point.getLatitude(), point.getLongitude()))
                .setAccessToken(getString(R.string.baato_access_token))
                .setRadius(2)
                .withListener(new BaatoReverse.BaatoReverseRequestListener() {
                    @Override
                    public void onSuccess(PlaceAPIResponse places) {
                        Log.d("TAG", "onSuccess:places "+places.toString());
                        // If the geocoder returns a result, we take the first in the list and show the address with the place name.
                        if (!places.getData().isEmpty()) {
                            Place place = places.getData().get(0);
                            bottomInfoLayout.setText(Html.fromHtml("<b><h6>" + place.getName() + "</h6></b>" + place.getAddress()));
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
