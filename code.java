package com.saferoutesapp.saferoutesapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.util.Log;
import android.Manifest;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

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
import java.util.List;

/**
 * Created by avniv on 4/14/2017.
 */


public class InputActivity extends AppCompatActivity {
    final private String TAG = this.getClass().getSimpleName();
    private LocationListener listener;
    private LocationManager locationManager;
    private LatLng src=null;
    private LatLng dest=null;
    private Geocoder geocoder;
    private AutoCompleteTextView sourceACTextView;
    private AutoCompleteTextView destinationACTextView;
    private ArrayList<String> placeIDs;
    private GoogleApiClient mGoogleApiClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_activity);

        sourceACTextView = (AutoCompleteTextView) findViewById(R.id.from_input);
        if(!runtime_permissions())
            enable_buttons();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();

        AutocompleteFilter filter = new AutocompleteFilter.Builder().setCountry("US").build();
        Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@"+src);
        LatLngBounds bounds = new  LatLngBounds(src, dest);
        placeIDs = new ArrayList<>();
        destinationACTextView = (AutoCompleteTextView) findViewById(R.id.to_input);
        destinationACTextView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item, mGoogleApiClient, bounds,filter));
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

    }

    private void enable_buttons() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // getting the current location
                src  = new LatLng(location.getLatitude(), location.getLongitude());

                // getting the current address from the lat, long
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
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    public void onFindRoutes(View v) {

        // if the current location is null, set it to tempe explicitly
        if(src==null)
            src = new LatLng(33.4284307, -111.9504421);

        //dest = new LatLng(33.4236, -111.9393);
        Log.d(TAG, "----------------src :: "+src);
        Log.d(TAG, "----------------dest :: "+dest);
        Intent intent = new Intent(this, MapsActivity.class);
        Bundle args = new Bundle();
        args.putParcelable("src", src);
        args.putParcelable("dest", dest);
        intent.putExtra("bundle", args);

        startActivity(intent);

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
        private LatLngBounds mBounds;
        private GoogleApiClient mGoogleApiClient;
        private AutocompleteFilter mPlaceFilter;

        PlaceAPI mPlaceAPI = new PlaceAPI();

        public PlacesAutoCompleteAdapter(Context context, int resource,  GoogleApiClient googleApiClient,
                                         LatLngBounds bounds, AutocompleteFilter filter) {
            super(context, resource);

            mContext = context;
            mResource = resource;
            mGoogleApiClient = googleApiClient;
            mBounds = bounds;
            mPlaceFilter = filter;
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
}

