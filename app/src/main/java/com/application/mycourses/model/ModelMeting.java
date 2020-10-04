package com.application.mycourses.model;

public class ModelMeting {

    private String idMeting;
    private String meting;
    private String information;
    private String urlDocument;
    private String urlAudio;
    private String urlCover;

    public ModelMeting() {
    }

    public ModelMeting(String idMeting, String meting, String information, String urlDocument, String urlAudio, String urlCover) {
        this.idMeting = idMeting;
        this.meting = meting;
        this.information = information;
        this.urlDocument = urlDocument;
        this.urlAudio = urlAudio;
        this.urlCover = urlCover;
    }

    public String getIdMeting() {
        return idMeting;
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
