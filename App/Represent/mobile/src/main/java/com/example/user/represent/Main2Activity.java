package com.example.user.represent;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    // The list of data objects that will populate the ListView
    private List<Representative> repList = new ArrayList<Representative>();
    private Button backButton;

    // ZipPos sent from other activity
    // Also is the value passed in as toSaveZipPos
    private String sentZipPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        backButton = (Button) findViewById(R.id.item_button_back);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //XXX String sentZipPos = "Null";
        // XXXX ??? MIGHT WANT TO FEATURE populateRepList as a seperate file?
        // Pull in data based on zipcode or position.
        // May come from data from Main, randomZip from Listener or savedZipPos from Details
        if (extras != null) {
            sentZipPos = extras.getString("ZIPCODE_OR_USEPOS");
            if (sentZipPos.equals("usePos")) {
                // XXXX FILL LATER: Do what needs to be done to gather data by position
                // Arbitrary zipcode for DUMMY function
                populateRepList(true, 23456);
            } else {
                // FILL LATER  Do what needs to be done to gather data by Zipcode
                // Pull and use the zipcode
                int zipcode = Integer.parseInt(sentZipPos);
                populateRepList(false, zipcode);
            }
        }
        // XXX ??? HOW TO GET IN VALUES?  Sent from Intents, mostly. Saving values.
        // Retrieve/receive UseCurrentPosition and zipcode
        //Create data to populate ListView
        ////populateRepList(true, 23456);

        // Fill in each sub-view with data from the data
        // semi-DUMMY function.  May change based on how pulled data is represented.
        populateListView();

        // XXX Custom Method for allowing an item to be clicked
        //  XXX Not used here
        //XXX registerClickCallback();

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

    }


    private void populateRepList(Boolean useCurrentPos, int zipcode) {
        // XXX Dummy:  Test data
        List<String> dumComs = new ArrayList<String>();
        dumComs.add("Committee of superordination");
        dumComs.add("Committee of small mammals");
        dumComs.add("Committee of whateverIdunno");
        List<String> dumBills = new ArrayList<String>();
        dumComs.add("Bill to prevent flossing");
        dumComs.add("1985 cross-chronological homestead act");
        if (useCurrentPos) {
            // DO ONE THING FOR POSITION
            // XXX DUMMY: This is test
            repList.add(new Representative(R.drawable.andrew_jackson, "Mr. Samuels", "Independent", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1856", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.dianne_feinstein, "Horace Mumphis", "Republican", "IMail@woosh.com",
                    "www.hello.me", "tweettwitter", "1/22/2071", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.barbara_boxer, "Jasmine Clarice", "Democrat", "IHaveMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet", "1/22/1986", (ArrayList) dumComs,
                    (ArrayList) dumBills));
            repList.add(new Representative(R.drawable.teddy_roosevelt, "Marvin Coolander", "Democrat", "IHaveYourMail@mail.com",
                    "www.Iam.me", "tweettweet tweettweet tweettweet tweettweet tweettweet " +
                    "tweettweet tweettweet tweettweet tweettweet tweettweet tweettweet tweettweet",
                    "1/22/1992", (ArrayList) dumComs,
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
                repList.add(new Representative(R.drawable.barbara_boxer, "Jasmine Clarice", "Democrat", "IHaveMail@mail.com",
                        "www.Iam.me", "tweettweet tweettweet", "1/22/1986", (ArrayList) dumComs,
                        (ArrayList) dumBills));
                repList.add(new Representative(R.drawable.teddy_roosevelt, "Marvin Coolander", "Democrat", "IHaveYourMail@mail.com",
                        "www.Iam.me", "tweettweet tweettweet", "1/22/1992", (ArrayList) dumComs,
                        (ArrayList) dumBills));
            }
        }
    }

    private void populateListView() {
        ArrayAdapter<Representative> thisAdapater = new MyListAdapter();
        ListView repListView = (ListView) findViewById(R.id.listView);
        repListView.setAdapter(thisAdapater);
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
            //Photo
            ImageView imageViewPhoto = (ImageView) itemView.findViewById(R.id.item_imageview_photo);
            imageViewPhoto.setImageResource(currentRepresentative.getPhoto());
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
                    // Allows to reconstruct Main2
                    // XXX ??? but what if call in from watch?  Handled by listener???
                    // XXX No need// intent.putExtra("TOSAVE_ZIPPOS", sentZipPos);
                    Log.d("T", "Main2 about to start watch DetailedInfoActivity with NAME: " + name);
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
            urlEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //XXX FILLING
                    // Have currentRepresetative
                    Representative cRep = (Representative) v.getTag();
                }
            });

            //URL Website
            TextView urlWebsite = (TextView) itemView.findViewById(R.id.item_url_website);
            urlWebsite.setText(currentRepresentative.getUrl_website());
            urlWebsite.setTag(currentRepresentative);
            urlWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //XXX FILLING
                    // Have currentRepresetative
                    Representative cRep = (Representative) v.getTag();
                }
            });

            //Twitter
            TextView textviewTwitter = (TextView) itemView.findViewById(R.id.item_textview_twitter);
            textviewTwitter.setText(currentRepresentative.getTwitter());

            return itemView;
            //return super.getView(position, convertView, parent);
        }
    }
    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
}
