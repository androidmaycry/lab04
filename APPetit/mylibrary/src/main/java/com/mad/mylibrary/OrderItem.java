package com.mad.mylibrary;

import java.lang.reflect.Array;
import java.util.ArrayList;

public final class OrderItem {
    public String name, addrCustomer, addrRestaurant, cell, time, totPrice, img;
    public ArrayList<String> order;

    public OrderItem(){

    }

    public OrderItem(String name, String addrCustomer, String addrRestaurant, String cell, String time, String totPrice, String img, ArrayList<String> order) {
        this.name = name;
        this.addrCustomer = addrCustomer;
        this.addrRestaurant = addrRestaurant;
        this.cell = cell;
        this.time = time;
        this.totPrice = totPrice;
        this.img = img;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getAddrCustomer() {
        return addrCustomer;
    }

    public String getAddrRestaurant() {
        return addrRestaurant;
    }

    public String getCell() {
        return cell;
    }

    public String getTime() {
        return time;
    }

    public String getTotPrice() {
        return totPrice;
    }

    public String getImg() {
        return img;
    }

    public ArrayList<String> getOrder() {
        return order;
    }
}
