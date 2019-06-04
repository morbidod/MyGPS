package com.diemme.mygps;

import android.util.Log;


/**
 * Created by XP011224 on 18/12/2017.
 */

public class UserDB {
    private String email,pwd,name,phone,photoURL;
    private long lastSeen;

    // Costructors
    public UserDB(){
    }



    public UserDB(String email, String pwd, String name, String phone, String photoURL, long lastSeen){
        this.email=email;
        this.pwd=pwd;
        this.name=name;
        this.phone=phone;
        this.photoURL=photoURL;
        this.lastSeen=lastSeen;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPhotoURL() {
        return photoURL;
    }
    public long getLastSeen(){return lastSeen;}

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
    public void printUser(){
        Log.d("UserDB","name:"+this.name+ " email:"+this.email + " phone:"+this.phone + " photoURL:"+this.photoURL+ " lastSeen:"+this.lastSeen);
    }

}
