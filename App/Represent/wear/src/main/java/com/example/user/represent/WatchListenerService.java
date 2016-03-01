package com.example.user.represent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

//XXX  Going to need to redo this: create one representative at a time XXX
public class WatchListenerService extends WearableListenerService {
    // In PhoneToWatchService, we passed in a path, with a command
    // These paths serve to differentiate different phone-to-watch messages
    private static final String USE_CURRENT = "/useCurrPos";
    private static final String USE_ZIPCODE = "/useZipcode";
    // Another setting for
    // Or put the launcher for this activity in Main2?  Just pull the first name to initialize
    //private static final String USE_NAME = "/useName";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, use current position vs a zipcode)
        String data = new String(messageEvent.getData());

        if( messageEvent.getPath().equalsIgnoreCase(USE_CURRENT) ) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, MainActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            // Will check if the "zipcode" is CurrPos before using it.
            intent.putExtra("ZIPCODE", "CurrPos");
            Log.d("T", "WatchListenerService about to start watch MainActivity with ZIPCODE: CurrPos");
            startActivity(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase( USE_ZIPCODE )) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            //you need to add this flag since you're starting a new activity from a service
            // Feed a string version of the zipcode into the new intent
            intent.putExtra("ZIPCODE", data);
            Log.d("T", "WatchListenerService about to start watch MainActivity with ZIPCODE: " + data);
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
