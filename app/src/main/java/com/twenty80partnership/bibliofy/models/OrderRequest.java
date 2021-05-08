package com.twenty80partnership.bibliofy.models;

public class OrderRequest {
    String addressId,method;
    Long userTimeAdded;
    String tsnId;
    String targetUpi;
    String pin;

    public OrderRequest() {
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Long getUserTimeAdded() {
        return userTimeAdded;
    }

    public String getTsnId() {
        return tsnId;
    }

    public String getTargetUpi() {
        return targetUpi;
    }

    public void setTargetUpi(String targetUpi) {
        this.targetUpi = targetUpi;
    }

    public void setTsnId(String tsnId) {
        this.tsnId = tsnId;
    }

    public void setUserTimeAdded(Long userTimeAdded) {
        this.userTimeAdded = userTimeAdded;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
