package com.diemme.mygps;

/**
 * Created by XP011224 on 06/01/2018.
 */

public class FamilyMember {
    private String uid;
    private Boolean isFamily;

    public FamilyMember(){

    };

    public FamilyMember(String uid, Boolean isFamily){
        this.uid=uid;
        this.isFamily=isFamily;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getFamily() {
        return isFamily;
    }

    public void setFamily(Boolean family) {
        isFamily = family;
    }
}
