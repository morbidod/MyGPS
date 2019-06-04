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
import android.widget.SeekBar;
import android.widget.TextView;
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

class RadiusFragment extends Fragment {
    private static final String LOG_TAG="RadiusFragment DEBUG";
    private int min=1, max=1000, current=70;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radius, container, false);
        Log.d(LOG_TAG, "onCreateView");
        // Snippet from "Navigate to the next Fragment" section goes here.
        setUpToolbar(view);
        final TextView radiusTextView=(TextView) view.findViewById(R.id.textViewRadius);
        radiusTextView.setText("" + current);
        SeekBar radiusSeekbar=(SeekBar) view.findViewById(R.id.radiusSeekBar);
        //radiusSeekbar.setMin(min);
        radiusSeekbar.setMax(max);
        radiusSeekbar.setProgress(current);
        radiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                current=progress;
                radiusTextView.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

     return view;

    } // End onCreateView

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.radiustoolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
        toolbar.setTitle("Search Radius");
        toolbar.setNavigationIcon(R.drawable.ic_back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Back",Toast.LENGTH_LONG).show();
                Bundle bundle = new Bundle();
                bundle.putInt("radius",current);

                getFragmentManager().popBackStack();
            }
        });
    }
}
