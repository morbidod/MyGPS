package com.diemme.mygps;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


class UserFragment extends Fragment {
    private static final String LOG_TAG="UserFragement DEBUG";

    private static final int RC_PHOTO_PICKER =9001;
    private static final int PLACE_PICKER_REQUEST = 9002;
    private static final int THUMBSIZE=128;

    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFamilyDbRef, mUserDbReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfileStorageReference;
    private FirebaseUser currentUser;

    private ProgressBar spinner;

    private BitmapFactory.Options bmOptions;
    private Bitmap profilebitmap;
    private ImageView userImage;
    private Uri newPictureUri=null;
    private Uri profileUri=null;
    private List<String> listFamilyUid;

    private GeoFire mGeoFire;
    private DatabaseReference mGeoDbReference;
    private GeoQuery mGeoQuery;

    private ProgressBar myspinner;
    private List<String> listFamilyUID;
    private List<FamilyUser> listFamilyUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(LOG_TAG,"onCreate");

        listFamilyUid=new ArrayList<>();
        listFamilyUser=new ArrayList<>();

        mFirebaseStorage=FirebaseStorage.getInstance();
        mProfileStorageReference=mFirebaseStorage.getReference().child("user_photos");
        mFirebaseDB=FirebaseDatabase.getInstance();


        mFamilyDbRef=mFirebaseDB.getReference().child("family");
        mUserDbReference=mFirebaseDB.getReference().child("users");

        mFamilyDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot familyDataSnaphot:dataSnapshot.getChildren()){
                    final String uid=familyDataSnaphot.getKey();
                    Log.d(LOG_TAG,"key:"+uid);
                    Log.d(LOG_TAG,"value:"+familyDataSnaphot.getValue().toString());
                    if ((Boolean) familyDataSnaphot.getValue() == true){
                        //it is a family UID - save it
                        listFamilyUid.add(uid);
                        Log.d(LOG_TAG,"ListFamilyUID updated: added "+ uid);
                        //retreive UserDB from DB
                        mUserDbReference.child(familyDataSnaphot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserDB user= (UserDB) dataSnapshot.getValue(UserDB.class);
                                user.printUser();
                                final FamilyUser fu = new FamilyUser(uid,user.getName(),user.getPhotoURL(),user.getLastSeen());
                                fu.printDebug();
                                listFamilyUser.add(fu);
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
        currentUser =FirebaseAuth.getInstance().getCurrentUser();

    } //End OnCreate


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_user_welcome, container, false);
        Log.d(LOG_TAG, "onCreateView");
        // Snippet from "Navigate to the next Fragment" section goes here.
        setUpToolbar(view);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("FamilyMap");


        TextView userTV= (TextView) view.findViewById(R.id.userTextView);


        userImage = (ImageView) view.findViewById(R.id.imageView);
        if (currentUser!=null){
            Log.d(LOG_TAG,"Current User:"+currentUser.getUid()+currentUser.getDisplayName()+currentUser.getEmail());
            if (TextUtils.isEmpty(currentUser.getDisplayName())){
                Log.d(LOG_TAG,"DisplayName is null, showDialog");
                showDialogNameInsert();
            }
            userTV.setText(currentUser.getDisplayName());
            updateProfileImage();
        }

        Button changeProfileButton=(Button) view.findViewById(R.id.btnChangeProfileImage);
        Button mapButton = (Button) view.findViewById(R.id.map_button);
        changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete action using"),RC_PHOTO_PICKER);
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),MapsActivity.class);
                i.putExtra("listFamily", (Serializable) listFamilyUser);
                startActivity(i);
            }
        });

        return view;
    } // ********************************** END onCreateView ***************************************************



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.familygroup:
                showFamily();
                return true;
            case R.id.logout:
                Toast.makeText(this.getContext(),"Logging out",Toast.LENGTH_SHORT).show();

                AuthUI.getInstance().signOut(getActivity()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(getActivity(),MainActivity.class);
                        startActivity(i);
                    }
                });
                return true;
            case R.id.radius:
                Toast.makeText(this.getContext(),"Radius",Toast.LENGTH_SHORT).show();
                showRadiusFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRadiusFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container,new RadiusFragment())
                .addToBackStack(null)
                .commit();
        // Intent familyIntent = new Intent(this.getContext(),FamilyActivity.class);
        // startActivity(familyIntent);
        return;
    }

    private void showFamily(){
        //let's try with fragment 30/05

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container,new FamilyFragment())
                .addToBackStack("familytrans")
                .commit();
       // Intent familyIntent = new Intent(this.getContext(),FamilyActivity.class);
       // startActivity(familyIntent);
        return;
    }
    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuinflater) {
        //inflater.inflate(R.menu.user_menu, menu);
        menuinflater.inflate(R.menu.user_menu,menu);
        Log.d(LOG_TAG,"OnCreateOptionsMenu called");
        super.onCreateOptionsMenu(menu, menuinflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            InputStream is = null;
            try {
                is = getActivity().getContentResolver().openInputStream(selectedImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeStream(is);
            int bmpratio;
            int new_width;
            int new_height;
            if (bmp.getHeight() > bmp.getWidth()) {
                //portrait
                new_width = THUMBSIZE;
                new_height = THUMBSIZE * (bmp.getHeight() / bmp.getWidth());
            } else {
                new_height = THUMBSIZE;
                new_width = THUMBSIZE * (bmp.getWidth() / bmp.getHeight());
            }

            //String imagepath=getThumbnailPath(selectedImageUri);
            //File file = new File(imagepath);
            StorageReference photoRef = mProfileStorageReference.child(currentUser.getUid());
            //new jan 29th
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp = Bitmap.createScaledBitmap(bmp, new_width, new_height, false);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] dataimage = baos.toByteArray();
            photoRef.putBytes(dataimage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    newPictureUri = taskSnapshot.getDownloadUrl();
                    //update users - photoURL
                    //mDatabase.child("users").child(userId).child("username").setValue(name);
                    Log.d(LOG_TAG, "Updating users db " + currentUser.getUid() + " with photoURL:" + newPictureUri.toString());
                    mUserDbReference.child(currentUser.getUid()).child("photoURL").setValue(newPictureUri.toString());
                    updateProfileImage();
                    //Log.d(LOG_TAG,"Upload OK - pictureURL:"+newPictureUri.toString());
                    UserProfileChangeRequest userChangeRequest = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(newPictureUri).build();
                    currentUser.updateProfile(userChangeRequest)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("UpdateProfile", "OnCompleteListener");
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("UpdateProfile", "OnSuccessListener");
                                }
                            }).
                            addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("UpdateProfile", "OnFailureListener" + e.getMessage());
                                }
                            }); //end currentUser.updateProfile
                } //end OnSuccess
            });//end putbyte onSuccess listener
        }
    }

    private void showUser() {
        if (currentUser != null) {
            // Name, email address, and profile photo Url
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = currentUser.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = currentUser.getUid();
            Log.d(LOG_TAG,"name:"+name+" email:"+email+" photoUrl:"+photoUrl.toString()+ " uid:"+uid);
        }
    }

    private void updateProfileImage() {

        if (currentUser.getPhotoUrl()!=null){  Log.d(LOG_TAG,"Getting ProfileImage:"+currentUser.getPhotoUrl().toString());}
        mProfileStorageReference.child(currentUser.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(LOG_TAG,"OnSuccessListener for UID profile");
                        profileUri=uri;
                        Log.d(LOG_TAG,"profile uri:"+profileUri.toString());
                      /*
                        Glide.with(userImage.getContext())
                                .load(profileUri)
                                //.load(currentUser.getPhotoUrl())
                                .transform(new CircleTransform(userImage.getContext()))
                                .into(userImage);
                                */
                        Glide.with(userImage.getContext())
                                .load(profileUri)
                                //.load(currentUser.getPhotoUrl())
                                .apply(RequestOptions.circleCropTransform())
                                // .transform(new CircleTransform(userImage.getContext()))
                                .into(userImage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOG_TAG,"No UserDB image profile found");
                        mProfileStorageReference.child("genericprofile.jpg").getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(userImage.getContext())
                                                .load(uri)
                                                //.load(currentUser.getPhotoUrl())
                                                // .transform(new CircleTransform(userImage.getContext()))
                                                .apply(RequestOptions.circleCropTransform())
                                                .into(userImage);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOG_TAG,"Fail to load genericprofile:"+e.getMessage());
                                        userImage.setImageResource(R.drawable.genericprofile);
                                    }
                                });

                    }
                });
       /*
        profilebitmap = LoadImage(currentUser.getPhotoUrl().getPath(), bmOptions);

        if (profilebitmap !=null){
            userImage.setImageBitmap(profilebitmap);
        }
        else {
            userImage.setImageResource(R.drawable.genericprofile);
        }
       */
        if (profileUri!=null) {
            Log.d(LOG_TAG,"ProfileUri:"+profileUri.toString());
        }
        else {Log.d(LOG_TAG,"ProfileUri is null");}

    /*    Glide.with(userImage.getContext())
              .load(profileUri)
             //.load(currentUser.getPhotoUrl())
             .transform(new CircleTransform(this))
             .into(userImage);
     */
    }

    private void showDialogNameInsert() {
        Log.d(LOG_TAG,"ShowDialogNameInsert");
        final Context context=getActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("INSERT NAME");
        dialog.setMessage("Please enter your Name");

        LayoutInflater inflater = LayoutInflater.from(context);
        //View name_layout = inflater.inflate(R.layout.name_layout, null);
        View name_layout = inflater.inflate(R.layout.name_layout,null);


        final MaterialEditText editName = (MaterialEditText) name_layout.findViewById(R.id.editName);

        dialog.setView(name_layout);

        dialog.setPositiveButton("INSERT Name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editName.addValidator(new LengthValidator("Name too short",4));

                if (editName.validate()) {

                    Log.d(LOG_TAG,"Name inserted is valid");
                    Toast.makeText(context,"Thanks",Toast.LENGTH_SHORT).show();
                    //update Profile DisplayName
                    UserProfileChangeRequest userChangeRequest=new UserProfileChangeRequest.Builder()
                                                                   .setDisplayName(editName.getText().toString())
                                                                   .build();
                    currentUser.updateProfile(userChangeRequest)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(LOG_TAG, "User profile updated.");
                                    }
                                }
                            });
                    dialog.dismiss();

                }
                else { //name provided is  null
                    Log.d(LOG_TAG,"Name inserted is null");
                    Toast.makeText(context, "Name cannot be null", Toast.LENGTH_LONG).show();
                    editName.setError("Null");
                    }
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean isNameValid(String input){
        return !TextUtils.isEmpty(input);
    }

}
