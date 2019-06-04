package com.diemme.mygps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClusteringDemoActivity extends BaseDemoActivity {
    private static final String LOG_TAG = "ClusteringDemoActivity DEBUG";
    private ClusterManager<MyItem> mClusterManager;
    private ItemRenderer mItemRenderer;
    private PersonRenderer mPersonRenderer;
    private Resources resources;
    private MyItem fake1, fake2, fake3, fake4, fake5, fake6, fake7, fake8, fake9, fake10;

    private class PersonRenderer extends DefaultClusterRenderer<MyItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);
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
            int resourceID = resources.getIdentifier(person.getTitle().toLowerCase(), "drawable", getPackageName());
            mImageView.setImageResource(resourceID);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MyItem p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                int resourceID = resources.getIdentifier(p.getTitle().toLowerCase(), "drawable", getPackageName());
                Drawable drawable = getDrawable(resourceID);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

    }



    @Override
    protected void startDemo() {
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
        Long ts=System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YY hh:mm:ss");
        String dateString =sdf.format(new Date(ts));
        Toast.makeText(getApplicationContext(),ts.toString()+dateString,Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG,"Timestamp:"+ts+" "+dateString);
        //getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.503186, 12.126446), 10));

        mClusterManager = new ClusterManager<MyItem>(this, getMap());
        /*
        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
        */
        //addItems();
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
        mClusterManager.cluster();

        Log.d(LOG_TAG, "Add item completed, ClusterManager:" + mClusterManager.toString());
        Log.d(LOG_TAG, "ClusterManager:" + mClusterManager.getAlgorithm().getItems().size());
        mItemRenderer = new ItemRenderer(getApplicationContext(), getMap(), mClusterManager);
        mPersonRenderer = new PersonRenderer();
        //mClusterManager.setRenderer(mItemRenderer);
        mClusterManager.setRenderer(mPersonRenderer);
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        LatLng ll=new LatLng(Double.parseDouble("42.36"), Double.parseDouble("12.43"));
        LatLng ll2=new LatLng(Double.parseDouble("42.46"), Double.parseDouble("12.53"));
        LatLng ll3=new LatLng(Double.parseDouble("42.56"), Double.parseDouble("12.63"));
        int resourceID = resources.getIdentifier("genericprofile", "drawable", getPackageName());

        //try to draw a fancy marker
        Bitmap sourcebitmap=BitmapFactory.decodeResource(resources,resourceID);
       Bitmap sourceRoundedBitmap=getCircularBitmap(sourcebitmap);
       Bitmap destBorderBitmap=addBorderToCircularBitmap(sourceRoundedBitmap,8,Color.GREEN);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(160,160,conf);
        //Bitmap bmp = getMarkerBitmapFromView(resourceID);
        Paint color = new Paint();
        color.setColor(Color.GREEN);
        color.setTextSize((float) 20.0);
        Canvas mCanvas = new Canvas(bmp);
        mCanvas.drawBitmap(bmp,0,0,color);
        mCanvas.drawBitmap(BitmapFactory.decodeResource(resources,resourceID),0,0,color);
        mCanvas.drawText("Ciao!",30,30,color);
        Bitmap srcBitmap=getCircularBitmap(getMarkerBitmapFromView(resourceID));
        Bitmap roundedBitmap = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        RoundedBitmapDrawable roundedDrawable= RoundedBitmapDrawableFactory.create(resources,roundedBitmap);
        Bitmap mroundedBitmap=roundedDrawable.getBitmap();
        MarkerOptions mo=new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(bmp));
       // MarkerOptions mo2=new MarkerOptions().position(ll2).icon(BitmapDescriptorFactory.fromBitmap(addBorderToBitmap(srcBitmap,4,Color.MAGENTA)));
        MarkerOptions mo3=new MarkerOptions().position(ll3).icon(BitmapDescriptorFactory.fromBitmap(destBorderBitmap));
       // MarkerOptions mo=new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(mCanvas.));
        getMap().addMarker(mo);
       // getMap().addMarker(mo2);
        getMap().addMarker(mo3);

    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        List<MyItem> items = new MyItemReader().read(inputStream);
        items.add(fake1);
        items.add(fake2);
        items.add(fake3);
        items.add(fake4);
        items.add(fake5);
        items.add(fake6);
        items.add(fake7);
        items.add(fake8);
        //print list items

        mClusterManager.addItems(items);
        Log.d("ClusteringDemo", "items size:" + items.size());
    }

    private void addItems() {
        List<MyItem> listItem = new ArrayList<>();
        listItem.add(fake1);
        listItem.add(fake2);
        listItem.add(fake3);
        listItem.add(fake4);
        listItem.add(fake5);
        listItem.add(fake6);
        listItem.add(fake7);
        listItem.add(fake8);
        mClusterManager.addItems(listItem);
    }

    private void print_items() {

    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.multi_profile, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.image);
       // Log.d(LOG_TAG,"getMarkerFromView resID:"+resId);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
    protected Bitmap getCircularBitmap(Bitmap srcBitmap) {
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());

        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas to draw circular bitmap
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */
        // Initialize a new Rect instance
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);

        /*
            RectF
                RectF holds four float coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be
                accessed directly. Use width() and height() to retrieve the rectangle's width and
                height. Note: most methods do not check to see that the coordinates are sorted
                correctly (i.e. left <= right and top <= bottom).
        */
        // Initialize a new RectF instance
        RectF rectF = new RectF(rect);

        /*
            public void drawOval (RectF oval, Paint paint)
                Draw the specified oval using the specified paint. The oval will be filled or
                framed based on the Style in the paint.

            Parameters
                oval : The rectangle bounds of the oval to be drawn

        */
        // Draw an oval shape on Canvas
        canvas.drawOval(rectF, paint);

        /*
            public Xfermode setXfermode (Xfermode xfermode)
                Set or clear the xfermode object.
                Pass null to clear any previous xfermode. As a convenience, the parameter passed
                is also returned.

            Parameters
                xfermode : May be null. The xfermode to be installed in the paint
            Returns
                xfermode
        */
        /*
            public PorterDuffXfermode (PorterDuff.Mode mode)
                Create an xfermode that uses the specified porter-duff mode.

            Parameters
                mode : The porter-duff mode that is applied

        */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth-srcBitmap.getWidth())/2;
        float top = (squareBitmapWidth-srcBitmap.getHeight())/2;

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the

                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */
        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(srcBitmap, left, top, paint);

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
    }

    private Bitmap addBorderToCircularBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
        Bitmap destBitmap=Bitmap.createBitmap(srcBitmap.getWidth()+borderWidth*2,
                                                srcBitmap.getHeight()+borderWidth*2,
                                                       Bitmap.Config.ARGB_8888);
        int dstBitmapWidth = srcBitmap.getWidth()+borderWidth*2;
        Canvas canvas = new Canvas(destBitmap);
        canvas.drawBitmap(srcBitmap,0,0,null);
        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);


        canvas.drawCircle(srcBitmap.getWidth() / 2 , srcBitmap.getHeight() / 2 , srcBitmap.getWidth() / 2  - borderWidth / 2, paint);
       // paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //canvas.drawBitmap(srcBitmap,rect,rect,paint);
        return destBitmap;
    }

}
