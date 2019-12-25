package com.twenty80partnership.bibliofy.models;

public class UpiTransaction {
    String uId,tsnId,targetUpi;
    Long timeAdded;
    Boolean checked;

    public UpiTransaction() {
    }

    public UpiTransaction(String uId, String tsnId, String targetUpi, Long timeAdded, Boolean checked) {
        this.uId = uId;
        this.tsnId = tsnId;
        this.targetUpi = targetUpi;
        this.timeAdded = timeAdded;
        this.checked = checked;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
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

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
