package com.saferoutesapp.saferoutesapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.LocationManager;
import android.location.LocationListener;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
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


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GlobalConst {

    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    final private String TAG = "MapsActivity";
    ArrayList<LatLng> MarkerPoints;
    public String starredLocationEnabled = "false";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        LatLng src = bundle.getParcelable("src");
        LatLng dest = bundle.getParcelable("dest");

        MarkerPoints = new ArrayList<>();
        MarkerPoints.add(src);
        MarkerPoints.add(dest);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
        };

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

        // Add a marker in Sydney and move the camera
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        LatLng starred_position = bundle.getParcelable("starred_position");
        String starred_address = bundle.getString("starred_address");

        if(starred_position != null){
            System.out.println(starred_address);
            System.out.println(starred_position);
            mMap.addMarker(new MarkerOptions().position(starred_position).title(starred_address));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(starred_position));
            mMap.setTrafficEnabled(true);
        }
        else {
            // Add markers
            LatLng src = MarkerPoints.get(0);
            LatLng dest = MarkerPoints.get(1);
            // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void onMapClick(final LatLng latLng) {


        //Getting address for Marker which will be set as title
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

        /*
        Send Post Request to Backend
        {"place":{"lat":"37.4220","long":"-122.0841"}}
         */
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST, "https://api.myjson.com/bins/si253/starred",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Posted This is the response");
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
                System.out.println(error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if (latLng != null) {
                    params.put("latitude", Double.toString(latLng.latitude));
                    params.put("latitude", Double.toString(latLng.longitude));
                } else {
                    params.put("latitude", Double.toString(0.0));
                    params.put("latitude", Double.toString(0.0));
                }
                return params;
            }
        };
        queue.add(sr);
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
            Log.d(TAG, "------****"+result);
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

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
            Log.d("downloadUrl", data.toString());
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
     * A class to parse the Google Places in JSON format
     myMap.setTrafficEnabled(true);
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
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
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
                lineOptions.width(15);
                float[] hsv = new float[3];
                lineOptions.color(Color.argb(192, 21, 153, 219));

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

           // }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
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
        String url = SERVER + "/routes?" + parameters;
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
