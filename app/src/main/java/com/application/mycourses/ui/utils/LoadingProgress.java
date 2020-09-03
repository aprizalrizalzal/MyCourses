package com.application.mycourses.ui.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;


import com.application.mycourses.R;

import java.util.Objects;

public class LoadingProgress {

    Activity activity;
    Dialog progressDialog;

    public LoadingProgress(Activity myActivity){
        activity = myActivity;
    }

    @SuppressLint("InflateParams")
    public void startLoadingProgress(){
        progressDialog = new Dialog(activity);
        progressDialog.setContentView(R.layout.loading_progress);
        progressDialog.setCancelable(false);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
    }

    public void dismissLoadingProgress() {
        progressDialog.dismiss();
    }
}
