package com.mad.appetit;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.mad.mylibrary.Restaurateur;

import static com.mad.mylibrary.SharedClass.Address;
import static com.mad.mylibrary.SharedClass.Description;
import static com.mad.mylibrary.SharedClass.Mail;
import static com.mad.mylibrary.SharedClass.Name;
import static com.mad.mylibrary.SharedClass.Phone;
import static com.mad.mylibrary.SharedClass.Photo;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.Time;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Profile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */

public class Profile extends Fragment {
    private String name, addr, descr, mail, phone, photoUri, time;
    private OnFragmentInteractionListener mListener;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef.child(RESTAURATEUR_INFO + "/" + ROOT_UID);

        view.findViewById(R.id.logout).setOnClickListener(e -> {
            FirebaseAuth.getInstance().signOut();

            Intent mainActivity = new Intent(getContext(), MainActivity.class);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Restaurateur restaurateur = new Restaurateur();
                    InputStream inputStream = null;

                    for(DataSnapshot d : dataSnapshot.getChildren()) {
                        if(d.getKey().equals("info")){
                            restaurateur = d.getValue(Restaurateur.class);
                            break;
                        }
                    }

                    name = restaurateur.getName();
                    addr = restaurateur.getAddr();
                    descr = restaurateur.getCuisine();
                    mail = restaurateur.getMail();
                    phone = restaurateur.getPhone();
                    photoUri = restaurateur.getPhotoUri();
                    time = restaurateur.getOpeningTime();

                    ((TextView)view.findViewById(R.id.name)).setText(name);
                    ((TextView)view.findViewById(R.id.address)).setText(addr);
                    ((TextView)view.findViewById(R.id.description)).setText(descr);
                    ((TextView)view.findViewById(R.id.mail)).setText(mail);
                    ((TextView)view.findViewById(R.id.phone2)).setText(phone);
                    ((TextView)view.findViewById(R.id.time_text)).setText(time);

                    if(photoUri != null) {
                        Glide.with(Objects.requireNonNull(view.getContext()))
                                .load(photoUri)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .into((ImageView) view.findViewById(R.id.profile_image));
                    }
                    else {
                        Glide.with(Objects.requireNonNull(view.getContext()))
                                .load(R.drawable.restaurant_home)
                                .into((ImageView) view.findViewById(R.id.profile_image));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("DAILY OFFER", "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.add:
                Intent i = new Intent(getContext(), EditProfile.class);

                i.putExtra(Name, name);
                i.putExtra(Description, descr);
                i.putExtra(Address, addr);
                i.putExtra(Mail, mail);
                i.putExtra(Phone, phone);
                i.putExtra(Photo, photoUri);
                i.putExtra(Time, time);

                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
