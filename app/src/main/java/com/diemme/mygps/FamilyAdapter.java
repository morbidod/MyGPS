package com.diemme.mygps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XP011224 on 11/01/2018.
 */

public class FamilyAdapter extends ArrayAdapter<FamilyUser> {
    private List<FamilyUser> listFamily = new ArrayList<>();
    private FirebaseStorage storage;

    public FamilyAdapter(Context context, int resource, List<FamilyUser> listFamily) {
        super(context, resource, listFamily);
        this.listFamily=listFamily;
        storage = FirebaseStorage.getInstance();

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.list_item_family_user,parent,false);
        }
        FamilyUser fu = listFamily.get(position);
        Log.d("FamilyAdapter DBG","name:"+fu.getUserName()+" url:"+fu.getPhotoURL());
        StorageReference userPhotoReference = storage.getReferenceFromUrl(fu.getPhotoURL());

        ImageView icon = (ImageView) convertView.findViewById(R.id.userImageView);
        Glide.with(getContext())
               // .load("https://firebasestorage.googleapis.com/v0/b/myfirstfirebase-6b5d8.appspot.com/o/chat_photos%2Fimage%3A31511?alt=media&token=14b5da13-a3d8-4657-aa59-783a1cce73a3")
                .load(fu.getPhotoURL())
                .apply(RequestOptions.circleCropTransform())
               // .transform(new CircleTransform(getContext()))
                .into(icon);
        //get the icon
        //icon.setImageDrawable(userDrawable);

        TextView username = (TextView) convertView.findViewById(R.id.userNameTextView);
        username.setText(fu.getUserName());

        return convertView;
    }


}
