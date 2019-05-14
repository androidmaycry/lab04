package com.mad.customer;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad.mylibrary.Restaurateur;
import com.squareup.picasso.Picasso;

public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView name;
    TextView addr;
    TextView cuisine;
    TextView opening;
    ImageView img;
    int position;
    Restaurateur current;
    private String key;

    public RestaurantViewHolder(View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.listview_name);
        addr = itemView.findViewById(R.id.listview_address);
        cuisine = itemView.findViewById(R.id.listview_cuisine);
        img = itemView.findViewById(R.id.restaurant_image);
        opening = itemView.findViewById(R.id.listview_opening);

        itemView.setOnClickListener(this);
    }

    void setData (Restaurateur current, int position, String key){
        this.name.setText(current.getName());
        this.addr.setText(current.getAddr());
        this.cuisine.setText(current.getCuisine());
        this.opening.setText(current.getOpeningTime());
        if(current.getPhotoUri()!="") {
            Picasso.get()
                    .load(current.getPhotoUri())
                    .resize(150, 150)
                    .centerCrop()
                    .into(this.img);
        }
        this.position = position;
        this.current = current;
        this.key = key;

    }

    @Override
    public void onClick(View view) {
        //Toast.makeText(view.getContext(), "Item"+getAdapterPosition(), Toast.LENGTH_LONG).show();

        //Toast.makeText(view.getContext(), this.key, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(view.getContext(), Ordering.class);
        intent.putExtra("name", current.getName());
        intent.putExtra("addr", current.getAddr());
        intent.putExtra("cell", current.getPhone());
        intent.putExtra("cuisine", current.getCuisine());
        intent.putExtra("email", current.getMail());
        intent.putExtra("opening", current.getOpeningTime());
        intent.putExtra("img", current.getPhotoUri());
        intent.putExtra("key", this.key);
        view.getContext().startActivity(intent);
    }
}
