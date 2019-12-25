package com.twenty80partnership.bibliofy.models;

public class CountData {
    private Integer quantity;
    private Boolean status;

    public Integer getQuantity() {
        return quantity;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public CountData() {
    }
}
