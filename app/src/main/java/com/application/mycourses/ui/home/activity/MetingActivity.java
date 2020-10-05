package com.application.mycourses.ui.home.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.model.ModelMeting;
import com.application.mycourses.ui.home.activity.meeting.CreateMetingActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MetingActivity extends AppCompatActivity {

    private List<ModelMeting> modelMetings = new ArrayList<>();
    private MetingAdapter metingAdapter;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private String classId, userId, urlCover, courses;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rvMeting;
    private LoadingProgress loadingProgress;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meting);

        Intent intent = getIntent();
        classId = intent.getStringExtra("classId");
        userId = intent.getStringExtra("userId");
        urlCover = intent.getStringExtra("urlCover");
        courses = intent.getStringExtra("courses");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        refreshLayout = findViewById(R.id.swipeRefresh);
        rvMeting = findViewById(R.id.rvMeting);
        progressBar = findViewById(R.id.progressBar);

        loadingProgress = new LoadingProgress(this);

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
        metingClass(classId,database);

        refreshLayout = findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(() -> {
            metingClass(classId,database);
            refreshLayout.setRefreshing(false);
        });
        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);

        String idUser = user.getUid();
        fabCreate.setOnClickListener(view -> {
            if (haveConnection()){
                Intent intentMeting = new Intent(MetingActivity.this, CreateMetingActivity.class);
                intentMeting.putExtra("userId",userId);
                intentMeting.putExtra("urlCover",urlCover);
                intentMeting.putExtra("courses",courses);
                intentMeting.putExtra("classId",classId);
                startActivity(intentMeting);
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection), Toast.LENGTH_SHORT).show();
            }
        });

        if (idUser.equals(userId)){
            fabCreate.setVisibility(View.VISIBLE);
        }
    }

    private void metingClass(String classId, FirebaseDatabase database) {
        if (classId != null) {
            progressBar.setVisibility(View.VISIBLE);

            DatabaseReference databaseReference;
            databaseReference = database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting));
            databaseReference.keepSynced(true);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            progressBar.setVisibility(View.GONE);
                        }
                    }else {
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
        getMenuInflater().inflate(R.menu.main_meeting, menu);
        MenuItem itemMember = menu.findItem(R.id.action_class_members);
        itemMember.setOnMenuItemClickListener(menuItem -> {

           return true;
        });
        MenuItem itemLeave = menu.findItem(R.id.action_leave_class);
        itemLeave.setOnMenuItemClickListener(menuItem -> {
            String myId = user.getUid();
            if (userId.equals(myId)){
                Toast.makeText(MetingActivity.this,getString(R.string.delete_exit_class),Toast.LENGTH_SHORT).show();
            } else {
                loadingProgress.startLoadingProgress();
                database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.name_class_member)).child(myId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        loadingProgress.dismissLoadingProgress();
                        Intent intent = new Intent(MetingActivity.this, MainNavActivity.class);
                        Toast.makeText(MetingActivity.this,getString(R.string.delete_and_left_class),Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                        finish();
                    } else {
                        loadingProgress.dismissLoadingProgress();
                        Toast.makeText(MetingActivity.this,getString(R.string.delete_class_failed),Toast.LENGTH_SHORT).show();
                    }
                });
            }
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