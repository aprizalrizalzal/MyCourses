package com.application.mycourses.ui.home.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateMetingActivity extends AppCompatActivity {

    private Spinner spinnerMeting;
    private EditText edtInfo, edtAttachDoc, edtAttachAudio;
    private String spinMeting,info,doc,audio;
    private String userId, urlCover, courses, classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meting);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        urlCover = intent.getStringExtra("urlCover");
        courses = intent.getStringExtra("courses");
        classId = intent.getStringExtra("classId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerMeting = findViewById(R.id.spinnerMeeting);
        edtInfo = findViewById(R.id.edtInfo);
        edtAttachDoc = findViewById(R.id.edtAttachDoc);
        edtAttachAudio = findViewById(R.id.edtAttachAudio);
        Button btnSave = findViewById(R.id.btnSave);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

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

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        btnSave.setOnClickListener(view -> saveMeting(user,userId,classId,database));

    }

    private void saveMeting(FirebaseUser user, String userId, String classId, FirebaseDatabase database) {
        if (!validSpin()){
            return;
        }
        spinMeting = spinnerMeting.getSelectedItem().toString();
        database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(spinMeting).exists()){
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.update_meting_failed,spinMeting),Toast.LENGTH_SHORT).show();
                }else {
                    createMeting(user,userId,classId,database);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createMeting(FirebaseUser user, String userId, String classId, FirebaseDatabase database) {
        if (!validInfo() || !validDoc() || !validAudio()){
            return;
        }

        spinMeting = spinnerMeting.getSelectedItem().toString();
        info = edtInfo.getText().toString();
        doc = edtAttachDoc.getText().toString();
        audio = edtAttachAudio.getText().toString();

        String idUser = user.getUid();
        if (idUser.equals(userId)){
            Map<String,Object> mapMeting = new HashMap<>();
            mapMeting.put("urlCover","urlCover");
            mapMeting.put("meting",spinMeting);
            mapMeting.put("information",info);
            mapMeting.put("document",doc);
            mapMeting.put("audio",audio);

            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).setValue(mapMeting).addOnCompleteListener(this, task -> {
                Intent intent = new Intent(this, MetingActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("urlCover",urlCover);
                intent.putExtra("courses",courses);
                intent.putExtra("classId",classId);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                finish();
            }).addOnFailureListener(this, e -> Toast.makeText(CreateMetingActivity.this, getString(R.string.update_failed),Toast.LENGTH_SHORT).show());
        }
    }

    private boolean validSpin(){
        spinMeting = spinnerMeting.getSelectedItem().toString();
        if (spinMeting.equals(getString(R.string.select))){
            Toast.makeText(this, getString(R.string.select_meting),Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validInfo(){
        info = edtInfo.getText().toString();
        if (TextUtils.isEmpty(info)){
            edtInfo.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtInfo.setError(null);
            return true;
        }
    }

    private boolean validDoc(){
        doc = edtAttachDoc.getText().toString();
        if (TextUtils.isEmpty(doc)){
            edtAttachDoc.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtAttachDoc.setError(null);
            return true;
        }
    }

    private boolean validAudio(){
        audio = edtAttachAudio.getText().toString();
        if (TextUtils.isEmpty(audio)){
            edtAttachAudio.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtAttachAudio.setError(null);
            return true;
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateMetingActivity.this);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MetingActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("urlCover",urlCover);
        intent.putExtra("courses",courses);
        intent.putExtra("classId",classId);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();
    }
}