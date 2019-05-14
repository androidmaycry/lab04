package com.mad.customer;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// TODO: DELETE PICASSO
//import com.squareup.picasso.Picasso;

public class DishViewHolder extends RecyclerView.ViewHolder{ // implements View.OnClickListener

    TextView name;
    TextView desc;
    TextView price;
    ImageView img;
    int position;
    DishItem current;



    public DishViewHolder(View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.dish_name);
        desc = itemView.findViewById(R.id.dish_desc);
        price = itemView.findViewById(R.id.dish_price);
        img = itemView.findViewById(R.id.dish_image);

        //itemView.setOnClickListener(this);
    }
    void setData (DishItem current, int position){

        Float price = current.getPrice();
        this.name.setText(current.getName());
        this.desc.setText(current.getDesc());
        this.price.setText(price.toString());
        /*Picasso.get()
                .load(current.getPhotoUri())
                .resize(150, 150)
                .centerCrop()
                .into(this.img);*/
        this.position = position;
        this.current = current;
    }
}
