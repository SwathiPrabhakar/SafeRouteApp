package com.saferoutesapp.saferoutesapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ajothomas on 4/18/17.
 */

public interface GlobalConst {
    public static final String BASE_URL = "http://3359ecc7.ngrok.io";
    public static final String PLACES_API_KEY = "AIzaSyCOs74qA9ySSKcx6itspX_K8RPzVGinFmA";
    public static final String API_KEY = "AIzaSyDmNBpYDBoxkwYTW5Aw9H3YrEXaSi-tnAo";


    /* Server takes few seconds to set current_location
    * That is why current_location is set to cidese(BrickYard) location -(33.423567, -111.939269)
    * Varibles for Task-17 Traffic Incidents
    */
    public static final LatLng BYAC_LOC = new LatLng(33.423567, -111.939269);
    public static final Double BOUNDING_BOX_SIZE = 0.03;

    /* Varibles for Task-16 Traffic Incidents
    */
    public static final Integer LIST_STARRED_LOCATIONS_LIMIT = 15;
}