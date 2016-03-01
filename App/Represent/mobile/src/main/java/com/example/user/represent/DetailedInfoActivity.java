package com.example.user.represent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DetailedInfoActivity extends AppCompatActivity {

    // Import in representative at creation. Use currRep.committees and currRep.bills to
    // populate the ListViews of this Activity
    // XXX ??? How to pass in?
    private Button backButton;

    private Representative currentRepresentative;
    private List<String> listCommittees;
    private List<String> listBills;
    //private int photo;
    private String name;
    //private  String party;
    //private String date;

    // XXX details_listview_<name>
    //NO NEED XXX// private String savedPosZip;
    //
    //XXX DUMMY
    List<String> dumComs;
    List<String> dumBills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);

        Intent intent = getIntent();
        Log.d("T", "DetailedInfoActivity got intent: " + intent);
        Bundle extras = intent.getExtras();
        name = extras.getString("NAME");
        Log.d("T", "DetailedInfoActivity got name: " + name);
        //NO NEED XXX//savedPosZip = extras.getString("SAVED_ZIP_OR_POS");

        // XXX Dummy:  Test data
        dumComs = new ArrayList<String>();
        dumComs.add("Committee of superordination");
        dumComs.add("Committee of small mammals");
        dumComs.add("Committee of whateverIdunno");
        dumBills = new ArrayList<String>();
        dumBills.add("Bill to prevent flossing");
        dumBills.add("1985 cross-chronological homestead act");
        // XXX DUMMY: This is test
        // XXXX retreieve current reperesentative
        // currRep
        // XXXX DUMMY
        //party = "Democrat";
        dummyPopulateData();

        // Populate Data
        // BUG
        listCommittees = currentRepresentative.getCommittees();
        listBills = currentRepresentative.getBills();
        // Populate sub-views
        populateListViews();

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
    }

    private void dummyPopulateData() {
        Log.d("T", "DetailedInfoActivity dummyPopulateData() procced: ");
        if (name.equals("Mr. Samuels")) {
            currentRepresentative = new Representative(R.drawable.andrew_jackson, "Mr. Samuels", "Independent", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1856", (ArrayList) dumComs,
                    (ArrayList) dumBills);
        }
        if (name.equals("Horace Mumphis")) {
            currentRepresentative = new Representative(R.drawable.dianne_feinstein, "Horace Mumphis", "Republican", "IMail@woosh.com",
                    "www.hello.me", "tweettwitter", "1/22/2071", (ArrayList) dumComs,
                    (ArrayList) dumBills);
        }
        if (name.equals("Jasmine Clarice") || name.equals("Jasmine")) {
            currentRepresentative = new Representative(R.drawable.barbara_boxer, "Jasmine Clarice", "Democrat", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1986", (ArrayList) dumComs,
                    (ArrayList) dumBills);
        }
        if (name.equals("Marvin Coolander")) {
            currentRepresentative = new Representative(R.drawable.teddy_roosevelt, "Marvin Coolander", "Democrat", "IHaveYourMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1992", (ArrayList) dumComs,
                    (ArrayList) dumBills);
        }
    }

    private void populateListViews() {
        //Photo
        ImageView imageViewPhoto = (ImageView) findViewById(R.id.details_imageview_photo);
        imageViewPhoto.setImageResource(currentRepresentative.getPhoto());
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
        commList.setAdapter(commAdapter);

        ArrayAdapter<String> billAdapter = new ArrayAdapter<String>(DetailedInfoActivity.this,
                android.R.layout.simple_list_item_1,android.R.id.text1, listBills);
        ListView billList = (ListView) findViewById(R.id.details_listview_bills);
        billList.setAdapter(billAdapter);
    }
    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
    // Swiping Coding based off http://stackoverflow.com/questions/937313/fling-gesture-detection-on-grid-layout
}
