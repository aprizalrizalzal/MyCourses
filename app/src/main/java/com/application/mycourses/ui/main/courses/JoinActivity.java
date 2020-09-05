package com.application.mycourses.ui.main.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinActivity extends AppCompatActivity {

    private Button btnJoin;
    private CircleImageView imgView;
    private EditText edtClassId,edtUni,edtFac,edtStud,edtSem,edtCour;
    private String classId;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        imgView =findViewById(R.id.img_view);
        edtClassId = findViewById(R.id.edtClassId);
        edtUni = findViewById(R.id.edtUniversity);
        edtFac = findViewById(R.id.edtFaculty);
        edtStud = findViewById(R.id.edtStudy);
        edtSem = findViewById(R.id.edtSemester);
        edtCour = findViewById(R.id.edtCourses);
        btnJoin = findViewById(R.id.btnJoin);

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.join));

        loadingProgress= new LoadingProgress(this);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(view -> {
            if (haveConnection()){
                if (firebaseUser != null) {
                    searchClass(firebaseUser,database);
                }
            } else {
                Snackbar.make(btnSearch, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                        .setAction(getString(R.string.retry),viewRetry -> searchClass(firebaseUser,database))
                        .show();
            }
        });

    }

    private void searchClass(FirebaseUser firebaseUser,FirebaseDatabase database) {
        if (!validateClassId()){
            return;
        }
        classId = edtClassId.getText().toString();
        database.getReference(getString(R.string.name_class_list)).child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ModelHome modelHome = snapshot.getValue(ModelHome.class);
                    if (modelHome !=null && modelHome.getClassId().equals(classId)){
                        Toast.makeText(JoinActivity.this, getString(R.string.id_class_found), Toast.LENGTH_SHORT).show();
                        String idClass = modelHome.getClassId();
                        String idUser = modelHome.getUserId();
                        idClassFound(firebaseUser,database,idUser,idClass);
                    }
                } else {
                    Toast.makeText(JoinActivity.this, getString(R.string.id_class_not_found), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void idClassFound(FirebaseUser firebaseUser, FirebaseDatabase database, String idUser, String idClass) {
        database.getReference(getString(R.string.name_class)).child(idUser).child(idClass).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelHome modelHome = snapshot.getValue(ModelHome.class);
                if (modelHome != null) {
                    if (modelHome.getUrlCover().equals("urlPicture")){
                        Glide.with(getApplication())
                                .load(R.mipmap.ic_launcher_round)
                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                .into(imgView);
                    }else {
                        Glide.with(getApplication())
                                .load(modelHome.getUrlCover())
                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                .into(imgView);
                    }
                    edtUni.setText(modelHome.getUniversity());
                    edtUni.setInputType(InputType.TYPE_NULL);
                    edtUni.setEnabled(true);
                    edtFac.setText(modelHome.getFaculty());
                    edtFac.setInputType(InputType.TYPE_NULL);
                    edtFac.setEnabled(true);
                    edtStud.setText(modelHome.getStudy());
                    edtStud.setInputType(InputType.TYPE_NULL);
                    edtStud.setEnabled(true);
                    edtSem.setText(modelHome.getSemester());
                    edtSem.setInputType(InputType.TYPE_NULL);
                    edtSem.setEnabled(true);
                    edtCour.setText(modelHome.getCourses());
                    edtCour.setInputType(InputType.TYPE_NULL);
                    edtCour.setEnabled(true);

                    btnJoin.setEnabled(true);
                    btnJoin.setOnClickListener(view -> {
                        if (haveConnection()){
                            joinClass(firebaseUser,idUser,idClass,database,modelHome.getUrlCover());
                        } else {
                            Snackbar.make(btnJoin, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                                    .setAction(getString(R.string.retry),viewRetry -> searchClass(firebaseUser,database))
                                    .show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void joinClass(FirebaseUser firebaseUser, String idUser, String idClass, FirebaseDatabase database, String urlCover) {
        loadingProgress.startLoadingProgress();

        classId = edtClassId.getText().toString();
        String saveCurrencyDate, saveCurrencyTime;
        String university = edtUni.getText().toString().toUpperCase();
        String faculty = edtFac.getText().toString().toUpperCase();
        String study = edtStud.getText().toString().toUpperCase();
        String semester = edtSem.getText().toString();
        String courses = edtCour.getText().toString().toUpperCase();

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
        saveCurrencyDate = dateFormat.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        saveCurrencyTime = timeFormat.format(calendar.getTime());

        String dateJoin = String.format("%s at %s",saveCurrencyDate,saveCurrencyTime);

        Map<String, Object> map = new HashMap<>();
        map.put("courses", courses);
        map.put("dateJoin", dateJoin);
        map.put("faculty", faculty);
        map.put("classId",classId);
        map.put("semester", semester);
        map.put("study", study);
        map.put("university", university);
        map.put("urlCover",urlCover);
        map.put("userId",idUser);

        String userId = firebaseUser.getUid();
        database.getReference(getString(R.string.name_class)).child(userId).child(idClass).setValue(map).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                Toast.makeText(JoinActivity.this, R.string.join_class,Toast.LENGTH_SHORT).show();
                loadingProgress.dismissLoadingProgress();
                startActivity(new Intent(JoinActivity.this, MainNavActivity.class));
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                finish();
            }else {
                Toast.makeText(JoinActivity.this,getText(R.string.join_failed),Toast.LENGTH_SHORT).show();
                loadingProgress.dismissLoadingProgress();
            }
        });
    }

    private boolean haveConnection(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean validateClassId(){
        classId = edtClassId.getText().toString();
        if (classId.isEmpty()){
            edtClassId.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtClassId.setError(null);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
        finish();
    }
}