package com.mad.customer.Items;

public class RestaurantItem {
    private String name;
    private String addr;
    private Long cell;
    private String cuisine;
    private String description;
    private String email;
    private String opening;
    private String img;

    public RestaurantItem() {
        //empty contructor
    }

    public RestaurantItem(String name, String addr, Long cell, String cuisine, String description, String email, String opening, String img) {
        this.name = name;
        this.addr = addr;
        this.cell = cell;
        this.cuisine = cuisine;
        this.description = description;
        this.email = email;
        this.opening = opening;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Long getCell() {
        return cell;
    }

    public void setCell(Long cell) {
        this.cell = cell;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
