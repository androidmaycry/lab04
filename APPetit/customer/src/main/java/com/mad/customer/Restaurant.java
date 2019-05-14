package com.mad.customer;

import static com.mad.mylibrary.SharedClass.*;
import com.firebase.ui.database.SnapshotParser;
import com.mad.mylibrary.Restaurateur;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class Restaurant extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static FirebaseRecyclerOptions<Restaurateur> options =
            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                            new SnapshotParser<Restaurateur>(){
                                @NonNull
                                @Override
                                public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                    Restaurateur searchRest;
                                    if(snapshot.child("info").child("photoUri").getValue() == null){
                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                snapshot.child("info").child("name").getValue().toString(),
                                                snapshot.child("info").child("addr").getValue().toString(),
                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                snapshot.child("info").child("phone").getValue().toString(),
                                                "null");
                                    }
                                    else{
                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                snapshot.child("info").child("name").getValue().toString(),
                                                snapshot.child("info").child("addr").getValue().toString(),
                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                snapshot.child("info").child("phone").getValue().toString(),
                                                snapshot.child("info").child("photoUri").getValue().toString());
                                    }
                                    return searchRest;
                                }
                            }).build();

    public Restaurant() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.restaurant_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                String key = getRef(position).getKey();
                holder.setData(model, position, key);
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                return new RestaurantViewHolder(view);
            }
        };
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onStop();
                if(newText.length()==0){
                    options =
                            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                                            new SnapshotParser<Restaurateur>(){
                                                @NonNull
                                                @Override
                                                public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                                    Restaurateur searchRest;
                                                    if(snapshot.child("info").child("photoUri").getValue() == null){
                                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                snapshot.child("info").child("name").getValue().toString(),
                                                                snapshot.child("info").child("addr").getValue().toString(),
                                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                                snapshot.child("info").child("phone").getValue().toString(),
                                                                "null");
                                                    }
                                                    else{
                                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                snapshot.child("info").child("name").getValue().toString(),
                                                                snapshot.child("info").child("addr").getValue().toString(),
                                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                                snapshot.child("info").child("phone").getValue().toString(),
                                                                snapshot.child("info").child("photoUri").getValue().toString());
                                                    }

                                                    return searchRest;
                                                }
                                            }).build();
                }
                else {
                    options =
                            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                    .setQuery(FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO), new SnapshotParser<Restaurateur>(){
                                        @NonNull
                                        @Override
                                        public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                            Restaurateur searchRest = new Restaurateur();

                                            if (snapshot.child("info").child("name").exists() && snapshot.child("info").child("name").getValue().toString().contains(newText)) {

                                                if (snapshot.child("info").child("photoUri").getValue() != null) {
                                                    searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                            snapshot.child("info").child("name").getValue().toString(),
                                                            snapshot.child("info").child("addr").getValue().toString(),
                                                            snapshot.child("info").child("cuisine").getValue().toString(),
                                                            snapshot.child("info").child("openingTime").getValue().toString(),
                                                            snapshot.child("info").child("phone").getValue().toString(),
                                                            snapshot.child("info").child("photoUri").getValue().toString());
                                                } else {
                                                    searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                            snapshot.child("info").child("name").getValue().toString(),
                                                            snapshot.child("info").child("addr").getValue().toString(),
                                                            snapshot.child("info").child("cuisine").getValue().toString(),
                                                            snapshot.child("info").child("phone").getValue().toString(),
                                                            snapshot.child("info").child("openingTime").getValue().toString(),
                                                            "null");
                                                }
                                            }

                                            return searchRest;
                                        }
                                    }).build();
                }
                mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                        String key = getRef(position).getKey();
                        if(model.getName().equals("")){
                            holder.itemView.findViewById(R.id.restaurant).setLayoutParams(new FrameLayout.LayoutParams(0,0));
                            //holder.itemView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        }
                        else {
                            holder.setData(model, position, key);
                        }
                    }

                    @NonNull
                    @Override
                    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                        return new RestaurantViewHolder(view);
                    }
                };
                recyclerView.setAdapter(mAdapter);
                onStart();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.search:
                return true;

            case R.id.cuisine_fra:
                setFilter("French");
                return true;

            case R.id.cuisine_ita:
                setFilter("Italian");
                return true;

            case R.id.cuisine_jap:
                setFilter("Japanese");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFilter(String filter){
        onStop();
        options = new FirebaseRecyclerOptions.Builder<Restaurateur>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO), new SnapshotParser<Restaurateur>(){
                    @NonNull
                    @Override
                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Restaurateur searchRest = new Restaurateur();

                        if (snapshot.child("info").child("cuisine").exists() && snapshot.child("info").child("cuisine").getValue().toString().equals(filter)) {

                            if (snapshot.child("info").child("photoUri").getValue() != null) {
                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                        snapshot.child("info").child("name").getValue().toString(),
                                        snapshot.child("info").child("addr").getValue().toString(),
                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                        snapshot.child("info").child("phone").getValue().toString(),
                                        snapshot.child("info").child("photoUri").getValue().toString());
                            } else {
                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                        snapshot.child("info").child("name").getValue().toString(),
                                        snapshot.child("info").child("addr").getValue().toString(),
                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                        snapshot.child("info").child("phone").getValue().toString(),
                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                        null);
                            }
                        }
                        return searchRest;
                    }
                }).build();

        mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                String key = getRef(position).getKey();
                if(model.getName().equals("")){
                    holder.itemView.findViewById(R.id.restaurant).setLayoutParams(new FrameLayout.LayoutParams(0,0));
                    //holder.itemView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                }
                else {
                    holder.setData(model, position, key);
                }
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                return new RestaurantViewHolder(view);
            }
        };
        recyclerView.setAdapter(mAdapter);
        onStart();
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

    public interface OnFragmentInteractionListener {
    }
}
