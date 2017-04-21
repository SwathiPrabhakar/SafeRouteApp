package com.saferoutesapp.saferoutesapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.LocationManager;
import android.location.LocationListener;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, OnMapClickListener, GlobalConst {

    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    final private String TAG = "MapsActivity";
    ArrayList<LatLng> MarkerPoints;
    public String starredLocationEnabled = "false";
    public static final String MY_PREFS = "saferoutes";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        LatLng src = null;
        LatLng dest = null;
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        MarkerPoints = new ArrayList<>();
        if (bundle != null) {
            src = bundle.getParcelable("src");
            dest = bundle.getParcelable("dest");
            MarkerPoints.add(src);
            MarkerPoints.add(dest);
        }

        //locationManager.requestLocationUpdates("gps", 0, 0, locationListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //Checking acitivity initiated from starred location
        if (getIntent().hasExtra("starredLocationEnabled")) {
            starredLocationEnabled = getIntent().getStringExtra("starredLocationEnabled");
            if (starredLocationEnabled == null) {
                Log.e("starredLocationEnabled", "null");

            }
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        if (!MarkerPoints.isEmpty()) {
            LatLng src = MarkerPoints.get(0);
            LatLng dest = MarkerPoints.get(1);
            mMap.addMarker(new MarkerOptions().position(src));
            mMap.addMarker(new MarkerOptions().position(dest));

            // Getting URL to the Google Directions API
            String url = getUrl(src, dest);
            Log.d(TAG, url.toString());
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(src));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    public void onMapClick(final LatLng latLng) {
        //Getting address for Marker which will be set as title
        System.out.println("map click");
        String markerTitle = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address markerAddress = null;
            if (addresses.size() > 0) {
                markerAddress = addresses.get(0);
            }
            if (markerAddress == null && latLng != null) {
                markerTitle = latLng.toString();
                Toast.makeText(this, latLng.toString(), Toast.LENGTH_SHORT).show();
            } else {
                markerTitle = markerAddress.getThoroughfare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(markerTitle);
        mMap.addMarker(new MarkerOptions().position(latLng).title(markerTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Map<String, String> params = new HashMap<String, String>();
        if (latLng != null) {
            params.put("lat", Double.toString(latLng.latitude));
            params.put("lng", Double.toString(latLng.longitude));
        } else {
            params.put("lat", Double.toString(0.0));
            params.put("lng", Double.toString(0.0));
        }
        /*
        Send Post Request to Backend
        {"place":{"lat":"37.4220","long":"-122.0841"}}
         */

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request_json = new JsonObjectRequest(BASE_URL + "/starred/", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Process os success response
                        System.out.println("Posted latitude longitude respose on starred location" + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                String token = prefs.getString("authtoken", null);
                System.out.print(token.toString());
                headers.put("Authorization", "Token " + token);
                return headers;
            }
        };
        queue.add(request_json);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";
            JSONObject json = null;

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Log.d(TAG, "------****" + result);
            ParserTask parserTask = new ParserTask();
            ParserTask1 parserTask1 = new ParserTask1();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
            parserTask1.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Plotting the routes
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                // Starts parsing data
                routes = parser.parse1(jObject);

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
           /* for (int i = 0; i < result.size(); i++) {*/
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            if (result != null) {
                if (result.size() > 0) {
                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(0);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(20);
                    lineOptions.color(Color.argb(192, 21, 153, 219));

                    Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                    // }

                    // Drawing polyline in the Google Map for the i-th route
                    if (lineOptions != null) {
                        mMap.addPolyline(lineOptions);

                    } else {
                        Log.d("onPostExecute", "without Polylines drawn");
                    }
                }
            }
        }
    }

    /**
     * Plotting the routes
     */
    private class ParserTask1 extends AsyncTask<String, Integer, List<List<LatLng>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<LatLng>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<LatLng>> crimeSpots = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                // Starts parsing data
                crimeSpots = parser.parse2(jObject);

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return crimeSpots;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<LatLng>> result) {

            if (result != null) {
                if (result.size() > 0) {
                    List<LatLng> crimeCoords = result.get(0);

                    for (int j = 0; j < crimeCoords.size(); j++) {
                        mMap.addMarker(new MarkerOptions()
                                .position(crimeCoords.get(j))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.crime_image)));
                    }
                }
            }
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "frm=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "to=" + dest.latitude + "," + dest.longitude;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest;

        // Building the url to the web service
        //http://127.0.0.1:5000/routes/?frm=33.416565,-111.925015&to=33.418000, -111.931827
        String url = BASE_URL + "/routes?" + parameters;
        //return "http://ziptasticapi.com/85281";
        return url;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

}
