package com.twenty80partnership.bibliofy.modules;

import java.io.Serializable;

public class Order  implements Serializable {
    String orderId;
    Address address;
    PriceDetails priceDetails;
    String status;
    Long timeAdded;
    String tOid;
    Boolean userViewed;


    public Order(String orderId, Address address, PriceDetails priceDetails, String status, Long timeAdded, String tOid, Boolean userViewed) {
        this.orderId = orderId;
        this.address = address;
        this.priceDetails = priceDetails;
        this.status = status;
        this.timeAdded = timeAdded;
        this.tOid = tOid;
        this.userViewed = userViewed;
    }

    public Boolean getUserViewed() {
        return userViewed;
    }

    public void setUserViewed(Boolean userViewed) {
        this.userViewed = userViewed;
    }

    public String gettOid() {
        return tOid;
    }

    public void settOid(String tOid) {
        this.tOid = tOid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Order() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public PriceDetails getPriceDetails() {
        return priceDetails;
    }

    public void setPriceDetails(PriceDetails priceDetails) {
        this.priceDetails = priceDetails;
    }



}
