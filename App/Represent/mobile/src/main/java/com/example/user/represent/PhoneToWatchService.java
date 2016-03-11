package com.example.user.represent;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class PhoneToWatchService extends Service {
    // ??? Remove?
    //public PhoneToWatchService() {
    //}

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        Log.d("T", "PhoneToWatchService created: ");
        super.onCreate();
        Log.d("T", "super.onCreate() complete:  Starting new GoogleApiClient");
        //initialize the googleAPIClient for message passing
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .build();
    }

    @Override
    public void onDestroy() {
        Log.d("T", "Destroying PhoneToWatch GoogleApiClient: ");
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("T", "PhoneToWatchService onStartCommand started: ");
        // Grab commandMessage from INTENT
        // which was passed over when we called startService
        //BUG:  Why is this being called when watch is active and push "Details" button?
        Log.d("T", "PhoneToWatchActivity got intent: " + intent.toString());
        Bundle extras = intent.getExtras();
        // hacky solution:  exit the code with as little impact as possible
        if (extras == null) {
            Log.d("T", "PhoneToWatchService got a null extras: ");
            return START_NOT_STICKY;
        }
        // "useZipcode" or "useCurrPos" or UseFullUrl
        final String command = extras.getString("COMMAND");
        // May also be a sunlight URL
        final String zipcode = extras.getString("ZIPCODE");
        Log.d("T", "PhoneToWatchService extras passed: ");
        //final Boolean useCurrentpos = extras.getBoolean("USE_CURRENTPOS");

        // Send the message with the info
        new Thread(new Runnable() {
            @Override
            public void run() {
                //first, connect to the apiclient
                mApiClient.connect();
                Log.d("T", "PhoneToWatchService connected mApiClient: ");
                //now that you're connected, send a massage with the info
                sendMessage("/" + command, zipcode);
                Log.d("T", "PhoneToWatchService senMessage called. finish onStartCommand: ");
            }
        }).start();

        return START_STICKY;
    }

    @Override //remember, all services need to implement an IBiner. Doesn't need to do anything.
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage( final String path, final String text) {
        //one way to send message: start a new thread and call .await()
        //see watchtophoneservice for another way to send a message
        Log.d("T", "PhoneToWatchService sendMessage started: ");
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                Log.d("T", "PhoneToWatchService SendMessage GetConnectedNodes: ");
                for(Node node : nodes.getNodes()) {
                    Log.d("T", "PhoneToWatchService SendMessage Inside for loop: ");
                    //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                    //send a message for each of these nodes (just one, for an emulator)
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                    //4 arguments: api client, the node ID, the path (for the listener to parse),
                    //and the message itself (you need to convert it to bytes.)
                    Log.d("T", "PhoneToWatchService MessageApi.sendMessageResult procced with text: " + text);
                }
            }
        }).start();
    }
}
