package com.twenty80partnership.bibliofy.models;

public class Branch {
    private String  name,code;

    public Branch(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Branch() {
    }
}
