package com.application.mycourses.ui.main.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateCoursesActivity extends AppCompatActivity {

    private EditText edtUni,edtFac,edtStud,edtSem,edtCour;
    private String userId,university,faculty,study,semester,courses;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_create);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

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

        /*MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        loadingProgress= new LoadingProgress(this);

        TextView run = findViewById(R.id.tvRun);
        run.setText(getString(R.string.create_class));
        run.setSelected(true);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                if (user != null) {
                    createClass(user,firestore,database);
                }
            } else {
                Snackbar.make(btnSave, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                        .setAction(getString(R.string.retry),viewRetry -> createClass(user, firestore, database))
                        .show();
            }
        });
    }

    private void createClass(FirebaseUser user, FirebaseFirestore firestore, FirebaseDatabase database) {
        if (haveConnection()) {
            if (!validateUni() || !validateFac() || !validateStud() || !validateSem() || !validateCour()) {
                return;
            }
            loadingProgress.startLoadingProgress();

            userId = user.getUid();
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

            String dateCreated = String.format("%s at %s", saveCurrencyDate, saveCurrencyTime);
            String idClass = UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> map = new HashMap<>();
            map.put("classId", idClass);
            map.put("courses", courses);
            map.put("dateCreated", dateCreated);
            map.put("faculty", faculty);
            map.put("semester", semester);
            map.put("study", study);
            map.put("university", university);
            map.put("urlCover", "urlCover");
            map.put("userId", userId);
            database.getReference(getString(R.string.name_class)).child(idClass).setValue(map).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    Map<String, Object> mapClass = new HashMap<>();
                    mapClass.put("userId", userId);
                    database.getReference(getString(R.string.name_class)).child(idClass).child(getString(R.string.name_class_member)).child(userId).updateChildren(mapClass).addOnCompleteListener(this, taskClass -> {
                        if (taskClass.isSuccessful()){
                            loadingProgress.dismissLoadingProgress();
                            Toast.makeText(CreateCoursesActivity.this, R.string.data_update, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateCoursesActivity.this, MainNavActivity.class));
                            overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
                            finish();
                        }else {
                            Toast.makeText(CreateCoursesActivity.this, getText(R.string.update_failed), Toast.LENGTH_SHORT).show();
                            loadingProgress.dismissLoadingProgress();
                        }
                    });
                }else {
                    Toast.makeText(CreateCoursesActivity.this, getText(R.string.update_failed), Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateCoursesActivity.this);
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