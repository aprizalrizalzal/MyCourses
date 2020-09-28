package com.application.mycourses.model;

public class ModelMeting {

    private String meting;
    private String information;
    private String document;
    private String audio;
    private String urlCover;

    public ModelMeting() {
    }

    public ModelMeting(String meting, String information, String document, String audio, String urlCover) {
        this.meting = meting;
        this.information = information;
        this.document = document;
        this.audio = audio;
        this.urlCover = urlCover;
    }

    public String getMeting() {
        return meting;
    }

    public String getInformation() {
        return information;
    }

    public String getDocument() {
        return document;
    }

    public String getAudio() {
        return audio;
    }

    public String getUrlCover() {
        return urlCover;
    }
}
