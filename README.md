# Baato Android Demo app
![Baato Splash](/baato_splash.png)

This is a public demo of the Baato App for Android. The demo app shows off the usages of the APIs [baato.io](http://baato.io/) offer with the use of our [java-client](https://github.com/baato/java-client) library.

Visit [the overview page ](http://baato.io:8081/#/v1/libraries/java-client) to get started using the Baato library for Android in your Android project.

### Running locally

#### 1. Setting the Baato access token
This demo app requires a Baato account and a Baato access token. Obtain your access token on the [Baato account page](http://baato.io/). Paste your access token into ``` strings.xml```.

```
<string name="baato_access_token">PASTE_YOUR_TOKEN_HERE</string>
```
#### 2. Baato logo attribution
NOTE: We highly request you to include Baato logo in the map view while using any of our Baato map styles. So, please download the Baato logo from [this link](https://i.postimg.cc/k5DpLQKQ/baato-Logo.png). Add the downloaded image to your res/drawable folder and follow the steps as mentioned in the code snippet below.

```
    mapView = findViewById(R.id.mapView);

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

    });
    mapView.onCreate(savedInstanceState);
```
If you're still having issues, please feel free to contact us at support@baato.io.

#### Built With

* [Mapbox](https://www.mapbox.com/) - Used maps sdk to load our baato map styles.
