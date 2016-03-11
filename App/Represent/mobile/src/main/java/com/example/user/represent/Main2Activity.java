package com.example.user.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.fabric.sdk.android.Fabric;
import retrofit.http.GET;
import retrofit.http.Query;

// For GoogleApi Implements
public class Main2Activity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    // The list of data objects that will populate the ListView
    private List<Representative> repList = new ArrayList<Representative>();
    private Button backButton;

    // ZipPos sent from other activity
    // Also is the value passed in as toSaveZipPos  (???)
    private String sentZipPos;

    // For GoogleApi    May need to find current position (lat, long) to determine representatives
    private GoogleApiClient mGoogleApiClient;
    private String mlatitude;
    private String mlongitude;
    private Location mLastLocation;
    // .. examples
    // congress.api.sunlightfoundation.com/legislators/locate?zip=94702&apikey=7dfe13f41a8d41d4aab7c74a86f84962
    // congress.api.sunlightfoundation.com/legislators/locate?latitude=37.9&longitude=122.3&apikey=7dfe13f41a8d41d4aab7c74a86f84962
    // congress.api.sunlightfoundation.com/committees?member_ids=L000551&apikey=7dfe13f41a8d41d4aab7c74a86f84962
    // congress.api.sunlightfoundation.com/bills/search?sponsor_id=L000551&apikey=7dfe13f41a8d41d4aab7c74a86f84962
    //
    // For Sunlight API
    // Receives zipcode from outside, performs async.execute() (NO ASYNC), and builds repList from within that
    // except for photo and twitter, which are added by later calls of twitter API
    // .. This will be modified before executing(), and will be referebced by doInBackground()
    //private String requestURL; //Replaced by SunlightQuery
    private static final String sunlightURL = "https://congress.api.sunlightfoundation.com";
    private static final String sunlightGetLEGISLATORS = "/legislators/locate";
    private static final String sunlightSearchZIP = "?zip=";
    private static final String sunlightSearchLAT = "?latitude=";
    private static final String sunlightAndLONG = "&longitude=";

    private static final String sunlightGetBills = "/bills/search";
    private static final String sunlightSearchMemberIds = "?member_ids=";

    private static final String sunlightGetCommittees = "/committees";
    private static final String sunlightSearchSponsorId = "?sponsor_id=";

    private String sunLightQuery = "";
    private static final String sunlightGetAPIKEY = "&apikey=";
    private static final String sunlightKEY = "7dfe13f41a8d41d4aab7c74a86f84962";

    // For Google Geocoding
    private static final String geocodingURL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String geocodingGetZIPCODE = "?address=";
    private static final String geocodingGetLATLNG = "?latlng=";
    private static final String geocodingGetKEY = "&key=";
    //WKEY
    private static final String geocodingKEY = "AIzaSyBFlS49OBIEWsyNpQXeJMei67o8GXlWdaw";

    // For Async
    private Boolean async_useCurrentPos;
    private String async_zipcode;
    private JSONParser jParser = new JSONParser();

    // For restart watchMain again
    private Boolean usingRandomZip = false;
    private String repListString;
    private String stateCountyString;
    private  JSONArray gottenFromCheckNoReps;

    // For Twitter
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "gf2wuUHRMXdPMLa2rdqtXkfQE";
    private static final String TWITTER_SECRET = "X1QbUmyIzU0ad3yKLfc4hEvwl7JGTPSqnKYQnUKlRhIMXZKrYc";

    //private MyTwitterApiClient twitterApiClient;
    //private TweetViewFetchAdapter adapter;
    private View globalItemView;
    private String twitterImage ;
    private Bitmap twitterImageBitmap;
    private String twitterTweet = "Tweet-tweet.";
    private ArrayList<Tweet> tweets = new ArrayList<>();
    private ImageView globalPhotoView;
    //private Bitmap globalImageBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Twitter
        //TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        //Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main2);

        // Call when need.  Not needed?
        // //TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();

        backButton = (Button) findViewById(R.id.item_button_back);

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


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NOT RESTART!  Go back to MainActivity
                finish();
                // Restart the MainActivity
                //Intent sendIntent = new Intent(getBaseContext(), MainActivity.class);
                //XXX No need// sendIntent.putExtra("TOSAVE_ZIPPOS", sentZipPos);
                //Log.d("T", "about to start watch MainActivity with TOSAVE_ZIPPOS: " + sentZipPos);
                //Log.d("T", "about to start watch MainActivity: ");
                //startService(sendIntent);
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //XXX String sentZipPos = "Null";
        // XXXX ??? MIGHT WANT TO FEATURE populateRepList as a seperate file?
        // Pull in data based on zipcode or position.
        // May come from data from Main, randomZip from Listener or savedZipPos from Details
        if (extras != null) {
            // PART C:
            sentZipPos = extras.getString("ZIPCODE_OR_USEPOS");
            if (sentZipPos.equals("usePos")) {
                // XXXX FILL LATER: Do what needs to be done to gather data by position
                // Arbitrary zipcode for DUMMY function
                async_useCurrentPos = true;
                async_zipcode = "23456";
            //    populateRepList(true, "23456");
            } else {
                // FILL LATER  Do what needs to be done to gather data by Zipcode
                // Pull and use the zipcode
                async_useCurrentPos = false;
                async_zipcode = sentZipPos;
                //int zipcode = Integer.parseInt(sentZipPos);
            //    populateRepList(false, sentZipPos);// zipcode);
            }
            if (extras.getString("USERAND") != null ) {
                Log.d("T", "Main2: USERAND not null");
                usingRandomZip = true;
            }
            mlatitude = extras.getString("LATITUDE");
            mlongitude = extras.getString("LONGITUDE");
        }
        // XXX ??? HOW TO GET IN VALUES?  Sent from Intents, mostly. Saving values.
        // Retrieve/receive UseCurrentPosition and zipcode
        //Create data to populate ListView
        ////populateRepList(true, 23456);

        // Fill in each sub-view with data from the data
        // semi-DUMMY function.  May change based on how pulled data is represented.
    //    populateListView();

        new FinishMain2Create().execute();

        // XXX Custom Method for allowing an item to be clicked
        //  XXX Not used here
        //XXX registerClickCallback();

    }

    // For GoogleApi
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }
    // For GoogleApi
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("T", "Main2 onConnected procced: ");
        //if checkPermission(FINE)
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //Log.d("T", "Main2 onConnected mLastLocation: " + mLastLocation.toString());
        } catch (SecurityException s) {
            Log.d("T", "Main2 onConnected threw an exception: " + s.toString());
        }
        if (mLastLocation != null) {
            Log.d("T", "Main2 onConnected mLastLocation not null: ");
            mlatitude = (String.valueOf(mLastLocation.getLatitude()));
            mlongitude = (String.valueOf(mLastLocation.getLongitude()));
            Log.d("T", "Main2 onConnected lat lang: " + mlatitude + ",  " + mlongitude);
        } else {
            Log.d("T", "Main2 onConnected mLastLocation IS null: ");
        }
        Log.d("T", "Main2 onConnected finished: ");
    }
    @Override
    public void onConnectionSuspended(int i) {Log.d("T", "Main2 onCnnectionSuspended procced: ");}
    @Override
    public void onConnectionFailed(ConnectionResult connResult) {Log.d("T", "Main2 onConnectionFailed procced: ");}


    class FinishMain2Create extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //JSONParser jParser = new JSONParser();
            // params never used

            if (usingRandomZip) {
                try {
                    Log.d("T", "Main2 Proccing checkRandomZip: ");
                    checkRandomZip();
                } catch (Exception e) {
                    Log.e("fail3", e.toString());
                    Log.d("T", "FAILURE IN MAIN2 doInBackGround: ");
                }
            }

            Log.d("T", "Main2 AsyncTask procced: ");
            populateRepList(async_useCurrentPos, async_zipcode);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean useless) {
            if (usingRandomZip) {
                Log.d("T", "Main2 Start Launching watch: ");
                launchWatch();
            }
            populateListView();
        }
    }

    private void checkRandomZip() {
        int i = 0;
        while (checkNoReps(async_zipcode)) {
            async_zipcode = createRandomZip();
            i++;
        }
        Log.d("T", "MAIN2 CheckRandom: Iterations = " + i);
        populateRepListStateCountyStrings(gottenFromCheckNoReps);
        //launchWatch();
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        String value = "Shook to: " + async_zipcode;
        Toast toast = Toast.makeText(context, value, duration);
        toast.show();
    }
    // Helpers for checkRandomZip...
    private String createRandomZip() {
        String zip = "";
        for (int i=0; i < 5; i++) {
            zip = zip + Integer.toString(new Random().nextInt(10));
        }
        return zip;
    }

    private Boolean checkNoReps(String zipcode) {
        String myPost;
        String sunlightQuery = sunlightSearchZIP + zipcode;
        // Get the address to pass in.
        myPost = sunlightURL + sunlightGetLEGISLATORS + sunlightQuery + sunlightGetAPIKEY + sunlightKEY;
        //Log.d("T", "Main2 checkNoReps myPost: " + myPost);
        /*
        Interior of AsyncTask went here.  No return then.
         */
        // Now actually can test the reps
        try {
            JSONObject jObj_wrapper = jParser.getJSONFromUrl(myPost);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            if (numReps <= 0) {
                Log.d("T", "MAIN2 CheckNoReps returning true: ");
                return true;
            } else {
                gottenFromCheckNoReps = jObj_wrapper.getJSONArray("results");
                Log.d("T", "MAIN2 CheckNoReps returning false: ");
                return false;
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN MAIN2 CheckNoReps: " + e.toString());
        }
        Log.d("T", "FAILURE IN MAIN2 CheckNoReps: How did it get to the end?");
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
                // aren't senind twitter_id thru
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
                    + async_zipcode + geocodingGetKEY + geocodingKEY;
            JSONObject jGeo = jParser.getJSONFromUrl(geoUrl);
            JSONArray jAddressComponents = jGeo.getJSONArray("results").getJSONObject(0)
                    .getJSONArray("address_components");
            String type;
            for (int i=0; i < jAddressComponents.length(); i++) {
                JSONObject oneObj = jAddressComponents.getJSONObject(i);
                type = oneObj.getJSONArray("types").getString(0);
                if (type.equalsIgnoreCase("administrative_area_level_1")) {
                    state = oneObj.getString("short_name");
                } else if (type.equalsIgnoreCase("administrative_area_level_2")) {
                    county = oneObj.getString("short_name").split("\\s+")[0];
                    Log.d("T", "Main created county = " + county);
                }
            }
            stateCountyString = "" + state + "," + county;
        } catch (JSONException e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN MAIN:  Failed outside JArray Loop. When trying to get state,county ?");
        }
    }

    private void populateRepList(Boolean useCurrentPos, String zipcode) {
        String myPost;
        if (useCurrentPos) {
            // DO ONE THING FOR POSITION:  Get position from mGoogleApi, input LAT and LONG
            // PART C FILLING
            sunLightQuery = sunlightSearchLAT + mlatitude + sunlightAndLONG + mlongitude;
        } else {
            // DO SOMETHING WITH zipcode:  Input zipcode
            sunLightQuery = sunlightSearchZIP + zipcode;
        }
        // Get the address to pass in.
        myPost = sunlightURL + sunlightGetLEGISLATORS + sunLightQuery + sunlightGetAPIKEY + sunlightKEY;
                // Now actually can populate list
        Log.d("T", "Main2 populateRepList myPost: " + myPost);
        try {
            JSONObject jObj_wrapper = jParser.getJSONFromUrl(myPost);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            JSONArray jArr_reps = jObj_wrapper.getJSONArray("results");

            for (int i=0; i < jArr_reps.length(); i++)
            {
                try {
                    JSONObject oneObj = jArr_reps.getJSONObject(i);
                    Representative currRep = new Representative();
                    // Pulling items from the array
                    currRep.setBioguide_id(oneObj.getString("bioguide_id"));
                    String name = oneObj.getString("title") + " " + oneObj.getString("first_name")
                            + " " + oneObj.getString("last_name");
                    currRep.setName(name);
                    String party = oneObj.getString("party");
                    if (party.equalsIgnoreCase("D")) {
                        party = "Democrat";
                    } else if (party.equalsIgnoreCase("R")) {
                        party = "Republican";
                    } else {
                        party = "Independent";
                    }
                    currRep.setParty(party);
                    currRep.setUrl_email(oneObj.getString("oc_email"));
                    currRep.setUrl_website(oneObj.getString("website"));
                    currRep.setDate(oneObj.getString("term_end"));
                    //Other
                    currRep.setState(oneObj.getString("state"));
                    currRep.setTwitter_id(oneObj.getString("twitter_id"));
                    // PART C:   Perform Twitter calls here?  Or when populate view?

                    repList.add(currRep);
                } catch (JSONException e) {
                    Log.e("fail 3", e.toString());
                    Log.d("T", "FAILURE IN MAIN2:  Failed inside JArray Loop");
                }
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN MAIN2:  Most likely a JSON Exception");
        }


    }

    private void populateListView() {
        ArrayAdapter<Representative> thisAdapater = new MyListAdapter();
        ListView repListView = (ListView) findViewById(R.id.listView);
        repListView.setAdapter(thisAdapater);
        // PART C :  TWITTER EXAMPLE ??
    }

    private void launchWatch() {
        // Send message to watch
        Log.d("T", "MAIN2: launchWatch about to getBaseContext()");
        Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
        String rawData = repListString + "&" + stateCountyString;

        sendIntent.putExtra("COMMAND", "rawData");

        sendIntent.putExtra("ZIPCODE", rawData);

        Log.d("T", "about to start watch Watch Main with ZIPCODE: " + rawData
                + " and COMMAND: " + "rawData  and rawData:  " + rawData);
        Log.d("T", "sendIntent is: " + sendIntent.toString());
        startService(sendIntent);
        /// TEMP  Remove if needed ^
    }

    // ArrayAdapter:  This is where the created sub-view layout comes into play.
    private class MyListAdapter extends ArrayAdapter<Representative> {
        public MyListAdapter() {
            //Context, layout, data
            super(Main2Activity.this, R.layout.layout, repList);
        }

        // XXX Can't do this bc can't define a custom interface in an inner class
        //XXX customButtonListener thisCustomListener;
        //XXX public interface customButtonListener {
        //XXX    public void onButtonClickListener( );
        //}


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // position: position in passed in list (replist)
            // Pull a sub-view if don't already have it.
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.layout, parent, false);
            }

            //Find Representative
            // semi-DUMMY
            Representative currentRepresentative = repList.get(position);

            //Fill sub-view
            //Photo  PART C TWITTER
            ImageView imageViewPhoto = (ImageView) itemView.findViewById(R.id.item_imageview_photo);
            imageViewPhoto.setImageResource(currentRepresentative.getPhoto());
            String bioG = currentRepresentative.getBioguide_id();
            String theActualPhotoUrl = "https://theunitedstates.io/images/congress/225x275/" +
                    bioG + ".jpg";
            // PICASSO.  this vs getBaseContext vs getContext ?
            Picasso.with(getBaseContext())
                    .load(theActualPhotoUrl)
                    .into(imageViewPhoto);
            //Name
            TextView textViewName = (TextView) itemView.findViewById(R.id.item_textview_name);
            textViewName.setText(currentRepresentative.getName());
            //Party
            TextView textViewParty = (TextView) itemView.findViewById(R.id.item_textview_party);
            textViewParty.setText(currentRepresentative.getParty());

            //More Info:  Sends
            Button buttonMoreInfo = (Button) itemView.findViewById(R.id.item_button_moreinfo);
            buttonMoreInfo.setTag(currentRepresentative);
            buttonMoreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //XXX FILLING
                    // Have currentRepresetative
                    Representative cRep = (Representative) v.getTag();
                    String name = cRep.getName();
                    Intent startDetailsIntent = new Intent(getBaseContext(), DetailedInfoActivity.class);
                    startDetailsIntent.putExtra("NAME", name);
                    startDetailsIntent.putExtra("BIOGUIDE", cRep.getBioguide_id());
                    //startDetailsIntent.putExtra();
                    // BBBB
                    // Allows to reconstruct Main2
                    // XXX ??? but what if call in from watch?  Handled by listener???
                    // XXX No need// intent.putExtra("TOSAVE_ZIPPOS", sentZipPos);
                    Log.d("T", "Main2 about to start watch DetailedInfoActivity with NAME: " + name
                            + " and bioguide " + cRep.getBioguide_id());
                    Log.d("T", "Main2 about to start intent: " + startDetailsIntent.toString());
                    startActivity(startDetailsIntent);
                }
            });
            // XXX !!! Am using setTag and getTag to push in the Representative, and all its data
            // !!! Each individual ovverridden onClick will have different coding, appropriate
            // to the function the click must perform (launch new activity, web surfing, ...)

            //URL Email
            TextView urlEmail = (TextView) itemView.findViewById(R.id.item_url_email);
            urlEmail.setText(currentRepresentative.getUrl_email());
            urlEmail.setTag(currentRepresentative);
            /*urlEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //XXX FILLING
                    // Have currentRepresetative
                    Representative cRep = (Representative) v.getTag();
                }
            });*/

            //URL Website
            TextView urlWebsite = (TextView) itemView.findViewById(R.id.item_url_website);
            urlWebsite.setText(currentRepresentative.getUrl_website());
            urlWebsite.setTag(currentRepresentative);
            /*urlWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //XXX FILLING
                    // Have currentRepresetative
                    Representative cRep = (Representative) v.getTag();
                }
            });*/

            //Twitter  PART C TWITTER TwitTwit twit TWIT
            globalItemView = itemView;
            TextView textviewTwitter = (TextView) itemView.findViewById(R.id.item_textview_twitter);
            textviewTwitter.setText(currentRepresentative.getTwitter());

            //*
            String twitID = currentRepresentative.getTwitter_id();
            //UserTimeline userTimeline = new UserTimeline.Builder().screenName(twitID).build();

            TwitterSession session =
                    Twitter.getSessionManager().getActiveSession();

            //START GetTweet
            TwitterCore.getInstance().getApiClient(session).getStatusesService()
                    .userTimeline(null,
                            twitID, // screenname
                            1, //number tweets to get
                            null, null, null, null, null, null,
                            new Callback<List<Tweet>>() {
                                @Override
                                public void success(Result<List<Tweet>> result) {
                                    for (Tweet t : result.data) {
                                        tweets.add(t);
                                        android.util.Log.d("twittercommunity", "tweet is " + t.text);
                                        twitterTweet = t.text;
                                    }
                                    TextView tweetView = (TextView) globalItemView.findViewById(R.id.item_textview_twitter);
                                    tweetView.setText(twitterTweet);
                                }

                                @Override
                                public void failure(TwitterException exception) {
                                    android.util.Log.d("twittercommunity", "exception " + exception);
                                }
                            });
            //END GetTweet    /textviewTwitter.setText(twitterTweet);


            /*
            //START GetPhoto  TWITTER PHOTO
            Twitter.getApiClient(session).getAccountService()
                    .verifyCredentials(true, false, new Callback<User>() {

                        @Override
                        public void success(Result<User> userResult) {

                            User user = userResult.data;
                            twitterImage = user.profileImageUrl;
                            android.util.Log.d("T", "tweetImage is " + twitterImage);


                            globalPhotoView = (ImageView) globalItemView.findViewById(R.id.item_imageview_photo);
                            /// CREATE GLOBAL.  SET GLOBAL.  SET THINGIES
                            // 1)  Create new Async to set photoView
                            // 2)  Create Login Button
                            //photoView.setImageBitmap();

                            //    new DownloadImageTask().execute(twitterImage);
                        }

                        @Override
                        public void failure(TwitterException e) {

                        }
                    });
            // END GETPHOTO  TWITTER PHOTO
            */
            //imageViewPhoto.setImageBitmap();
                    //setImageURI(twitterImage);
                    //setImageResource(twitterImage);

            /*TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(getBaseContext())
                    .setTimeline(userTimeline)
                    .build();
            setListAdapter(adapter); */
            //textviewTwitter.setText(currentRepresentative.getTwitter());

            return itemView;
            //return super.getView(position, convertView, parent);
        }
    }

    // TWITTER PHOTO
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        //ImageView bmImage;

        //public DownloadImageTask(ImageView bmImage) {
        //    this.bmImage = bmImage;
       // }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            twitterImageBitmap = mIcon11;
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            globalPhotoView.setImageBitmap(result);
        }
    }

    /*
    // New Class for getting user images
    class MyTwitterApiClient extends TwitterApiClient {
        public MyTwitterApiClient(TwitterSession session) {
            super(session);
        }

        public UsersService getUsersService() {
            return getService(UsersService.class);
        }
    }

    interface UsersService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") Long userId,
                  @Query("screen_name") String screenName,
                  @Query("include_entities") Boolean includeEntities,
                  Callback<User> cb);
    }
    */

    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
    //  Async Load image modified from  http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    //http://stackoverflow.com/questions/30518639/get-list-of-tweets-in-user-time-line-using-fabric-android
}
