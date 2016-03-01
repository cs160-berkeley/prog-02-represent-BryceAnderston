package com.example.user.represent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // View elements
    private Button findReps;
    private EditText zipcodeEdit;
    private CheckBox useCurrentCheck;

    // Saves a zipPos from another Mobile activity, in case of SHAKE or CLICK_REP
    // XXX No Need (.finish() as back button)
    //private String savedZipPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findReps = (Button) findViewById(R.id.main_button_findReps);
        zipcodeEdit = (EditText) findViewById(R.id.main_editText_zipcode);
        useCurrentCheck = (CheckBox) findViewById(R.id.main_checkbox_useCurrent);

        //Pull relevant info from intent (here just toSaveZipPos)
        // XXX FILL

        // The ClickListener for findReps:
        //  Creates new instance of Main2 (and from that WatchMain) based on current data.
        //  Sets savedZipPos to the new zipPos, just to be safe.
        findReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the message to pass to other activities (they'll gather representatives)
                // A representation of the entered zipcode
                String zipstring = zipcodeEdit.getText().toString();
                // Use current position? Set command to track that.
                String commandMessage;
                if (!useCurrentCheck.isChecked()) {
                    commandMessage = "useZipcode";
                } else {
                    commandMessage = "useCurrPos";
                }
                // XXX FILLING:  put in code to check zipstring is proper
                if (zipstring.isEmpty()) {
                    zipstring = "00000";
                    zipstring = Integer.toString(new Random().nextInt(10000));
                }

                // Send message to watch
                // XXX NOT Doing this in create of Main2Activity
                Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                sendIntent.putExtra("COMMAND", commandMessage);
                sendIntent.putExtra("ZIPCODE", zipstring);
                Log.d("T", "about to start watch Watch Main with ZIPCODE: " + zipstring
                        + " and COMMAND: " + commandMessage);
                //Log.d("T", "sendIntent is: " + sendIntent.toString());
                startService(sendIntent);

                //Start Main2Activity
                String sendZipPos = zipstring;
                if (commandMessage.equals("useCurrPos")) {
                    sendZipPos = "usePos";
                }
                Intent intent = new Intent(getBaseContext(), Main2Activity.class);
                intent.putExtra("ZIPCODE_OR_USEPOS", sendZipPos);
                Log.d("T", "about to start watch Main2Activity with ZIPCODE_OR_USEPOS: " + sendZipPos);
                startActivity(intent);
            }
        });
    }

    // !!! On second though, create the repLst in here
    // On second second though, don't (no easy way to pass a lot of information across intents)
}
