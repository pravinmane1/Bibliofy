package com.twenty80partnership.bibliofy.models;

public class UpiTransaction {
    String oId,tsnId,targetUpi;
    Long timeAdded;
    String upiStatus;

    public UpiTransaction() {
    }

    public UpiTransaction(String oId, String tsnId, String targetUpi, Long timeAdded, String upiStatus) {
        this.oId = oId;
        this.tsnId = tsnId;
        this.targetUpi = targetUpi;
        this.timeAdded = timeAdded;
        this.upiStatus = upiStatus;
    }

    public String getoId() {
        return oId;
    }

    public void setoId(String oId) {
        this.oId = oId;
    }

    public String getTsnId() {
        return tsnId;
    }

    public void setTsnId(String tsnId) {
        this.tsnId = tsnId;
    }

    public String getTargetUpi() {
        return targetUpi;
    }

    public void setTargetUpi(String targetUpi) {
        this.targetUpi = targetUpi;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUpiStatus() {
        return upiStatus;
    }

    public void setUpiStatus(String upiStatus) {
        this.upiStatus = upiStatus;
    }
}
