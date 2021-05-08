package com.twenty80partnership.bibliofy.models;

public class UserCourse {
    String courseCode,branchCode,yearCode,courseName,branchName;

    public UserCourse(String courseCode, String courseName, String branchCode, String branchName, String yearCode) {
        this.courseCode = courseCode;
        this.branchCode = branchCode;

        if (yearCode!=null)
            this.yearCode = yearCode;

        this.courseName = courseName;
        this.branchName = branchName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    public UserCourse() {
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getYearCode() {
        return yearCode;
    }

    public void setYearCode(String yearCode) {
        this.yearCode = yearCode;
    }
}
