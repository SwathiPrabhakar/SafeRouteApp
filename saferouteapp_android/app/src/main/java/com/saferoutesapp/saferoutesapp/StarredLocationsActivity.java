package com.saferoutesapp.saferoutesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.saferoutesapp.saferoutesapp.R.id.listView;
import static com.saferoutesapp.saferoutesapp.R.id.place_autocomplete_search_button;


/**
 * Created by avniv on 4/16/2017.
 */

public class StarredLocationsActivity extends AppCompatActivity {
    static ArrayAdapter arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starred_locations);

        //list view for starred locations
        ListView listView = (ListView)findViewById(R.id.listView);

        //On Create get List of Starred Locations for this user


        System.out.println("before adapter");
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, InputActivity.addresses);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position);
                System.out.println(InputActivity.locations.get(position));
                Intent intent = new Intent(getApplicationContext(),InputActivity.class);
                Bundle args = new Bundle();
                args.putParcelable("starred_position", InputActivity.locations.get(position));
                args.putString("starred_address", InputActivity.addresses.get(position));
                intent.putExtra("bundle", args);
                startActivity(intent);
            }
        });
    }
}

