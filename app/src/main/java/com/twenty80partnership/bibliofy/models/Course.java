package com.twenty80partnership.bibliofy.models;

import java.util.ArrayList;

public class Course implements Comparable<Course>{
    String name,id;
    Float priority;
    Integer year;
    ArrayList<Branch> branchList;


    public ArrayList<Branch> getBranchList() {
        return branchList;
    }

    public void setBranchList(ArrayList<Branch> branchList) {
        this.branchList = branchList;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public Course() {
    }

    @Override
    public int compareTo(Course o) {
        return this.priority.compareTo(o.getPriority());
    }
}
