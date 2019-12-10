package com.twenty80partnership.bibliofy.modules;

import java.util.ArrayList;

public class Course {
    String code,name;
    Integer years;
    ArrayList<Branch> branchList;

    public ArrayList<Branch> getBranchList() {
        return branchList;
    }

    public void setBranchList(ArrayList<Branch> branchList) {
        this.branchList = branchList;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYears() {
        return years;
    }

    public void setYears(Integer years) {
        this.years = years;
    }

    public Course() {
    }

    public Course(String code, String name, Integer years) {
        this.code = code;
        this.name = name;
        this.years = years;
    }
}
