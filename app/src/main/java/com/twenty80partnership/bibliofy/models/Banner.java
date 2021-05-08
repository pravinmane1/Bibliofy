package com.twenty80partnership.bibliofy.models;

public class Banner implements Comparable<Banner> {
    private String id, img, name;
    float priority;

    public float getPriority() {
        return priority;
    }

    public Banner(String id, String img, String name, float priority) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.priority = priority;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public Banner(String id, String img) {
        this.id = id;
        this.img = img;
    }

    public Banner() {
    }

    public int compareTo(Banner b) {

        Float p = priority;
        return p.compareTo(b.priority);
    }
}
