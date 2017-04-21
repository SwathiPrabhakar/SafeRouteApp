package com.saferoutesapp.saferoutesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.saferoutesapp.saferoutesapp.InputActivity.addresses;

/**
 * Created by avniv on 4/20/2017.
 */

public class TrafficIncident extends AppCompatActivity implements GlobalConst {
    public static final String MY_PREFS = "saferoutes";
    static ArrayAdapter arrayAdapter;
    public static ArrayList<String> evenText = new ArrayList<String>();
    public static ArrayList<String> fullDesc = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_incidents);
        ListView listView = (ListView)findViewById(R.id.listView);
        System.out.println("before adapter");

        System.out.println(evenText.size());
        Set<String> set = new HashSet<String>();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        set = prefs.getStringSet("fullDesc", null);
        ArrayList<String> evenTextList  = new ArrayList<String>(set);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, evenTextList);
        listView.setAdapter(arrayAdapter);
    }


}
