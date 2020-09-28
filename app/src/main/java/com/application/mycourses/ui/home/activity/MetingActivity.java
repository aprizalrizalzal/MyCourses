package com.application.mycourses.ui.home.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.model.ModelMeting;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MetingActivity extends AppCompatActivity {

    private List<ModelMeting> modelMetings = new ArrayList<>();
    private MetingAdapter metingAdapter;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rvMeting;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meting);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String urlCover = intent.getStringExtra("urlCover");
        String courses = intent.getStringExtra("courses");
        String classId = intent.getStringExtra("classId");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        refreshLayout = findViewById(R.id.swipeRefresh);
        rvMeting = findViewById(R.id.rvMeting);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CircleImageView imgAppBar = findViewById(R.id.imgAppBar);
        if (urlCover != null) {
            if (urlCover.equals("urlCover")){
                Glide.with(getApplication())
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgAppBar);
            } else {
                Glide.with(getApplication())
                        .load(urlCover)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgAppBar);
            }
        }
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(courses);

        FloatingActionButton fab = findViewById(R.id.fabMeting);
        String idUser = user.getUid();
        fab.setOnClickListener(view -> {
            Intent intentMeting = new Intent(MetingActivity.this, CreateMetingActivity.class);
            intentMeting.putExtra("userId",userId);
            intentMeting.putExtra("urlCover",urlCover);
            intentMeting.putExtra("courses",courses);
            intentMeting.putExtra("classId",classId);
            startActivity(intentMeting);
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        });

        if (idUser.equals(userId)){
            fab.setVisibility(View.VISIBLE);
        }

        if (classId != null) {
            progressBar.setVisibility(View.VISIBLE);
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        modelMetings.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ModelMeting modelMeting = dataSnapshot.getValue(ModelMeting.class);
                            if (modelMeting !=null){
                                modelMetings.add(modelMeting);
                                metingAdapter = new MetingAdapter(MetingActivity.this, modelMetings);
                            }
                            rvMeting.setAdapter(metingAdapter);
                            rvMeting.setLayoutManager(new LinearLayoutManager(MetingActivity.this));
                            rvMeting.setHasFixedSize(true);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_nav, menu);
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        itemSearch.setVisible(false);
        MenuItem itemSetting = menu.findItem(R.id.action_setting);
        itemSetting.setVisible(false);
        MenuItem itemHelp = menu.findItem(R.id.action_help);
        itemHelp.setOnMenuItemClickListener(menuItem -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MetingActivity.this);
            alertDialogBuilder
                    .setTitle(R.string.action_help)
                    .setMessage(R.string.help_or_questions)
                    .setCancelable(true)
                    .setNeutralButton(R.string.yes, (dialog, id) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aprizal040498@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sign_in) + "\n \n");

                        if (emailIntent.resolveActivity(this.getPackageManager()) != null) {
                            this.startActivity(emailIntent);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
            alertDialog.show();
            return true;
        });

        return true;
    }

    private boolean haveConnection(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MetingActivity.this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();

    }
}