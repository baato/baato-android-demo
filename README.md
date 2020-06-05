# Baato Android Demo app
![Reverse API](/baato_splash.png)

This is a public demo of the Baato App for Android. The demo app shows off the usages of the APIs [baato.io](http://baato.io/) offer with the use of our [java-client](https://github.com/baato/java-client) library.

Visit [the overview page ](http://baato.io:8081/#/v1/libraries/java-client) to get started using the Baato library for Android in your Android project.

### Running locally

#### 1. Setting the Baato access token
This demo app requires a Baato account and a Baato access token. Obtain your access token on the [Baato account page](http://baato.io/). Paste your access token into ``` strings.xml```.

```
<string name="baato_access_token">PASTE_YOUR_TOKEN_HERE</string>
```
#### 2. Setting the Mapbox access token
This demo app uses Mapbox sdk to load maps, which require a Mapbox account and a Mapbox access token. Obtain a free access token on the [Mapbox account page](https://account.mapbox.com/access-tokens/). Paste your access token into ``` strings.xml```.

```
<string name="mapbox_token">PASTE_YOUR_TOKEN_HERE</string>
```
If you're still having issues, please feel free to contact us at support@baato.io.

### Built With

* [Mapbox](https://www.mapbox.com/) - Used to load maps.
