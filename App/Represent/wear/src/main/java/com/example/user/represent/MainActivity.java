package com.example.user.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("T", "Watch Main onCreate begun: ");
        super.onCreate(savedInstanceState);
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
            message = extras.getString("ZIPCODE");
            if (message.equals("CurrPos")) {
                // Arbitrary zipcode
                populateRepList(true, 23456);
            } else {
                // Pull and use the zipcode
                populateRepList(false, Integer.parseInt(message));
            }
        } else {
            //Populate based on current position if brought up naturally.
            populateRepList(true, 23456);
        }
        zipcodeOrCurrpos = message;
        // XXX ??? HOW TO GET IN VALUES?
        //Create data to populate ListView
        // XXX ??? Might just pull in data from Mobile Activity.  Probably not.
        //populateRepList(true, 23456);
        // Fill in each sub-view with data from the data
        populateListView();
        // Custom Method for allowing an item to be clicked
        registerClickCallback();
    }

    private void populateRepList(Boolean useCurrentPos, int zipcode) {
        // XXX Dummy:  Test data
        Log.d("T", "Watch Main poupulateRepList procced with: useCurrentPos =" + useCurrentPos.toString()
                + " and " + zipcode);
        List<String> dumComs = new ArrayList<String>();
        dumComs.add("Committee of superordination");
        dumComs.add("Committee of small mammals");
        dumComs.add("Committee of Getting Things DONE... tomorrow");
        List<String> dumBills = new ArrayList<String>();
        dumComs.add("Bill to prevent flossing");
        dumComs.add("1985 Cross-chronological Homestead Act");
        if (useCurrentPos) {
            // DO ONE THING with current position. However get that.
            // XXX DUMMY: This is test
            repList.add(new Representative(R.drawable.andrew_jackson, "Mr. Samuels", "Independent", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1856", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.dianne_feinstein, "Horace Mumphis", "Republican", "IMail@woosh.com",
                    "www.hello.me", "tweettwitter", "1/22/2071", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.barbara_boxer, "Jasmine", "Democrat", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1986", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.teddy_roosevelt, "Marvin Coolander", "Democrat", "IHaveYourMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1992", (ArrayList) dumComs,
                    (ArrayList) dumBills));
        } else {
            // DO SOMETHING WITH zipcode
            // XXX DUMMY:  This is test code
            if (zipcode > 4999) {
                repList.add(new Representative(R.drawable.andrew_jackson, "Mr. Samuels", "Independent", "IHaveMail@mail.com",
                        "www.Iam.me", "tweettweet tweettweet", "1/22/1856", (ArrayList) dumComs,
                        (ArrayList) dumBills));
                repList.add(new Representative(R.drawable.dianne_feinstein, "Horace Mumphis", "Republican", "IMail@woosh.com",
                        "www.hello.me", "tweettwitter", "1/22/2071", (ArrayList) dumComs,
                        (ArrayList) dumBills));
            } else {
                repList.add(new Representative(R.drawable.barbara_boxer, "Jasmine", "Democrat", "IHaveMail@mail.com",
                        "www.Iam.me", "tweettweet tweettweet", "1/22/1986", (ArrayList) dumComs,
                        (ArrayList) dumBills));
                repList.add(new Representative(R.drawable.teddy_roosevelt, "Marvin Coolander", "Democrat", "IHaveYourMail@mail.com",
                        "www.Iam.me", "tweettweet tweettweet", "1/22/1992", (ArrayList) dumComs,
                        (ArrayList) dumBills));
            }
        }
    }

    private void populateListView() {
        Log.d("T", "Watch Main populateListView procced: ");
        ArrayAdapter<Representative> thisAdapater = new MyListAdapter();
        ListView repList = (ListView) findViewById(R.id.listView);
        repList.setAdapter(thisAdapater);
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
                Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                // !!! Set to "None" for random shake
                sendIntent.putExtra("REP_NAME", sendName);
                // !!! Set to a random value if are calling for a random
                sendIntent.putExtra("RANDOM_ZIPCODE", "None");
                Log.d("T", "OK! about to start mobile Details with REP_NAME: "+ sendName);
                startService(sendIntent);
                // Also go to this representative's county's election results
                Intent resultIntent = new Intent(getBaseContext(), ElectionResultsActivity.class );
                resultIntent.putExtra("REP_NAME", sendName);
                Log.d("T", "about to start watch ElectionResultsActivity  for" +
                        " county of : " + sendName);
                startActivity(resultIntent);
                // XXX !!! !!! What happens if click a rep while mobile on home screen?
                // Are merely launching a new activity. The current activity has no bearing.
            }
        });
    }

    // XXX FILL:  Implemt the shake functionality to call for a random Zip_code
    //Deactivate sensor when not in use
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
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
                    String randomZip = Integer.toString(new Random().nextInt(10000));

                    // FILLING:  Send the random zipcode through. Also: Rebuild self.
                    Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                    // !!! Set to "None" for random shake
                    sendIntent.putExtra("REP_NAME", "None");
                    // !!! Set to a random value if are calling for a random
                    sendIntent.putExtra("RANDOM_ZIPCODE", randomZip);
                    Log.d("T", "about to start watch Main2Activity with RANDOM_ZIPCODE: " + randomZip);
                    // DUMMY
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    String value = "Shook to: " + randomZip;
                    Toast toast = Toast.makeText(context, value, duration);
                    toast.show();

                    startService(sendIntent);

                    //Self.rebuild
                    Intent intent = new Intent(getBaseContext(), MainActivity.class );
                    //getBaseContext() vs this ?  Originally was this
                    // Feed a string version of the zipcode into the new intent
                    intent.putExtra("ZIPCODE", randomZip);
                    Log.d("T", "about to restart watch MainActivity with ZIPCODE: " + randomZip);
                    startActivity(intent);
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
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
