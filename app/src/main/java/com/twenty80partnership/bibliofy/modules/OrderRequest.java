package com.twenty80partnership.bibliofy.modules;

public class OrderRequest {
    String addressId,method;

    public OrderRequest() {
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
