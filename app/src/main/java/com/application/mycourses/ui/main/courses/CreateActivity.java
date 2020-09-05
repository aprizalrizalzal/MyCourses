package com.application.mycourses.ui.main.courses;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateActivity extends AppCompatActivity {

    private EditText edtUni,edtFac,edtStud,edtSem,edtCour;
    private String university,faculty,study,semester,courses;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.create));

        edtUni = findViewById(R.id.edtUniversity);
        edtFac = findViewById(R.id.edtFaculty);
        edtStud = findViewById(R.id.edtStudy);
        edtSem = findViewById(R.id.edtSemester);
        edtCour = findViewById(R.id.edtCourses);

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadingProgress= new LoadingProgress(this);

        Calendar welcome = Calendar.getInstance();
        int timeOfDay = welcome.get(Calendar.HOUR_OF_DAY);

        TextView run = findViewById(R.id.tvRun);
        if (timeOfDay >=0 && timeOfDay <12){
            run.setText(getString(R.string.welcome,getString(R.string.morning)));
            run.setSelected(true);
        } else if (timeOfDay >=12 && timeOfDay <18){
            run.setText(getString(R.string.welcome,getString(R.string.afternoon)));
            run.setSelected(true);
        }else if (timeOfDay >=18 && timeOfDay <24) {
            run.setText(getString(R.string.welcome, getString(R.string.night)));
            run.setSelected(true);
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                if (firebaseUser != null) {
                    createClass(firebaseUser,database);
                }
            } else {
                Snackbar.make(btnSave, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                        .setAction(getString(R.string.retry),viewRetry -> createClass(firebaseUser,database))
                        .show();
            }
        });
    }

    private void createClass(FirebaseUser firebaseUser, FirebaseDatabase database) {
        if (haveConnection()){
            if (!validateUni()||!validateFac()||!validateStud()||!validateSem()||!validateCour()){
                return;
            }
            loadingProgress.startLoadingProgress();

            String userId = firebaseUser.getUid();
            String saveCurrencyDate, saveCurrencyTime;
            university = edtUni.getText().toString().toUpperCase();
            faculty = edtFac.getText().toString().toUpperCase();
            study = edtStud.getText().toString().toUpperCase();
            semester = edtSem.getText().toString();
            courses = edtCour.getText().toString().toUpperCase();

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
            saveCurrencyDate = dateFormat.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            saveCurrencyTime = timeFormat.format(calendar.getTime());

            String dateCreated = String.format("%s at %s",saveCurrencyDate,saveCurrencyTime);
            String uniqueID = UUID.randomUUID().toString().substring(0,8);

            Map<String, Object> map = new HashMap<>();
            map.put("classId",uniqueID);
            map.put("courses",courses);
            map.put("dateCreated",dateCreated);
            map.put("faculty",faculty);
            map.put("semester",semester);
            map.put("study",study);
            map.put("university",university);
            map.put("urlCover","urlCover");
            map.put("userId",userId);

            database.getReference(getString(R.string.name_class)).child(userId).child(uniqueID).setValue(map).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){

                    Map<String, Object> mapClass = new HashMap<>();
                    mapClass.put("classId",uniqueID);
                    mapClass.put("userId",userId);

                    database.getReference(getString(R.string.name_class_list)).child(uniqueID).setValue(mapClass).addOnCompleteListener(this, taskClass -> {
                        if (taskClass.isSuccessful()){
                            Toast.makeText(CreateActivity.this, R.string.data_update,Toast.LENGTH_SHORT).show();
                            loadingProgress.dismissLoadingProgress();
                            startActivity(new Intent(CreateActivity.this, MainNavActivity.class));
                            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                            finish();
                        }else {
                            Toast.makeText(CreateActivity.this,getText(R.string.update_failed),Toast.LENGTH_SHORT).show();
                            loadingProgress.dismissLoadingProgress();
                        }
                            });
                }else {
                    Toast.makeText(CreateActivity.this,getText(R.string.update_failed),Toast.LENGTH_SHORT).show();
                    loadingProgress.dismissLoadingProgress();
                }
            });
        }else {
            Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateActivity.this);
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
                            startActivity(emailIntent);
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

    private boolean validateUni(){
        university = edtUni.getText().toString().toUpperCase();
        if (university.isEmpty()){
            edtUni.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtUni.setError(null);
            return true;
        }
    }

    private boolean validateFac(){
        faculty = edtFac.getText().toString().toUpperCase();
        if (faculty.isEmpty()){
            edtFac.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtFac.setError(null);
            return true;
        }
    }private boolean validateStud(){
        study = edtStud.getText().toString().toUpperCase();
        if (study.isEmpty()){
            edtStud.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtStud.setError(null);
            return true;
        }
    }private boolean validateSem(){
        semester = edtSem.getText().toString();
        if (semester.isEmpty()){
            edtSem.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtSem.setError(null);
            return true;
        }
    }private boolean validateCour(){
        courses = edtCour.getText().toString().toUpperCase();
        if (courses.isEmpty()){
            edtCour.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtCour.setError(null);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();
    }
}