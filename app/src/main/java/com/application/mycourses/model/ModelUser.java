package com.application.mycourses.model;

public class ModelUser {
    private String userId;
    private String userName;
    private String gender;
    private String birth;
    private String numberPhone;
    private String email;
    private Boolean emailVerify;
    private Boolean userSignIn;
    private String urlPicture;
    private Boolean userOnline;
    private String lastDate;
    private String lastTime;

    public ModelUser() {
    }

    public ModelUser(String userId, String userName, String gender, String birth, String numberPhone, String email, Boolean emailVerify, Boolean userSignIn, String urlPicture, Boolean userOnline, String lastDate, String lastTime) {
        this.userId = userId;
        this.userName = userName;
        this.gender = gender;
        this.birth = birth;
        this.numberPhone = numberPhone;
        this.email = email;
        this.emailVerify = emailVerify;
        this.userSignIn = userSignIn;
        this.urlPicture = urlPicture;
        this.userOnline = userOnline;
        this.lastDate = lastDate;
        this.lastTime = lastTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getGender() {
        return gender;
    }

    public String getBirth() {
        return birth;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailVerify() {
        return emailVerify;
    }

    public Boolean getUserSignIn() {
        return userSignIn;
    }

    public String getUrlPicture() {
        return urlPicture;
    }

    public Boolean getUserOnline() {
        return userOnline;
    }

    public String getLastDate() {
        return lastDate;
    }

    public String getLastTime() {
        return lastTime;
    }
}
