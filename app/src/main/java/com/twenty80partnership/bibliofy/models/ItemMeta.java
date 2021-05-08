package com.twenty80partnership.bibliofy.models;

public class ItemMeta {
    String itemId;
    String itemLocation;
    Long timeAdded;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ItemMeta() {
    }

    public ItemMeta(String itemId, String itemLocation, Long timeAdded, String type) {
        this.itemId = itemId;
        this.itemLocation = itemLocation;
        this.timeAdded = timeAdded;
        this.type = type;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }
}
