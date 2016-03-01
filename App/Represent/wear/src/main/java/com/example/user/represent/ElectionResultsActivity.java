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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

public class ElectionResultsActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private TextView text_countyState;
    private TextView text_winner;
    private TextView text_winpercent;
    private TextView text_loser;
    private TextView text_losepercent;

    // Sensor Variables:  Setup
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_results);

        //Create view elements
        text_countyState = (TextView) findViewById(R.id.results_county_state);
        text_winner = (TextView) findViewById(R.id.results_winner);
        text_winpercent = (TextView) findViewById(R.id.results_winpercent);
        text_loser = (TextView) findViewById(R.id.results_loser);
        text_losepercent = (TextView) findViewById(R.id.results_losepercent);

        //Create sensors
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Gestures Swipe Variant 2
        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };


        //  XXX FILL Still need to fill in the o
        // XXX Not Even the right fields!
        //final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        //stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
        //    @Override
        //    public void onLayoutInflated(WatchViewStub stub) {
        //        mTextView = (TextView) stub.findViewById(R.id.text);
        //    }
        //});

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //DUMMY case, but will need to use REP_NAME or other passed value
        if (extras != null) {
            Log.d("T", "Results has been created.");
            String name = extras.getString("REP_NAME");
            if (name.equalsIgnoreCase("Jasmine Clarice") || name.equalsIgnoreCase("Jasmine")
                    || name.equalsIgnoreCase("Marvin Coolander")) {
                text_countyState.setText("Alameda, CA");
                text_winner.setText("Obama");
                text_winpercent.setText("68%");
                text_loser.setText("Romney");
                text_losepercent.setText("32%");
            } else {
                text_countyState.setText("Mono, CA");
                text_winner.setText("Romney");
                text_winpercent.setText("54%");
                text_loser.setText("Obama");
                text_losepercent.setText("46%");
            }
        } else {
            text_countyState.setText("Alameda, CA");
            text_winner.setText("Obama");
            text_winpercent.setText("68%");
            text_loser.setText("Romney");
            text_losepercent.setText("32%");
            Log.d("T", "WARNING: ElectionResults got no extras");
        }
    }

    // Shake functionality to call for a random Zip_code
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
                    startService(sendIntent);

                    //Self.rebuild
                    Intent intent = new Intent(getBaseContext(), MainActivity.class );
                    // This vs getBaseContext()?
                    // Feed a string version of the zipcode into the new intent
                    intent.putExtra("ZIPCODE", randomZip);
                    Log.d("T", "about to start watch MainActivity with ZIPCODE: " + randomZip);
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

    // XXX: FILL:  Implement the swipe (?) to MainActivity
    // return to ManActivity
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
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(SelectFilterActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    // A right swipe
                    // End activity and return to MainActivity
                    finish();
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
}
