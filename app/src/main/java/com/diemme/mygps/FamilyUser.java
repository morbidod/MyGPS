package com.diemme.mygps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

/**
 * Created by XP011224 on 11/01/2018.
 */

public class FamilyUser implements Serializable, ClusterItem {
    private String uid;
    private String userName;
    private String photoURL;
    private LatLng mPosition=null;
    private long mlastSeen=0;

    public FamilyUser(String uid, String name, String photoURL,LatLng position, long lastSeen){
        this.uid=uid;
        this.userName=name;
        this.photoURL=photoURL;
        mPosition=position;
        mlastSeen=lastSeen;
    }

    public FamilyUser(String uid,String name, String photoURL,long lastSeen){
        this.uid=uid;
        this.userName=name;
        this.photoURL=photoURL;
        mlastSeen=lastSeen;
    }

    public FamilyUser(String uid,String name, String photoURL){
        this.uid=uid;
        this.userName=name;
        this.photoURL =photoURL;
    }

    public String getUserName() {
        return userName;
    }

    public String getUid(){ return uid;}

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public LatLng getPosition(){ return mPosition;}

    public long getLastSeen(){return mlastSeen;}

    @Override
    public String getTitle() {
        return userName;
    }

    @Override
    public String getSnippet() {
        return photoURL;
    }


    public void printDebug(){
        Log.d("FamilyUser DBG","uid:"+this.uid +" " +this.userName +" "+this.photoURL+" lastSeen:"+this.mlastSeen);
        if (mPosition!=null){ Log.d("FamilyUser DBG",mPosition.latitude+" "+mPosition.longitude);
        }
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = this.photoURL;
    }

}
