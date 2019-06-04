package com.diemme.mygps;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FamilyActivity extends AppCompatActivity {
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfileStorageReference;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFamilyDbRef, mUserDbReference;
    private List<String> listFamilyUid;
    private List<FamilyUser> listFamilyUser;

    private static final String LOG_TAG="FamilyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        listFamilyUid=new ArrayList<>();
        listFamilyUser=new ArrayList<>();

        final ListView familyLV=(ListView) findViewById(R.id.familyListView);
        final FamilyAdapter mFamilyAdapter = new FamilyAdapter(this,R.layout.list_item_family_user,listFamilyUser);
        familyLV.setAdapter(mFamilyAdapter);

        mFirebaseStorage= FirebaseStorage.getInstance();
        mProfileStorageReference=mFirebaseStorage.getReference().child("user_photos");

        mFirebaseDB= FirebaseDatabase.getInstance();
        mFamilyDbRef=mFirebaseDB.getReference().child("family");
        mUserDbReference=mFirebaseDB.getReference().child("users");

        mFamilyDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot familyDataSnaphot:dataSnapshot.getChildren()){
                    final String  uid=familyDataSnaphot.getKey();
                    Log.d(LOG_TAG,"key:"+uid);
                    Log.d(LOG_TAG,"value:"+familyDataSnaphot.getValue().toString());
                    if ((Boolean) familyDataSnaphot.getValue() == true){
                        //it is a family UID - save it
                        listFamilyUid.add(uid);
                        Log.d(LOG_TAG,"ListFamilyUID updated: added "+ uid);
                        mUserDbReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserDB user = (UserDB) dataSnapshot.getValue(UserDB.class);
                                user.printUser();
                                final FamilyUser fu = new FamilyUser(uid,user.getName(),user.getPhotoURL());
                                fu.printDebug();
                                listFamilyUser.add(fu);
                                mFamilyAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                   /* Log.d(LOG_TAG, "Got Family member:" + familyMember.getUid());
                    listFamilyUid.add(familyMember.getUid());
                    */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //setup the adapter
    }
}
