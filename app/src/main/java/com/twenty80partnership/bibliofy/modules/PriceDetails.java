package com.twenty80partnership.bibliofy.modules;

import java.io.Serializable;

public class PriceDetails implements Serializable {
    Integer amountDiscounted,count,amountOriginal;
    String method;

    public PriceDetails(Integer amountDiscounted, Integer count, Integer amountOriginal, String method) {
        this.amountDiscounted = amountDiscounted;
        this.count = count;
        this.amountOriginal = amountOriginal;
        this.method = method;
    }

    public PriceDetails() {
    }

    public Integer getAmountDiscounted() {
        return amountDiscounted;
    }

    public void setAmountDiscounted(Integer amountDiscounted) {
        this.amountDiscounted = amountDiscounted;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getAmountOriginal() {
        return amountOriginal;
    }

    public void setAmountOriginal(Integer amountOriginal) {
        this.amountOriginal = amountOriginal;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
