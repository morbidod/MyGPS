package com.diemme.mygps;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.diemme.mygps.MapsActivity.MY_PERMISSIONS_REQUEST_LOCATION;

//import com.bumptech.glide.request.animation.GlideAnimation;

public class ClusterMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
       public final static String LOG_TAG="MapsDEBUG";

       private final static String TAG="MapActivity DEBUG";
    //private final static
    private ClusterManager<MyItem> mClusterManager;
    private MyItem fake1, fake2, fake3;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fake1=new MyItem(Double.parseDouble("42.16"),Double.parseDouble("12.23"),"Pippo","snippet1");
        fake2=new MyItem(Double.parseDouble("42.17"),Double.parseDouble("12.24"),"Pluto","snippet2");
        fake3=new MyItem(Double.parseDouble("42.18"),Double.parseDouble("12.25"),"Paperino","snippet3");

        getSupportActionBar().setTitle("ClusterMap");
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");

    }




    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
       // mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        //initialize ClusterMamager
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.1503186, 12.126446), 10));
        mClusterManager=new ClusterManager<MyItem>(this,mGoogleMap);
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        Log.d(LOG_TAG,"fake1 again:"+fake1.getPosition().toString());
        mClusterManager.addItem(fake1);
        mClusterManager.addItem(fake2);
        mClusterManager.addItem(fake3);
        mClusterManager.setAnimation(true);
        //mClusterManager.cluster();
    }

    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: ");

    }
    @Override
    public void onLocationChanged(Location location)
    {
        Log.d("OnLocationChanged",location.getLatitude()+" / "+ location.getLongitude()+" acc:"+location.getAccuracy());
    }
    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection Failed: +"+connectionResult.toString());
    }

    private boolean checkPermission() {
        boolean permissionGranted=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                //Location Permission already granted
                permissionGranted=true;
            } else {
                //Request Location Permission
                permissionGranted=false;
            }
            return permissionGranted;
        }
        else {
            return true;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION );
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

                        Log.d(TAG,"Permission have been granted!");
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

}
