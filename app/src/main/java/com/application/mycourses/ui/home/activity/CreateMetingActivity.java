package com.application.mycourses.ui.home.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.application.mycourses.model.ModelMeting;
import com.application.mycourses.ui.utils.LoadingProgress;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateMetingActivity extends AppCompatActivity {

    private CircleImageView imgCreateCover, imgCoverDialog;
    private Spinner spinCheck, spinnerMeting;
    private LinearLayout labeled;
    private ImageButton btnAddCover;
    private EditText edtInfo, edtAttachDoc, edtAttachAudio;
    private Button btnCheck, btnUpdate, btnSave;
    private Dialog dialog;
    private TextView tvMeetingDialog,tvInfoDialog,tvDocDialog,tvAudioDialog,tvUpdateDialog,tvDeleteDialog;
    private String check, spinMeting,info,doc,audio;
    private String userId, urlCover, courses, classId;
    private FirebaseDatabase database;
    private Uri uriImage,uriDoc,uriAudio;
    private StorageTask<UploadTask.TaskSnapshot> taskUpload;
    private static final int IMAGE_REQUEST = 1;
    private static final int IMAGE_REQUEST_INTENT = 2;
    private static final int DOC_REQUEST = 3;
    private static final int DOC_REQUEST_INTENT = 4;
    private static final int AUDIO_REQUEST = 5;
    private static final int AUDIO_REQUEST_INTENT = 6;
    private LoadingProgress loadingProgress;
    private StorageReference storageReferenceCover,storageReferenceDoc,storageReferenceAudio;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meting);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        urlCover = intent.getStringExtra("urlCover");
        courses = intent.getStringExtra("courses");
        classId = intent.getStringExtra("classId");

        dialog = new Dialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinCheck = findViewById(R.id.spinnerCheck);
        labeled = findViewById(R.id.labeled);
        imgCreateCover = findViewById(R.id.img_view);
        spinnerMeting = findViewById(R.id.spinnerMeeting);
        edtInfo = findViewById(R.id.edtInfo);
        edtAttachDoc = findViewById(R.id.edtAttachDoc);
        edtAttachAudio = findViewById(R.id.edtAttachAudio);
        btnCheck = findViewById(R.id.btnCheck);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSave = findViewById(R.id.btnSave);
        btnAddCover = findViewById(R.id.btnAddCover);

        database = FirebaseDatabase.getInstance();
        storageReferenceCover = FirebaseStorage.getInstance().getReference("urlCover").child(getString(R.string.name_class)).child(classId);
        storageReferenceDoc = FirebaseStorage.getInstance().getReference("docPdf").child(getString(R.string.name_class)).child(classId);
        storageReferenceAudio = FirebaseStorage.getInstance().getReference("audioMp3").child(getString(R.string.name_class)).child(classId);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        loadingProgress = new LoadingProgress(this);

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

        spinCheck.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                check = spinCheck.getSelectedItem().toString().trim();
                if (check.equals(getString(R.string.select))){
                    labeled.setVisibility(View.GONE);
                    spinnerMeting.setEnabled(false);
                    text();
                    btnAddCover.setVisibility(View.INVISIBLE);
                    btnSave.setVisibility(View.GONE);
                    btnCheck.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.GONE);
                } else if (check.equals(getString(R.string.create_meeting))){
                    labeled.setVisibility(View.VISIBLE);
                    spinnerMeting.setEnabled(true);
                    text();
                    btnAddCover.setVisibility(View.VISIBLE);
                    btnSave.setVisibility(View.VISIBLE);
                    btnCheck.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.GONE);
                } else if (check.equals(getString(R.string.up_or_del))){
                    labeled.setVisibility(View.GONE);
                    spinnerMeting.setEnabled(true);
                    text();
                    btnAddCover.setVisibility(View.INVISIBLE);
                    btnSave.setVisibility(View.GONE);
                    btnCheck.setVisibility(View.VISIBLE);
                    btnUpdate.setVisibility(View.GONE);
                }
            }

            private void text() {
                edtInfo.setText(null);
                edtAttachDoc.setText(null);
                edtAttachAudio.setText(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnAddCover.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(CreateMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectImage();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(CreateMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST);
            }
        });

        edtAttachDoc.setInputType(InputType.TYPE_NULL);
        edtAttachDoc.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(CreateMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectDoc();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(CreateMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},DOC_REQUEST);
            }
        });

        edtAttachAudio.setInputType(InputType.TYPE_NULL);
        edtAttachAudio.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(CreateMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectAudio();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(CreateMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},AUDIO_REQUEST);
            }
        });

        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateMetingActivity.this);
                    alertDialogBuilder
                            .setTitle(R.string.save)
                            .setMessage(R.string.valid_data)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, (dialog, id) -> saveMeting(user,userId,classId,database))
                            .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                    alertDialog.show();
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT ).show();
            }
        });

        btnCheck.setOnClickListener(view -> {
            if (haveConnection()){
                spinMeting = spinnerMeting.getSelectedItem().toString().trim();
                if (!validSpin()){
                    return;
                }
                database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            dialog.setContentView(R.layout.dialog_update_delete);
                            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            tvMeetingDialog = dialog.findViewById(R.id.tv_meetingDialog);
                            imgCoverDialog = dialog.findViewById(R.id.img_viewDialog);
                            tvInfoDialog = dialog.findViewById(R.id.tv_infoDialog);
                            tvDocDialog = dialog.findViewById(R.id.tv_docDialog);
                            tvAudioDialog = dialog.findViewById(R.id.tv_audioDialog);
                            tvUpdateDialog = dialog.findViewById(R.id.tvUpdateDialog);
                            tvDeleteDialog = dialog.findViewById(R.id.tvDeleteDialog);

                            labeled.setVisibility(View.GONE);

                            ModelMeting modelMeting = snapshot.getValue(ModelMeting.class);
                            if (modelMeting !=null){
                                if (modelMeting.getUrlCover().equals(getString(R.string.urlCover))){
                                    Glide.with(CreateMetingActivity.this)
                                            .load(R.mipmap.ic_launcher_round)
                                            .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                            .into(imgCoverDialog);
                                } else {
                                    Glide.with(CreateMetingActivity.this)
                                            .load(modelMeting.getUrlCover())
                                            .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                            .into(imgCoverDialog);
                                }

                                tvMeetingDialog.setText(getString(R.string.metingDialog,modelMeting.getMeting()));
                                tvInfoDialog.setText(modelMeting.getInformation());
                                edtInfo.setText(modelMeting.getInformation());

                                if (modelMeting.getUrlDocument() !=null){
                                    tvDocDialog.setText(courses.toLowerCase()+"_"+modelMeting.getMeting()+".pdf");
                                    edtAttachDoc.setText(courses.toLowerCase()+"_"+modelMeting.getMeting()+".pdf");
                                } else if (modelMeting.getUrlAudio().equals(getString(R.string.urlAudio))){
                                    tvAudioDialog.setText(null);
                                    edtAttachAudio.setText(null);
                                } else {
                                    tvAudioDialog.setText(courses.toLowerCase()+"_"+modelMeting.getMeting()+".mp3");
                                    edtAttachAudio.setText(courses.toLowerCase()+"_"+modelMeting.getMeting()+".mp3");
                                }

                                tvUpdateDialog.setOnClickListener(viewUpdate -> {
                                    if (modelMeting.getUrlCover().equals(getString(R.string.urlCover))){
                                        Glide.with(CreateMetingActivity.this)
                                                .load(R.mipmap.ic_launcher_round)
                                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                                .into(imgCreateCover);
                                    } else {
                                        Glide.with(CreateMetingActivity.this)
                                                .load(modelMeting.getUrlCover())
                                                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                                .into(imgCreateCover);
                                    }
                                    labeled.setVisibility(View.VISIBLE);
                                    btnAddCover.setVisibility(View.VISIBLE);
                                    spinnerMeting.setEnabled(false);
                                    btnCheck.setVisibility(View.GONE);
                                    btnSave.setVisibility(View.GONE);
                                    btnUpdate.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                });

                                tvDeleteDialog.setOnClickListener(view -> {
                                    spinMeting = spinnerMeting.getSelectedItem().toString().trim();
                                    if (haveConnection()){
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateMetingActivity.this);
                                        alertDialogBuilder
                                                .setTitle(R.string.delete)
                                                .setMessage(getString(R.string.valid_data_delete,spinMeting))
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.yes, (dialog, id) -> {
                                                    loadingProgress.startLoadingProgress();
                                                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).removeValue().addOnCompleteListener(CreateMetingActivity.this, task -> {
                                                        if (task.isSuccessful()){
                                                            loadingProgress.dismissLoadingProgress();
                                                            backActivity();
                                                            Toast.makeText(CreateMetingActivity.this, getString(R.string.delete_meting,spinMeting),Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            loadingProgress.dismissLoadingProgress();
                                                            Toast.makeText(CreateMetingActivity.this, getString(R.string.delete_field,spinMeting),Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                })
                                                .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                                        alertDialog.show();
                                    } else {
                                        Toast.makeText(CreateMetingActivity.this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT ).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(CreateMetingActivity.this, getString(R.string.empty_meting,spinMeting),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT ).show();
            }
            dialog.show();
        });

        btnUpdate.setOnClickListener(view -> {
            if (haveConnection()){
                //updateMeting(user,userId,classId,database);
            }
        });
    }

    private void saveMeting(FirebaseUser user, String userId, String classId, FirebaseDatabase database) {
        if (!validSpin()){
            return;
        }
        loadingProgress.startLoadingProgress();
        spinMeting = spinnerMeting.getSelectedItem().toString();
        database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(spinMeting).exists()){
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.update_meting_failed,spinMeting),Toast.LENGTH_SHORT).show();
                }else {
                    loadingProgress.dismissLoadingProgress();
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
        loadingProgress.startLoadingProgress();
        spinMeting = spinnerMeting.getSelectedItem().toString();
        info = edtInfo.getText().toString();
        doc = edtAttachDoc.getText().toString();
        audio = edtAttachAudio.getText().toString();

        String idUser = user.getUid();
        if (idUser.equals(userId)){
            Map<String,Object> mapMeting = new HashMap<>();
            mapMeting.put("urlCover","urlCover");
            mapMeting.put("meting",spinMeting);
            mapMeting.put("idMeting",spinMeting+classId);
            mapMeting.put("information",info);
            mapMeting.put("urlDocument",doc);
            mapMeting.put("urlAudio",audio);

            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).setValue(mapMeting).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    uploadImage();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.update_failed),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*private void updateMeting(FirebaseUser user, String userId, String classId, FirebaseDatabase database) {
        if (!validInfo() || !validDoc() || !validAudio()){
            return;
        }
        loadingProgress.startLoadingProgress();
        spinMeting = spinnerMeting.getSelectedItem().toString();
        info = edtInfo.getText().toString();
        doc = edtAttachDoc.getText().toString();
        audio = edtAttachAudio.getText().toString();

        String idUser = user.getUid();
        if (idUser.equals(userId)){
            Map<String,Object> mapMeting = new HashMap<>();
            mapMeting.put("information",info);
            mapMeting.put("urlDocument",doc);
            mapMeting.put("urlAudio",audio);

            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).setValue(mapMeting).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    uploadImage();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.update_failed),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_REQUEST && grantResults [0] == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }else if (requestCode == DOC_REQUEST && grantResults [0] == PackageManager.PERMISSION_GRANTED){
            selectDoc();
        }else if (requestCode == AUDIO_REQUEST && grantResults [0] == PackageManager.PERMISSION_GRANTED){
            selectAudio();
        }else {
            Toast.makeText(CreateMetingActivity.this, getString(R.string.allow_permission_storage), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_INTENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            if (taskUpload != null && taskUpload.isInProgress()) {
                Toast.makeText(getApplicationContext(), getString(R.string.upload_in_process), Toast.LENGTH_SHORT).show();
            } else {
                imgCreateCover.setImageURI(uriImage);
            }
        }
        if (requestCode == DOC_REQUEST_INTENT && resultCode == RESULT_OK && data !=null && data.getData() != null){
            uriDoc=data.getData();
            if (taskUpload != null && taskUpload.isInProgress()) {
                Toast.makeText(getApplicationContext(), getString(R.string.upload_in_process), Toast.LENGTH_SHORT).show();
            } else {
                edtAttachDoc.setText(uriDoc.getLastPathSegment());
                edtAttachDoc.setEnabled(true);
            }
        }
        if (requestCode == AUDIO_REQUEST_INTENT && resultCode == RESULT_OK && data !=null && data.getData() != null){
            uriAudio=data.getData();
            if (taskUpload != null && taskUpload.isInProgress()) {
                Toast.makeText(getApplicationContext(), getString(R.string.upload_in_process), Toast.LENGTH_SHORT).show();
            } else {
                edtAttachAudio.setText(uriAudio.getLastPathSegment());
                edtAttachAudio.setEnabled(true);
            }
        }
    }

    private void uploadImage() {
        if (uriImage !=null) {
            spinMeting = spinnerMeting.getSelectedItem().toString();
            final StorageReference fileReference = storageReferenceCover.child(spinMeting+classId+"."+ getFileExtension(uriImage));
            taskUpload = fileReference.putFile(uriImage);
            taskUpload.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(uriTask -> {
                if (uriTask.isSuccessful()) {
                    Uri downloadUri = uriTask.getResult();
                    assert downloadUri != null;
                    String myUri = downloadUri.toString();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("urlCover", myUri);
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()){
                            uploadDoc();
                        }
                    });
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.create_picture), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.image_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("urlCover", "urlCover");
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).updateChildren(map).addOnCompleteListener(voidTask -> {
                if (voidTask.isSuccessful()) {
                    uploadDoc();
                    Toast.makeText(getApplicationContext(), getString(R.string.blank_image), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadDoc() {
        if (uriDoc != null) {
            spinMeting = spinnerMeting.getSelectedItem().toString();

            final StorageReference fileReference = storageReferenceDoc.child(spinMeting+classId+ "." + getFileExtension(uriDoc));
            taskUpload = fileReference.putFile(uriDoc);
            taskUpload.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(uriTask -> {
                if (uriTask.isSuccessful()) {
                    Uri downloadUri = uriTask.getResult();
                    assert downloadUri != null;
                    String myUri = downloadUri.toString();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("urlDocument", myUri);
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()) {
                            uploadAudio();
                        }
                    });
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.create_doc), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.doc_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadingProgress.dismissLoadingProgress();
            Toast.makeText(getApplicationContext(), getString(R.string.blank_doc), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAudio() {
        if (uriAudio != null) {
            spinMeting = spinnerMeting.getSelectedItem().toString();

            final StorageReference fileReference = storageReferenceAudio.child(spinMeting+classId+ "." + getFileExtension(uriAudio));
            taskUpload = fileReference.putFile(uriAudio);
            taskUpload.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(uriTask -> {
                if (uriTask.isSuccessful()) {
                    Uri downloadUri = uriTask.getResult();
                    assert downloadUri != null;
                    String myUri = downloadUri.toString();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("urlAudio", myUri);
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()) {
                            backActivity();
                            loadingProgress.dismissLoadingProgress();
                        }
                    });
                    Toast.makeText(CreateMetingActivity.this, getString(R.string.create_audio), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.audio_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("urlAudio", "urlAudio");
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(spinMeting).updateChildren(map).addOnCompleteListener(voidTask -> {
                if (voidTask.isSuccessful()) {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.blank_audio), Toast.LENGTH_SHORT).show();
                    backActivity();
                }
            });
        }
    }

    private void backActivity() {
        Intent intent = new Intent(this, MetingActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("urlCover",urlCover);
        intent.putExtra("courses",courses);
        intent.putExtra("classId",classId);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();
    }

    private boolean haveConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_INTENT);
    }

    private void selectDoc() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, DOC_REQUEST_INTENT);
    }

    private void selectAudio() {
        Intent intent = new Intent();
        intent.setType("audio/mpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, AUDIO_REQUEST_INTENT);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
            edtAttachAudio.setText(getString(R.string.urlAudio));
        }
        return true;
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