package com.diemme.mygps;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    MyItem fake1,fake2,fake3;
    List<MyItem> itemList;
    private final static String LOG_TAG="TestActivity DEBUG";
    private int mDimension;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_profile);
        ImageView iv=(ImageView) findViewById(R.id.image);

        mDimension= (int) getApplicationContext().getResources().getDimension(R.dimen.custom_profile_image);
        fake1=new MyItem(Double.parseDouble("42.116"),Double.parseDouble("12.23"),"John","snippet1");
        fake2=new MyItem(Double.parseDouble("42.117"),Double.parseDouble("12.234"),"Stefan","snippet2");
        fake3=new MyItem(Double.parseDouble("42.118"),Double.parseDouble("12.2395"),"Yeats","snippet3");
        itemList=new ArrayList<>();
        itemList.add(fake1);
        itemList.add(fake2);
        itemList.add(fake3);
        Resources resources=getApplicationContext().getResources();
        List<Drawable> listPictures=new ArrayList<>(Math.min(4,itemList.size()));
        for(MyItem item: itemList) {
            if (listPictures.size() == 4) break;
            Drawable d;
            String itemTitle = item.getTitle();
            if (itemTitle != null) {
                Log.d(LOG_TAG, "Item:" + item.getTitle() + " " + item.getSnippet() + " " + item.getPosition().toString());
                int resourceID = resources.getIdentifier(itemTitle.toLowerCase(), "drawable", getApplicationContext().getPackageName());
                if (resourceID != 0) {
                    d = resources.getDrawable(resourceID, null);
                    Log.d(LOG_TAG, "Added a picture to listPicures");
                    listPictures.add(d);
                }
            }
        }
        MultiDrawable multiDrawable=new MultiDrawable(listPictures);
        //multiDrawable.setBounds(0,0,mDimension,mDimension);
        Log.d(LOG_TAG,"multiD:"+multiDrawable.toString()+" Bounds:"+multiDrawable.getBounds()+" "+multiDrawable.getIntrinsicHeight());
        //iv.setImageResource(R.drawable.genericprofile);
        iv.setImageDrawable(multiDrawable);

    }
}
