package com.mad.appetit;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.mylibrary.OrderItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.DoubleStream;

import static com.mad.mylibrary.SharedClass.ACCEPTED_ORDER_PATH;
import static com.mad.mylibrary.SharedClass.ORDER_ID;
import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.RIDERS_ORDER;
import static com.mad.mylibrary.SharedClass.RIDERS_PATH;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

class RiderInfo{
    private String name;
    private Double dist;

    public RiderInfo(String name, Double dist) {
        this.name = name;
        this.dist = dist;
    }

    public String getName() {
        return name;
    }

    public Double getDist() {
        return dist;
    }
}

class ListRiderAdapter extends RecyclerView.Adapter<ListRiderAdapter.MyViewHolder> {
    private ArrayList<RiderInfo> mDataset;
    private LayoutInflater mInflater;
    private ListRiderFragment listRiderFragment;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListRiderAdapter(Context context, ArrayList<RiderInfo> myDataset, ListRiderFragment listRiderFragment) {
        mInflater = LayoutInflater.from(context);
        this.mDataset = myDataset;
        this.listRiderFragment = listRiderFragment;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView nameRider, distanceValue;
        View view_item;
        public MyViewHolder(View itemView){
            super(itemView);
            this.view_item = itemView;
            nameRider = itemView.findViewById(R.id.name_rider);
            distanceValue = itemView.findViewById(R.id.distance);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListRiderAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  mInflater.inflate(R.layout.list_rider_item, parent, false);

        view.findViewById(R.id.confirm_rider).setOnClickListener(e -> listRiderFragment.selectRider());

        return new MyViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        RiderInfo mCurrent = mDataset.get(position);
        holder.nameRider.setText(mCurrent.getName());
        DecimalFormat df = new DecimalFormat("#.##");
        holder.distanceValue.setText((df.format(mCurrent.getDist())) + " km");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

public class ListRiderFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private TreeMap<Double, String> distanceMap;
    private HashMap<String, String> ridersMap;
    private ArrayList<RiderInfo> ridersList;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public ListRiderFragment() {
        // Required empty public constructor
    }

    public static ListRiderFragment newInstance() {
        ListRiderFragment fragment = new ListRiderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_rider, container, false);

        ridersMap = ((MapsActivity) getActivity()).getRidersMap();
        distanceMap = ((MapsActivity) getActivity()).getDistanceMap();
        treeMapToList(ridersMap, distanceMap);

        recyclerView = view.findViewById(R.id.list_rider_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ListRiderAdapter(getContext(), ridersList, this);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    public void treeMapToList(HashMap<String, String> ridersMap, TreeMap<Double, String> distanceMap){
        ridersList = new ArrayList<>();

        for(Map.Entry<Double,String> entry : distanceMap.entrySet()) {
            ridersList.add(new RiderInfo(ridersMap.get(entry.getValue()), entry.getKey()));
        }
    }

    public void selectRider() {
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        String id = getActivity().getIntent().getStringExtra(ORDER_ID);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            Query queryDel = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                    + "/" + RESERVATION_PATH).orderByChild("name").equalTo(id);

            queryDel.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DatabaseReference acceptOrder = database.getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                                + "/" + ACCEPTED_ORDER_PATH);
                        Map<String, Object> orderMap = new HashMap<>();

                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            OrderItem reservationItem = d.getValue(OrderItem.class);
                            orderMap.put(Objects.requireNonNull(acceptOrder.push().getKey()), reservationItem);
                            d.getRef().removeValue();
                        }

                        acceptOrder.updateChildren(orderMap);

                        // choosing the first available rider which assign the order
                        Query queryRider = database.getReference(RIDERS_PATH);
                        queryRider.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    String keyRider = "", name = "";

                                    for(DataSnapshot d : dataSnapshot.getChildren()){
                                        if((boolean)d.child("available").getValue()){
                                            keyRider = d.getKey();
                                            name = d.child("rider_info").child("name").getValue(String.class);
                                            break;
                                        }
                                    }

                                    DatabaseReference addOrderToRider = database.getReference(RIDERS_PATH + "/" + keyRider + RIDERS_ORDER);
                                    addOrderToRider.updateChildren(orderMap);

                                    //setting to 'false' boolean variable of rider
                                    DatabaseReference setFalse = database.getReference(RIDERS_PATH + "/" + keyRider + "/available");
                                    setFalse.setValue(false);

                                    Toast.makeText(getContext(), "Order assigned to rider " + name, Toast.LENGTH_LONG).show();

                                    getActivity().finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("RESERVATION", "Failed to read value.", error.toException());
                }
            });

            reservationDialog.dismiss();
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(e -> reservationDialog.dismiss());

        reservationDialog.setView(view);
        reservationDialog.setTitle("Are you sure to select this rider?\n");

        reservationDialog.show();
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
