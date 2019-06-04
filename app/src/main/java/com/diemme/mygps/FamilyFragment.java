package com.diemme.mygps;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

class FamilyFragment extends Fragment {
    private static final String LOG_TAG="FamilyFragment DEBUG";
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfileStorageReference;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFamilyDbRef, mUserDbReference;
    private List<String> listFamilyUid;
    private List<FamilyUser> listFamilyUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_family, container, false);
        Log.d(LOG_TAG, "onCreateView");
        // Snippet from "Navigate to the next Fragment" section goes here.
        setUpToolbar(view);
        //android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) view.findViewById(R.id.familytoolbar);
        //toolbar.setTitle("Family List");
        listFamilyUid=new ArrayList<>();
        listFamilyUser=new ArrayList<>();

        final ListView familyLV=(ListView) view.findViewById(R.id.familyListView);
        final FamilyAdapter mFamilyAdapter = new FamilyAdapter(container.getContext(),R.layout.list_item_family_user,listFamilyUser);
        Log.d(LOG_TAG,"ListView:"+familyLV.toString());
        Log.d(LOG_TAG,"Family Adapter:"+mFamilyAdapter.toString());
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

     return view;

    } // End onCreateView

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.familytoolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
        toolbar.setTitle("Family List");
        toolbar.setNavigationIcon(R.drawable.ic_back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Click",Toast.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            }
        });
    }
}
