package com.saferoutesapp.saferoutesapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ajothomas on 4/16/17.
 */
public class DataParser {
    public final String TAG = this.getClass().getSimpleName();
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse1(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ((JSONObject)( (JSONObject)jRoutes.get(i)).get("route")).getJSONArray("legs");
                List path = new ArrayList<>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude) );
                            hm.put("lng", Double.toString((list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return routes;
    }


    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<LatLng>> parse2(JSONObject jObject){

        List<List<LatLng>> crimeCoordinates = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jCoordsArr;
        JSONArray jCoords;
        JSONObject jLoc;

        try {

            jRoutes = jObject.getJSONArray("routes");
            Log.d(TAG, "------**********--------"+jRoutes.length());
            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jCoordsArr = (JSONArray) ((JSONObject)jRoutes.get(i)).getJSONArray("crimeSpots");

                List<LatLng> tempCoords = new ArrayList<>();
                for(int j=0;j<jCoordsArr.length();j++){
                    jLoc = (JSONObject) ((JSONObject)jCoordsArr.get(j)).getJSONObject("location");
                    Log.d(TAG, "-------"+j+"-------"+jLoc.toString());
                    jCoords = (JSONArray) jLoc.getJSONArray("coordinates");
                    tempCoords.add(new LatLng((Double) jCoords.get(1), (Double) jCoords.get(0)));
                }
                crimeCoordinates.add(tempCoords);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return crimeCoordinates;
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}