package com.diemme.mygps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class FamilyItem implements ClusterItem {
    private final LatLng mPosition;

    private String uid;

    private String mPhotoURL;
    private final String mTitle;
    private final String mSnippet;
    private long mlastSeen;

    public FamilyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mTitle="Null";
        mSnippet="Null";
        mlastSeen=0;
    }

    public FamilyItem(String uid, double lat, double lng, String title, String snippet) {
        this.uid=uid;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mlastSeen=0;
    }

    public FamilyItem(String uid, double lat, double lng, String title, String snippet, long lastSeen) {
        this.uid=uid;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mlastSeen=lastSeen;
    }

    public FamilyItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    public String getUid() {
        return uid;
    }

    public long getLastSeen(){
        return mlastSeen;
    }

    public String getmPhotoURL() {
        return mPhotoURL;
    }

    public void setmPhotoURL(String photoURL){
        mPhotoURL=photoURL;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
