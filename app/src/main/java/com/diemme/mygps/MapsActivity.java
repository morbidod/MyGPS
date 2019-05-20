package com.diemme.mygps;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
//import com.bumptech.glide.request.animation.GlideAnimation;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private FusedLocationProviderApi mFusedLocationProvider;

    private GeoFire mGeoFire;
    private DatabaseReference mGeoDbReference;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFamilyDBRef, mUserDBRef;
    private FirebaseUser currentUser;

    private List<String> listFamilyUid=new ArrayList<>();

    private GeoQuery mGeoQuery;
    private GeoQueryEventListener mGeoListener;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfileStorageReference;

    public final static String LOG_TAG="MapsDEBUG";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int DEFAULT_RADIUS=100;
    private static final LatLng DEFFAULT_LATLNG=new LatLng(42.2217927, 12.1798146);
    private final static int INTERVAL=40000;
    private final static int FASTEST_INTERVAL=20000;
    private final static int DELTA_TIME=60000; //mininum update time
    private final static String TAG="MapActivity DEBUG";
    //private final static
    private final static int DISTANCEMIN=10;
    private static Boolean isFamilyShown=false;
    private Bitmap userBitmap;
    private List<Marker> familyMarkers;
    private LatLngBounds.Builder builder;
    private LatLngBounds bounds;
    private Location mLocation;
    private static final float ZOOM_DEFAULT=10.0f;
    private float zoom=13.0f;
    private float maxdistance=0f;
    private List<FamilyUser> mListFamily;
    private List<FamilyItem> mListFamilyItem;
    private ClusterManager<MyItem> mClusterManager;
    private PersonRenderer mPersonRenderer;
    private MyItem fake1, fake2, fake3, fake4, fake5, fake6, fake7, fake8, fake9, fake10;
    private long timestampLastUpdate=0l;
    private String currentUserID;

    private HashMap<String,Bitmap> mHashMap=new HashMap<>();
    private HashMap<String,String> mUserNameHash=new HashMap<>();
    private HashMap<String,Long> mLastSeenHash=new HashMap<>();
    private HashMap<String,Marker> mMarkerHash = new HashMap<>();

    private class PersonRenderer extends DefaultClusterRenderer<MyItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;
        private Resources resources;

        public PersonRenderer() {
            super(getApplicationContext(), mGoogleMap, mClusterManager);
            resources = getApplicationContext().getResources();
            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            Log.d(LOG_TAG,"onBeforeClusterItem Called for person title:"+person.getTitle()+ " key:"+person.getKey()+ " spnippet:"+person.getSnippet());
            int resourceID = resources.getIdentifier(person.getTitle().toLowerCase(), "drawable", getPackageName());
            mImageView.setImageResource(resourceID);
            Bitmap icon = mIconGenerator.makeIcon();
            //added May 12
            // ************************************************** 12 May ***************************************
            String key=person.getKey();
            LatLng latlong = new LatLng(person.getPosition().latitude,person.getPosition().longitude);
            if (mHashMap.get(key)!=null){
                Log.d(LOG_TAG,"bitmap for key:"+key + " is not null - Showing custom icon");
            /*    markerOptions = new MarkerOptions()
                        .position(latlong)
                        .icon(BitmapDescriptorFactory.fromBitmap(mHashMap.get(key)))
                        .title(mUserNameHash.get(key))
                        .snippet(mLastSeenHash.get(key).toString())
                        .anchor(0.5f,0.5f);
            */

            markerOptions
                    .position(latlong)
                    .icon(BitmapDescriptorFactory.fromBitmap(mHashMap.get(key)))
                    .title(mUserNameHash.get(key))

               // May16 replace mLastSeen with timedisplay     .snippet(mLastSeenHash.get(key).toString())
                    .snippet(getTimeDisplay(mLastSeenHash.get(key).toString()))
                    .anchor(0.5f,0.5f);

            }
            else { //bitmap is null
                Log.d(LOG_TAG, "bitmap for key:" + key + " is null - Showing Generic Green ICON");
                String titleString = "";
                String lastSeenString = "";
                if (mUserNameHash.get(key) != null) {
                    titleString = mUserNameHash.get(key);
                }
                if (mLastSeenHash.get(key) != null) {
                    lastSeenString = mLastSeenHash.get(key).toString();
                }
                markerOptions
                        .position(latlong)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(titleString)
                        .snippet(getTimeDisplay(lastSeenString))
                        .anchor(0.5f, 0.5f);
            }
            // ************************************* end add 12 May *****************************************88
            //let's try a default marker
            //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.getTitle());
            // markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title(person.getTitle());
        }

        @Override
        protected void onClusterItemRendered(MyItem person, Marker marker) {
            super.onClusterItemRendered(person, marker);
            mMarkerHash.put(person.getKey(), marker);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            List<String> keysInCluster=new ArrayList<>();
            Bitmap bitmapIcon;
            String clusterTitle, clusterSnippet;
            clusterTitle="";
            clusterSnippet="";
            for (MyItem p : cluster.getItems()) {
                keysInCluster.add(p.getKey());
                if(clusterTitle.isEmpty()){
                    clusterTitle=p.getTitle();
                    clusterSnippet=getTimeDisplay(p.getSnippet());
                }
                else {
                    clusterTitle+=" & "+p.getTitle();
                    clusterSnippet+=" & "+p.getSnippet();
                }
                clusterSnippet+=" respectively";
            }
            if (keysInCluster.size()==1){
                //only1 in Cluster?
                bitmapIcon=mHashMap.get(keysInCluster.get(0));
            }
            else{
                //if is 2 or more
                Bitmap bm1,bm2;
                bm1=mHashMap.get(keysInCluster.get(0));
                bm2=mHashMap.get(keysInCluster.get(1));
                bitmapIcon=splitMerge(bm1,bm2);
            }
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
   // let's remove it
   /*
            Log.d(LOG_TAG,"OnBeforeClusterRendered Called. Cluster size:"+cluster.getSize());
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MyItem p : cluster.getItems()) {
                // Draw 4 at most.
                int resourceID;
                if (profilePhotos.size() == 4) break;
                Log.d(LOG_TAG,"loop Cluster Items title:"+p.getTitle());
                if (p.getTitle().contains("Emm")){
                    resourceID = resources.getIdentifier("emma", "drawable", getPackageName());
                }
                else if(p.getTitle().contains("Daniele")){
                    resourceID = resources.getIdentifier("pluto", "drawable", getPackageName());
                }
                else{
                    resourceID = resources.getIdentifier(p.getTitle().toLowerCase(), "drawable", getPackageName());
                }

                Drawable drawable = getDrawable(resourceID);
                Log.d(LOG_TAG,"Getting drawable...ResourceID:"+resourceID+" Drawable:"+drawable.toString());
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);
  */
    //        mClusterImageView.setImageDrawable(multiDrawable);
           Drawable d=new BitmapDrawable(getResources(),bitmapIcon);
           mClusterImageView.setImageDrawable(d);
           Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
           markerOptions
                   .icon(BitmapDescriptorFactory.fromBitmap(icon))
                   .title(clusterTitle)
                   .snippet(clusterSnippet)
                   .anchor(0.5f,0.5f)
                   .alpha(0.6f);
           //  markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon)) ;
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

    }
// End PersonRenderer




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFamilyDBRef=mFirebaseDatabase.getReference().child("groups/family/members");
        mUserDBRef=mFirebaseDatabase.getReference().child("users");
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        userBitmap=getUserBitmap(80,80);
        currentUserID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        //FamilyUser list is passed from UserWelcomeActivity

        mListFamily=(List<FamilyUser>) getIntent().getSerializableExtra("listFamily");
        mListFamilyItem=new ArrayList<>();
        int indexFamily=0;
        Log.d(LOG_TAG,"Family List from UserWelcomeActivity");
        //for each FamilyUser retrieve the photoURL and store it to mHashMap with the key of the FamilyUser
        while (indexFamily<mListFamily.size()){
            final FamilyUser mFamilyUser=mListFamily.get(indexFamily++);
            FamilyItem fi;
            LatLng ll;
            if (mFamilyUser.getPosition()!=null){
                ll=mFamilyUser.getPosition();
                fi=new FamilyItem(mFamilyUser.getUid(),ll.latitude,ll.longitude,mFamilyUser.getUserName(),mFamilyUser.getPhotoURL(),mFamilyUser.getLastSeen());
            }
            else{
                fi=new FamilyItem(mFamilyUser.getUid(),0,0,mFamilyUser.getUserName(),mFamilyUser.getPhotoURL(),mFamilyUser.getLastSeen() );
            }
            Log.d(LOG_TAG,"FamilyItem:"+fi.getTitle()+" snippet:"+fi.getSnippet()+" location:"+fi.getPosition()+" LastSeen:"+fi.getLastSeen());
            mUserNameHash.put(mFamilyUser.getUid(),mFamilyUser.getUserName());
            mLastSeenHash.put(mFamilyUser.getUid(),mFamilyUser.getLastSeen());

            mListFamilyItem.add(fi);
            //mFamilyUser.printDebug();
            Glide.with(this)
                  .load(mFamilyUser.getPhotoURL())
                  .apply(RequestOptions.circleCropTransform().override(150,150))
                  .into(new SimpleTarget<Drawable>() {
                      @Override
                      public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                          BitmapDrawable bitmapDrawable=(BitmapDrawable) resource;
                          mHashMap.put(mFamilyUser.getUid(),bitmapDrawable.getBitmap());
                      }
                  });
        }

        fake1 = new MyItem(Double.parseDouble("42.116"), Double.parseDouble("12.23"), "Emma", "snippet1");
        fake2 = new MyItem(Double.parseDouble("42.117"), Double.parseDouble("12.234"), "Pico", "snippet2");
        fake3 = new MyItem(Double.parseDouble("42.118"), Double.parseDouble("12.2395"), "Paperino", "snippet3");
        fake4 = new MyItem(Double.parseDouble("42.128"), Double.parseDouble("12.2395"), "Pico", "Pico de Paperis");
        fake5 = new MyItem(Double.parseDouble("42.138"), Double.parseDouble("12.241"), "Paperino", "snippet3");
        fake6 = new MyItem(Double.parseDouble("42.148"), Double.parseDouble("12.242"), "EvaMaria", "snippet3");
        fake7 = new MyItem(Double.parseDouble("42.158"), Double.parseDouble("12.243"), "Pluto", "snippet3");
        fake8 = new MyItem(Double.parseDouble("42.168"), Double.parseDouble("12.244"), "Paperina", "snippet3");
        fake9 = new MyItem(Double.parseDouble("42.178"), Double.parseDouble("12.245"), "Paperina", "snippet3");
        fake10 = new MyItem(Double.parseDouble("42.188"), Double.parseDouble("12.246"), "Paperina", "snippet3");

/*
        LatLng mlatlng2=new LatLng(Double.parseDouble("42.17"),Double.parseDouble("12.24"));
        fake2=new FamilyUser("yyyy","Pluto","",mlatlng2,0);

        LatLng mlatlng3=new LatLng(Double.parseDouble("42.18"),Double.parseDouble("12.25"));
        fake3=new FamilyUser("zzzz","Paperino","",mlatlng3,0);
*/

/* removed on 2019-0403
        //read family users and store key-bitmap
        mFamilyDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot familyDataSnaphot:dataSnapshot.getChildren()) {
                    final String familyUserUid=familyDataSnaphot.getKey();
                    Log.d(LOG_TAG, "key:" + familyUserUid);
                    Log.d(LOG_TAG, "value:" + familyDataSnaphot.getValue().toString());

                    if (familyUserUid != currentUser.getUid()){
                        //it is not the current user - save it
                        Log.d(LOG_TAG,"Saving user info familylist:"+familyUserUid +" currentUser:"+currentUser.getUid());
                        listFamilyUid.add(familyDataSnaphot.getKey());
                        DatabaseReference userRef = mUserDBRef.child(familyUserUid);
                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    final UserDB familyUser=(UserDB) dataSnapshot.getValue(UserDB.class);
                                    familyUser.printUser();
                                    Glide.with(getApplicationContext())
                                            .load(familyUser.getPhotoURL())
                                            //.asBitmap()
                                            // .override(120,120)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(new SimpleTarget<Drawable>() {
                                                @Override
                                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                    BitmapDrawable bitmapDrawable = (BitmapDrawable) resource;
                                                    mHashMap.put(familyUserUid,bitmapDrawable.getBitmap());
                                                }
                                            });
                                }
                                else {
                                    Log.d(LOG_TAG,"No datasnapshot");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } //end if familyUID is current UID


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/
        getSupportActionBar().setTitle("Where is my Family");
        // Switch to turn off the position
        Switch switchgps= (Switch) findViewById(R.id.switchgps) ;

        switchgps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //
                    startLocationUpdates();
                }
                else {
                    // Not Checked
                    stopLocationUpdate();
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }


                }
            }
        });

        if (!checkPermission()) {
            checkLocationPermission();
        }

        familyMarkers=new ArrayList<>();
        builder = new LatLngBounds.Builder();

        mFusedLocationProvider=LocationServices.FusedLocationApi;

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        //mGeoDbReference= FirebaseDatabase.getInstance().getReference("user_locations");
        mGeoDbReference= FirebaseDatabase.getInstance().getReference("groups/family/locations");
        mGeoFire= new GeoFire(mGeoDbReference);


        mFirebaseStorage=FirebaseStorage.getInstance();
        mProfileStorageReference=mFirebaseStorage.getReference().child("user_photos");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
        else mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());

    }



    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        printFamilyHash();
        stopLocationUpdate();
        stopShowFamilyLocation();
        familyMarkers.clear();



    }

    private void printFamilyHash() {
        Log.d(LOG_TAG,"PrintFamilyHash***********************************");
        for (Map.Entry<String, Bitmap> entry : mHashMap.entrySet()){
            String key = entry.getKey();
            Log.d(LOG_TAG,"key:"+key +" Bitmap:"+entry.getValue().toString());
        }
    }

    private void stopLocationUpdate() {
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                Log.d(TAG,"Stop LocationUpdate");
            }

        }

    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        Log.d(LOG_TAG,"ApiConnected:"+mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            showFamilyLocation();
        }
        else {
            Log.d(LOG_TAG,"...API Connecting");
            mGoogleApiClient.connect();
        }
        printClusterMarker();

    }




    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d(LOG_TAG,"onMapReady");
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mClusterManager = new ClusterManager<MyItem>(this, mGoogleMap);
        // add fake items to the Cluster
        //addItems();
        mPersonRenderer = new PersonRenderer();
        mClusterManager.setRenderer(mPersonRenderer);
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);
        // Added 2 May 2019

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {
                View v= getLayoutInflater().inflate(R.layout.custom_info_layout,null);
                TextView infoTextView=(TextView) v.findViewById(R.id.titleMarkerTextView);
                Log.d(LOG_TAG,"infoWindow Marker:"+marker.getId()+" title:"+marker.getTitle()+" snippet:"+marker.getSnippet());
                String snippetString=marker.getSnippet();
                /* removed May 16
                if (snippetString.contains(";")){
                    //cluster with multiple timestamp
                    String[] snippetList=snippetString.split(";");
                    for (String s:snippetList){
                        snippetString+= getTimeDisplay(s) +" ";
                    }
                }
                else snippetString=getTimeDisplay(marker.getSnippet());
                //String timeDisplay=getTimeDisplay(marker.getSnippet());
                */
                infoTextView.setText(marker.getTitle()+" here "+snippetString+" ago");
                return v;

            }
        });




        Location loc=mFusedLocationProvider.getLastLocation(mGoogleApiClient);
        //initialize ClusterMamager
       // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.1503186, 11.626446), 10));
        if (loc!=null){
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),loc.getLongitude()), 10));
        }
        else {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.1503186, 11.626446), 10));
        }



        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                if (mGoogleApiClient==null) {
                    buildGoogleApiClient();
                    mGoogleApiClient.connect();
                }
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            if (mGoogleApiClient==null) {
                buildGoogleApiClient();
                mGoogleApiClient.connect();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
        showFamilyLocation();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "LocationUpdate started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection Failed: +"+connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d("OnLocationChanged",location.getLatitude()+" / "+ location.getLongitude()+" acc:"+location.getAccuracy());
        mLastLocation=location;

        updateUI(location);

    }

    private void updateUI(final Location location) {

        Log.d(TAG, "Update UI - New Location received");
        String key=currentUserID;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double accuracy = location.getAccuracy();
        Log.d(TAG, "Latitude:" + latitude + " Longitude:" + longitude +" Accuracy:" + accuracy);


        /* Remove updateCurrentLocationMarker
        //update CurrentMarker position

        Log.d(TAG,"Updating Current Marker position");
        if (mCurrLocationMarker != null) {
            //mCurrLocationMarker.remove();
            Log.d(TAG,"currentLocationMarker:"+mCurrLocationMarker.toString())
            mCurrLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        else{
            Log.d(TAG,"*****First time Initialize mCurrentLocation ***********");
            mCurrLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    //   icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)).
                    //.icon(BitmapDescriptorFactory.fromBitmap(userBitmap))
                    .position(new LatLng(latitude, longitude))
                    .anchor(0.5f,0.5f)
                    .snippet("Hello!"+key)
                    .title("This is me"));
        }
        */
        //Calculate time difference since last update;
        timestampLastUpdate=getTimeLastUpdate();
        Long ts=System.currentTimeMillis();
        Long timeDifference=ts-timestampLastUpdate;
        Log.d(LOG_TAG,"Current:"+ts+" Last Update"+timestampLastUpdate+" time Difference:"+timeDifference);
        //Add to cluster
        if(mMarkerHash.get(key)!=null){
            //marker available update the position
            Marker marker=mMarkerHash.get(key);
            marker.setPosition(new LatLng(latitude,longitude));
        }
        else{
            MyItem item = new MyItem(latitude, longitude, mUserNameHash.get(key), String.valueOf(ts),key);
            mClusterManager.addItem(item);
            mClusterManager.cluster();
        }

        //update the lastSeen HashMap
        mLastSeenHash.put(key,ts);
        if (timeDifference>DELTA_TIME){
            Log.d(LOG_TAG,"Time difference >60s...updating DB");

        //upload results to firebase cloud

            mGeoFire.setLocation(key, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Log.d(LOG_TAG, "GeoFire OnComplete Key:"+key );
                        Long ts=System.currentTimeMillis();
                        Log.d(LOG_TAG,"Updating DB for key:"+key+" with value:"+ts);
                        //update lastseen for that user
                        mUserDBRef.child(key).child("lastSeen").setValue(ts).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(LOG_TAG,"Timestamp updated");
                            }
                        });

                        timestampLastUpdate=ts;

                    }
                }
         );
        }
        // mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
        // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)),zoom);
        //the include method will calculate the min and max bound.
        builder.include(new LatLng(location.getLatitude(),location.getLongitude()));
        for (Marker m:mMarkerHash.values()){
            Log.d(LOG_TAG,"Updating builder for marker:"+m.toString());
            builder.include(m.getPosition());
        }

        final LatLngBounds bounds = builder.build();
        final int zoomWidth = getResources().getDisplayMetrics().widthPixels;
        final int zoomHeight = getResources().getDisplayMetrics().heightPixels;
        final int zoomPadding = (int) (zoomWidth * 0.10); // offset from edges of the map 12% of screen
        Log.d(LOG_TAG,"Calculating zoom:"+zoomWidth+";"+zoomHeight+";"+zoomPadding);
        Log.d(LOG_TAG,"Bounds:"+bounds.toString());

        //show MAP
        //
        if(mMarkerHash.values().size()>1) { //Bounds are constructed via builder
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomWidth, zoomHeight, zoomPadding));
        }
        else{
            // no other markers available - show current location with default soom level
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),ZOOM_DEFAULT));
        }


    }

    private void showFamilyLocation(){
        Log.d(LOG_TAG,"showFamilyLocation");
        isFamilyShown=true;
        mGeoQuery= mGeoFire.queryAtLocation(new GeoLocation(DEFFAULT_LATLNG.latitude,DEFFAULT_LATLNG.longitude),DEFAULT_RADIUS);
        mGeoListener = new GeoQueryEventListener() {
            /* Key entered the search area:
               check if marker is already present in the mMarkerHash.
               if is already present - > set the new location
               if not present, create an entry on the HashMap and display the Marker on the Map
             */
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                if (key.contains(currentUserID)) {
                    Log.d(LOG_TAG, "This is the current user, will be handled differently");
                } else {
                    // it is a family
                    Location loc = new Location("familyloc");
                    loc.setLatitude(location.latitude);
                    loc.setLongitude(location.longitude);

                    float distance;
                    if (mLastLocation != null) {
                        distance = loc.distanceTo(mLastLocation);
                        if (distance > maxdistance) {
                            maxdistance = distance;
                            Log.d(LOG_TAG, "New distance:" + maxdistance);
                            setzoomFromDistance(maxdistance);
                        }
                    }

                    // if marker is already available: just move the marker to the new position
                    if (is_marker_present(key)) {
                        mMarkerHash.get(key).setPosition(new LatLng(location.latitude, location.longitude));
                    }

                    // if marker is not available for that key: create a new Marker, add it to the HashMap and display it on the map
                    // added 4 May 2019: and add it to the cluster manager
                    else {
//remove this part
/*
                        MarkerOptions mMarkerOption;



                    if (mHashMap.get(key)!=null){
                        Log.d(LOG_TAG,"bitmap for key:"+key + " is not null - Showing custom icon");
                        LatLng latlong = new LatLng(location.latitude,location.longitude);
                        mMarkerOption = new MarkerOptions()
                                .position(latlong)
                                .icon(BitmapDescriptorFactory.fromBitmap(mHashMap.get(key)))
                                .title(mUserNameHash.get(key))
                                .snippet(mLastSeenHash.get(key).toString())
                                .anchor(0.5f,0.5f);
                        MyItem item=new MyItem(location.latitude,location.longitude,mUserNameHash.get(key),mLastSeenHash.get(key).toString(),key);
                        mClusterManager.addItem(item);
                        mClusterManager.cluster();
                        Log.d(LOG_TAG,"mClusterManager added item Size:"+mClusterManager.getAlgorithm().getItems().size());
                    }
                    else { //bitmap is null
                        Log.d(LOG_TAG,"bitmap for key:"+key + " is null - Showing Generic Green ICON");
                        String titleString="";
                        String lastSeenString="";
                        if(mUserNameHash.get(key)!=null){
                            titleString=mUserNameHash.get(key);
                        }
                        if(mLastSeenHash.get(key)!=null){
                            lastSeenString=mLastSeenHash.get(key).toString();
                        }
                       mMarkerOption=new MarkerOptions()
                               .position(new LatLng(location.latitude,location.longitude))
                               .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                               .title(titleString)
                               .snippet(lastSeenString)
                               .anchor(0.5f,0.5f);

                         MyItem item=new MyItem(location.latitude,location.longitude,mUserNameHash.get(key),mLastSeenHash.get(key).toString());
                        mClusterManager.addItem(item);
                        mClusterManager.cluster();

                    }
                    Marker marker =mGoogleMap.addMarker(mMarkerOption);
                    mMarkerHash.put(key,marker);

                }
      */
                        MyItem item = new MyItem(location.latitude, location.longitude, mUserNameHash.get(key), mLastSeenHash.get(key).toString(),key);
                        mClusterManager.addItem(item);
                        mClusterManager.cluster();

                    }

                }
            } //end onKeyEntered
            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
                //remove Marker from the map
                if (is_marker_present(key)){
                    mMarkerHash.get(key).remove();
                }

                Toast.makeText(getApplicationContext(),mUserNameHash.get(key)+ " has just left the coverage area",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                //update marker position
                mMarkerHash.get(key).setPosition(new LatLng(location.latitude,location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        };
        mGeoQuery.addGeoQueryEventListener(mGeoListener);

    }
    private String getTimeDisplay(String snippet) {
        Long ts=System.currentTimeMillis();
        Log.d(LOG_TAG,"getTimeDisplay snippet:"+snippet+ "ts:"+ts);
        Long timeSnippet=Long.valueOf(snippet);
        Long timeDifference=ts-timeSnippet;
        //Long timeDifferenceSeconds=(Long) timeDifference/1000;
        String returnStringTime="";

       // Duration d = Duration.ofSeconds(timeDifferenceSeconds);
        long days = TimeUnit.DAYS.convert(timeDifference,TimeUnit.MILLISECONDS);
        timeDifference = timeDifference - days*3600*24*1000;
        long hours = TimeUnit.HOURS.convert(timeDifference,TimeUnit.MILLISECONDS);
        timeDifference = timeDifference - hours*3600*1000;
        long mins = TimeUnit.MINUTES.convert(timeDifference,TimeUnit.MILLISECONDS);
        timeDifference = timeDifference - mins*60*1000;
        Log.d(LOG_TAG,"timedifference in seconds:"+timeDifference);
        long seconds = TimeUnit.SECONDS.convert(timeDifference,TimeUnit.MILLISECONDS);

        if (days > 0) {
            returnStringTime+=days + " day(s)";
        }
        if (hours > 0) {
            returnStringTime+=hours + " hour(s)";
        }
        if (mins > 0) {
            returnStringTime+=mins + " min(s)";
        }
        if (seconds > 0) {
            returnStringTime+=seconds + " second(s)";
        }
        Log.d(LOG_TAG,"getTime returning:"+returnStringTime);
        return returnStringTime;
    }

    private boolean is_marker_present(String key){
        if (mMarkerHash.containsKey(key)) {
            return true;
        }
        else{
            return false;
        }
    }

    private void setzoomFromDistance(float distance) {
        if (distance<200){
            zoom=15.0f;
        }
        else if (distance <1000){
            zoom=14.0f;}
        else if (distance<5000){
            zoom=13.0f;
        }
        else if (distance<10000){
            zoom=12.0f;
        }
        else if (distance<20000){
            zoom=11.0f;
        }

        else if (distance<50000){
            zoom=10.0f;
        }
        else if (distance<1000000){
            zoom=9.0f;
        }
        else zoom=5.0f;

    }

    private void stopShowFamilyLocation(){

        Log.d(LOG_TAG,"stopShowFamilyLocation");
        if (isFamilyShown){
            if (mGeoListener!=null){
                if (mGeoQuery!=null){
                    mGeoQuery.removeGeoQueryEventListener(mGeoListener);
                }
                mGeoListener=null;
            }

        }

    }

    private Long getTimeLastUpdate(){
        String key=currentUserID;
        if (mLastSeenHash.containsKey(key)){
            Log.d(LOG_TAG,"getTimeLastUpdate key found:"+mLastSeenHash.get(key));
            return Long.valueOf(mLastSeenHash.get(key));
        }
        else return 0L;

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(LOG_TAG,"Marker Click. title:"+marker.getTitle()+" snippet:"+marker.getSnippet());
        marker.showInfoWindow();
        return true;
    }

    private void addItems(){

        mClusterManager.addItem(fake1);
        mClusterManager.addItem(fake2);
        mClusterManager.addItem(fake3);
        mClusterManager.addItem(fake4);
        mClusterManager.addItem(fake5);
        mClusterManager.addItem(fake6);
        mClusterManager.addItem(fake7);
        mClusterManager.addItem(fake8);
        mClusterManager.addItem(fake9);
        mClusterManager.addItem(fake10);
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

    private Bitmap getUserBitmap(int w, int h){
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bmp= Bitmap.createBitmap(w,h,config);
        Canvas mCanvas = new Canvas(bmp);
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);
        mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.genericprofile),0,0,color);
        mCanvas.drawText("Ciccio",20,20,color);
        return bmp;
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float)(width / 2)
                , (float)(height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    private void printClusterMarker(){
        if(mClusterManager!=null){
            for(Marker m: mClusterManager.getMarkerCollection().getMarkers()){
                Log.d(LOG_TAG,"ClusterManager Debug marker:"+m.toString());
            }
        }
        else{
            Log.d(LOG_TAG,"ClusterManager not yet initialized!");
        }

    }
    private Bitmap splitBitmap_left(Bitmap sourceBitmap){
        Log.d(LOG_TAG,"splitLeft:"+sourceBitmap.getWidth()+sourceBitmap.getHeight());
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth()/2 , sourceBitmap.getHeight());
    }
    private Bitmap splitBitmap_right(Bitmap sourceBitmap){
        Log.d(LOG_TAG,"splitRight:"+sourceBitmap.getWidth()+sourceBitmap.getHeight());
        return Bitmap.createBitmap(sourceBitmap, sourceBitmap.getWidth()/2, 0, sourceBitmap.getWidth()/2 , sourceBitmap.getHeight());
    }

    /*
    function splitMerge takes as input 2 bitmaps and as result return a new bitmap with left part taken from bitmap1 and rtigh part from bitmpa2
     */

    private Bitmap splitMerge(Bitmap bitmap1, Bitmap bitmap2){
        //first bitmap is split in 2 and left part is saved as leftBitmap
        Bitmap leftBitmap=splitBitmap_left(bitmap1);
        //second bitmap is split in 2 and right part is saved as righttBitmap
        Bitmap rightBitmap=splitBitmap_right(bitmap2);
        //we suppose that bitmap1 and bitmap2 have the same size --TO HANDLE if DIFFERENT
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();

        //create a bitmap of a size which can hold the complete image after merging
        Bitmap bitmapResult = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444);

        //create a canvas for drawing all those small images
        Canvas canvas = new Canvas(bitmapResult);
        Paint paint = new Paint();
        paint.setColor(Color.RED); // Text Color
        paint.setTextSize(40); // Text Size


        // Using the canvas draw on the muteable bitmap

        canvas.drawBitmap(leftBitmap, 0, 0, null);
        canvas.drawBitmap(rightBitmap,width/2,0,null);
        canvas.drawText("2", width/2, height/2, paint);
        return bitmapResult;
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
