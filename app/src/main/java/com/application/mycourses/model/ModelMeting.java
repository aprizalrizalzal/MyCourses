package com.application.mycourses.model;

public class ModelMeting {

    private String classId;
    private String idMeting;
    private String userId;
    private String courses;
    private String meting;
    private String information;
    private String urlDocument;
    private String urlAudio;
    private String urlCover;

    public ModelMeting() {
    }

    public ModelMeting(String classId, String idMeting, String userId, String courses, String meting, String information, String urlDocument, String urlAudio, String urlCover) {
        this.classId = classId;
        this.idMeting = idMeting;
        this.userId = userId;
        this.courses = courses;
        this.meting = meting;
        this.information = information;
        this.urlDocument = urlDocument;
        this.urlAudio = urlAudio;
        this.urlCover = urlCover;
    }

    public String getClassId() {
        return classId;
    }

    public String getIdMeting() {
        return idMeting;
    }

    public String getUserId() {
        return userId;
    }

    public String getCourses() {
        return courses;
    }

    public String getMeting() {
        return meting;
    }

    public String getInformation() {
        return information;
    }

    public String getUrlDocument() {
        return urlDocument;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

    public String getUrlCover() {
        return urlCover;
    }
}
