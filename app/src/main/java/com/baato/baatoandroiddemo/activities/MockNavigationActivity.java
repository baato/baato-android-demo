package com.baato.baatoandroiddemo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.baato.baatoandroiddemo.interfaces.Constants;
import com.baato.baatolibrary.models.NavResponse;
import com.example.baatoandroiddemo.R;
import com.baato.baatoandroiddemo.helpers.TimeCalculation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.baato.baatolibrary.models.DirectionsAPIResponse;
import com.baato.baatolibrary.models.LatLon;
import com.baato.baatolibrary.models.Place;
import com.baato.baatolibrary.models.PlaceAPIResponse;
import com.baato.baatolibrary.services.BaatoReverse;
import com.baato.baatolibrary.services.BaatoRouting;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.baato.baatolibrary.utilities.BaatoUtil.decodePolyline;

public class MockNavigationActivity extends AppCompatActivity implements PermissionsListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    List<Point> points;
    LinearLayout bottomInfoLayout;
    Button btnGo;
    TextView bottomText;

    private DirectionsRoute currentRoute;
    private LocationLayerPlugin locationLayer;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private String navMode = "car";
    private static DecimalFormat df = new DecimalFormat("0.00");
    private Point originPoint = null, destinationPoint = null;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private final int[] padding = new int[]{50, 100, 50, 120}; //left, top, right, bottom
    private NavigationMapRoute navigationMapRoute;
    private MarkerOptions marker;
    private Icon startIcon, destinationIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mapView = findViewById(R.id.mapView);
        btnGo = findViewById(R.id.btnGo);
        bottomText = findViewById(R.id.bottomText);
        bottomInfoLayout = findViewById(R.id.bottomInfoLayout);

        Mapbox.getInstance(this,null);
        //add your map style url here
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

            mapboxMap.setStyle(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token),
                    style -> {
                        this.mapboxMap = mapboxMap;
                        initLocationEngine();
                        initLocationLayer();
                        setUpMarkers();
                        mapboxMap.addOnMapClickListener(point -> {
                            onMapClick(point);
                        });
                    });
        });
        mapView.onCreate(savedInstanceState);
    }

    private void initLocationLayer() {
        locationLayer = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationLayer.setRenderMode(RenderMode.COMPASS);
    }

    private void initLocationEngine() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            getMyLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    // this method is used to add and update marker for start point.
    private void addOriginMarker(LatLng latLng) {
        if (mapboxMap.getMarkers().isEmpty()) {
            marker = new MarkerOptions().icon(startIcon).position(new LatLng(originPoint.latitude(), originPoint.longitude()));
            mapboxMap.addMarker(marker);
            bottomInfoLayout.setVisibility(VISIBLE);
            moveCameraTo(latLng);
        } else {
            Marker marker = mapboxMap.getMarkers().get(0);
            marker.setPosition(latLng);
            mapboxMap.updateMarker(marker);
        }
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

    // this method is used to add and update marker for destination point.
    private void addDestinationMarker(LatLng point) {
        if (mapboxMap.getMarkers().size() == 2) {
            Marker marker = mapboxMap.getMarkers().get(1);
            marker.setPosition(point);
            mapboxMap.updateMarker(marker);

        } else if (mapboxMap.getMarkers().size() == 1) {
            marker = new MarkerOptions().icon(destinationIcon).position(point);
            mapboxMap.addMarker(marker);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted)
            Toast.makeText(this, "Please wait ...", Toast.LENGTH_LONG).show();
        getMyLocation();
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    permissionLocation = checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, this::onLocationChanged);
                    PendingResult result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(result1 -> {
                        final Status status = result1.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied.
                                // But could be fixed by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    status.startResolutionForResult(this,
                                            Constants.GPS_REQUEST);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied.
                                // You can initialize location requests here.
                                int permissionLocation1 = 0;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    permissionLocation1 = checkSelfPermission(
                                            Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                                if (permissionLocation1 == PackageManager.PERMISSION_GRANTED) {

                                    mylocation = LocationServices.FusedLocationApi
                                            .getLastLocation(googleApiClient);
                                    if (mylocation != null) {
                                        originPoint = Point.fromLngLat(mylocation.getLongitude(), mylocation.getLongitude());
                                        addOriginMarker(new LatLng(mylocation.getLatitude(), mylocation.getLongitude()));
                                        getAddressFromLibrary(new LatLng(mylocation.getLatitude(), mylocation.getLongitude()), "start");
                                    }
                                }

                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                //finish();
                                break;
                        }
                    });
                }
            } else googleApiClient.connect();
        } else setUpGClient();
    }

    //initialize icons for markers of start point and destination point
    private void setUpMarkers() {
        IconFactory mIconFactory = IconFactory.getInstance(this);
        Drawable drawables = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_circle_stroke, null);
        Bitmap sBitmap = BitmapUtils.getBitmapFromDrawable(drawables);
        assert sBitmap != null;
        startIcon = mIconFactory.fromBitmap(sBitmap);


        Drawable drawabled = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
        Bitmap dBitmap = BitmapUtils.getBitmapFromDrawable(drawabled);
        assert dBitmap != null;
        destinationIcon = mIconFactory.fromBitmap(dBitmap);
    }

    /**
     * This method is used to handle on Map clicked event.
     *
     * @param point The point to add marker as destination point
     */
    public void onMapClick(LatLng point) {
        try {
            btnGo.setVisibility(View.GONE);
            addDestinationMarker(point);
            destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            if (originPoint != null && destinationPoint != null) {
                getRoute(originPoint, destinationPoint, navMode);
            }
            //to get address of a tapped point
            getAddressFromLibrary(point, "destination");
        } catch (Exception e) {
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.GPS_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Please wait the GPS is locating you", Toast.LENGTH_LONG).show();
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    /**
     * This method is used to perform reverse geocode where the user has tapped on the map.
     *
     * @param point The location to use for the search
     */
    private void getAddressFromLibrary(LatLng point, String marker) {
        new BaatoReverse(this)
                .setLatLon(new LatLon(point.getLatitude(), point.getLongitude()))
                .setAccessToken(getString(R.string.baato_access_token))
                .setRadius(2)
                .withListener(new BaatoReverse.BaatoReverseRequestListener() {
                    @Override
                    public void onSuccess(PlaceAPIResponse places) {
                        // If the geocoder returns a result, we take the first in the list and show the address with the place name.
                        if (!places.getData().isEmpty()) {
                            Place place = places.getData().get(0);
                            addSnippetToMarker(marker, place);
                        } else {
                            bottomText.setText("No address found!");
                        }
                    }

                    @Override
                    public void onFailed(Throwable error) {
                        bottomText.setText(error.getMessage());
                    }
                })
                .doRequest();
    }

    private void addSnippetToMarker(String point, Place place) {
        if (!mapboxMap.getMarkers().isEmpty() && point.contains("start")) {
            Marker start = mapboxMap.getMarkers().get(0);
            start.setSnippet(place.getName() + "\n" + place.getAddress());
            mapboxMap.updateMarker(start);
        } else if (mapboxMap.getMarkers().size() > 1 && point.contains("destination")) {
            Marker dest = mapboxMap.getMarkers().get(1);
            dest.setSnippet(place.getName() + "\n" + place.getAddress());
            mapboxMap.updateMarker(dest);
        }
    }

    /**
     * This method is used to perform routing between two points.
     *
     * @param origin         The location to use as a start point
     * @param destination    The location to use as a destination point
     * @param navigationMode The mode used default is car
     */
    private void getRoute(Point origin, Point destination, String navigationMode) {
        String[] points = new String[2];
        points[0] = origin.latitude() + "," + origin.longitude();
        points[1] = destination.latitude() + "," + destination.longitude();

        new BaatoRouting(this)
                .setPoints(points)
                .setAccessToken(getString(R.string.baato_access_token))
                .setMode("car") //eg bike, car, foot
                .setAlternatives(false) //optional parameter
                .setInstructions(true) //optional parameter
                .withListener(new BaatoRouting.BaatoRoutingRequestListener() {
                    @Override
                    public void onSuccess(DirectionsAPIResponse directionResponse) {
                        // If the routing returns a result, we take the first in the list and show the route.
                        NavResponse navResponse = directionResponse.getData().get(0);
                        initRouteCoordinates(navResponse.getEncoded_polyline());
                        double distanceInKm = navResponse.getDistanceInMeters() / 1000;
                        long time = navResponse.getTimeInMs() / 1000;

                        btnGo.setVisibility(VISIBLE);
                        bottomText.setText("Distance: " + df.format(distanceInKm) + " km" +
                                "  Time: " + TimeCalculation.giveMeTimeFromSecondsFormat(time));
                        bottomInfoLayout.setVisibility(VISIBLE);

                        String parsedNavigationResponse = BaatoRouting.getParsedNavResponse(directionResponse, navigationMode, getApplicationContext());
                        DirectionsResponse directionsResponse = DirectionsResponse.fromJson(parsedNavigationResponse);
                        currentRoute = directionsResponse.routes().get(0);

                        //show the route from here
                        navMapRoute(currentRoute);

                        btnGo.setOnClickListener(v -> {
                            //start Navigation using mock location engine
                            Intent intent = new Intent(MockNavigationActivity.this, MockNavigationHelperActivity.class);
                            intent.putExtra("Route", directionsResponse);
                            intent.putExtra("origin", origin);
                            intent.putExtra("lastLocation", mylocation);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        if (t.getMessage() != null && t.getMessage().contains("Failed to connect"))
                            Toast.makeText(MockNavigationActivity.this, "Please connect to internet to get the routes!", Toast.LENGTH_SHORT).show();

                    }
                })
                .doRequest();
    }

    private void navMapRoute(DirectionsRoute myRoute) {
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        } else {
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        }
        navigationMapRoute.addRoute(myRoute);
    }

    private void initRouteCoordinates(String encoded_polyline) {
        points = new ArrayList<>();
        List<LatLng> bboxPoints = new ArrayList<>();
        for (List<Double> coordinates :
                decodePolyline(encoded_polyline, false)) {
            points.add(Point.fromLngLat(coordinates.get(1), coordinates.get(0)));
            bboxPoints.add(new LatLng(coordinates.get(0), coordinates.get(1)));
        }

        if (bboxPoints.size() > 1) {
            try {
                LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
                // left, top, right, bottom
                animateCameraBbox(bounds, CAMERA_ANIMATION_DURATION, padding);
            } catch (InvalidLatLngBoundsException exception) {
                Toast.makeText(this, R.string.error_valid_route_not_found, Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void animateCameraBbox(LatLngBounds bounds, int animationTime, int[] padding) {
        CameraPosition position = mapboxMap.getCameraForLatLngBounds(bounds, padding);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationTime);
    }

    @Override
    public void onLocationChanged(Location location) {
        originPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
        mylocation = location;
        addOriginMarker(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationLayer != null)
            locationLayer.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationLayer != null)
            locationLayer.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

