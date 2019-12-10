package com.twenty80partnership.bibliofy.modules;

public class User {
    private String name,email,photo,phone,college,courseCode,branchCode,yearCode,semCode,userCode,course,uId,searchName;
    private Integer orders;
    private Long registerDate;

    public User(String name, String email,String photo, String college, String courseCode,
                String branchCode, String yearCode,
                String userCode, Integer orders,String course,Long registerDate,String uId,String searchName) {
        this.name = name;
        this.email = email;
        this.college = college;
        this.courseCode = courseCode;
        this.branchCode = branchCode;
        this.yearCode = yearCode;
        this.orders = orders;
        this.photo = photo;
        this.userCode = userCode;
        this.course = course;
        this.registerDate = registerDate;
        this.uId = uId;
        this.searchName = searchName;
    }

    public User(String name, String email,String photo,int orders,Long registerDate,String uId,String searchName){
        this.name = name;
        this.email = email;
        this.orders = orders;
        this.photo = photo;
        this.registerDate = registerDate;
        this.uId = uId;
        this.searchName = searchName;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public Long getRegisterDate() {
        return registerDate;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setRegisterDate(Long registerDate) {
        this.registerDate = registerDate;
    }

    public User() {
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSemCode() {
        return semCode;
    }

    public void setSemCode(String semCode) {
        this.semCode = semCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCollege() {
        return college;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getYearCode() {
        return yearCode;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public void setYearCode(String yearCode) {
        this.yearCode = yearCode;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
