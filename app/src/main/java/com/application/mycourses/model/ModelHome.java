package com.application.mycourses.model;

public class ModelHome {

    private String classId;
    private String courses;
    private String dateCreated;
    private String dateJoin;
    private String faculty;
    private String lastUpdate;
    private String semester;
    private String study;
    private String university;
    private String urlCover;
    private String userId;

    public ModelHome() {
    }

    public ModelHome(String classId, String courses, String dateCreated, String dateJoin, String faculty, String lastUpdate, String semester, String study, String university, String urlCover, String userId) {
        this.classId = classId;
        this.courses = courses;
        this.dateCreated = dateCreated;
        this.dateJoin = dateJoin;
        this.faculty = faculty;
        this.lastUpdate = lastUpdate;
        this.semester = semester;
        this.study = study;
        this.university = university;
        this.urlCover = urlCover;
        this.userId = userId;
    }

    public String getClassId() {
        return classId;
    }

    public String getCourses() {
        return courses;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateJoin() {
        return dateJoin;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getSemester() {
        return semester;
    }

    public String getStudy() {
        return study;
    }

    public String getUniversity() {
        return university;
    }

    public String getUrlCover() {
        return urlCover;
    }

    public String getUserId() {
        return userId;
    }
}
