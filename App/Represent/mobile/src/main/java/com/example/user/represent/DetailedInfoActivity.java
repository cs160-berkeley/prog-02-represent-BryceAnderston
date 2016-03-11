package com.example.user.represent;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// For GoogleApi NO NEED FOR GOOGLE API in DETAILS
public class DetailedInfoActivity extends AppCompatActivity {

    // Import in representative at creation. Use currRep.committees and currRep.bills to
    // populate the ListViews of this Activity
    // XXX ??? How to pass in?
    private Button backButton;

    //For Sunlight
    private static final String sunlightURL = "https://congress.api.sunlightfoundation.com";
    //private static final String sunlightGetLEGISLATORS = "/legislators/locate";
    private static final String sunlightGetLEGISLATOR = "/legislators";
    private static final String sunlightSearchBioguide = "?bioguide_id=";
    //private static final String sunlightSearchZIP = "?zip=";
    //private static final String sunlightSearchLAT = "?latitude=";
    //private static final String sunlightAndLONG = "&longitude=";

    private static final String sunlightGetBills = "/bills/search";
    private static final String sunlightSearchSponsorId = "?sponsor_id=";

    private static final String sunlightGetCommittees = "/committees";
    private static final String sunlightSearchMemberIds = "?member_ids=";

    //private String sunLightQuery = "";
    private  String sunLightRepQuery = "";
    String sunlightBillQuery = "";
    String sunlightCommQuery = "";
    private static final String sunlightGetAPIKEY = "&apikey=";
    private static final String sunlightKEY = "7dfe13f41a8d41d4aab7c74a86f84962";

    // Representative data
    private Representative currentRepresentative = new Representative();
    private List<String> listCommittees = new ArrayList<String>();
    private List<String> listBills = new ArrayList<String>();
    //private int photo;
    private String name;
    private String bioguide_id;
    //private  String party;
    //private String date;

    // XXX details_listview_<name>
    //NO NEED XXX// private String savedPosZip;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);

        Intent intent = getIntent();
        Log.d("T", "DetailedInfoActivity got intent: " + intent);
        Bundle extras = intent.getExtras();
        //name = extras.getString("NAME");
        bioguide_id = extras.getString("BIOGUIDE");
        Log.d("T", "DetailedInfoActivity got name: " + name + " and bioguide_id: " + bioguide_id);
        //NO NEED XXX//savedPosZip = extras.getString("SAVED_ZIP_OR_POS");


        backButton = (Button) findViewById(R.id.details_button_back);
        // XXX Redo back nutton once know how to reset Main2
        // Current thinking:  Just let constructor have no input fields, and just bring in the
        // global list (which set in MainActivity? Watch shake says no.)
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NOT RESTART.  Go back to.
                finish();
                // Restart the Main2Activity
                //Intent sendIntent = new Intent(getBaseContext(), MainActivity.class);
                //startService(sendIntent);
            }
        });

        sunlightBillQuery = sunlightURL + sunlightGetBills + sunlightSearchSponsorId
                + bioguide_id + sunlightGetAPIKEY + sunlightKEY;
        sunlightCommQuery = sunlightURL + sunlightGetCommittees + sunlightSearchMemberIds
                + bioguide_id + sunlightGetAPIKEY + sunlightKEY;
        //populateBillData(sunlightBillQuery);
        //populateCommitteeData(sunlightCommQuery);

        sunLightRepQuery = sunlightURL + sunlightGetLEGISLATOR + sunlightSearchBioguide
                + bioguide_id + sunlightGetAPIKEY + sunlightKEY;
        //populateRepresentative(sunLightRepQuery);

        // Populate Data
        // BUG:  Improper data.
        // Don't populate Rep Comms/Bills directly anymore, so this would get null
        //listCommittees = currentRepresentative.getCommittees();
        //listBills = currentRepresentative.getBills();
        // Populate sub-views
        //populateListViews();
        new FinishDetailsCreate().execute();

    }

    class FinishDetailsCreate extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //JSONParser jParser = new JSONParser();
            // params never used
            Log.d("T", "Details AsyncTask procced: ");
            Log.d("T", "Details AsyncTask BillQ: " + sunlightBillQuery);
            Log.d("T", "Details AsyncTask CommQ: " + sunlightCommQuery);
            Log.d("T", "Details AsyncTask repQ: " + sunLightRepQuery);
            populateBillData(sunlightBillQuery);
            populateCommitteeData(sunlightCommQuery);
            populateRepresentative(sunLightRepQuery);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean useless) {
            populateListViews();
        }
    }

    private void populateBillData(String mUrl) {
        // Get the address to pass in. Already is mUrl
        InputStream inputStream = null;
        String result = null;
        // Perform the JSON get.
        try {
            HttpURLConnection urlConnection = null;
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS BillData:  Most likely invalid URL");
        } finally {
            // Not sure of purpose of this.
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        // Now actually can populate list
        try {
            JSONObject jObj_wrapper = new JSONObject(result);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            JSONArray jArr_reps = jObj_wrapper.getJSONArray("results");

            Log.d("T", "Details listBills (pre-loop): " + listBills.toString());
            for (int i=0; i < jArr_reps.length(); i++)
            {
                try {
                    JSONObject oneObj = jArr_reps.getJSONObject(i);
                    Log.d("T", "Details inside Bill loop; current JSON: " + oneObj.toString());
                    Log.d("T", "Details listBills: " + listBills.toString());
                    String input;
                    //Log.d("T", "Details inside Bill loop; popular title: " + oneObj.getString("popular_title"));
                    //if (oneObj.getString("popular_title").equalsIgnoreCase("null")) {
                    //    Log.d("T", "Details inside Bill loop; actually string null: ");
                    //}
                    //if (oneObj.getString("official_title") != null) {
                    //    listBills.add(oneObj.getString("official_title"));
                    //} else
                    if (! oneObj.getString("popular_title").equalsIgnoreCase("null")) {
                        listBills.add(oneObj.getString("popular_title"));
                    } else if (! oneObj.getString("short_title").equalsIgnoreCase("null")) {
                        listBills.add(oneObj.getString("short_title"));
                    } else if (! oneObj.getString("official_title").equalsIgnoreCase("null")) {
                        listBills.add(oneObj.getString("official_title"));
                    } else {
                        Log.d("T", "Odd in Details:  A Bill with no title. id: " + i);
                    }
                    //Log.d("T", "Details inside Bill loop; listBills: XX");
                } catch (JSONException e) {
                    Log.e("fail 3", e.toString());
                    Log.d("T", "FAILURE IN BillData:  Failed inside JArray Loop");
                }
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS billData:  Most likely a JSON Exception");
        }
    }

    private void populateCommitteeData(String mUrl) {
        // Get the address to pass in. Already is mUrl
        InputStream inputStream = null;
        String result = null;
        // Perform the JSON get.
        try {
            HttpURLConnection urlConnection = null;
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            Log.d("T", "Details AsyncTask procced up to BufferedReader: ");
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
            Log.d("T", "Details AsyncTask procced result: " + result);
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS commdata:  Most likely invalid URL");
        } finally {
            // Not sure of purpose of this.
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        // Now actually can populate list
        try {
            JSONObject jObj_wrapper = new JSONObject(result);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            JSONArray jArr_reps = jObj_wrapper.getJSONArray("results");
            Log.d("T", "Details AsyncTask count: " + numReps);
            Log.d("T", "Details AsyncTask jArr_reps: " + jArr_reps.toString());



            for (int i=0; i < jArr_reps.length(); i++)
            {
                try {
                    JSONObject oneObj = jArr_reps.getJSONObject(i);
                    listCommittees.add(oneObj.getString("name"));
                    Log.d("T", "Details inside populateCommittee; listCommittees: " + listCommittees.toString());
                } catch (JSONException e) {
                    Log.e("fail 3", e.toString());
                    Log.d("T", "FAILURE IN Details Commdata:  Failed inside JArray Loop");
                }
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS commData:  Most likely a JSON Exception");
        }
    }

    private void populateRepresentative(String mUrl) {
        // Get the address to pass in. Already is mUrl
        InputStream inputStream = null;
        String result = null;
        // Perform the JSON get.
        try {
            HttpURLConnection urlConnection = null;
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS:  Most likely invalid URL");
        } finally {
            // Not sure of purpose of this.
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        // Now actually can populate list
        try {
            JSONObject jObj_wrapper = new JSONObject(result);
            // No need to count number representatives here
            int numReps = jObj_wrapper.getInt("count");
            JSONArray jArr_reps = jObj_wrapper.getJSONArray("results");

            for (int i=0; i < jArr_reps.length(); i++)
            {
                try {
                    JSONObject oneObj = jArr_reps.getJSONObject(i);
                    currentRepresentative.setName(oneObj.getString("title") + " "
                            + oneObj.getString("first_name") + " " + oneObj.getString("last_name"));
                    String party = oneObj.getString("party");
                    if (party.equalsIgnoreCase("D")) {
                        party = "Democrat";
                    } else if (party.equalsIgnoreCase("R")) {
                        party = "Republican";
                    } else {
                        party = "Independent";
                    }
                    currentRepresentative.setParty(party);
                    currentRepresentative.setDate(oneObj.getString("term_end"));

                    currentRepresentative.setState(oneObj.getString("state"));
                    currentRepresentative.setTwitter_id(oneObj.getString("twitter_id"));
                    // PART C:   Perform Twitter calls here?  Or when populate view?
                    // Photo
                } catch (JSONException e) {
                    Log.e("fail 3", e.toString());
                    Log.d("T", "FAILURE IN DETAILS RepData:  Failed inside JArray Loop");
                }
            }
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN DETAILS RepData:  Most likely a JSON Exception");
        }
    }

    private void populateListViews() {
        //Photo  // PART C TWITTER
        ImageView imageViewPhoto = (ImageView) findViewById(R.id.details_imageview_photo);
        //imageViewPhoto.setImageResource(currentRepresentative.getPhoto());
        //PICASSO
        String bioG = bioguide_id;
        String theActualPhotoUrl = "https://theunitedstates.io/images/congress/225x275/" +
                bioG + ".jpg";
        // PICASSO.  this vs getBaseContext vs getContext ?
        Picasso.with(getBaseContext())
                .load(theActualPhotoUrl)
                .into(imageViewPhoto);
        //Name
        TextView textViewName = (TextView) findViewById(R.id.details_textview_name);
        textViewName.setText(currentRepresentative.getName());
        //Party
        TextView textViewParty = (TextView) findViewById(R.id.details_textview_party);
        textViewParty.setText(currentRepresentative.getParty());
        //Party
        TextView textViewDate = (TextView) findViewById(R.id.details_textview_date);
        textViewDate.setText(currentRepresentative.getDate());


        ArrayAdapter<String> commAdapter = new ArrayAdapter<String>(DetailedInfoActivity.this,
                android.R.layout.simple_list_item_1,android.R.id.text1, listCommittees);
        ListView commList = (ListView) findViewById(R.id.details_listview_committees);
        //Log.d("T", "about to start watch Details currentRep: " + commList.toString());
        Log.d("T", "Details listBills: " + listBills.toString());
        Log.d("T", "Details listCommittees: " + listCommittees.toString());
        Log.d("T", "Details commadapter: " + commAdapter.toString());
        Log.d("T", "Details commlist: " + commList.toString());
        commList.setAdapter(commAdapter);

        ArrayAdapter<String> billAdapter = new ArrayAdapter<String>(DetailedInfoActivity.this,
                android.R.layout.simple_list_item_1,android.R.id.text1, listBills);
        ListView billList = (ListView) findViewById(R.id.details_listview_bills);
        billList.setAdapter(billAdapter);
    }
    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
    // Swiping Coding based off http://stackoverflow.com/questions/937313/fling-gesture-detection-on-grid-layout
}
