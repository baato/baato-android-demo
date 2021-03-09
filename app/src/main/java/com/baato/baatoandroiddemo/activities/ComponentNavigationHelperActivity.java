package com.baato.baatoandroiddemo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.baato.baatolibrary.models.DirectionsAPIResponse;
import com.baato.baatolibrary.services.BaatoRouting;
import com.example.baatoandroiddemo.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.camera.DynamicCamera;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;
import java.util.Locale;
import com.mapbox.services.android.navigation.ui.v5.SoundButton;

public class ComponentNavigationHelperActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, ProgressChangeListener,
        MilestoneEventListener, OffRouteListener {

    private static final int FIRST = 0;
    private static final int ONE_HUNDRED_MILLISECONDS = 100;
    private static final int BOTTOMSHEET_PADDING_MULTIPLIER = 4;
    private static final int TWO_SECONDS_IN_MILLISECONDS = 2000;
    private static final double BEARING_TOLERANCE = 90d;
    private static final String LONG_PRESS_MAP_MESSAGE = "Long press the map to select a destination.";
    private static final String SEARCHING_FOR_GPS_MESSAGE = "Searching for GPS...";
    private static final int ZERO_PADDING = 0;
    private static final double DEFAULT_ZOOM = 12.0;
    private static final double DEFAULT_TILT = 0d;
    private static final double DEFAULT_BEARING = 0d;
    private static final int ONE_SECOND_INTERVAL = 1000;

    ConstraintLayout navigationLayout;
    MapView mapView;
    InstructionView instructionView;
    SummaryBottomSheet summaryBottomSheet;

    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private MapboxNavigation navigation;
    private NavigationSpeechPlayer speechPlayer;
    private NavigationMapboxMap navigationMap;
    private Location lastLocation;
    private DirectionsRoute route;
    private Point destination;
    private MapState mapState;
    private MapboxMap mapboxMap;
    private String navigationMode = "car";
    private Point origin;
    DirectionsResponse directionsResponse;
    Location initatingLoacation;
    private SoundButton soundButton;

    private enum MapState {
        INFO,
        NAVIGATION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For styling the InstructionView
        setTheme(R.style.CustomInstructionView);
        setContentView(R.layout.activity_component_navigation);
        Mapbox.getInstance(getApplicationContext(),null);

        mapView = findViewById(R.id.mapView);
        navigationLayout = findViewById(R.id.componentNavigationLayout);
        instructionView = findViewById(R.id.instructionView);
        summaryBottomSheet = findViewById(R.id.summaryBottomSheet);
        soundButton = instructionView.findViewById(R.id.soundLayout);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            directionsResponse = (DirectionsResponse) extras.get("Route");
            route = directionsResponse.routes().get(0);
            origin = (Point) extras.get("origin");
            destination = (Point) extras.get("destination");
            lastLocation = (Location) extras.get("lastLocation");
            initatingLoacation = lastLocation;
            // and get whatever type user account id is
        }

        //add your baato logo attribution here
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(250, 104);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params.setMargins(12, 12, 12, 12);
        ImageView imageview = new ImageView(this);
        imageview.setImageResource(R.drawable.baato_logo);
        imageview.setLayoutParams(params);
        mapView.addView(imageview);

        mapView.setStyleUrl(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token));
        mapView.onCreate(savedInstanceState);

        // Will call onMapReady
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        //remove mapbox attribute
        mapboxMap.getUiSettings().setAttributionEnabled(false);
        mapboxMap.getUiSettings().setLogoEnabled(false);

        mapboxMap.setStyleUrl(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token),
                style -> {
                    mapboxMap.setCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(origin.latitude(), origin.longitude()))
                            .zoom(14)
                            .build());
                    mapState = MapState.NAVIGATION;
                    navigationMap = new NavigationMapboxMap(mapView, mapboxMap);
                    // For voice instructions
                    initializeSpeechPlayer();

                    // For Location updates
                    initializeLocationEngine();

                    // For navigation logic / processing
                    initializeNavigation(mapboxMap);
//        handleRoute(directionsResponse, false);
                });

    }

    public void onNavigationReady() {
        // Transition to navigation state
        mapState = MapState.NAVIGATION;

        // Show the InstructionView
        TransitionManager.beginDelayedTransition(navigationLayout);
        instructionView.setVisibility(View.VISIBLE);
        summaryBottomSheet.setVisibility(View.VISIBLE);

        // Start navigation
        adjustMapPaddingForNavigation();
        navigation.startNavigation(route);

        if (soundButton.hasOnClickListeners()) {
            soundButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundButton.toggleMute();
                }
            });
        } else {
            soundButton.addOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundButton.toggleMute();
                }
            });
        }
    }

    /*
     * LocationEngine listeners
     */

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation == null) {
            // Move the navigationMap camera to the first Location
            moveCameraTo(location);

            // Allow navigationMap clicks now that we have the current Location
            showSnackbar("Please wait...", BaseTransientBottomBar.LENGTH_LONG);
        }

        // Cache for fetching the route later
        updateLocation(location);
    }

    /*
     * Navigation listeners
     */

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        // Cache "snapped" Locations for re-route Directions API requests
        updateLocation(location);

        // Update InstructionView data from RouteProgress
        instructionView.update(routeProgress);
        summaryBottomSheet.update(routeProgress);
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
        playAnnouncement(milestone);
    }

    @Override
    public void userOffRoute(Location location) {
        calculateRouteWith(destination, true);
    }

    /*
     * Activity lifecycle methods
     */

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (navigationMap != null) {
            navigationMap.onStart();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (navigationMap != null) {
            navigationMap.onStop();
        }
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

        // Ensure proper shutdown of the SpeechPlayer
        if (speechPlayer != null) {
            speechPlayer.onDestroy();
        }

        // Prevent leaks
        removeLocationEngineListener();

        ((DynamicCamera) navigation.getCameraEngine()).clearMap();
        // MapboxNavigation will shutdown the LocationEngine
        navigation.onDestroy();
    }

    private void initializeSpeechPlayer() {
        String english = Locale.US.getLanguage();
        String accessToken = "pk.xxx";
        SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(getApplication(), english, true, accessToken);
        speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
    }

    private void initializeLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainLocationEngineBy(LocationEngine.Type.ANDROID);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.addLocationEngineListener(this);
        locationEngine.setFastestInterval(ONE_SECOND_INTERVAL);
        locationEngine.activate();
    }

    private void initializeNavigation(MapboxMap mapboxMap) {
        navigation = new MapboxNavigation(this, "pk.xxx");
        navigation.setLocationEngine(locationEngine);
        navigation.setCameraEngine(new DynamicCamera(mapboxMap));
        navigation.addProgressChangeListener(this);
        navigation.addMilestoneEventListener(this);
        navigation.addOffRouteListener(this);
        navigationMap.addProgressChangeListener(navigation);
        navigationMap.resetCameraPosition();
        onNavigationReady();
    }

    private void showSnackbar(String text, int duration) {
        Snackbar.make(navigationLayout, text, duration).show();
    }

    private void playAnnouncement(Milestone milestone) {
        if (milestone instanceof VoiceInstructionMilestone) {
            SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                    .voiceInstructionMilestone((VoiceInstructionMilestone) milestone)
                    .build();
            Log.d("Announcement", announcement.toString());
            speechPlayer.play(announcement);
        }
    }

    private void updateLocation(Location location) {
        lastLocation = location;
        navigationMap.updateLocation(location);
    }

    private void moveCameraTo(Location location) {
        CameraPosition cameraPosition = buildCameraPositionFrom(location, location.getBearing());
        navigationMap.retrieveMap().animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
        );
    }

    private void moveCameraToInclude(Point destination) {
        LatLng origin = new LatLng(lastLocation);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(new LatLng(destination.latitude(), destination.longitude()))
                .build();
        Resources resources = getResources();
        int routeCameraPadding = 56;
        int[] padding = {routeCameraPadding, routeCameraPadding, routeCameraPadding, routeCameraPadding};
        CameraPosition cameraPosition = navigationMap.retrieveMap().getCameraForLatLngBounds(bounds, padding);
        navigationMap.retrieveMap().animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
        );
    }

    private void moveCameraOverhead() {
        if (lastLocation == null) {
            return;
        }
        CameraPosition cameraPosition = buildCameraPositionFrom(lastLocation, DEFAULT_BEARING);
        navigationMap.retrieveMap().animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
        );
    }

    @NonNull
    private CameraPosition buildCameraPositionFrom(Location location, double bearing) {
        return new CameraPosition.Builder()
                .zoom(DEFAULT_ZOOM)
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(bearing)
                .tilt(DEFAULT_TILT)
                .build();
    }

    private void adjustMapPaddingForNavigation() {
        Resources resources = getResources();
        int mapViewHeight = mapView.getHeight();
        int bottomSheetHeight = summaryBottomSheet.getHeight();
        int topPadding = mapViewHeight - (bottomSheetHeight * BOTTOMSHEET_PADDING_MULTIPLIER);
        navigationMap.retrieveMap().setPadding(ZERO_PADDING, topPadding, ZERO_PADDING, ZERO_PADDING);
    }

    private void resetMapAfterNavigation() {
        navigationMap.removeRoute();
        navigationMap.clearMarkers();
        navigation.stopNavigation();
        moveCameraOverhead();
    }

    private void removeLocationEngineListener() {
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
        }
    }

    private void addLocationEngineListener() {
        if (locationEngine != null) {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void calculateRouteWith(Point destination, boolean isOffRoute) {
        Point origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
        Double bearing = Float.valueOf(lastLocation.getBearing()).doubleValue();
        getRoute(origin, destination, isOffRoute);
    }

    private void getRoute(Point origin, Point destination, boolean isOffRoute) {
        String[] points = new String[2];
        points[0] = origin.latitude() + "," + origin.longitude();
        points[1] = destination.latitude() + "," + destination.longitude();

        new BaatoRouting(this)
                .setPoints(points)
                .setAccessToken(getString(R.string.baato_access_token))
                .setMode(navigationMode) //eg bike, car, foot
                .setAlternatives(false) //optional parameter
                .setInstructions(true) //optional parameter
                .withListener(new BaatoRouting.BaatoRoutingRequestListener() {
                    @Override
                    public void onSuccess(DirectionsAPIResponse directionResponse) {
                        com.baato.baatolibrary.models.NavResponse navResponse = directionResponse.getData().get(0);
                        double distanceInKm = navResponse.getDistanceInMeters() / 1000;
                        long time = navResponse.getTimeInMs() / 1000;

                        String parsedNavigationResponse = BaatoRouting.getParsedNavResponse(directionResponse, navigationMode, getApplicationContext());
                        DirectionsResponse directionsResponse = DirectionsResponse.fromJson(parsedNavigationResponse);
                        route = directionsResponse.routes().get(0);
                        handleRoute(directionsResponse, isOffRoute);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        if (t.getMessage() != null && t.getMessage().contains("Failed to connect"))
                            Toast.makeText(getApplicationContext(), "Please connect to internet to get the routes!", Toast.LENGTH_SHORT).show();

                    }
                })
                .doRequest();
    }

    private void handleRoute(DirectionsResponse response, boolean isOffRoute) {
        List<DirectionsRoute> routes = response.routes();
        if (!routes.isEmpty()) {
            route = routes.get(FIRST);
            navigationMap.drawRoute(route);
            if (isOffRoute) {
                navigation.startNavigation(route);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ONE_HUNDRED_MILLISECONDS, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(ONE_HUNDRED_MILLISECONDS);
        }
    }

}
