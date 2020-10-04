package com.application.mycourses.ui.main.edit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditCoursesActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private CircleImageView imgAppBar,imgCover;
    private TextView appBar;
    private StorageTask<UploadTask.TaskSnapshot> taskUpload;
    private Uri uriImage;
    private static final int IMAGE_REQUEST = 1;
    private static final int IMAGE_REQUEST_INTENT = 2;
    private EditText edtUni,edtFac,edtStud,edtSem,edtCour;
    private String university,faculty,study,semester,courses;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference(getString(R.string.name_class));

        imgAppBar = findViewById(R.id.imgAppBar);
        appBar = findViewById(R.id.tvAppBar);

        imgCover=findViewById(R.id.imgCover);
        ImageButton btnAddCover = findViewById(R.id.btnAddCover);
        edtUni = findViewById(R.id.edtUniversity);
        edtFac = findViewById(R.id.edtFaculty);
        edtStud = findViewById(R.id.edtStudy);
        edtSem = findViewById(R.id.edtSemester);
        edtCour = findViewById(R.id.edtCourses);

        Intent intent = getIntent();
        String idClass = intent.getStringExtra("classId");
        String courses = intent.getStringExtra("courses");
        String urlCover = intent.getStringExtra("urlCover");
        String university = intent.getStringExtra("university");
        String faculty = intent.getStringExtra("faculty");
        String study = intent.getStringExtra("study");
        String semester = intent.getStringExtra("semester");

        if (urlCover != null) {
            readItems(urlCover,university,faculty,study,semester,courses);
        }

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

        btnAddCover.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditCoursesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                ActivityCompat.requestPermissions(EditCoursesActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST);
            }
        });

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                if (!validateUni()||!validateFac()||!validateStud()||!validateSem()||!validateCour()){
                    return;
                }
                if (firebaseUser != null) {
                    editClass(database,idClass,urlCover);
                }
            }
        });
    }

    private void readItems(String urlCover, String university, String faculty, String study, String semester, String courses) {
        appBar.setText(courses);
        if (urlCover.equals("urlPicture")){
            Glide.with(getApplication())
                    .load(R.mipmap.ic_launcher_round)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                    .into(imgAppBar);
            Glide.with(getApplication())
                    .load(R.mipmap.ic_launcher_round)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                    .into(imgCover);
        }else {
            Glide.with(getApplication())
                    .load(urlCover)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                    .into(imgAppBar);
            Glide.with(getApplication())
                    .load(urlCover)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                    .into(imgCover);
        }
        edtUni.setText(university);
        edtFac.setText(faculty);
        edtStud.setText(study);
        edtSem.setText(semester);
        edtCour.setText(courses);
    }

    private void editClass(FirebaseDatabase database, String idClass, String urlCover) {

        loadingProgress.startLoadingProgress();

        String saveCurrencyDate, saveCurrencyTime;
        university = edtUni.getText().toString().toUpperCase();
        faculty = edtFac.getText().toString().toUpperCase();
        study = edtStud.getText().toString().toUpperCase();
        courses = edtCour.getText().toString().toUpperCase();
        semester = edtSem.getText().toString();

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
        saveCurrencyDate = dateFormat.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        saveCurrencyTime = timeFormat.format(calendar.getTime());

        String lastUpdate = String.format("%s at %s",saveCurrencyDate,saveCurrencyTime);

        Map<String, Object> map = new HashMap<>();
        map.put("courses",courses);
        map.put("faculty",faculty);
        map.put("lastUpdate", lastUpdate);
        map.put("semester",semester);
        map.put("study",study);
        map.put("university",university);
        map.put("urlCover",urlCover);

        database.getReference(getString(R.string.name_class)).child(idClass).updateChildren(map).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                loadingProgress.dismissLoadingProgress();
                uploadImage(database,storageReference,idClass);
                database.getReference(getString(R.string.name_class_member)).child(idClass).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ModelHome modelHome = dataSnapshot.getValue(ModelHome.class);
                            if (modelHome !=null){
                                String userIdMember = modelHome.getUserId();
                                loadingProgress.dismissLoadingProgress();
                                memberClassUpdate(userIdMember,idClass,database,urlCover);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else {
                Toast.makeText(EditCoursesActivity.this,getText(R.string.update_failed),Toast.LENGTH_SHORT).show();
                loadingProgress.dismissLoadingProgress();
            }
        });
    }

    private void memberClassUpdate(String userIdMember, String idClass, FirebaseDatabase database, String urlCover) {

        loadingProgress.startLoadingProgress();
        Map<String, Object> map = new HashMap<>();
        map.put("courses",courses);
        map.put("faculty",faculty);
        map.put("semester",semester);
        map.put("study",study);
        map.put("university",university);
        map.put("urlCover",urlCover);

        database.getReference(getString(R.string.name_class)).child(userIdMember).child(idClass).updateChildren(map).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                loadingProgress.dismissLoadingProgress();
                Toast.makeText(EditCoursesActivity.this, R.string.data_update,Toast.LENGTH_SHORT).show();
            } else {
                loadingProgress.dismissLoadingProgress();
                Toast.makeText(EditCoursesActivity.this,getText(R.string.update_failed),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_INTENT);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(EditCoursesActivity.this, R.string.allow_permission_storage, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void uploadImage(FirebaseDatabase database, StorageReference storageReference, String idClass) {

        loadingProgress.startLoadingProgress();
        if (uriImage != null) {
            StorageReference fileReference = storageReference.child(idClass).child(idClass+"."+getFileExtension(uriImage));
            taskUpload = fileReference.putFile(uriImage);
            taskUpload.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String myUri = downloadUri.toString();

                    Map<String, Object> map = new HashMap<>();
                    map.put("urlCover", myUri);

                    database.getReference(getString(R.string.name_class)).child(idClass).updateChildren(map).addOnCompleteListener(this, taskUpdate -> {
                        if (task.isSuccessful()){
                            loadingProgress.dismissLoadingProgress();
                            Toast.makeText(EditCoursesActivity.this, R.string.update_picture, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditCoursesActivity.this, MainNavActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
                            finish();
                        }else {
                            loadingProgress.dismissLoadingProgress();
                            Toast.makeText(EditCoursesActivity.this, R.string.update_picture_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(EditCoursesActivity.this, R.string.update_picture_failed, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            loadingProgress.dismissLoadingProgress();
            Snackbar.make(imgCover, getString(R.string.no_image_upload), BaseTransientBottomBar.LENGTH_SHORT).setAction(getString(R.string.yes), null).show();
            startActivity(new Intent(EditCoursesActivity.this, MainNavActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_INTENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();

            if (taskUpload != null && taskUpload.isInProgress()) {
                Toast.makeText(getApplicationContext(), R.string.upload_in_process, Toast.LENGTH_SHORT).show();
            } else {
                imgCover.setImageURI(uriImage);
            }
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditCoursesActivity.this);
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
    }

    private boolean validateCour(){
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
        startActivity(new Intent(EditCoursesActivity.this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();

    }
}