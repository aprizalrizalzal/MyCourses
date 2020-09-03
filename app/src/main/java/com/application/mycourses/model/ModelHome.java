package com.application.mycourses.model;

public class ModelHome {

    private String urlCover;
    private String idClass;
    private String university;
    private String faculty;
    private String study;
    private String semester;
    private String courses;
    private String dateCreated;
    private String lastUpdate;

    public ModelHome() {
    }

    public ModelHome(String urlCover, String idClass, String university, String faculty, String study, String semester, String courses, String dateCreated, String lastUpdate) {
        this.urlCover = urlCover;
        this.idClass = idClass;
        this.university = university;
        this.faculty = faculty;
        this.study = study;
        this.semester = semester;
        this.courses = courses;
        this.dateCreated = dateCreated;
        this.lastUpdate = lastUpdate;
    }

    public String getUrlCover() {
        return urlCover;
    }

    public String getIdClass() {
        return idClass;
    }

    public String getUniversity() {
        return university;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getStudy() {
        return study;
    }

    public String getSemester() {
        return semester;
    }

    public String getCourses() {
        return courses;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }
}
