package com.mad.customer;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.Adapters.RecyclerAdapterOrdered;
import com.mad.mylibrary.OrderItem;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */

class ViewHolderOrder extends RecyclerView.ViewHolder{
    private TextView name, addr, cell, time;
    private ImageView img;
    private int position;

    public ViewHolderOrder(View itemView){
        super(itemView);

        name = itemView.findViewById(R.id.listview_name);
        addr = itemView.findViewById(R.id.listview_address);
        cell = itemView.findViewById(R.id.listview_cellphone);
        img = itemView.findViewById(R.id.profile_image);
        time = itemView.findViewById(R.id.textView_time);
    }

    void setData(OrderItem current, int position){
        this.name.setText(current.getName());
        this.addr.setText(current.getAddrRestaurant());
        this.cell.setText(current.getCell());
        this.time.setText(current.getTime());
        if(current.getImg() != null) {
            Glide.with(itemView.getContext()).load(current.getImg()).into(img);
        }
        this.position = position;
    }
}

public class Order extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<OrderItem, ViewHolderOrder> mAdapter;

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapterOrdered mAdapter_ordered;
    private RecyclerView recyclerView_ordered;
    private static FirebaseRecyclerOptions<OrderItem> options =
            new FirebaseRecyclerOptions.Builder<OrderItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference("reservation"),
                            OrderItem.class).build();

    private Order.OnFragmentInteractionListener mListener;

    public Order() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerView = view.findViewById(R.id.ordered_list);
        mAdapter = new FirebaseRecyclerAdapter<OrderItem, ViewHolderOrder>(options) {
            @NonNull
            @Override
            public ViewHolderOrder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.order_item, viewGroup, false);

                view.findViewById(R.id.open_reservation).setOnClickListener(k -> {
                    String id = ((TextView)view.findViewById(R.id.listview_name)).getText().toString();
                    viewOrder(id);
                });

                return new ViewHolderOrder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolderOrder holder, int position, @NonNull OrderItem model) {
                holder.setData(model, position);
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    public void viewOrder(String id){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.order_list_dialog, null);
        final OrderItem[] i = {new OrderItem()};
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query;

        query = database.getReference().child("reservation").orderByChild("name").equalTo(id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
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