package com.twenty80partnership.bibliofy.modules;

import java.io.Serializable;

public class Book implements Serializable {
    private String name,author,publication,img,id;
    private Integer originalPrice,discountedPrice,discount;
    private Boolean availability;

    public Book() {
    }

    public Book(String id,String name, String author, String publication,String img,
                Integer originalPrice, Integer discountedPrice, Integer discount, Boolean availability) {
        this.id=id;
        this.name = name;
        this.author = author;
        this.publication = publication;
        this.img=img;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discount = discount;
        this.availability = availability;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setDiscountedPrice(Integer discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getPublication() {
        return publication;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getOriginalPrice() {
        return originalPrice;
    }

    public Integer getDiscountedPrice() {
        return discountedPrice;
    }

    public Integer getDiscount() {
        return discount;
    }

    public Boolean getAvailability() {
        return availability;
    }
}
