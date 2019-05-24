package com.mad.customer;

import com.mad.customer.Adapters.ConfirmRecyclerAdapter;
import com.mad.mylibrary.OrderItem;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.TimeOpen;
import static com.mad.mylibrary.SharedClass.user;

public class Confirm extends AppCompatActivity {

    private String time = "";
    private String tot;
    private String restAddr;
    private String photo;
    private ArrayList<String> keys;
    private ArrayList<String> names;
    private ArrayList<String> prices;
    private ArrayList<String> nums;
    private String key;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String desiredTime = "";
    private Button desiredTimeButton;
    private boolean timeOpen_open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getIncomingIntent();
        desiredTimeButton =  findViewById(R.id.desired_time);
        desiredTimeButton.setOnClickListener(l->{setDesiredTimeDialog();});
        findViewById(R.id.confirm_order_button).setOnClickListener(e->{
            if(desiredTime.trim().length() > 0){

                //TODO controlla formato orario
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" +
                        key + RESERVATION_PATH);
                HashMap<String, Object> orderMap = new HashMap<>();
                Log.d("user", ""+names);
                Log.d("test", restAddr);
                orderMap.put(myRef.push().getKey(), new OrderItem(user.getName(), user.getAddr(), restAddr, user.getPhone(), desiredTime,tot, user.getPhotoPath(), names));
                myRef.updateChildren(orderMap);
                Toast.makeText(this, "Order confirmed", Toast.LENGTH_LONG).show();
                setResult(1);
                finish();
            }
            else
                Toast.makeText(this, "Please select desired time", Toast.LENGTH_LONG).show();
        });
    }

    private void getIncomingIntent (){
        keys = getIntent().getStringArrayListExtra("keys");
        names = getIntent().getStringArrayListExtra("names");
        prices = getIntent().getStringArrayListExtra("prices");
        nums = getIntent().getStringArrayListExtra("nums");
        key = getIntent().getStringExtra("key");
        restAddr = getIntent().getStringExtra("raddr");
        photo = getIntent().getStringExtra("photo");

        recyclerView = findViewById(R.id.dish_conf_recyclerview);
        mAdapter = new ConfirmRecyclerAdapter(this, names, prices, nums, Confirm.this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        updatePrice();
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

    private String[] setTimeValue(){
        String[] cent = new String[100];
        for(int i=0; i<100; i++){
            if(i<10) {
                cent[i] = "0" +i;
            }
            else{
                cent[i] = ""+i;
            }
        }
        return cent;
    }

    private void setDesiredTimeDialog(){
        AlertDialog openingTimeDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(Confirm.this);
        final View viewOpening = inflater.inflate(R.layout.opening_time_dialog, null);

        timeOpen_open = true;

        NumberPicker hour = viewOpening.findViewById(R.id.hour_picker);
        NumberPicker min = viewOpening.findViewById(R.id.min_picker);

        openingTimeDialog.setView(viewOpening);

        openingTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            timeOpen_open = false;

            int hourValue = hour.getValue();
            int minValue = min.getValue();

            String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);

            if(hourValue < 10)
                hourString = "0" + hourValue;
            if(minValue < 10)
                minString = "0" + minValue;

            desiredTime = hourString + ":" + minString;

            desiredTimeButton.setText(desiredTime);
        });
        openingTimeDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", (dialog, which) -> {
            timeOpen_open = false;
            dialog.dismiss();
        });

        String[] hours = setTimeValue();
        hour.setDisplayedValues(hours);
        hour.setMinValue(0);
        hour.setMaxValue(23);
        hour.setValue(0);

        String[] mins = setTimeValue();
        min.setDisplayedValues(mins);
        min.setMinValue(0);
        min.setMaxValue(59);
        min.setValue(0);

        openingTimeDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            saveAndGoBack();
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveAndGoBack();
        super.onBackPressed();

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(TimeOpen, timeOpen_open);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getBoolean(TimeOpen))
            setDesiredTimeDialog();
    }

    public void updatePrice (){
        tot =calcoloTotale(prices, nums);
        TextView totale = findViewById(R.id.totale);
        totale.setText(tot + " €");
    }

    public void removeItem (int pos){
       keys.remove(pos);
       updatePrice();
       if (keys.isEmpty()){
           saveAndGoBack();
           finish();
       }
    }

    public void saveAndGoBack (){
        Intent intent = new Intent();
        intent.putStringArrayListExtra("keys", (ArrayList<String>) keys);
        intent.putStringArrayListExtra("names", (ArrayList<String>) names);
        intent.putStringArrayListExtra("prices", (ArrayList<String>) prices);
        intent.putStringArrayListExtra("nums", (ArrayList<String>) nums);
        setResult(0, intent);
    }
}

