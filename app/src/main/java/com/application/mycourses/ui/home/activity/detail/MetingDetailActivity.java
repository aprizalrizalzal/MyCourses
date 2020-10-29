package com.application.mycourses.ui.home.activity.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.application.mycourses.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class MetingDetailActivity extends AppCompatActivity {

    private PDFView pdfView;
    private String classId, userId,idMeting,meting,courses,urlCover,urlDocument,urlAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meting_detail);

        Intent intent = getIntent();
        classId = intent.getStringExtra("classId");
        userId = intent.getStringExtra("userId");
        courses = intent.getStringExtra("courses");
        meting = intent.getStringExtra("meting");
        idMeting = intent.getStringExtra("idMeting");
        urlCover = intent.getStringExtra("urlCover");
        urlDocument = intent.getStringExtra("urlDocument");
        urlAudio = intent.getStringExtra("urlAudio");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvAppBar = findViewById(R.id.tvAppBar);
        CircleImageView imgAppBar = findViewById(R.id.imgAppBar);

        /*MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        Glide.with(this)
                .load(urlCover)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                .into(imgAppBar);

        tvAppBar.setText(getString(R.string.metingDialog,meting));

        File fileDoc = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), idMeting);
        pdfView = findViewById(R.id.pdf_view);

        if (idMeting != null) {
            pdfView.fromUri(Uri.fromFile(fileDoc))
                    .pageSnap(true)
                    .swipeHorizontal(false)
                    //.scrollHandle(new ScrollHandle(this))
                    .enableSwipe(true)
                    .pageFitPolicy(FitPolicy.BOTH)
                    .spacing(10)
                    .load();
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_meeting_detail, menu);
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        MenuItem itemClasswork = menu.findItem(R.id.action_classwork);
        return true;
    }
}