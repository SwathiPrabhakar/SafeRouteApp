package com.saferoutesapp.saferoutesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private LatLng src=null;
    private LatLng dest=null;
    private Geocoder geocoder;
    private AutoCompleteTextView sourceACTextView;
    private AutoCompleteTextView destinationACTextView;
    private ArrayList<String> placeIDs;
    static ArrayList<String> addresses = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 100;
    public static final String MY_PREFS = "saferoutes";

    //google api client
    private GoogleApiClient mGoogleApiClient;

    //Signin constant to check the activity result


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_activity);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();

        //logIn();

//        auth end
        Intent i = new Intent(this, GCMRegistrationIntentService.class);
        startService(i);

        placeIDs = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());


            sourceACTextView = (AutoCompleteTextView) findViewById(R.id.from_input);
        if(!runtime_permissions())
            enable_buttons();


        destinationACTextView = (AutoCompleteTextView) findViewById(R.id.to_input);

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        if(bundle != null){
            LatLng starred_position = bundle.getParcelable("starred_position");
            String starred_address = bundle.getString("starred_address");

            if(starred_position != null ){
                destinationACTextView.setText(starred_address);
            }
        }
        destinationACTextView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        destinationACTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                System.out.println("description-" + description);
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

    }

    private void logIn(){
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
                src  = new LatLng(location.getLatitude(), location.getLongitude());
                StringBuilder builder = new StringBuilder();
                try {
                    List<Address> address = geocoder.getFromLocation(src.latitude, src.longitude, 1);
                    int maxLines = address.get(0).getMaxAddressLineIndex();
                    for (int i=0; i<maxLines; i++) {
                        String addressStr = address.get(0).getAddressLine(i);
                        builder.append(addressStr);
                        builder.append(" ");
                    }

                    String finalAddress = builder.toString(); //This is the complete address.
                    sourceACTextView.setText(finalAddress);
                } catch (IOException e) {}
                catch (NullPointerException e) {}

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,listener);

    }


    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }
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
    }

    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();


            handleBackendSignIn(this, acct.getEmail(), acct.getId());
            Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show();
//            dummyGetWithToken(this);

        } else {
            //If login fails
            System.out.print("Failed ");
            Log.e("result", result.toString());
            Toast.makeText(this, "Login Failed!!!!!!!!" + result.toString(), Toast.LENGTH_LONG).show();
            if(result != null)
                System.out.print(result.toString());

//            logIn();
        }
    }

    public void dummyGetWithToken(final Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest req = new JsonObjectRequest(SERVER +  "/blog/", null,
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

    public void handleBackendSignIn(final Context context, final String email, final String id){
        RequestQueue queue = Volley.newRequestQueue(context);
        Map<String,String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("uid", id);

        JsonObjectRequest request_json = new JsonObjectRequest(SERVER + "/auth/register/", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Process os success response
                            System.out.println("Posted This is the response");
                            String token  = response.getString("token");
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
    public boolean onCreateOptionsMenu(Menu menu){
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
        params.put("user_id", ""+user_id);
        client.post(SERVER+"/history/", params, new AsyncHttpResponseHandler(){
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
        if(src==null)
        src = new LatLng(33.4284307, -111.9504421);

        //dest = new LatLng(33.4236, -111.9393);
        Log.d(TAG, "----------------src :: "+src);
        Log.d(TAG, "----------------dest :: "+dest);
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
                parseStarredLocations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void onAlertFriend(View v) {
        System.out.println("Alerting a Friend");
        String messageAlert = "Alerting my friend";
        //Get last known location --> mLastLocation
        getCurrentLocation();
/*        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                messageAlert += lastKnownLocationGPS.toString();
            } else {
                Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if(loc!=null)
                    messageAlert += loc.toString();
            }
        }*/
        System.out.println(messageAlert);

        //Intiate a "New message" activity
        Intent intentt = new Intent(Intent.ACTION_VIEW);
        intentt.setData(Uri.parse("sms:"));
        intentt.setType("vnd.android-dir/mms-sms");
        intentt.putExtra(Intent.EXTRA_TEXT, "");
        intentt.putExtra("sms_body", messageAlert);
        startActivityForResult(Intent.createChooser(intentt, ""), 0);
    }

    private void parseStarredLocations() {

/*
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://api.myjson.com/bins/m68r3",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseStarredLocationsJson(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
                System.out.println(error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
*/

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(SERVER +  "/starred/", null,
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

    private void addStarredLocation() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("starredLocationEnabled", "set");
        startActivity(intent);
    }

    public void parseStarredLocationsJson(String result) {
        System.out.println("Parsing JSON - getting list of starred locations");
        JSONObject json;
        try{
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
            if(jsonPlaces.length() > 0){
                //Clear previous values
                addresses.clear();
                locations.clear();
            }
            for (int j = 0; j < jsonPlaces.length(); j++) {
                JSONObject jsonobject = jsonPlaces.getJSONObject(j);
                getLocation = new LatLng( Double.parseDouble(jsonobject.getString("lat")), Double.parseDouble(jsonobject.getString("lng")));
                getAdrress = jsonobject.getString("name");
                System.out.println(getLocation.toString() + "  - " + getAdrress);
                addresses.add(getAdrress);
                locations.add(getLocation);
            }
            Intent intent = new Intent(this, StarredLocationsActivity.class);
            startActivity(intent);
        }catch (JSONException e) {
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
                    }
                    else {
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

