package com.saferoutesapp.saferoutesapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.content.BroadcastReceiver;
import android.widget.TextView;
import android.util.Log;
import android.Manifest;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by avniv on 4/14/2017.
 */

public class InputActivity extends AppCompatActivity{
    private BroadcastReceiver broadcastReceiver;
    private TextView from_input;
    final private String TAG = "InputActivity";
    private LocationListener listener;
    private LocationManager locationManager;
    private LatLng src;
    private LatLng dest;

    protected void onCreate(Bundle savedInstanceState) {
//        Intent i = new Intent(this, RegistrationService.class);
//        startService(i);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_activity);
        from_input = (TextView) findViewById(R.id.from_input);
        if(!runtime_permissions())
            enable_buttons();
    }

    private void enable_buttons() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                src  = new LatLng(location.getLatitude(), location.getLongitude());
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
        // remove this later
        src = new LatLng(33.4284307, -111.9504421);
        dest = new LatLng(33.4236, -111.9393);
        Log.d(TAG, "----------------src :: "+src);
        Log.d(TAG, "----------------dest :: "+dest);
        Intent intent = new Intent(this, MapsActivity.class);
        Bundle args = new Bundle();
        args.putParcelable("src", src);
        args.putParcelable("dest", dest);
        intent.putExtra("bundle", args);

        startActivity(intent);

    }
}
