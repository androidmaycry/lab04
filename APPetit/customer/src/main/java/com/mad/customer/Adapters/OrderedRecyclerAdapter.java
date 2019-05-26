package com.mad.customer.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.customer.R;

import java.util.ArrayList;

public class OrderedRecyclerAdapter extends RecyclerView.Adapter<OrderedRecyclerAdapter.MyViewHolder>  {

    ArrayList<String> dishes;
    LayoutInflater mInflater;

    public OrderedRecyclerAdapter(Context context, ArrayList<String> dishes){
        mInflater = LayoutInflater.from(context);
        this.dishes = dishes;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView dish_name;

        public MyViewHolder(View itemView){
            super(itemView);
            dish_name = itemView.findViewById(R.id.label);
        }
    }

    @NonNull
    @Override
    public OrderedRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =  mInflater.inflate(R.layout.dish_view, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderedRecyclerAdapter.MyViewHolder myViewHolder, int position) {
        String mCurrent = dishes.get(position);
        myViewHolder.dish_name.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }
}
