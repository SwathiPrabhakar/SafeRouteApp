package com.saferoutesapp.saferoutesapp;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Class is extending GcmListenerService
public class GCMPushReceiverService extends GcmListenerService {

    private static final int REQUEST_CODE = 10;

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //Intent i = new Intent(this, ResultActivity.class);
        //i.putExtra("yourkey", string);

//        //Getting the message from the bundle
//        String message = data.getString("message");
//        try {
//            JSONObject object = new JSONObject(message);
//            JSONArray jArray = object.getJSONArray("result");
//            for (int i=0; i < jArray.length(); i++) {
//
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        //Displaying a notiffication with the message
        //if(data.getString("result")!=null)
            sendNotification(data);
    }

    //This method is generating a notification and displaying the notification
    private void sendNotification(Bundle data) {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtras(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SafeRoute")
                .setContentText("Click here to check your next trip!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }


}