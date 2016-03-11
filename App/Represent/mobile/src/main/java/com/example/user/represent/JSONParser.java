package com.example.user.represent;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 3/7/2016.
 */
public class JSONParser {

    // constructor
    public JSONParser() {

    }

    public JSONObject getJSONFromUrl(String myPost) {

        InputStream inputStream = null;
        String result = null;
        // Perform the JSON get.
        Log.d("T", "In JSON Parser:  URL myPost:  " + myPost);
        try {
            HttpURLConnection urlConnection = null;
            Log.d("T", "In JParse:  new URL(myPost):  ");
            URL url = new URL(myPost);
            Log.d("T", "In JParse:  url.openConnection():  ");
            urlConnection = (HttpURLConnection) url.openConnection();
            Log.d("T", "In JParse:  .connect():  ");
            urlConnection.connect();
            Log.d("T", "In JParse:  .getInputStream:  ");
            inputStream = urlConnection.getInputStream();
            // json is UTF-8 by default
            Log.d("T", "In JParse:  new BuffereredReader(...:  ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
            Log.d("T", "In JParse:  got result:  ");
            return new JSONObject(result);
        } catch (Exception e) {
            Log.e("fail 3", e.toString());
            Log.d("T", "FAILURE IN JSONParser:  Most likely invalid URL");
        } finally {
            // Not sure of purpose of this.
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception squish) {
            }
        }
        Log.d("T", "FAILURE IN JSONParser:  Reached return null");
        return null;
    }
}

