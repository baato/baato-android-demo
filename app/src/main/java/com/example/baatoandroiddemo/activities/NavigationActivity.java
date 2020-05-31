package com.example.baatoandroiddemo.activities;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.baatoandroiddemo.R;
import com.example.baatoandroiddemo.helpers.TimeCalculation;
import com.example.baatoandroiddemo.interfaces.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.kathmandulivinglabs.baatolibrary.models.DirectionsAPIResponse;
import com.kathmandulivinglabs.baatolibrary.models.LatLon;
import com.kathmandulivinglabs.baatolibrary.models.Place;
import com.kathmandulivinglabs.baatolibrary.models.PlaceAPIResponse;
import com.kathmandulivinglabs.baatolibrary.services.BaatoReverse;
import com.kathmandulivinglabs.baatolibrary.services.BaatoRouting;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEngineProvider;
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
import static com.kathmandulivinglabs.baatolibrary.utilities.BaatoUtil.decodePolyline;
import static com.mapbox.android.core.location.LocationEnginePriority.LOW_POWER;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class NavigationActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    List<Point> points;
    LinearLayout bottomInfoLayout;
    Button btnGo;
    TextView bottomText;

    // variables for adding location layer
    private Point currentLocation;
    private DirectionsRoute currentRoute, directionsRoute;
    private LocationLayerPlugin locationLayer;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    //    private LocationComponent locationComponent;
    private static final String TAG = "DirectionsActivity";
    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private String encodedPolyline, navMode = "car", styleUrl = "";
    private ImageView[] optionButton;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private boolean showBottomNav = true;
    private Point originPoint = null, destinationPoint = null, startPoint = null;
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

        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle("http://baato.io/api/v1/styles/retro?key=" + getString(R.string.baato_access_token), style -> {
            this.mapboxMap = mapboxMap;
            initLocationEngine();
            initLocationLayer();
            setUpMarkers();
            mapboxMap.addOnMapClickListener(point -> {
                onMapClick(point);
            });
        }));
    }

    private void initLocationLayer() {
        locationLayer = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationLayer.setRenderMode(RenderMode.COMPASS);
    }

    private void initLocationEngine() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
            locationEngine.setPriority(LOW_POWER);
            locationEngine.setInterval(0);
            locationEngine.setFastestInterval(1000);
            locationEngine.addLocationEngineListener(this);
            locationEngine.activate();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (locationEngine.getLastLocation() != null) {
                Location lastLocation = locationEngine.getLastLocation();
                onLocationChanged(lastLocation);
                originPoint = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
                addOriginMarker(new LatLng(originPoint.latitude(), originPoint.longitude()));
                mylocation = lastLocation;
            } else getMyLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

    }

    private void addOriginMarker(LatLng latLng) {
        if (mapboxMap.getMarkers().isEmpty()) {
            marker = new MarkerOptions().icon(startIcon).position(new LatLng(originPoint.latitude(), originPoint.longitude()));
            mapboxMap.addMarker(marker);
            bottomInfoLayout.setVisibility(VISIBLE);
        } else {
            Marker marker = mapboxMap.getMarkers().get(0);
            marker.setPosition(latLng);
            mapboxMap.updateMarker(marker);
        }
    }

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
        if (!granted) {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
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

    @Override
    public void onConnected(Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

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
                                Log.d(TAG, "getMyLocation: up");
                                if (permissionLocation1 == PackageManager.PERMISSION_GRANTED) {
                                    Log.d(TAG, "getMyLocation: +granted");

                                    mylocation = LocationServices.FusedLocationApi
                                            .getLastLocation(googleApiClient);
                                    Log.d(TAG, "getMyLocation: +my"+mylocation);
                                    if (mylocation != null) {
                                        originPoint = Point.fromLngLat(mylocation.getLongitude(), mylocation.getLongitude());
                                        addOriginMarker(new LatLng(mylocation.getLatitude(), mylocation.getLongitude()));
                                        getAddressFromLibrary(new LatLng(mylocation.getLatitude(), mylocation.getLongitude()), "start");
                                        Log.d(TAG, "getMyLocation:my " + mylocation.getLatitude() + mylocation.getLongitude());
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

    public void onMapClick(LatLng point) {
        try {
            btnGo.setVisibility(View.GONE);
            addDestinationMarker(point);
            destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            if (originPoint != null && destinationPoint != null) {
                getRoute(originPoint, destinationPoint, navMode);
            }
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

    private void getAddressFromLibrary(LatLng position, String point) {
        new BaatoReverse(this)
                .setLatLon(new LatLon(position.getLatitude(), position.getLongitude()))
                .setAccessToken(getString(R.string.baato_access_token))
                .setRadius(2)
                .withListener(new BaatoReverse.BaatoReverseRequestListener() {
                    @Override
                    public void onSuccess(PlaceAPIResponse places) {
                        if (!places.getData().isEmpty()) {
                            Place place = places.getData().get(0);
                            addSnippet(point, place);
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

    private void addSnippet(String point, Place place) {
        if (point.contains("start")) {
            Marker start = mapboxMap.getMarkers().get(0);
            start.setSnippet(place.getName() + "\n" + place.getAddress());
            mapboxMap.updateMarker(start);
        } else if (point.contains("destination")) {
            Marker dest = mapboxMap.getMarkers().get(1);
            dest.setSnippet(place.getName() + "\n" + place.getAddress());
            mapboxMap.updateMarker(dest);
        }
    }


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
                        com.kathmandulivinglabs.baatolibrary.models.NavResponse navResponse = directionResponse.getData().get(0);
                        initRouteCoordinates(navResponse.getEncoded_polyline());
                        double distanceInKm = navResponse.getDistanceInMeters() / 1000;
                        long time = navResponse.getTimeInMs() / 1000;

                        btnGo.setVisibility(VISIBLE);
                        bottomText.setText("Distance: " + df.format(distanceInKm) + " km" +
                                "  Time: " + TimeCalculation.giveMeTimeFromSecondsFormat(time));
                        bottomInfoLayout.setVisibility(VISIBLE);

                        String parsedNavigationResponse = BaatoRouting.getParsedNavResponse(directionResponse, navigationMode);
                        DirectionsResponse directionsResponse = DirectionsResponse.fromJson(parsedNavigationResponse);
                        currentRoute = directionsResponse.routes().get(0);

                        //show the route from here
                        navMapRoute(currentRoute);

                        btnGo.setOnClickListener(v -> {
                            boolean simulateRoute = false;
                            Intent intent = new Intent(NavigationActivity.this, MockNavigationActivity.class);
                            intent.putExtra("Route", directionsResponse);
                            intent.putExtra("origin", origin);
                            intent.putExtra("lastLocation", mylocation);
                            startActivity(intent);
                            //this is the actual library method
//                            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                    .directionsRoute(currentRoute)
//                                    .shouldSimulateRoute(simulateRoute)
//                                    .build();
//                            NavigationLauncher.startNavigation(NavigationActivity.this, options);
                        });
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        if (t.getMessage() != null && t.getMessage().contains("Failed to connect"))
                            Toast.makeText(NavigationActivity.this, "Please connect to internet to get the routes!", Toast.LENGTH_SHORT).show();

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

    private LatLng giveMeLatLong(Point originPoint) {
        return new LatLng(originPoint.latitude(), originPoint.longitude());
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        originPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
        mylocation = location;
        addOriginMarker(new LatLng(location.getLatitude(), location.getLongitude()));
//        mapboxMap.addMarker(startPoint);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

