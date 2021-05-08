package com.twenty80partnership.bibliofy.models;

public class Branch implements Comparable<Branch>{
    private String  name,code;
    private Float priority;

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Branch() {
    }

    @Override
    public int compareTo(Branch o) {
        return this.priority.compareTo(o.getPriority());
    }
}
