package com.mad.appetit;

import static com.mad.mylibrary.SharedClass.*;
import com.mad.mylibrary.OrderItem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Reservation.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */

class ViewHolderReservation extends RecyclerView.ViewHolder{
    private TextView name, addr, cell, time, price;
    private int position;

    public ViewHolderReservation(View itemView){
        super(itemView);

        name = itemView.findViewById(R.id.listview_name);
        addr = itemView.findViewById(R.id.listview_address);
        cell = itemView.findViewById(R.id.listview_cellphone);
        time = itemView.findViewById(R.id.textView_time);
        price = itemView.findViewById(R.id.listview_price);
    }

    void setData(OrderItem current, int position){
        this.name.setText(current.getName());
        this.addr.setText(current.getAddrCustomer());
        this.cell.setText(current.getCell());
        this.time.setText(current.getTime());
        this.price.setText(current.getTotPrice());
        this.position = position;
    }
}

public class Reservation extends Fragment {
    private RecyclerView recyclerView,recyclerView_accepted;
    private FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation> mAdapter;
    private FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation> mAdapter_accepted;
    private RecyclerAdapterOrdered mAdapter_ordered;
    private RecyclerView.LayoutManager layoutManager;

    private static FirebaseRecyclerOptions<OrderItem> options =
            new FirebaseRecyclerOptions.Builder<OrderItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                            + "/" + RESERVATION_PATH),
                            OrderItem.class).build();

    private static FirebaseRecyclerOptions<OrderItem> options2 =
            new FirebaseRecyclerOptions.Builder<OrderItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                                    + "/" + ACCEPTED_ORDER_PATH),
                            OrderItem.class).build();

    private Reservation.OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView_ordered;

    public Reservation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        recyclerView = view.findViewById(R.id.ordered_list);
        mAdapter = new FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation>(options) {
            @NonNull
            @Override
            public ViewHolderReservation onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.reservation_listview, viewGroup, false);

                view.findViewById(R.id.confirm_reservation).setOnClickListener(e -> {
                    String id = ((TextView)view.findViewById(R.id.listview_name)).getText().toString();

                    Intent mapsIntent = new Intent(getContext(), MapsActivity.class);
                    mapsIntent.putExtra(ORDER_ID, id);
                    startActivity(mapsIntent);
                });

                view.findViewById(R.id.delete_reservation).setOnClickListener(h -> {
                    String id = ((TextView)view.findViewById(R.id.listview_name)).getText().toString();
                    removeOrder(id);
                });

                view.findViewById(R.id.open_reservation).setOnClickListener(k -> {
                    String id = ((TextView)view.findViewById(R.id.listview_name)).getText().toString();
                    viewOrder(id, false);
                });

                return new ViewHolderReservation(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolderReservation holder, int position, @NonNull OrderItem model) {
                holder.setData(model, position);
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView_accepted = view.findViewById(R.id.reservation_list_accepted);
        mAdapter_accepted = new FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation>(options2) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderReservation holder, int position, @NonNull OrderItem model) {
                holder.setData(model, position);
            }

            @NonNull
            @Override
            public ViewHolderReservation onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.reservation_listview, parent, false);

                view.findViewById(R.id.confirm_reservation).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.delete_reservation).setVisibility(View.INVISIBLE);

                view.findViewById(R.id.open_reservation).setOnClickListener(k -> {
                    String id = ((TextView)view.findViewById(R.id.listview_name)).getText().toString();
                    viewOrder(id, true);
                });

                return new ViewHolderReservation(view);
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView_accepted.setAdapter(mAdapter_accepted);
        recyclerView_accepted.setLayoutManager(layoutManager);

        return view;
    }

    public void removeOrder(String id){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            Query queryDel = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                    + "/" + RESERVATION_PATH).orderByChild("name").equalTo(id);

            queryDel.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot d : dataSnapshot.getChildren())
                            d.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("RESERVATION", "Failed to read value.", error.toException());
                }
            });

            mAdapter.notifyDataSetChanged();

            reservationDialog.dismiss();
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(e -> reservationDialog.dismiss());

        reservationDialog.setView(view);
        reservationDialog.setTitle("Delete Reservation?");

        reservationDialog.show();
    }

    public void viewOrder(String id, boolean order){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.dishes_list_dialog, null);
        final OrderItem[] i = {new OrderItem()};
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query;

        if(!order)
            query = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                    + "/" + RESERVATION_PATH).orderByChild("name").equalTo(id);
        else
            query = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                    + "/" + ACCEPTED_ORDER_PATH).orderByChild("name").equalTo(id);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        i[0] = d.getValue(OrderItem.class);
                    }

                    recyclerView_ordered = view.findViewById(R.id.ordered_list);
                    mAdapter_ordered = new RecyclerAdapterOrdered(reservationDialog.getContext(), i[0].getOrder());
                    layoutManager = new LinearLayoutManager(reservationDialog.getContext());
                    recyclerView_ordered.setAdapter(mAdapter_ordered);
                    recyclerView_ordered.setLayoutManager(layoutManager);

                    view.findViewById(R.id.back).setOnClickListener(e -> reservationDialog.dismiss());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("RESERVATION", "Failed to read value.", error.toException());
            }
        });

        reservationDialog.setView(view);
        reservationDialog.setTitle("Order");

        reservationDialog.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
        mAdapter_accepted.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
        mAdapter_accepted.stopListening();
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
        void onFragmentInteraction(Uri uri);
    }
}