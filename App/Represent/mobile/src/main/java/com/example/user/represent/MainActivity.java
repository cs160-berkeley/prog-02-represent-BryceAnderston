package com.example.user.represent;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

// For GoogleApi implements
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "gf2wuUHRMXdPMLa2rdqtXkfQE";
    private static final String TWITTER_SECRET = "X1QbUmyIzU0ad3yKLfc4hEvwl7JGTPSqnKYQnUKlRhIMXZKrYc";

    // View elements
    private Button findReps;
    private EditText zipcodeEdit;
    private CheckBox useCurrentCheck;

    private TwitterLoginButton loginButton;

    // For GoogleApi
    private GoogleApiClient mGoogleApiClient;
    private String mlatitude = "null";
    private String mlongitude = "null";
    private Location mLastLocation;

    // For sunlight Query
    private String requestURL;
    //
    //private static final String sunlightURL = "congress.api.sunlightfoundation.com";
    private static final String sunlightURL = "https://congress.api.sunlightfoundation.com";
    private static final String sunlightGetLEGISLATORS = "/legislators/locate";
    private static final String sunlightSearchZIP = "?zip=";
    private static final String sunlightSearchLAT = "?latitude=";
    private static final String sunlightAndLONG = "&longitude=";
    private String sunlightQuery = "";
    private static final String sunlightGetAPIKEY = "&apikey=";
    // For Google Geocoding
    //https://maps.googleapis.com/maps/api/geocode/json?address=94702&key=
    //https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=
    //https://maps.googleapis.com/maps/api/geocode/json?address=78703&key=
    // For Google Geocoding
    private static final String geocodingURL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String geocodingGetZIPCODE = "?address=";
    private static final String geocodingGetLATLNG = "?latlng=";
    private static final String geocodingGetKEY = "&key=";
    //WKEYS
    private static final String sunlightKEY = "7dfe13f41a8d41d4aab7c74a86f84962";
    private static final String geocodingKEY = "AIzaSyBFlS49OBIEWsyNpQXeJMei67o8GXlWdaw";
    //
    private String defaultGeocodingURL = geocodingURL + geocodingGetZIPCODE
            + "94702" + geocodingGetKEY + geocodingKEY;

    //For getting query
    //private InputStream inputStream = null;
    //private String result = null;
    //private Boolean useCurrPos_forAsync;
    private String commandMessage;
    private String zipstring;
    private JSONParser jParser = new JSONParser();
    //  Stores full_name,party,bioguide_id&full_name,party,bioguide_id etc. from the query
    private String repListString;
    private String stateCountyString;
    // Array of all representatives
    private  JSONArray gottenFromCheckNoReps;

    // Saves a zipPos from another Mobile activity, in case of SHAKE or CLICK_REP
    // XXX No Need (.finish() as back button)
    //private String savedZipPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        //START TWITTER LOGIN 1
        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
        //END TWITTER LOGIN 1

        findReps = (Button) findViewById(R.id.main_button_findReps);
        zipcodeEdit = (EditText) findViewById(R.id.main_editText_zipcode);
        useCurrentCheck = (CheckBox) findViewById(R.id.main_checkbox_useCurrent);

        //For GoogleApi
        // PART C:  Main either finds zip of current location (?) or just sends inputted zip to
        // Main2 and watch Main.  ???:  Main sends usePos, so Main2, etc handle local position?
        //  YES.  Need to save Lat, Lang or Zip at other end anyway.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Pull relevant info from intent (here just toSaveZipPos)
        // XXX FILL

        // The ClickListener for findReps:
        //  Creates new instance of Main2 (and from that WatchMain) based on current data.
        //  Sets savedZipPos to the new zipPos, just to be safe.
        findReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PART C:  zipcodes will be manipulated as strings
                // Set the message to pass to other activities (they'll gather representatives)
                // A representation of the entered zipcode
                Log.d("T", "Main findreps.onClickListener procced: ");
                zipstring = zipcodeEdit.getText().toString();
                // Use current position? Set command to track that.
                //String commandMessage;
                if (!useCurrentCheck.isChecked()) {
                    commandMessage = "useZipcode";
                } else {
                    commandMessage = "useCurrPos";
                }
                // XXX FILLING:  put in code to check zipstring is proper
                if (zipstring.isEmpty()) {
                    //zipstring = "00000";
                    zipstring = "94702";
                    //zipstring = Integer.toString(new Random().nextInt(10000));
                }
                new FinishMainCreate().execute();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    class FinishMainCreate extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //JSONParser jParser = new JSONParser();
            // params never used
            Log.d("T", "Main AsyncTask procced: ");

            // Make sure get a valid address
            if (checkNoReps(commandMessage.equalsIgnoreCase("useCurrPos"), zipstring)) {
                //Context context = getApplicationContext();
                //int duration = Toast.LENGTH_LONG;
                //String value = "We're sorry, but we found no representatives.";
                //Toast toast = Toast.makeText(context, value, duration);
                //toast.show();
                //Log.d("T", "MAIN: doInBackground found nothing");
                return true;
            }
            Log.d("T", "MAIN: doInBackground found something");
            populateRepListStateCountyStrings(gottenFromCheckNoReps);
            return false;

            // Getting JSON from URL
            //JSONObject json = jParser.getJSONFromUrl(requestURL);
        }

        @Override
        protected void onPostExecute(Boolean foundNothing) {
            if (foundNothing) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                String value = "We're sorry, but we found no representatives.";
                Toast toast = Toast.makeText(context, value, duration);
                toast.show();
                return;
            }
            // Send message to watch
            // XXX NOT Doing this in create of Main2Activity
            Log.d("T", "MAIN: onPostExecute about to getBaseContext()");
            Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
            String rawData = repListString + "&" + stateCountyString;
            //sendIntent.putExtra("COMMAND", "useFullUrl");
            sendIntent.putExtra("COMMAND", "rawData");
            //sendIntent.putExtra("ZIPCODE", zipstring);
            sendIntent.putExtra("ZIPCODE", rawData);
            //String tempMessage = commandMessage;
            //commandMessage = "useFullUrl";
            Log.d("T", "about to start watch Watch Main with ZIPCODE: " + rawData
                    + " and COMMAND: " + "rawData  and rawData:  " + rawData);//zipString,useFullUrl
            //Log.d("T", "sendIntent is: " + sendIntent.toString());
            startService(sendIntent);
            /// TEMP  Remove if needed ^

            //Start Main2Activity
            String sendZipPos = zipstring;
            if (commandMessage.equals("useCurrPos")) {
                sendZipPos = "usePos";
            }
            Intent intent = new Intent(getBaseContext(), Main2Activity.class);
            intent.putExtra("ZIPCODE_OR_USEPOS", sendZipPos);
            intent.putExtra("LATITUDE", mlatitude);
            intent.putExtra("LONGITUDE", mlongitude);
            Log.d("T", "about to start watch Main2Activity with ZIPCODE_OR_USEPOS: " + sendZipPos);
            startActivity(intent);
        }
    }

    private Boolean checkNoReps(Boolean useCurrentPos, String zipcode) {
        String myPost;
        Log.d("T", "Main checkNoReps procced: ");
        //Log.d("T", "Main checkNoReps useCurrentPos: ");
        if (useCurrentPos) {
            // DO ONE THING FOR POSITION:  Get position from mGoogleApi, input LAT and LONG
            // PART C FILLING
            sunlightQuery = sunlightSearchLAT + mlatitude + sunlightAndLONG + mlongitude;
            Log.d("T", "Main checkNoReps useCurr procced, made url fragment: " + sunlightQuery);
        } else {
            // DO SOMETHING WITH zipcode:  Input zipcode
            sunlightQuery = sunlightSearchZIP + zipcode;
            Log.d("T", "Main checkNoReps useCurr not procced, made url fragment: " + sunlightQuery);

        }
        // Get the address to pass in.
        myPost = sunlightURL + sunlightGetLEGISLATORS + sunlightQuery + sunlightGetAPIKEY + sunlightKEY;
        requestURL = myPost;
        Log.d("T", "Main checkNoReps myPost: " + myPost);
        Log.d("T", "Main checkNoReps requestUrl for getJSON: " + requestURL);
        /*
        Interior of AsyncTask went here.  No return then.
         */
        // Now actually can test the reps
        try {
            JSONObject jObj_wrapper = jParser.getJSONFromUrl(requestURL);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            if (numReps <= 0) {
                return true;
            } else {
                gottenFromCheckNoReps = jObj_wrapper.getJSONArray("results");
                return false;
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN MAIN: " + e.toString());
        }
        Log.d("T", "FAILURE IN MAIN: How did it get to the end?");
        return true;
    }

    private void populateRepListStateCountyStrings(JSONArray jReps) {
        //takes form REP&REP&REP... where REP is full_name,party,bioguide_id
        repListString = "";
        for (int i=0; i < jReps.length(); i++)
        {
            try {
                JSONObject oneObj = jReps.getJSONObject(i);
                //Representative currRep = new Representative();
                // Pulling items from the array
                String biog =  oneObj.getString("bioguide_id");
                String name = oneObj.getString("title") + " " + oneObj.getString("first_name")
                        + " " + oneObj.getString("last_name");
                String party = oneObj.getString("party");
                if (party.equalsIgnoreCase("D")) {
                    party = "Democrat";
                } else if (party.equalsIgnoreCase("R")) {
                    party = "Republican";
                } else {
                    party = "Independent";
                }
                //currRep.setTwitter_id(oneObj.getString("twitter_id"));
                // PART C:   Perform Twitter calls here?  Or when populate view?

                repListString = repListString + name + "," + party + "," + biog;
                if (i < jReps.length() - 1) {
                    repListString = repListString + "_";
                }
            } catch (JSONException e) {
                Log.e("fail 3", e.toString());
                Log.d("T", "FAILURE IN MAIN:  Failed inside JArray Loop");
            }
        }
        try {
            String state = "";
            String county = "";
            String geoUrl = geocodingURL + geocodingGetZIPCODE
                    + zipstring + geocodingGetKEY + geocodingKEY;
            JSONObject jGeo = jParser.getJSONFromUrl(geoUrl);
            JSONArray jAddressComponents = jGeo.getJSONArray("results").getJSONObject(0)
                    .getJSONArray("address_components");
            String type;
            for (int i=0; i < jAddressComponents.length(); i++) {
                JSONObject oneObj = jAddressComponents.getJSONObject(i);
                type = oneObj.getJSONArray("types").getString(0);
                if (type.equalsIgnoreCase("administrative_area_level_1")) {
                    //state goes first
                    state = oneObj.getString("short_name");
                } else if (type.equalsIgnoreCase("administrative_area_level_2")) {
                    //County goes second
                    county = oneObj.getString("short_name").split("\\s+")[0];
                    Log.d("T", "Main created county = " + county);
                    Log.d("T", "Main original county = " + oneObj.getString("short_name"));
                    Log.d("T", "Main test county = " + oneObj.getString("short_name").replaceAll(" County",""));
                }
            }
            stateCountyString = "" + state + "," + county;
        } catch (JSONException e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN MAIN:  Failed outside JArray Loop. When trying to get state,county ?");
        }
    }


    // For GoogleApi
    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();

    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    // For GoogleApi
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("T", "Main onConnected procced: ");

        try {
            // This returns null
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //Log.d("T", "Main onConnected mLastLocation: " + mLastLocation.toString());
        } catch (SecurityException s) {
            Log.d("T", "Main onConnected threw an exception: " + s.toString());
        }
        if (mLastLocation != null) {
            mlatitude = (String.valueOf(mLastLocation.getLatitude()));
            Log.d("T", "Main onConnected mLastLocation not null: ");
            mlongitude = (String.valueOf(mLastLocation.getLongitude()));
            Log.d("T", "Main onConnected lat lang: " + mlatitude + ",  " + mlongitude);
        } else {
            Log.d("T", "Main onConnected mLastLocation IS null: ");
        }
        Log.d("T", "Main onConnected finished: ");
    }

    @Override
    public void onConnectionSuspended(int i) {Log.d("T", "Main onCnnectionSuspended procced: ");}

    @Override
    public void onConnectionFailed(ConnectionResult connResult) {Log.d("T", "Main onConnectionFailed procced: ");}

    // !!! On second though, create the repLst in here
    // On second second though, don't (no easy way to pass a lot of information across intents)
}
