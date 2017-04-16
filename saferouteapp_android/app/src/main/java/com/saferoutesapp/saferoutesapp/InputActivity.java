package com.saferoutesapp.saferoutesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

/**
 * Created by avniv on 4/14/2017.
 */

public class InputActivity extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
//        Intent i = new Intent(this, RegistrationService.class);
//        startService(i);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_activity);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    public void onFindRoutes(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
