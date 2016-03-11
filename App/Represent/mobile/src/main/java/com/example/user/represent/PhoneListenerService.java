package com.example.user.represent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

public class PhoneListenerService extends WearableListenerService {
// XXXXXXXXXXXX How to send large objects?
    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String RAND_ZIP = "/CallRandomZip";
    private static final String DETAILS = "/CallDetailsName";
    // The .data is a string of the requested position. ??????
    //private static final String SCROLL = "/CallScrollPosition";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "phone onMessageReceived, got: " + messageEvent.getPath());
        Log.d("T", "Message had data : " + new String(messageEvent.getData()));
        if( messageEvent.getPath().equalsIgnoreCase(RAND_ZIP) ) {
            // Calls Main2 with the random ZipCode
            String zipcode = new String(messageEvent.getData()); // StandardCharsets.UTF_8); ???
            Intent intent = new Intent(this, Main2Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("ZIPCODE_OR_USEPOS", zipcode);
            intent.putExtra("USERAND", "Yes");
            Log.d("T", "onMessageReceived about to start watch Main2Activity with USERAND and ZIPCODE_OR_USEPOS: " + zipcode);
            startActivity(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase(DETAILS)) {
            String name = new String(messageEvent.getData()); // StandardCharsets.UTF_8); ???
            Intent intent = new Intent(this, DetailedInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("BIOGUIDE", name);
            Log.d("T", "onMessageReceived about to start watch DetailedInfoActivity with BIOGUIDE: " + name);
            startActivity(intent);
        //} else if (messageEvent.getPath().equalsIgnoreCase(SCROLL)) {
            // UNUSED XXXX
            // Pull data from Global... and then what?
            // So much simpler if could put scrolling inside of watch.Main
        } else {
            Log.d("T", "onMessageReceived got an odd command: " + messageEvent.getPath());
            super.onMessageReceived( messageEvent );
        }

    }
}
