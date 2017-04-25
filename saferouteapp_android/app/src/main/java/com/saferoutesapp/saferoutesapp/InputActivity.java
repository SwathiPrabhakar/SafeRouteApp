package com.saferoutesapp.saferoutesapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.net.TrafficStatsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.BroadcastReceiver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.util.Log;
import android.Manifest;
import android.widget.Toast;

import com.loopj.android.http.*;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * Created by avniv on 4/14/2017.
 */


public class InputActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GlobalConst {
    final private String TAG = this.getClass().getSimpleName();
    private LocationListener listener;
    private LocationManager locationManager;
    private LatLng src = null;
    private LatLng dest = null;
    private Geocoder geocoder;
    private AutoCompleteTextView sourceACTextView;
    private AutoCompleteTextView destinationACTextView;
    private ArrayList<String> placeIDs;
    static ArrayList<String> addresses = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 100;
    public static final String MY_PREFS = "saferoutes";
    private int GPS_REQUEST = 10;
    private boolean srcSetFlag = false;
    private boolean destSetFlag = false;
    private Location srcLocation;
    public static String current_location = null;
    public static LatLng current_location_lat_long= null;
    public static ArrayList<String> evenText = new ArrayList<String>();
    public static ArrayList<String> fullDesc = new ArrayList<String>();

    //google api client
    private GoogleApiClient mGoogleApiClient;

    //Signin constant to check the activity result

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_activity);
        destinationACTextView = (AutoCompleteTextView) findViewById(R.id.to_input);
        sourceACTextView = (AutoCompleteTextView) findViewById(R.id.from_input);
        placeIDs = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();

        logIn();

        // getting the source location
        if (!runtime_permissions())
            enable_buttons();

        if (!srcSetFlag) {
            // Home : 33.4284328,-111.9501358
            // Nobel Library : 33.4201427,-111.9285955
            src = new LatLng(33.4201427,-111.9285955);
            srcSetFlag = true;
            sourceACTextView.setText(coordinatesToAddress(src));
            destinationACTextView.setText(coordinatesToAddress(src));
        }
            // Using push notification to get the destination
            Bundle bundleGCM = getIntent().getExtras();
        if (bundleGCM == null) {
            Intent i = new Intent(this, GCMRegistrationIntentService.class);
            startService(i);
        }

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        if (bundle != null) {
            LatLng starred_position = bundle.getParcelable("starred_position");
            String starred_address = bundle.getString("starred_address");

            if (starred_position != null) {
                destinationACTextView.setText(starred_address);
            }
        }

        // Destination box: Drop down adapter and click listener
        destinationACTextView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        destinationACTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocationName(description, 1);
                    Address addr1 = addressList.get(0);
                    dest = new LatLng(addr1.getLatitude(), addr1.getLongitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        // Destination box: Drop down adapter and click listener
        sourceACTextView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        sourceACTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocationName(description, 1);
                    Address addr1 = addressList.get(0);
                    src = new LatLng(addr1.getLatitude(), addr1.getLongitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void pushNotificationCalc() {
        srcLocation = new Location("");
        if (src != null) {
            srcLocation.setLatitude(src.latitude);
            srcLocation.setLongitude(src.longitude);
        }
        // Using push notification to get the destination
        Bundle bundleGCM = getIntent().getExtras();
        if (bundleGCM != null) {
            int maxCount = Integer.MIN_VALUE;
            try {
                String message = bundleGCM.getString("message");
                JSONObject json = new JSONObject(message);
                JSONArray jArray = json.getJSONArray("result");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject objectInArray = jArray.getJSONObject(i);

                    int count = Integer.parseInt(objectInArray.getString("count"));
                    LatLng tempSrc = new LatLng(Double.parseDouble(objectInArray.getString("src_lat")), Double.parseDouble(objectInArray.getString("src_lng")));
                    Location tempSrcLoc = new Location("");
                    tempSrcLoc.setLatitude(tempSrc.latitude);
                    tempSrcLoc.setLongitude(tempSrc.longitude);
                    LatLng tempDest = new LatLng(Double.parseDouble(objectInArray.getString("dest_lat")), Double.parseDouble(objectInArray.getString("dest_lng")));

                    double distance = 1000.00;
                    if (srcSetFlag)
                        distance = (double) tempSrcLoc.distanceTo(srcLocation) / (double) 1609;

                    Toast.makeText(this, "" + distance + srcSetFlag, Toast.LENGTH_LONG).show();

                    if (distance <= 0.5 && count > maxCount) {
                        dest = tempDest;
                        maxCount = count;
                        destSetFlag = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (destSetFlag)
            destinationACTextView.setText(coordinatesToAddress(dest));
    }

    private void logIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void logOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
    }

    private void enable_buttons() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Flag -------------------- " + srcSetFlag);
                if (!srcSetFlag) {
                    src = new LatLng(location.getLatitude(), location.getLongitude());
                    String finalAddress = coordinatesToAddress(src);
                    current_location = finalAddress;
                    current_location_lat_long = src ;
                    sourceACTextView.setText(finalAddress);
                    srcSetFlag = true;
                    pushNotificationCalc();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, listener);

    }

    // returns Address from latitude and longitude
    public String coordinatesToAddress(LatLng input) {
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geocoder.getFromLocation(input.latitude, input.longitude, 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }

        return new String(builder);
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }

    //################### TASK-1: Front end of Authentication Layer Backend and Android ################
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.print("Signinbef");
        super.onActivityResult(requestCode, resultCode, data);
        System.out.print("Signin");
        if (requestCode == RC_SIGN_IN) {
            System.out.print("Signin requestcode");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        if (resultCode == RESULT_OK && requestCode == GPS_REQUEST) {
            if (data.hasExtra("bundle")) {
                Bundle bundle = getIntent().getParcelableExtra("bundle");
                if (bundle != null) {
                    src = bundle.getParcelable("src");
                    Toast.makeText(this, Double.toString(src.latitude), Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();

            handleBackendSignIn(this, acct.getEmail(), acct.getId());
            Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show();
            //inserting values in shared preference for profile page
            SharedPreferences.Editor editorProfile = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
            System.out.println("here");
            System.out.println(acct.getEmail());
            System.out.println(acct.getDisplayName());
            editorProfile.putString("user_email", acct.getEmail());
            editorProfile.putString("user_name", acct.getDisplayName());
            editorProfile.putString("photo_url", acct.getPhotoUrl().toString());
            editorProfile.commit();

//            dummyGetWithToken(this);

        } else {
            //If login fails
            System.out.print("Failed ");
            Log.e("result", result.toString());
            Toast.makeText(this, "Login Failed!!!!!!!!" + result.toString(), Toast.LENGTH_LONG).show();
            if (result != null)
                System.out.print(result.toString());

            logIn();
        }
    }

    public void dummyGetWithToken(final Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest req = new JsonObjectRequest(BASE_URL + "/blog/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString(4));
                            System.out.print(response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        queue.add(req);

    }

    public void handleBackendSignIn(final Context context, final String email, final String id) {
        RequestQueue queue = Volley.newRequestQueue(context);
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("uid", id);

        JsonObjectRequest request_json = new JsonObjectRequest(BASE_URL + "/auth/register/", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Process os success response
                            System.out.println("Posted This is the response");
                            String token = response.getString("token");
                            System.out.println(token);
                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                            editor.putString("authtoken", token);
                            editor.commit();
                            dummyGetWithToken(context);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        queue.add(request_json);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    public void postData(LatLng src, LatLng dest, int user_id) {
        // Create a new HttpClient and Post Header
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("src_lat", Double.toString(src.latitude));
        params.put("src_long", Double.toString(src.longitude));
        params.put("dest_lat", Double.toString(dest.latitude));
        params.put("dest_long", Double.toString(dest.longitude));
        params.put("user_id", "" + user_id);
        client.post(BASE_URL + "/history/", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public void onFindRoutes(View v) {
        // remove this later
        if (src == null)
            src = new LatLng(33.4284307, -111.9504421);

        //dest = new LatLng(33.4236, -111.9393);
        Log.d(TAG, "----------------src :: " + src);
        Log.d(TAG, "----------------dest :: " + dest);
        postData(src, dest, 1);
        Intent intent = new Intent(this, MapsActivity.class);
        Bundle args = new Bundle();
        args.putParcelable("src", src);
        args.putParcelable("dest", dest);
        intent.putExtra("bundle", args);

        startActivity(intent);
    }

    //Handling Menu Item Selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.AddStarredLocations:
                addStarredLocation();
                return true;
            case R.id.listStarredLocations:
                //################### TASK-16: Frontend of Create & View Star Location ################
                parseStarredLocations();
                return true;
            case R.id.nearByTrafficIncidents:
                //################### TASK-17: Traffic Incidents  ################
                generateNearByTrafficIncidents();
                return true;
            case R.id.myProfile:
                //################### TASK-18: My Profile and Social Media​-  ################
                openProfilePage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String get_traffic_url(LatLng current_lat_long){
        String traffic_url = null;
        //Personal key: lYrP4vF3Uk5zgTiGGuEzQGwGIVDGuy24
        traffic_url = "https://www.mapquestapi.com/traffic/v2/incidents?&outFormat=json&key=lYrP4vF3Uk5zgTiGGuEzQGwGIVDGuy24&boundingBox=";
        if(current_lat_long !=null){
            Double x1 = current_lat_long.latitude - BOUNDING_BOX_SIZE;
            Double x2 = current_lat_long.latitude + BOUNDING_BOX_SIZE;
            Double y1 = current_lat_long.longitude - BOUNDING_BOX_SIZE;
            Double y2 = current_lat_long.longitude + BOUNDING_BOX_SIZE;
            traffic_url += x2.toString() + "," + y2.toString() + "," + x1.toString() + "," + y1.toString();
        }
        else{
            /* Server takes few seconds to set current_location
             * That is why current_location is set to cidese(BrickYard) location -(33.423567, -111.939269)
             */

            Double x1 = BYAC_LOC.latitude - BOUNDING_BOX_SIZE;
            Double x2 = BYAC_LOC.latitude + BOUNDING_BOX_SIZE;
            Double y1 = BYAC_LOC.longitude - BOUNDING_BOX_SIZE;
            Double y2 = BYAC_LOC.longitude + BOUNDING_BOX_SIZE;
            traffic_url += x2.toString() + "," + y2.toString() + "," + x1.toString() + "," + y1.toString();
        }
        System.out.println(traffic_url);
        return traffic_url;

    }
    //################### TASK-17: Traffic Incidents  ################
    private void generateNearByTrafficIncidents() {
            RequestQueue queue = Volley.newRequestQueue(this);

            String traffic_url = get_traffic_url(current_location_lat_long);

            /*
                    Reference: https://developer.mapquest.com/documentation/traffic-api/incidents/get/
                     */
            JsonObjectRequest req = new JsonObjectRequest(traffic_url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("Response:%n %s", response.toString(4));
                                parseNearByTrafficIncidentsJSON(response.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            queue.add(req);
    }

    public void parseNearByTrafficIncidentsJSON(String result){
            //using eventText and fulldesc
            System.out.println("Parsing JSON - getting list of nearby traffic incidents locations");
            JSONObject json;
            try{
                json = new JSONObject(result);
                JSONArray jsonIncidents = json.getJSONArray("incidents");
                for (int j = 0; j < jsonIncidents.length(); j++) {
                    JSONObject jsonobject = jsonIncidents.getJSONObject(j);
                    JSONObject description  = jsonobject.getJSONObject("parameterizedDescription");
                    System.out.println(description.get("eventText").toString());
                    evenText.add(j, description.get("eventText").toString());
                    fullDesc.add(jsonobject.get("fullDesc").toString());
                }
                SharedPreferences.Editor editorProfile = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                Set<String> set = new HashSet<String>();
                set.addAll(fullDesc);
                System.out.println("here");
                editorProfile.putStringSet("fullDesc", set);
                editorProfile.commit();

            }catch (JSONException e) {
                e.printStackTrace();
            }
        Intent intent = new Intent(this, TrafficIncident.class);
        startActivity(intent);
    }

    //################### TASK-18: My Profile and Social Media​-  ################
    private void openProfilePage() {
        //Satrting profile page activity
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }

    //################### TASK-15: Alert a friend ################
    public void onAlertFriend(View v) {
        System.out.println("Alerting a Friend");
        String messageAlert = "Alerting my friend...";
        //Get last known location --> mLastLocation
        if(current_location != null){
            messageAlert += "I am at " + current_location;
        }
        System.out.println(messageAlert);

        //Intiate a "New message" activity
        Intent intentt = new Intent(Intent.ACTION_VIEW);
        intentt.setData(Uri.parse("sms:"));
        intentt.setType("vnd.android-dir/mms-sms");
        intentt.putExtra(Intent.EXTRA_TEXT, "");
        intentt.putExtra("sms_body", messageAlert);
        try {
            startActivityForResult(Intent.createChooser(intentt, ""), 0);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "This phone does not have sms application", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseStarredLocations() {

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(BASE_URL + "/starred/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString(4));
                            System.out.print(response.toString());
                            parseStarredLocationsJson(response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        queue.add(req);


    }

    //################### TASK-14: Front end of Star a Location and Rest API ################
    private void addStarredLocation() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("starredLocationEnabled", "set");
        startActivity(intent);
    }


    public void parseStarredLocationsJson(String result) {
        System.out.println("Parsing JSON - getting list of starred locations");
        JSONObject json;
        try {
            /*
            Format of returning json object: {"places" : [
                {"lat":"123.43", "long":"-30.90", "place":"AddressABC"},
                {"lat":"23.21", "long":"98.30", "place":"AddressEFG"}
                ]}
             */
            json = new JSONObject(result);
            JSONArray jsonPlaces = json.getJSONArray("places");
            LatLng getLocation;
            String getAdrress;
            if (jsonPlaces.length() > 0) {
                //Clear previous values
                addresses.clear();
                locations.clear();
            }
            int count = Math.min(LIST_STARRED_LOCATIONS_LIMIT, jsonPlaces.length());
            int index = jsonPlaces.length();
            System.out.println(jsonPlaces.length());
            while(count >0){
                JSONObject jsonobject = jsonPlaces.getJSONObject(index-1);
                System.out.println(index-1);
                getLocation = new LatLng(Double.parseDouble(jsonobject.getString("lat")), Double.parseDouble(jsonobject.getString("lng")));
                getAdrress = jsonobject.getString("name");
                System.out.println(getLocation.toString() + "  - " + getAdrress);
                addresses.add(getAdrress);
                locations.add(getLocation);
                count--;
                index--;
            }
            Intent intent = new Intent(this, StarredLocationsActivity.class);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Object getCurrentLocation() {
        return null;
    }


    class PlaceAPI {

        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";

        private static final String API_KEY = "AIzaSyDmNBpYDBoxkwYTW5Aw9H3YrEXaSi-tnAo";

        public ArrayList<String> autocomplete(String input) {
            ArrayList<String> resultList = null;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();

            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                sb.append("?key=" + API_KEY);
                sb.append("&types=geocode");
                sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Error processing Places API URL", e);
                return resultList;
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Places API", e);
                return resultList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Log.d(TAG, jsonResults.toString());

                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                resultList = new ArrayList<String>(predsJsonArray.length());
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                    placeIDs.add(predsJsonArray.getJSONObject(i).getString("place_id"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
            }

            return resultList;
        }
    }

    class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        ArrayList<String> resultList;

        Context mContext;
        int mResource;

        PlaceAPI mPlaceAPI = new PlaceAPI();

        public PlacesAutoCompleteAdapter(Context context, int resource) {
            super(context, resource);

            mContext = context;
            mResource = resource;
        }

        @Override
        public int getCount() {
            // Last item will be the footer
            return resultList.size();
        }

        @Override
        public String getItem(int position) {
            return resultList.get(position);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        resultList = mPlaceAPI.autocomplete(constraint.toString());

                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };

            return filter;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

