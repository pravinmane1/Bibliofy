package com.twenty80partnership.bibliofy.models;

public class User {
    private String name,email,photo,phone,college,uId,searchName,gender;
    private Integer orders;
    private Long registerDate;
    private Boolean phoneVerified;
    private UserCourse userCourse;

    public User(String name,
                String email,
                String photo,
                String college,
                Integer orders,
                Long registerDate,
                String uId,
                String searchName,
                String gender,
                UserCourse userCourse) {
        this.name = name;
        this.email = email;
        this.college = college;
        this.orders = orders;
        this.photo = photo;
        this.registerDate = registerDate;
        this.uId = uId;
        this.searchName = searchName;
        this.gender = gender;
        this.userCourse = userCourse;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public UserCourse getUserCourse() {
        return userCourse;
    }

    public void setUserCourse(UserCourse userCourse) {
        this.userCourse = userCourse;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
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

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
