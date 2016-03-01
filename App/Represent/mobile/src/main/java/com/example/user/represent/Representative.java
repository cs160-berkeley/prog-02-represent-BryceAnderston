package com.example.user.represent;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by User on 2/24/2016.
 */
public class Representative {
    // implements Parcelable
    // For use by watch and mobile list and mobile details
    private int photo;
    private String name;
    private String party;
    // For use by mobile list
    private String url_email;
    private String url_website;
    private String twitter;
    // For use by mobile details (and the listViews thereof)
    private String date;
    private ArrayList<String> committees;
    private ArrayList<String> bills;

    // Create new constructors that take in fewer arguments?
    public Representative(int photo, String name, String party, String url_email,
                          String url_website, String twitter, String date,
                          ArrayList<String> committees, ArrayList<String> bills) {
        this.photo = photo;
        this.name = name;
        this.party = party;
        this.url_email = url_email;
        this.url_website = url_website;
        this.twitter = twitter;
        this.date = date;
        this.committees = committees;
        this.bills = bills;
    }

    /**  Maybe not... but keep parceable in mind.
    //Parceable Functions
    @ Override
    public int describeContents() {
        return 0; // hashCode might be better, but no needed
    }

    @ Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(photo);
        dest.writeString(name);
        dest.writeArray(committees);
    }

    // We reconstruct the object reading from the Parcel data
    public Representative(Parcel p) {
        name = p.readString();
        surname = p.readString();
        email = p.readString();
    }
    */

    // All Getters
    public int getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public String getUrl_email() {
        return url_email;
    }

    public String getUrl_website() {
        return url_website;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<String> getCommittees() {
        return committees;
    }

    public ArrayList<String> getBills() {
        return bills;
    }

    // All Setters.  May be useful
    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public void setUrl_email(String url_email) {
        this.url_email = url_email;
    }

    public void setUrl_website(String url_website) {
        this.url_website = url_website;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public void setDate(String date) {
        this.twitter = date;
    }

    public void setCommittees(ArrayList<String> committees) {
        this.committees = committees;
    }

    public void setBills(ArrayList<String> bills) {
        this.bills = bills;
    }
}
