package com.diemme.mygps;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;

//import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDBReferenceUsers;

    private Button btnSignIn, btnRegister, btnTest;
    private boolean user_is_authenticated;
    RelativeLayout rootLayout;

    private String LOG_TAG="LOG DEBUG";

    private int RC_SIGN_IN=7001;
    private final static int MY_PERMISSIONS_REQUEST_LOCATION=7002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        user_is_authenticated=false;
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mDBReferenceUsers=mFirebaseDatabase.getReference("users");

        mFirebaseAuth=FirebaseAuth.getInstance();

        if(checkPermission()){
            Log.d(LOG_TAG,"{Permission Granted");
        }
        else{
            Log.d(LOG_TAG,"Permission not GRANTED!");
            checkLocationPermission();
        }

       btnTest=(Button) findViewById(R.id.btn_test) ;
       btnTest.setEnabled(true);
        
        btnSignIn=(Button) findViewById(R.id.btn_sign_in);

        btnRegister=(Button) findViewById(R.id.btn_register);
        btnTest=(Button) findViewById(R.id.btn_test);
        enable_disable_buttons();

        if (user_is_authenticated){ btnSignIn.setText("SIGN OUT");}

       btnTest.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(LOG_TAG,"Test!!!!");
              // Intent i = new Intent(getApplicationContext(),ClusteringDemoActivity.class);
               Intent i = new Intent(getApplicationContext(),ClusteringDemoActivity.class);
               startActivity(i);
           }
       });
        
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRegister();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignIn= AuthUI.getInstance().createSignInIntentBuilder().
                        setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
                        )
                        .setIsSmartLockEnabled(false)
                        .build();
                //AuthUI.getInstance().createSignInIntentBuilder().
                //setProviders(AuthUI.EMAIL_PROVIDER,AuthUI.GOOGLE_PROVIDER).build();
                startActivityForResult(intentSignIn,RC_SIGN_IN);

            }
        });



        rootLayout=(RelativeLayout) findViewById(R.id.activity_main);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null){
                    Log.d(LOG_TAG,"UserDB is null!");
                    //need to sign in
                    Intent intentSignIn= AuthUI.getInstance().createSignInIntentBuilder()
                            .setTheme(R.style.AppTheme)
                            .setAvailableProviders(
                                    Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
                            )
                            .setIsSmartLockEnabled(true)
                            .build();
                            //AuthUI.getInstance().createSignInIntentBuilder().
                            //setProviders(AuthUI.EMAIL_PROVIDER,AuthUI.GOOGLE_PROVIDER).build();
                    startActivityForResult(intentSignIn,RC_SIGN_IN);
                }
                else {
                    //user is authenticated
                    Log.d(LOG_TAG,"UserDB is not null Name:"+user.getDisplayName()+" Email:"+user.getEmail());

                    Toast.makeText(getApplicationContext(),"Welcome "+user.getDisplayName(),Toast.LENGTH_SHORT).show();
                    //Intent i = new Intent(getApplicationContext(),UserWelcomeActivity.class);
                    Intent i = new Intent(getApplicationContext(),WelcomeActivity.class);
                    i.putExtra("UserDB",user.getDisplayName());
                    startActivity(i);

                }
            }
        };


    }

    private void enable_disable_buttons() {
        if (user_is_authenticated){
            btnSignIn.setEnabled(false);

            btnRegister.setEnabled(false);
        }
        else {
            //user is not authenticated
            btnSignIn.setEnabled(true);
            btnRegister.setEnabled(true);
        }
    }

    @Override
    public void onResume(){
        Log.d(LOG_TAG,"OnResume");
        super.onResume();
        enable_disable_buttons();
        //mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    public void onPause(){
        Log.d(LOG_TAG,"onPause");
        super.onPause();
        //mFirebaseAuth.removeAuthStateListener(mAuthStateListener);


        //detachDatabaseListener();
    }

    @Override
    public void onStart(){
        Log.d(LOG_TAG,"onStart");
        super.onStart();

    }

    @Override
    public void onStop(){
        Log.d(LOG_TAG,"onStop");
        super.onStop();

    }

    @Override
    public void onDestroy(){
        Log.d(LOG_TAG,"onDestroy");
        super.onDestroy();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"onActivityResult:"+requestCode+":"+resultCode);
        if (requestCode == RC_SIGN_IN) {
            // it was a SIGN IN activity
            if (resultCode == RESULT_OK) {

                Toast.makeText(getApplicationContext(), "Sign In success:" +data.getDataString(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(),WelcomeActivity.class);
                //i.putExtra("UserDB",user.getDisplayName());
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Sign In was not successfull", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        Log.d(LOG_TAG, "Permission have been granted!");
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showDialogRegister() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.register_layout,null);

        final MaterialEditText editEmail = (MaterialEditText) register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPwd = (MaterialEditText) register_layout.findViewById(R.id.editPwd);
        final MaterialEditText editName = (MaterialEditText) register_layout.findViewById(R.id.editName);
        final MaterialEditText editPhone = (MaterialEditText) register_layout.findViewById(R.id.editPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    //email is null
                    Snackbar.make(rootLayout,"Please enter email",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPwd.getText().toString())){
                    //email is null
                    Snackbar.make(rootLayout,"Please enter pwd",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(editName.getText().toString())){
                    //email is null
                    Snackbar.make(rootLayout,"Please enter Name",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(editPhone.getText().toString())){
                    //email is null
                    Snackbar.make(rootLayout,"Please enter phone",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                mFirebaseAuth.createUserWithEmailAndPassword(editEmail.getText().toString(),editPwd.getText().toString()).
                        addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        UserDB user = new UserDB();
                        FirebaseUser fUser= authResult.getUser();
                        user.setEmail(editEmail.getText().toString());
                        user.setPwd(editPwd.getText().toString());
                        user.setName(editName.getText().toString());
                        user.setPhone(editPhone.getText().toString());
                        Log.d(LOG_TAG,"UserDB ready to be pushed into db:"+ user.getName() +" "+ user.getEmail()+" "+ user.getPhone());
                        Log.d(LOG_TAG,"fUser:"+fUser.getDisplayName()+ " " + fUser.getEmail());
                        //push it to the db
                        mDBReferenceUsers.push().setValue(user).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout,"Register Success",Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed to Register:"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                                Log.d(LOG_TAG,"Failed to Register:"+e.getMessage());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed to Create UserDB:"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });

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

    private boolean checkPermission() {
        Log.d(LOG_TAG,"CheckPermissions");
        boolean permissionGranted=false;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(LOG_TAG,"Build:"+Build.VERSION.SDK_INT);
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                Log.d(LOG_TAG,"Permission ACCESS_FINE_LOCATION granted on Manifest");
                //Location Permission already granted
                permissionGranted=true;
            } else {
                //Request Location Permission
                Log.d(LOG_TAG,"Permission ACCESS_FINE_LOCATION NOT granted on Manifest");
                permissionGranted=false;
            }
            return permissionGranted;
        }
        else {
            // for oltder than M version of Android, just return true
            return true;
        }
    }

    private void checkLocationPermission() {
        Log.d(LOG_TAG,"checkLocationPermission");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            Log.d(LOG_TAG,"Permission in Manifest not GRANTED!");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION );
        }

    }
}