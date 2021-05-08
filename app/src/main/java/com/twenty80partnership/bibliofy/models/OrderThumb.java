package com.twenty80partnership.bibliofy.models;

public class OrderThumb {
    Integer daysForDelivery,totalItems;
    String orderId,orderStatus,paymentStatus;
    Long userTimeAdded;

    OrderThumb(){

    }

    public Integer getDaysForDelivery() {
        return daysForDelivery;
    }

    public void setDaysForDelivery(Integer daysForDelivery) {
        this.daysForDelivery = daysForDelivery;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getUserTimeAdded() {
        return userTimeAdded;
    }

    public void setUserTimeAdded(Long userTimeAdded) {
        this.userTimeAdded = userTimeAdded;
    }
}
