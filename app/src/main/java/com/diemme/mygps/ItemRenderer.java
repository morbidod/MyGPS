package com.diemme.mygps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

public class ItemRenderer extends DefaultClusterRenderer<MyItem> {
    private static final String LOG_TAG="ItemRendered DEBUG";
    private Context context;
    private ClusterManager<MyItem> mClusterManager;
    private IconGenerator mIconGenerator;
    private IconGenerator mClusterIconGenerator;
    private ImageView mImageView;
    private ImageView mClusterImageView;
    private int mDimension;
    private int mPaddingDimension;
    private View multiProfile;

    public ItemRenderer(Context context, GoogleMap map, ClusterManager mClusterManager){
        super(context,map,mClusterManager);
        this.context=context;
        this.mClusterManager=mClusterManager;
        //mDimension= context.getResources().getDimensionPixelSize(R.dimen.custom_profile_image);
        mDimension = (int) context.getResources().getDimension(R.dimen.custom_profile_image);
        mPaddingDimension=context.getResources().getDimensionPixelSize(R.dimen.custom_profile_padding);


        //mDimension=50;
        //mPaddingDimension=2;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        multiProfile=inflater.inflate(R.layout.multi_profile,null);
        mImageView=new ImageView(context);

        //mClusterImageView = new ImageView(context);
        mClusterImageView=(ImageView) multiProfile.findViewById(R.id.image);
        Log.d(LOG_TAG,"mclusterView:"+mClusterImageView.getMeasuredHeight()+","+mClusterImageView.getMeasuredHeight());

        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension,mDimension));
        mImageView.setPadding(mPaddingDimension,mPaddingDimension,mPaddingDimension,mPaddingDimension);
        Log.d(LOG_TAG,"mclusterimageview:"+mClusterImageView.getMeasuredWidth()+","+mClusterImageView.getMeasuredHeight());
        mIconGenerator=new IconGenerator(context);
        mIconGenerator.setContentView(mImageView);
        mClusterIconGenerator=new IconGenerator(context);

    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        String itemTitle=item.getTitle();
        Log.d(LOG_TAG,"title:"+itemTitle);
        Resources resources=context.getResources();
        if (itemTitle!=null) {
            int resourceID = resources.getIdentifier(itemTitle.toLowerCase(), "drawable", context.getPackageName());
            mImageView.setImageResource(resourceID);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getSnippet());
           }
           else {markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).snippet(item.getSnippet());}
        /*
        if (item.getTitle()=="Emma"){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.emma));
        }
        else if (item.getTitle()=="EvaMaria"){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.evamaria));
        }
        else {markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).snippet(item.getSnippet());}
       */
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions){
        mClusterImageView=(ImageView) multiProfile.findViewById(R.id.image);
        Log.d(LOG_TAG,"mclusterView:"+mClusterImageView.getMeasuredHeight()+","+mClusterImageView.getMeasuredHeight());
        mClusterImageView.setImageResource(R.drawable.genericprofile);
        Log.d(LOG_TAG,"OnBeforeClusterRendered ClusterSize:"+cluster.getSize());
        List<Drawable> listPictures=new ArrayList<>(Math.min(4,cluster.getSize()));
        Log.d(LOG_TAG,"listPictures:"+listPictures.size());
        Resources resources=context.getResources();
        Log.d(LOG_TAG,"Resources:"+resources.toString());
        for (MyItem item: cluster.getItems()){
            if (listPictures.size()==4) break;
            Drawable d;
            String itemTitle=item.getTitle();
            if(itemTitle!=null){
                Log.d(LOG_TAG,"Item:"+item.getTitle()+" "+item.getSnippet()+" "+item.getPosition().toString());
                int resourceID = resources.getIdentifier(itemTitle.toLowerCase(), "drawable", context.getPackageName());
                if (resourceID!=0){
                    d =resources.getDrawable(resourceID,null);
                    Log.d(LOG_TAG,"Added a picture to listPicures");
                    listPictures.add(d);
                }

            }
            else {
                Log.d(LOG_TAG,"Item with No Title");
            }

        }
        MultiDrawable multiDrawable=new MultiDrawable(listPictures);

        multiDrawable.setBounds(0,0,mDimension,mDimension);
        Log.d(LOG_TAG,"Size listPictures:"+listPictures.size()+" MultiDrawable:"+multiDrawable.toString());


        mClusterImageView.setImageDrawable(multiDrawable);
        /*
        mClusterImageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mClusterImageView.layout(0, 0, mClusterImageView.getMeasuredWidth(), mClusterImageView.getMeasuredHeight());
        mClusterImageView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(mClusterImageView.getMeasuredWidth(), mClusterImageView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable d = mClusterImageView.getBackground();
        if (d!=null){
            d.draw(canvas);
            mClusterImageView.draw(canvas);
        }
      */

        // Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
       // markerOptions.icon(BitmapDescriptorFactory.);


    }

    private static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}
