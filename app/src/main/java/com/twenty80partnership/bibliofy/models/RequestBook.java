package com.twenty80partnership.bibliofy.models;

public class RequestBook {
    private String course,branch,currentyear,bookName,time;

    public RequestBook() {
    }

    public RequestBook(String course, String branch, String currentyear, String bookName, String time) {
        this.course = course;
        this.branch = branch;
        this.currentyear = currentyear;
        this.bookName = bookName;
        this.time = time;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCurrentyear() {
        return currentyear;
    }

    public void setCurrentyear(String currentyear) {
        this.currentyear = currentyear;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
