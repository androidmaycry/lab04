package com.mad.riders;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.mad.lib.SharedClass.RESERVATION_PATH;
import static com.mad.lib.SharedClass.RIDERS_PATH;

public class NavApp extends AppCompatActivity implements
        Orders.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener,
        Profile.OnFragmentInteractionListener{


    public String UID;
    public boolean value;
    private BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item ->  {

        switch (item.getItemId()) {
            case R.id.navigation_home:
                checkBadge();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();

                return true;

            case R.id.navigation_profile:
                Bundle bundle = new Bundle();
                bundle.putString("UID",UID);
                Profile profile = new Profile();
                profile.setArguments(bundle);
                checkBadge();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profile).commit();
                return true;
            case R.id.navigation_reservation:
                Bundle bundle2 = new Bundle();
                bundle2.putString("UID",UID);
                if(value)
                    bundle2.putString("STATUS","true");
                else
                    bundle2.putString("STATUS","false");
                Orders orders = new Orders();
                orders.setArguments(bundle2);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, orders).commit();
                hideBadgeView();
                return true;
        }
        return false;
    };
    private View notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent i  = getIntent();
        UID = i.getStringExtra("UID");

        value = true;
        /* TODO: DEBUG */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference status = database.getReference(RIDERS_PATH+"/"+UID+"/available/");

        status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                value = (boolean)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkBadge();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Home()).commit();
            addBadgeView();
            hideBadgeView();
        }


        Log.d("UID",UID);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkBadge(){
        Query query = FirebaseDatabase.getInstance().getReference(RIDERS_PATH+"/"+UID+"/pending/");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long count = dataSnapshot.getChildrenCount();

                    ((TextView)notificationBadge.findViewById(R.id.count_badge)).setText(Long.toString(count));
                    refreshBadgeView();
                }
                else{
                    hideBadgeView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addBadgeView() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(2);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.notification_badge, menuView, false);

        itemView.addView(notificationBadge);
    }

    private void refreshBadgeView() {
        notificationBadge.setVisibility(VISIBLE);
    }

    private void hideBadgeView(){
        notificationBadge.setVisibility(INVISIBLE);
    }
}
