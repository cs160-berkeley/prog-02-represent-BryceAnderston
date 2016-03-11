package com.example.user.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
//import com.google.android.gms.location.LocationServices;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// For GoogleApi
public class MainActivity extends Activity implements SensorEventListener {// ,
        //GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    //private static final String TWITTER_KEY = "";
    //private static final String TWITTER_SECRET = "";
    //,
    // For Sunlight Api
    private String requestURL;
    private static final String sunlightURL = "https://congress.api.sunlightfoundation.com";
    private static final String sunlightGetLEGISLATORS = "/legislators/locate";
    private static final String sunlightSearchZIP = "?zip=";
    private static final String sunlightSearchLAT = "?latitude=";
    private static final String sunlightAndLONG = "&longitude=";
    private String sunLightQuery = "";
    private static final String sunlightGetAPIKEY = "&apikey=";
    private static final String sunlightKEY = "7dfe13f41a8d41d4aab7c74a86f84962";
        //GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener

    private static final String defaultData = "Rep Barbara Lee,Democrat,L000551" +
            "_Sen Barabara Boxer,Democrat,B000711_Sen Dianne Feinstein,Democrat,F000062" +
            "&CA,Alameda";

    //Latitude and Longitude
    String mlatitude;
    String mlongitude;
    //Use Double.parseDouble(str); to get doubles

    //For geoCoding, sending to Results
    private String mState;
    private String mCounty;

    private TextView mTextView;
    // The list of data objects that will populate the ListView
    private List<Representative> repList = new ArrayList<Representative>();
    // A basic container so that this view can be reconstructed after leaving election results
    // takes the intent's ZIPCODE as its value.  Might be "Null".
    private String zipcodeOrCurrpos;

    // Sensor Shake Variables:  Setup
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    // Detection
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    // Swipe Gesture variant 2
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    // For GoogleApi
    //private GoogleApiClient mGoogleApiClient;
    // CAN'T CALL GPS ON WATCH

    // For Sunlight
    // 7dfe13f41a8d41d4aab7c74a86f84962

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("T", "Watch Main onCreate begun: ");
        super.onCreate(savedInstanceState);
        //TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        //Fabric.with(this, new Twitter(authConfig));
        //XXX FILL change layout?
        setContentView(R.layout.activity_main);

        //Create sensors
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //Swipe Gesture Sensors
        //XXXX Don't know purpose od swipe_layout
        //ActivitySwipeDetector swipe = new ActivitySwipeDetector(this);
        //LinearLayout swipe_layout = (LinearLayout) findViewById(R.id.swipe_layout);
        //swipe_layout.setOnTouchListener(swipe);
        // Variant 2
        // Gesture detection
        // XXXX Remove?
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        /*
        //For GoogleApi
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
                */


        //XXX ??? Can remove the below? NO!
        //final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        //stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
        //    @Override
        //    public void onLayoutInflated(WatchViewStub stub) {
        //        mTextView = (TextView) stub.findViewById(R.id.text);
        //    }
        //});
        // XXXX XXX Going to need to redo all this
        // The watch side of the app must be manually started? So can't rely on zipcode, etc to exist
        Log.d("T", "Past sensor creation: ");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.d("T", "Watch Main onCreate got intent: " + intent.toString());
        //Is "Null" when there have been no requests from mobile yet.
        String message = "Null";
        // XXXX ??? MIGHT WANT TO FEATURE populateRepList as a seperate file?
        if (extras != null) {
            // Remember zipcode is now Rawdata
            message = extras.getString("ZIPCODE");
            Log.d("T", "Watch Main onCreate ZIPCODE = " + extras.getString("ZIPCODE"));
            //POPULATE
            populateRepList(message);
            /*  // No longer use this, since always a url getting passed in.
            if (message.equals("CurrPos")) {
                // Arbitrary zipcode
                populateRepList(true, 23456);
            } else {
                // Pull and use the zipcode
                populateRepList(false, Integer.parseInt(message));
            } */
        } else {
            //POPULATE
            //Populate based on default position if brought up naturally.
            populateRepList(defaultData);
            //populateRepList("congress.api.sunlightfoundation.com/legislators/locate?zip=94702&apikey=" +sunlightKEY);
        }
        // LOCATION API
        ////extractLatLong(message);
        //zipcodeOrCurrpos = message;
        // XXX ??? HOW TO GET IN VALUES?
        //Create data to populate ListView
        // XXX ??? Might just pull in data from Mobile Activity.  Probably not.
        //populateRepList(true, 23456);
        // Fill in each sub-view with data from the data
        populateListView();
        // Custom Method for allowing an item to be clicked
        registerClickCallback();
    }

    private void populateRepList(String rawData) {
        // Get the address to pass in. Already is mUrl
                // Now actually can populate list
        // split  by & to get stateCouunty and
        Log.d("T", "Watch Main populateRepList rawData: " + rawData);
        String rawReps = rawData.split("&")[0];
        String stateCounty = rawData.split("&")[1];
        // split stateCounty to get mState and mCounty
        mState = stateCounty.split(",")[0];
        if (stateCounty.split(",").length > 1){
            mCounty = stateCounty.split(",")[1];
        } else {
            mCounty = "Earthquake"; //County in state of Default
        }
        // split rawReps to get a list of raw reps
        for (String rawRep: rawReps.split("_")){
            Representative currRep = new Representative();
            // Set representative
            currRep.setName(rawRep.split(",")[0]);
            currRep.setParty(rawRep.split(",")[1]);
            currRep.setBioguide_id(rawRep.split(",")[2]);
            //Log.d("T", "Watch Main populateRepList: " + );
            repList.add(currRep);
        }
        Log.d("T", "Watch Main populateRepList: " + mState + ", " + mCounty + ", : " + rawReps);
    }

    private void populateListView() {
        Log.d("T", "Watch Main populateListView procced: ");
        ArrayAdapter<Representative> thisAdapater = new MyListAdapter();
        ListView repList = (ListView) findViewById(R.id.listView);
        repList.setAdapter(thisAdapater);
    }

    private void extractLatLong(String mUrl) {
        // UNUSED
        // congress.api.sunlightfoundation.com/legislators/locate?latitude
        // =37.9
        // &longitude
        // =122.3
        // &apikey=7dfe13f41a8d41d4aab7c74a86f84962
        // split by "=&" results in storage at [0 : cong...itude, 1: 37.9, 2: longitude, 3: 122.3
        mlatitude = mUrl.split("=|&")[1];
        mlongitude = mUrl.split("=|&")[3];
        Log.d("T", "Watch Main now has latitude and longitude: " + mlatitude + ", " + mlongitude);
        //double value = Double.parseDouble(text); //if need be
    }

    // ArrayAdapter:  This is where the created sub-view item_layout comes into play.
    private class MyListAdapter extends ArrayAdapter<Representative> {
        public MyListAdapter() {
            //Context, layout, data
            super(MainActivity.this, R.layout.item_layout, repList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // position: position in passed in list (replist)
            // Pull a sub-view if don't already have it.
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            //Find Representative
            Representative currentRepresentative = repList.get(position);

            //Fill sub-view
            //Photo
            ImageView imageViewPhoto = (ImageView) itemView.findViewById(R.id.item_photo);
            imageViewPhoto.setImageResource(currentRepresentative.getPhoto());
            //Name
            TextView textViewName = (TextView) itemView.findViewById(R.id.item_textview_name);
            textViewName.setText(currentRepresentative.getName());
            //Party
            TextView textViewParty = (TextView) itemView.findViewById(R.id.item_textview_party);
            textViewParty.setText(currentRepresentative.getParty());

            return itemView;
            //return super.getView(position, convertView, parent);
        }
    }

    private void registerClickCallback(){
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Representative clickedRep = repList.get(position);
                // CLICK
                // FILLING:  Load individual's detailed information onto mobile
                // XXX  !!! Sending the name, will be used to determine the representative to load
                // ??? Might want to create IDs for representatives?
                String sendName = clickedRep.getName();
                String sendBioguide = clickedRep.getBioguide_id();
                Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                // !!! Set to "None" for random shake
                // RANDOM
                sendIntent.putExtra("REP_NAME", sendBioguide);
                // !!! Set to a random value if are calling for a random
                sendIntent.putExtra("RANDOM_ZIPCODE", "None");
                Log.d("T", "OK! about to start mobile Details with REP_NAME: "+ sendBioguide);
                startService(sendIntent);

                // Also go to this representative's county's election results
                // GEOCODING API
                // sample:  "https://maps.googleapis.com/maps/api/geocode/output?parameters"
                // output = "json" (or "xml")
                // required parameters are latlng=DOUBLE,DOUBLE   &   key=MYKEY
                //                          no spaces!
                // location_type=LOCTYPE restricts results,    result_type=RESTYPE
                // won't be using location_type (don't include)
                // RESTYPE, for our purposes, is administrative_area_level_1 (state)
                //  or  administrative_area_level_2 (county)
                //examples:
                // https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=MY_KEY
                // https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&location_type=ROOFTOP&result_type=street_address&key=YOUR_API_KEY
                // http://maps.googleapis.com/maps/api/geocode/json?address={zipcode}
                // should return only locations with postal_code {zipcode} ???

                // Send the full URL.  Will be handled by basic code from within Results?
                //  Or pull out relevant values (STATE and COUNTY) here and now, and send those off?
                // For Results, lots of Intents:  State and County
                Intent resultIntent = new Intent(getBaseContext(), ElectionResultsActivity.class );
                //resultIntent.putExtra("REP_NAME", sendName);
                resultIntent.putExtra("STATE", mState);
                resultIntent.putExtra("COUNTY", mCounty);
                Log.d("T", "about to start watch ElectionResultsActivity  for" +
                        " county of : " + mCounty + ", " + mState);
                startActivity(resultIntent);
                // XXX !!! !!! What happens if click a rep while mobile on home screen?
                // Are merely launching a new activity. The current activity has no bearing.
            }
        });
    }

    // For GoogleApi (added mGoogle.__()
    // XXX FILL:  Implemt the shake functionality to call for a random Zip_code
    //Deactivate sensor when not in use
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
        //mGoogleApiClient.disconnect();
    }

    protected void onResume() {
        super.onResume();
        //mGoogleApiClient.connect();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // For GoogleApi
    /*
    @Override
    public void onConnected(Bundle bundle) {Log.d("T", "Watch Main onConnected procced: ");}

    @Override
    public void onConnectionSuspended(int i) {Log.d("T", "Watch Main onCnnectionSuspended procced: ");}

    @Override
    public void onConnectionFailed(ConnectionResult connResult) {Log.d("T", "Watch Main onConnectionFailed procced: ");}
    */

    // Get random number.
    // Send Intent to Mobile with random number as RANDOM_ZIPCODE
    // Restart this activity with ZIPCODE set to the random number
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    // Get random zipcode
                    //semi-DUMMY
                    // Choose valid random zipcode
                    int i = 0;
                    String randomZip = createRandomZip();//Integer.toString(new Random().nextInt(10000));
                    /* while (checkNoReps(randomZip)) {
                        randomZip = createRandomZip();
                        i += 1;
                        if (i > 100) {
                            randomZip = "94702";
                            i = -1;
                        }
                    }
                    Log.d("T", "Took X iterations to get randomZip.  X=" + i); */

                    // FILLING:  Send the random zipcode through. Also: Rebuild self.
                    Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                    // !!! Set to "None" for random shake
                    sendIntent.putExtra("REP_NAME", "None");
                    // !!! Set to a random value if are calling for a random
                    sendIntent.putExtra("RANDOM_ZIPCODE", randomZip);
                    Log.d("T", "about to start watch Main2Activity with RANDOM_ZIPCODE: " + randomZip);
                    // DUMMY
                    //Context context = getApplicationContext();
                    //int duration = Toast.LENGTH_LONG;
                    //String value = "Shook to: " + randomZip;
                    //Toast toast = Toast.makeText(context, value, duration);
                    //toast.show();

                    startService(sendIntent);
                    finish();

                    /*
                    //Self.rebuild
                    Intent intent = new Intent(getBaseContext(), MainActivity.class );
                    //getBaseContext() vs this ?  Originally was this
                    // Feed a string version of the zipcode into the new intent
                    // PART C :  remember that now require a URL directly
                    sunLightQuery = sunlightSearchZIP + randomZip;
                    String myPost = sunlightURL + sunlightGetLEGISLATORS + sunLightQuery
                            + sunlightGetAPIKEY + sunlightKEY;
                    intent.putExtra("ZIPCODE", myPost); //intent.putExtra("ZIPCODE", randomZip);
                    Log.d("T", "about to restart watch MainActivity with ZIPCODE: " + randomZip
                            + " and url " + myPost);
                    startActivity(intent);
                    */
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    private String createRandomZip() {
        String zip = "";
        for (int i=0; i < 5; i++) {
            zip = zip + Integer.toString(new Random().nextInt(10));
        }
        return zip;
    }

    // Checks to see if a zipcode is valid, by checking if it returns _any_ reps.
    // Returns True on a failuer (ie:  no reps detected)
    private Boolean checkNoReps(String zipcode) {
        String myPost;
        sunLightQuery = sunlightSearchZIP + zipcode;
        // Get the address to pass in.
        myPost = sunlightURL + sunlightGetLEGISLATORS + sunLightQuery + sunlightGetAPIKEY + sunlightKEY;
        InputStream inputStream = null;
        String result = null;
        // Perform the JSON get.
        try {
            HttpURLConnection urlConnection = null;
            URL url = new URL(myPost);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN watch MAIN:  Most likely invalid URL");
        } finally {
            // Not sure of purpose of this.
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception squish) {
            }
        }
        // Now actually can test the reps
        try {
            JSONObject jObj_wrapper = new JSONObject(result);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            if (numReps <= 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN watch MAIN: " + e.toString());
        }
        Log.d("T", "FAILURE IN MAIN: How did it get to the end?");
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // XXX: FILL:  Implement the swipe (?) to election results
    // launch ElectionResultsActivity
    // ???  Send it the current XXXX Nothing!
    //zipcodeOrCurrpos XXXXX
    // Gesture Swipe Variant 2
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(SelectFilterActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    // A left swipe
                    //Go to Results.  Must pull the
                    Log.d("T", "Left swipe: ");
                    //Intent intent = new Intent(getBaseContext(), ElectionResultsActivity.class );
                    //Log.d("T", "about to start watch ElectionResultsActivity: ");
                    //startActivity(intent);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(SelectFilterActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    // A right swipe
                    Log.d("T", "Right swipe: ");
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }


    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
    // Swiping Coding based off http://stackoverflow.com/questions/937313/fling-gesture-detection-on-grid-layout
    // Accelerator Coding based off http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
}
