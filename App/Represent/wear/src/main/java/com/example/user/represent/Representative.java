package com.example.user.represent;

import java.util.ArrayList;

/**
 * Created by User on 2/24/2016.  Stores all information about a representative.
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
    // Other fields
    private String bioguide_id;
    private String twitter_id;
    private String state;
    // private String state;

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
        this.bioguide_id = null;
        this.twitter_id = null;
        this.state = null;
    }

    public Representative() {
        this.photo = R.drawable.teddy_roosevelt;
        this.name = null;
        this.party = null;
        this.url_email = null;
        this.url_website = null;
        this.twitter = "TWEET TWEET TWEET TWEET";
        this.date = null;
        this.committees = new ArrayList<String>();
        this.bills = new ArrayList<String>();
        this.bioguide_id = null;
        this.twitter_id = null;
        this.state = null;
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

    public String getBioguide_id() {
        return bioguide_id;
    }

    public String getTwitter_id() {
        return twitter_id;
    }

    public String getState() {
        return state;
    }

    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
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
        this.date = date;
    }

    public void addCommittee(String committee) {
        this.committees.add(committee);
    }

    public void setCommittees(ArrayList<String> committees) {
        this.committees = committees;
    }

    public void setBills(ArrayList<String> bills) { this.bills = bills; }

    public void addBill(String bill) {
        this.bills.add(bill);
    }

    public void setBioguide_id(String bioguide_id) {
        this.bioguide_id = bioguide_id;
    }

    public void setTwitter_id(String twitter_id) {
        this.twitter_id = twitter_id;
    }

    public void setState(String state) {
        this.state = state;
    }
}
