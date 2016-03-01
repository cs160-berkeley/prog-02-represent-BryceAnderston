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

import java.util.ArrayList;
import java.util.List;

public class WatchToPhoneService extends Service {
    // implements GoogleApiClient.ConnectionCallbacks
    private GoogleApiClient mWatchApiClient;
    private List<Node> nodes = new ArrayList<>();

    @Override
    public void onCreate() {
        Log.d("T", "WatchToPhoneService onCreate() begun: ");
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mWatchApiClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .build();
        Log.d("T", "WatchToPhoneService mWatchClient = : " + mWatchApiClient.toString());
        //and actually connect it
        mWatchApiClient.connect();
        Log.d("T", "WatchToPhoneService onCreate() finish: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("T", "WatchToPhoneService onStartCommand begun: ");
        // Which representative are we calling for? Grab this info from INTENT
        // which was passed over when we called startService
        Log.d("T", "WatchToPhoneService onStartCommand got intent: " + intent.toString());
        Bundle extras = intent.getExtras();
        final String repName = extras.getString("REP_NAME");
        final String randomZip = extras.getString("RANDOM_ZIPCODE");
        Log.d("T", "WatchToPhoneService onStartCommand extras were: " + repName + " and "
                + randomZip);

        // Send the message with the cat name
        new Thread(new Runnable() {
            @Override
            public void run() {
                //first, connect to the apiclient
                mWatchApiClient.connect();
                Log.d("T", "WatchToPhoneService onStartCommand mWatchApiClient.connect() procced: ");
                //now that you're connected, send a massage with the rep name or zipcode
                if (randomZip.equals("None")) {
                    sendMessage("/CallDetailsName", repName);
                    Log.d("T", "WatchToPhoneService onStartCommand senMessage procced with: "
                            + "/CallDetailsName");
                } else if (repName.equals("None")) {
                    sendMessage("/CallRandomZip", randomZip);
                    Log.d("T", "WatchToPhoneService onStartCommand senMessage procced with: "
                            + "/CallRandomZip");
                }
            }
        }).start();
        Log.d("T", "WatchToPhoneService onStartCommand finish: ");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("T", "WatchToPhoneService onDestroy() called: ");
        super.onDestroy();
        mWatchApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage( final String path, final String text ) {
        Log.d("T", "WatchToPhoneService sendMessage begun: ");
        //one way to send message: start a new thread and call .await()
        //see catnip.watchtophoneservice for another way to send a message
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mWatchApiClient ).await();
                Log.d("T", "WatchToPhoneService sendMessage getConnectedNodes procced: ");
                for(Node node : nodes.getNodes()) {
                    Log.d("T", "WatchToPhoneService inside nodes: ");
                    //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                    //send a message for each of these nodes (just one, for an emulator)
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mWatchApiClient, node.getId(), path, text.getBytes() ).await();
                    Log.d("T", "WatchToPhoneService sendMessage SendMessageResult procced with text: " + text);
                    //4 arguments: api client, the node ID, the path (for the listener to parse),
                    //and the message itself (you need to convert it to bytes.)
                }
            }
        }).start();
        Log.d("T", "WatchToPhoneService sendMessage finish: ");
    }
}
