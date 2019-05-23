package com.mad.customer;

import static com.mad.mylibrary.SharedClass.*;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

//TODO ci mette un po a partire.. why? forse glide?

class ViewHolderDailyOffer extends RecyclerView.ViewHolder {
    private ImageView dishPhoto;
    private TextView dishName, dishDesc, dishPrice, dishQuantity;
    private DishItem current;
    private int position;
    private View view;

    ViewHolderDailyOffer(View itemView) {
        super(itemView);
        this.view = itemView;
        dishName = itemView.findViewById(R.id.dish_name);
        dishDesc = itemView.findViewById(R.id.dish_desc);
        dishPrice = itemView.findViewById(R.id.dish_price);
        //dishQuantity = itemView.findViewById(R.id.dish_quant);
        dishPhoto = itemView.findViewById(R.id.dish_image);
    }

    void setData(DishItem current, int position) {
        InputStream inputStream = null;

        this.dishName.setText(current.getName());
        this.dishDesc.setText(current.getDesc());
        this.dishPrice.setText(current.getPrice() + " €");
        //this.dishQuantity.setText(String.valueOf(current.getQuantity()));
        if (current.getPhotoUri() != null) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                inputStream = new URL(current.getPhotoUri()).openStream();
                if (inputStream != null)
                    Glide.with(itemView.getContext()).load(current.getPhotoUri()).into(dishPhoto);
                else
                    Glide.with(itemView.getContext()).load(R.drawable.hamburger).into(dishPhoto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.position = position;
        this.current = current;
    }

    public View getView() {
        return view;
    }
}

public class Ordering extends AppCompatActivity {
    Menu mymenu;
    private String key;
    private String restAddr;
    private String restPhoto;
    private static ArrayList<String> removed = new ArrayList<>();
    ArrayList<String> keys = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> nums = new ArrayList<String>();
    ArrayList<String> prices = new ArrayList<String>();
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOffer> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);
        recyclerView = findViewById(R.id.dish_recyclerview);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        getIncomingIntent();

        FirebaseRecyclerOptions<DishItem> options =
                new FirebaseRecyclerOptions.Builder<DishItem>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + key + "/dishes"),
                                new SnapshotParser<DishItem>(){
                                    @NonNull
                                    @Override
                                    public DishItem parseSnapshot(@NonNull DataSnapshot snapshot) {
                                        DishItem dishItem;
                                        if(snapshot.child("photoUri").getValue() != null){
                                            dishItem = new DishItem(snapshot.child("name").getValue().toString(),
                                                    snapshot.child("desc").getValue().toString(),
                                                    Float.parseFloat(snapshot.child("price").getValue().toString()),
                                                    Integer.parseInt(snapshot.child("quantity").getValue().toString()),
                                                    snapshot.child("photoUri").getValue().toString());
                                        }
                                        else{
                                            dishItem = new DishItem(snapshot.child("name").getValue().toString(),
                                                    snapshot.child("desc").getValue().toString(),
                                                    Float.parseFloat(snapshot.child("price").getValue().toString()),
                                                    Integer.parseInt(snapshot.child("quantity").getValue().toString()),
                                                    "null");
                                        }
                                        return dishItem;
                                    }
                                }).build();

        mAdapter = new FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOffer>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ViewHolderDailyOffer holder, int position, @NonNull DishItem model) {
                holder.setData(model, position);
                TextView numView = holder.getView().findViewById(R.id.dish_num);
                String dish_key = getRef(position).getKey();
                if(keys.contains(dish_key)){
                    int pos = keys.indexOf(dish_key);
                    String value_num = nums.get(pos);
                    numView.setText(value_num);
                    invalidateOptionsMenu();
                }
                else{
                    numView.setText("0");
                    invalidateOptionsMenu();
                }
                holder.getView().findViewById(R.id.add_dish).setOnClickListener(a->{
                    Integer num = Integer.parseInt((numView).getText().toString());
                    num++;
                    if(num>model.getQuantity()){
                        Toast.makeText(holder.getView().getContext(), "Maximum quantity exceeded", Toast.LENGTH_LONG).show();
                    }
                    else if (num>99){
                        Toast.makeText(holder.getView().getContext(), "Contact us to get more than this quantity", Toast.LENGTH_LONG).show();
                    }
                    else{
                        numView.setText(num.toString());
                        AddDish(dish_key, model.getName(),Float.toString(model.getPrice()),"add");
                        invalidateOptionsMenu();
                    }
                    if(!keys.isEmpty()){
                        findViewById(R.id.next).setBackgroundColor(Color.parseColor("#5aad54"));

                    }
                });
                holder.getView().findViewById(R.id.delete_dish).setOnClickListener(b->{

                    Integer num = Integer.parseInt((numView).getText().toString());
                    num--;
                    if(num<0){
                        Toast.makeText(holder.getView().getContext(), "Please select the right quantity", Toast.LENGTH_LONG).show();
                    }
                    else{
                        numView.setText(num.toString());
                        AddDish(dish_key, model.getName(),Float.toString(model.getPrice()),"remove");
                        invalidateOptionsMenu();
                    }
                    if(keys.isEmpty()){
                        findViewById(R.id.next).setBackgroundColor(Color.parseColor("#c1c1c1"));
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolderDailyOffer onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_item,parent,false);

                return new ViewHolderDailyOffer(view);
            }
        };
        recyclerView.setAdapter(mAdapter);
        findViewById(R.id.next).setOnClickListener(w->{
            if (keys.size()==0){
                Toast.makeText(this, "Inserire un piatto.", Toast.LENGTH_LONG);
            }
            else{
                Intent intent = new Intent(this, Confirm.class);
                intent.putExtra("key", key);
                intent.putExtra("raddr", restAddr);
                intent.putExtra("photo", restPhoto);
                intent.putStringArrayListExtra("keys", (ArrayList<String>) keys);
                intent.putStringArrayListExtra("names", (ArrayList<String>) names);
                intent.putStringArrayListExtra("prices", (ArrayList<String>) prices);
                intent.putStringArrayListExtra("nums", (ArrayList<String>) nums);
                startActivityForResult(intent, 0);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == 0) {
            keys = data.getStringArrayListExtra("keys");
            names = data.getStringArrayListExtra("names");
            prices = data.getStringArrayListExtra("prices");
            nums = data.getStringArrayListExtra("nums");
            mAdapter.notifyDataSetChanged();
        }
        else if (resultCode==1){
            finish();
        }
    }

    private void getIncomingIntent(){

        String myName = getIntent().getStringExtra("name");
        restAddr = getIntent().getStringExtra("addr");
        String myCell = getIntent().getStringExtra("cell");
        String myCuisine = getIntent().getStringExtra("cuisine");
        String myEmail = getIntent().getStringExtra("email");
        String myOpening = getIntent().getStringExtra("opening");
        restPhoto= getIntent().getStringExtra("img");
        this.key = getIntent().getStringExtra("key");
        setFields(myName, restAddr, myCell, myCuisine, myEmail, myOpening, restPhoto);

    }

    private void setFields (String name, String addr, String cell, String description, String email, String opening, String img){
        TextView mname = findViewById(R.id.rest_info_name);
        TextView maddr = findViewById(R.id.rest_info_addr);
        TextView mcell = findViewById(R.id.rest_info_cell);
        TextView memail = findViewById(R.id.rest_info_mail);
        TextView mopening = findViewById(R.id.rest_info_open);
        ImageView mimg = findViewById(R.id.imageView);

        mname.setText(name);
        maddr.setText(addr);
        mcell.setText(cell);
        memail.setText(email);
        mopening.setText(opening);

        if(img.length() != 0) {
            Glide.with(getApplicationContext())
                    .load(img)
                    .into(mimg);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(keys.size()!=0) {
            MenuItem menuItem = (MenuItem) menu.findItem(R.id.action_custom_button);
            TextView cart = menuItem.getActionView().findViewById(R.id.money);
            String snum = getQuantity(nums);
            String tot = calcoloTotale(prices, nums);
            cart.setText(snum+" | "+tot+"€");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public String getQuantity (ArrayList<String> nums){
        int num =0;
        for (String a : nums){
            num += Integer.parseInt(a);
        }
        String snum = Integer.toString(num);
        return snum;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ordering, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            this.finish();
        }


        return super.onOptionsItemSelected(item);
    }

    public void AddDish(String key, String name, String price, String mode){
        if(keys.contains(key) && mode.equals("add")){
            int i = keys.indexOf(key);
            Integer num = Integer.parseInt(nums.get(i))+1;
            nums.set(i, num.toString());
        }
        else if(keys.contains(key) && mode.equals("remove")){
            int i = keys.indexOf(key);
            Integer num = Integer.parseInt(nums.get(i))-1;
            if(num.equals(0)){
                keys.remove(i);
                nums.remove(i);
                names.remove(i);
                prices.remove(i);
            }
            else {
                nums.set(i, num.toString());
            }
        }
        else{
            keys.add(key);
            nums.add("1");
            names.add(name);
            prices.add(price);
        }
    }
    private String calcoloTotale (ArrayList<String> prices, ArrayList<String> nums){
        float tot=0;
        for(int i=0; i<prices.size(); i++){
            float price = Float.parseFloat(prices.get(i));
            float num = Float.parseFloat(nums.get(i));
            tot=tot+(price*num);
        }
        return Float.toString(tot);
    }
}

