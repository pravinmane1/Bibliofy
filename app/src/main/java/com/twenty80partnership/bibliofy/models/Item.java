package com.twenty80partnership.bibliofy.models;

public class Item implements Comparable<Item>{
String name,pic,id,code;
Long timeAdded;
Float priority;

    public Item(String name, String pic, String id, Long timeAdded, Float priority) {
        this.name = name;
        this.pic = pic;
        this.id = id;
        this.timeAdded = timeAdded;
        this.priority = priority;
    }

    public Item(String name, String id, Long timeAdded, Float priority) {
        this.name = name;
        this.id = id;
        this.timeAdded = timeAdded;
        this.priority = priority;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public Item() {
    }

public int compareTo(Item s){

        Float f = priority;
        return f.compareTo(s.priority);
        }


}
