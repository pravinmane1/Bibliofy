package com.twenty80partnership.bibliofy.modules;

public class OrderRequest {
    String addressId,method;
    Long userTimeAdded;

    public OrderRequest() {
    }

    public Long getUserTimeAdded() {
        return userTimeAdded;
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
