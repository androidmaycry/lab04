package com.mad.riders;

import java.util.ArrayList;

public class ReservationItem {
    private String name;
    private String addr;
    private String cell;
    private Integer img;
    private String time;
    private ArrayList<String> order;

    public ReservationItem() {
        //empty contructor
    }

    public ReservationItem(String name, String addr, String cell, Integer img,String time,ArrayList<String> order) {
        this.name = name;
        this.addr = addr;
        this.cell = cell;
        this.img = img;
        this.time = time;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String nome) {
        this.name = nome;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public Integer getImg() {
        return img;
    }

    public void setImg(Integer img) {
        this.img = img;
    }

    public String getTime(){return time;}

    public ArrayList<String> getOrder(){return order;}
}
