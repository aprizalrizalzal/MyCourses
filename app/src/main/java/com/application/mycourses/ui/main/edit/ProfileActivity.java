package com.application.mycourses.ui.main.edit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.model.ModelUser;
import com.application.mycourses.sign.SignInActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private SwipeRefreshLayout refreshLayout;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private StorageTask<UploadTask.TaskSnapshot> taskUpload;
    private CircleImageView imgAppBar,imgCover;
    private EditText edtUserName,edtGender,edtBirth,edtNumberPhone,edtEmail;
    private String userId,userName,gender,birth,numberPhone;
    private Uri uriImage;
    private static final int IMAGE_REQUEST = 1;
    private static final int IMAGE_REQUEST_INTENT = 2;
    private ImageButton btnAddCover,btnSaveCover;
    private Button btnVerify,btnEdit,btnSave;
    private AlertDialog.Builder dialogCalendarPicker;
    private LoadingProgress loadingProgress;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference(getString(R.string.app_name));

        imgAppBar = findViewById(R.id.imgAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.edit_profile));

        refreshLayout=findViewById(R.id.swipeRefresh);
        dialogCalendarPicker = new AlertDialog.Builder(ProfileActivity.this);
        loadingProgress = new LoadingProgress(ProfileActivity.this);

        imgCover= findViewById(R.id.imgCover);
        btnAddCover=findViewById(R.id.btnAddCover);
        btnSaveCover=findViewById(R.id.btnSaveCover);
        edtUserName = findViewById(R.id.edtUserName);
        edtGender = findViewById(R.id.edtGender);
        edtBirth = findViewById(R.id.edtBirth);
        edtNumberPhone = findViewById(R.id.edtNumberPhone);
        edtEmail = findViewById(R.id.edtEmail);

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

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        readProfile(firebaseUser,firebaseFirestore);

        refreshLayout.setOnRefreshListener(() -> {
            readProfile(firebaseUser,firebaseFirestore);
            refreshLayout.setRefreshing(false);
        });

        btnAddCover.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST);
            }
        });

        btnSaveCover.setOnClickListener(view -> uploadImage(firebaseUser,storageReference,firebaseFirestore));

        btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(view -> {
            if (firebaseUser != null) {
                loadingProgress.startLoadingProgress();
                firebaseUser.sendEmailVerification().addOnSuccessListener(this, aVoid -> {
                    Toast.makeText(this,getString(R.string.send_verify,firebaseUser.getEmail()),Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                    loadingProgress.dismissLoadingProgress();
                    finish();
                }).addOnFailureListener(this, e -> {
                    Toast.makeText(this,getString(R.string.send_verify_failed,firebaseUser.getEmail()),Toast.LENGTH_SHORT).show();
                    loadingProgress.dismissLoadingProgress();
                });
            }
        });

        btnEdit = findViewById(R.id.btnUpdate);
        btnEdit.setOnClickListener(view -> {
            edtUserName.setEnabled(true);
            edtGender.setEnabled(true);
            edtBirth.setEnabled(true);
            edtNumberPhone.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        });

        edtGender.setInputType(InputType.TYPE_NULL);
        edtGender.setOnClickListener(view -> {
            CharSequence[] items = {"Male","Female"};
            dialogCalendarPicker.setItems(items, (dialogInterface, i) -> edtGender.setText(items[i]));
            AlertDialog alertDialog = dialogCalendarPicker.create();
            alertDialog.show();
        });

        edtBirth.setInputType(InputType.TYPE_NULL);
        edtBirth.setOnClickListener(view -> {
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (viewCalendar, yearOfCalendar, monthOfYear, dayOfMonth) -> edtBirth.setText(dayOfMonth +"/"+ (monthOfYear+1) +"/"+ yearOfCalendar), year, month, day);
        datePickerDialog.show();
        });

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                if (!validUserName()||!validGender()||!validBirth()||!validNumberPhone()){
                    return;
                }
                if (firebaseUser != null) {
                    saveProfile(firebaseUser,firebaseFirestore);
                }
            }else {
                Toast.makeText(ProfileActivity.this,getText(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            Map<String, Object> map = new HashMap<>();
            map.put("emailVerify", true);
            firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map);
        }
    }

    private void readProfile(FirebaseUser firebaseUser, FirebaseFirestore firebaseFirestore) {
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            DocumentReference documentReference = firebaseFirestore.collection(getString(R.string.app_name)).document(userId);
            documentReference.addSnapshotListener((value, error) -> {
                if (value !=null && value.exists()){
                    ModelUser modelUser = value.toObject(ModelUser.class);
                    assert modelUser != null;

                    if (modelUser.getUrlPicture().equals("urlPicture")){
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
                                .load(modelUser.getUrlPicture())
                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                .into(imgAppBar);
                        Glide.with(getApplication())
                                .load(modelUser.getUrlPicture())
                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                .into(imgCover);
                    }

                    edtUserName.setText(modelUser.getUserName());
                    edtGender.setText(modelUser.getGender());
                    edtBirth.setText(modelUser.getBirth());
                    edtNumberPhone.setText(modelUser.getNumberPhone());

                    if (modelUser.getEmailVerify().equals(false)){
                        edtEmail.setText(getString(R.string.not_email_verified));
                    }else {
                        btnEdit.setVisibility(View.VISIBLE);
                        btnVerify.setVisibility(View.GONE);
                        edtEmail.setText(modelUser.getEmail());
                    }
                    btnSave.setVisibility(View.GONE);
                    edtUserName.setEnabled(false);
                    edtGender.setEnabled(false);
                    edtBirth.setEnabled(false);
                    edtNumberPhone.setEnabled(false);
                }
            });
        }
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
            Toast.makeText(ProfileActivity.this, R.string.allow_permission_storage, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(FirebaseUser firebaseUser, StorageReference storageReference, FirebaseFirestore firebaseFirestore) {
        loadingProgress.startLoadingProgress();
        if (uriImage != null) {
            userId = firebaseUser.getUid();
            StorageReference fileReference = storageReference.child("urlPicture").child(userId + "." + getFileExtension(uriImage));
            taskUpload = fileReference.putFile(uriImage);
            taskUpload.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(this, task -> {

                loadingProgress.dismissLoadingProgress();
                Uri downloadUri = task.getResult();
                assert downloadUri != null;
                String myUri = downloadUri.toString();

                Map<String, Object> map = new HashMap<>();
                map.put("urlPicture", myUri);

                firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map);
                Toast.makeText(ProfileActivity.this, R.string.update_picture, Toast.LENGTH_SHORT).show();

                btnAddCover.setVisibility(View.VISIBLE);
                btnSaveCover.setVisibility(View.GONE);

            }).addOnFailureListener(e -> {
                loadingProgress.dismissLoadingProgress();
                Toast.makeText(getApplicationContext(), R.string.update_picture_failed, Toast.LENGTH_SHORT).show();
                btnAddCover.setVisibility(View.GONE);
            });

        } else {
            loadingProgress.dismissLoadingProgress();
            Toast.makeText(getApplicationContext(), R.string.no_image_upload, Toast.LENGTH_SHORT).show();
            btnAddCover.setVisibility(View.GONE);

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
                btnAddCover.setVisibility(View.GONE);
                btnSaveCover.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveProfile(FirebaseUser firebaseUser, FirebaseFirestore firebaseFirestore) {

        loadingProgress.startLoadingProgress();

        edtUserName.setEnabled(false);
        edtGender.setEnabled(false);
        edtBirth.setEnabled(false);
        edtNumberPhone.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);

        userName = edtUserName.getText().toString();
        gender= edtGender.getText().toString();
        birth = edtBirth.getText().toString();
        numberPhone = edtNumberPhone.getText().toString();

        Map<String,Object> map = new HashMap<>();
        map.put("userName",userName);
        map.put("gender",gender);
        map.put("birth",birth);
        map.put("numberPhone",numberPhone);

        userId=firebaseUser.getUid();
        firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map).addOnCompleteListener(this, task -> {
            Toast.makeText(ProfileActivity.this, R.string.data_update,Toast.LENGTH_SHORT).show();
            loadingProgress.dismissLoadingProgress();
        }).addOnFailureListener(this, e -> {
            Toast.makeText(ProfileActivity.this,getText(R.string.update_failed),Toast.LENGTH_SHORT).show();
            loadingProgress.dismissLoadingProgress();
        });

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
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this);
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
            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
            alertDialog.show();
            return true;
        });

        return true;
    }

    private boolean haveConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean validUserName() {
        userName = edtUserName.getText().toString();
        if (userName.isEmpty()) {
            edtUserName.setError(getText(R.string.field_not_empty));
            return false;
        } else {
            edtUserName.setError(null);
            return true;
        }
    }

    private boolean validGender() {
        gender = edtGender.getText().toString();
        if (gender.isEmpty()) {
            edtGender.setError(getText(R.string.field_not_empty));
            return false;
        } else {
            edtGender.setError(null);
            return true;
        }
    }

    private boolean validBirth() {
        birth = edtBirth.getText().toString();
        if (birth.isEmpty()) {
            edtBirth.setError(getText(R.string.field_not_empty));
            return false;
        } else {
            edtBirth.setError(null);
            return true;
        }
    }

    private boolean validNumberPhone() {
        numberPhone = edtNumberPhone.getText().toString();
        if (numberPhone.isEmpty()) {
            edtNumberPhone.setError(getText(R.string.field_not_empty));
            return false;
        } else {
            edtNumberPhone.setError(null);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();

    }
}