package com.application.mycourses.ui.main.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinCoursesActivity extends AppCompatActivity {

    private Button btnJoin;
    private CircleImageView imgView;
    private LinearLayout result;
    private EditText edtClassId;
    private TextView tvUni,tvFac,tvStud,tvSem,tvCour;
    private String idClass;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_join);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        TextView run = findViewById(R.id.tvRun);
        run.setText(getString(R.string.join_id_class));
        run.setSelected(true);

        imgView =findViewById(R.id.img_view);
        edtClassId = findViewById(R.id.edtClassId);
        result = findViewById(R.id.result);
        tvUni = findViewById(R.id.tvUniversity);
        tvFac = findViewById(R.id.tvFaculty);
        tvStud = findViewById(R.id.tvStudy);
        tvSem = findViewById(R.id.tvSemester);
        tvCour = findViewById(R.id.tvCourses);
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
                if (user != null) {
                    searchClass(user,firestore,database);
                }
            } else {
                Snackbar.make(btnSearch, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                        .setAction(getString(R.string.retry),viewRetry -> searchClass(user, firestore, database))
                        .show();
            }
        });

    }

    private void searchClass(FirebaseUser user, FirebaseFirestore firestore, FirebaseDatabase database) {
        if (!validateClassId()){
            return;
        }
        idClass = edtClassId.getText().toString();
        database.getReference(getString(R.string.name_class)).child(idClass).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ModelHome modelHome = snapshot.getValue(ModelHome.class);
                    String idUser = user.getUid();
                    if (modelHome !=null && modelHome.getClassId().equals(idClass)){
                        if (snapshot.child(getString(R.string.name_class_member)).child(idUser).exists()) {
                            Toast.makeText(JoinCoursesActivity.this, getString(R.string.your_id_class), Toast.LENGTH_SHORT).show();
                            btnJoin.setEnabled(false);
                        } else {
                            Toast.makeText(JoinCoursesActivity.this, getString(R.string.id_class_found), Toast.LENGTH_SHORT).show();
                            String idClass = modelHome.getClassId();
                            btnJoin.setEnabled(true);
                            idClassFound(user,firestore,database,idClass);
                        }
                    }
                } else {
                    Toast.makeText(JoinCoursesActivity.this, getString(R.string.id_class_not_found), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void idClassFound(FirebaseUser user, FirebaseFirestore firestore, FirebaseDatabase database, String idClass) {
        database.getReference(getString(R.string.name_class)).child(idClass).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelHome modelHome = snapshot.getValue(ModelHome.class);
                if (modelHome != null) {
                    if (modelHome.getUrlCover().equals("")){
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

                    result.setVisibility(View.VISIBLE);
                    tvUni.setText(String.format("%s : %s",getString(R.string.university),modelHome.getUniversity()));
                    tvFac.setText(String.format("%s : %s",getString(R.string.faculty),modelHome.getFaculty()));
                    tvSem.setText(String.format("%s : %s",getString(R.string.semester),modelHome.getSemester()));
                    tvStud.setText(String.format("%s : %s",getString(R.string.study_program),modelHome.getStudy()));
                    tvCour.setText(String.format("%s : %s",getString(R.string.courses),modelHome.getCourses()));

                    btnJoin.setOnClickListener(view -> {
                        if (haveConnection()){
                            joinClass(user,idClass, firestore, database);
                        } else {
                            Snackbar.make(btnJoin, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_INDEFINITE )
                                    .setAction(getString(R.string.retry),viewRetry -> searchClass(user, firestore, database))
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

    private void joinClass(FirebaseUser user, String idClass, FirebaseFirestore firestore, FirebaseDatabase database) {

        loadingProgress.startLoadingProgress();
        String userId = user.getUid();
        Map<String, Object> mapClass = new HashMap<>();
        mapClass.put("userId", userId);
        database.getReference(getString(R.string.name_class)).child(idClass).child(getString(R.string.name_class_member)).child(userId).updateChildren(mapClass).addOnCompleteListener(this, taskClass -> {
            if (taskClass.isSuccessful()){
                loadingProgress.dismissLoadingProgress();
                Toast.makeText(JoinCoursesActivity.this, R.string.join_class,Toast.LENGTH_SHORT).show();
                startActivity(new Intent(JoinCoursesActivity.this, MainNavActivity.class));
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                finish();
            }else {
                Toast.makeText(JoinCoursesActivity.this,getText(R.string.join_failed),Toast.LENGTH_SHORT).show();
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
        idClass = edtClassId.getText().toString();
        if (idClass.isEmpty()){
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