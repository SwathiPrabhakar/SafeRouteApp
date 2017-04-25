package com.saferoutesapp.saferoutesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by avniv on 4/20/2017.
 */

//################### TASK-18: My Profile and Social Media​-  ################
/*
    Note: We are susing personal website as share link as application is not public yet!
     */

public class ProfilePage extends AppCompatActivity {
    //TextViews
    private TextView textViewName;
    private TextView textViewEmail;
    private NetworkImageView profilePhoto;

    //Image Loader
    private ImageLoader imageLoader;
    public static final String MY_PREFS = "saferoutes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        //Initializing Views
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        profilePhoto = (NetworkImageView) findViewById(R.id.profileImage);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        System.out.println(prefs.getString("user_name", null));
        System.out.println(prefs.getString("user_email", null));

        textViewName.setText(prefs.getString("user_name", null));
        textViewEmail.setText(prefs.getString("user_email", null));

        String profilePhotoUrl = prefs.getString("photo_url", null);

        //Initializing image loader
        imageLoader = CustomVolleyRequest.getInstance(this.getApplicationContext())
                .getImageLoader();

        imageLoader.get(profilePhotoUrl,
                ImageLoader.getImageListener(profilePhoto,
                        R.mipmap.ic_launcher,
                        R.mipmap.ic_launcher));

        //Loading image
        profilePhoto.setImageUrl(profilePhotoUrl, imageLoader);
    }
    public void onShareApp(View v){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "SafeRoutes");
            String sAux = "\nLet me recommend you this application\n\n";

            sAux = sAux + "http://asahoo.me/ \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose Option"));
        } catch(Exception e) {
            e.toString();
        }
    }
}
