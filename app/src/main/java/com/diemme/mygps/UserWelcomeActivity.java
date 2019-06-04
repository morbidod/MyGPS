package com.diemme.mygps;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.util.Log;
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
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserWelcomeActivity extends AppCompatActivity {

    private static final String LOG_TAG="UserWelcomeActivity";
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
    private ViewGroup fragmentViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


        setContentView(R.layout.activity_user_welcome);
       // android.support.v7.widget.Toolbar toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        android.support.v7.widget.Toolbar toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);


        toolbar.setTitle("FamilyMap");
        setSupportActionBar(toolbar);

        TextView userTV= (TextView) findViewById(R.id.userTextView);
        userImage = (ImageView) findViewById(R.id.imageView);

        currentUser =FirebaseAuth.getInstance().getCurrentUser();

        showUser();
        if (currentUser!=null){
            userTV.setText(currentUser.getDisplayName());
            updateProfileImage();
        }


        Button mapButton = (Button) findViewById(R.id.map_button);
        Button changeProfileButton = (Button) findViewById(R.id.btnChangeProfileImage) ;

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
                Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                i.putExtra("listFamily", (Serializable) listFamilyUser);
                startActivity(i);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.familygroup:
                showFamily();
                return true;
            case R.id.logout:
                Toast.makeText(getApplicationContext(),"Log Out",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.radius:
                Toast.makeText(getApplicationContext(),"Radius",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
           // Log.d(LOG_TAG,"name:"+name+" email:"+email+" photoUrl:"+photoUrl.toString()+ " uid:"+uid);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "OnResume");
        //updateProfileImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK){
           Uri selectedImageUri=data.getData();
            InputStream is=null;
            try {
                is = getContentResolver().openInputStream(selectedImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeStream(is);
            int bmpratio;
            int new_width;
            int new_height;
            if (bmp.getHeight()>bmp.getWidth()){
                //portrait
                new_width=THUMBSIZE;
                new_height=THUMBSIZE*(bmp.getHeight()/bmp.getWidth());
            }
            else{
                new_height=THUMBSIZE;
                new_width=THUMBSIZE*(bmp.getWidth()/bmp.getHeight());
            }

            //String imagepath=getThumbnailPath(selectedImageUri);
            //File file = new File(imagepath);
                       StorageReference photoRef = mProfileStorageReference.child(currentUser.getUid());
           //new jan 29th
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp=Bitmap.createScaledBitmap(bmp,new_width,new_height,false);
            bmp.compress(Bitmap.CompressFormat.PNG,100,baos);
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



           //old putFile
           /*
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    newPictureUri=taskSnapshot.getDownloadUrl();
                    //update users - photoURL
                    //mDatabase.child("users").child(userId).child("username").setValue(name);
                    Log.d(LOG_TAG,"Updating users db "+currentUser.getUid()+" with photoURL:"+newPictureUri.toString());
                    mUserDbReference.child(currentUser.getUid()).child("photoURL").setValue(newPictureUri.toString());

                    //Log.d(LOG_TAG,"Upload OK - pictureURL:"+newPictureUri.toString());
                    UserProfileChangeRequest userChangeRequest = new UserProfileChangeRequest.Builder().
                            setPhotoUri(newPictureUri).build();
                    currentUser.updateProfile(userChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("UpdateProfile","OnCompleteListener");

                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("UpdateProfile","OnSuccessListener");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("UpdateProfile","OnFailureListener");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(LOG_TAG,"Failed to upload:"+e.getMessage());
                }
            });
           */
        }
    }


    public String getThumbnailPath(Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA };
        String result="";
        // This method was deprecated in API level 11
        // Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(getContentResolver(),uri, MediaStore.Images.Thumbnails.MICRO_KIND,null);
        if( cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();//**EDIT**
            result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        }
        return result;
    }

    public Bitmap getThumbnailBitmap(Uri uri){
        String[] proj = { MediaStore.Images.Media._ID };

        // This method was deprecated in API level 11
        // Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(getContentResolver(),uri, MediaStore.Images.Thumbnails.MICRO_KIND,null);
        Bitmap bitmap=null;
        long imageId=0;
        //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._data);
       if (cursor!=null & cursor.getCount()>0){
           cursor.moveToFirst();
           int columnindex=cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
           imageId=cursor.getLong(columnindex);
       }


        //cursor.close();

        if (imageId>-1){
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    getContentResolver(), imageId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    (BitmapFactory.Options) null );
        }

        return bitmap;
    }

    public String[] getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID };
        Cursor cursor = getContentResolver().query(contentUri,
                proj, null, null, null);
        int path_index = cursor.getColumnIndexOrThrow(proj[0]);
        int id_index = cursor.getColumnIndexOrThrow(proj[1]);
        cursor.moveToFirst();
        return new String[] { cursor.getString(path_index),
                cursor.getLong(id_index) + "" };
    }
    private void showFamily(){

        Intent familyIntent = new Intent(getApplicationContext(),FamilyActivity.class);
        startActivity(familyIntent);
        return;
    }



}

