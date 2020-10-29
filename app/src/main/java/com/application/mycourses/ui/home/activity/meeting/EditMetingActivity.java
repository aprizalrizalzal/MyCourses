package com.application.mycourses.ui.home.activity.meeting;

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
import android.text.InputType;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.R;
import com.application.mycourses.ui.home.activity.MetingActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditMetingActivity extends AppCompatActivity {

    private CircleImageView imgCreateCover;
    private TextView tvMeting;
    private EditText edtInfo, edtAttachDoc, edtAttachAudio;
    private Button btnSave;
    private String info,doc,audio;
    private String classId, userId,idMeting, courses, meting, information, urlCover, urlDocument, urlAudio;
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
        setContentView(R.layout.activity_edit_meting);

        Intent intent = getIntent();
        classId = intent.getStringExtra("classId");
        userId = intent.getStringExtra("userId");
        idMeting = intent.getStringExtra("idMeting");
        courses = intent.getStringExtra("courses");
        information = intent.getStringExtra("information");
        meting = intent.getStringExtra("meting");
        urlCover = intent.getStringExtra("urlCover");
        urlDocument = intent.getStringExtra("urlDocument");
        urlAudio = intent.getStringExtra("urlAudio");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgCreateCover = findViewById(R.id.img_view);
        tvMeting = findViewById(R.id.tv_meeting);
        edtInfo = findViewById(R.id.edtInfo);
        edtAttachDoc = findViewById(R.id.edtAttachDoc);
        edtAttachAudio = findViewById(R.id.edtAttachAudio);
        btnSave = findViewById(R.id.btnSave);
        ImageButton btnAddCover = findViewById(R.id.btnAddCover);
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

        if (urlCover != null) {
            if (urlCover.equals("urlCover")){
                Glide.with(getApplication())
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgCreateCover);
            } else {
                Glide.with(getApplication())
                        .load(urlCover)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgCreateCover);
            }
        }

        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getString(R.string.metingView,courses,meting));

        /*MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        btnAddCover.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectImage();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(EditMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST);
            }
        });

        tvMeting.setText(getString(R.string.metingView,courses,meting));
        edtInfo.setText(information);

        edtAttachDoc.setInputType(InputType.TYPE_NULL);
        edtAttachDoc.setText(urlDocument);
        edtAttachDoc.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectDoc();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(EditMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},DOC_REQUEST);
            }
        });

        edtAttachAudio.setInputType(InputType.TYPE_NULL);
        edtAttachAudio.setText(urlAudio);
        edtAttachAudio.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditMetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                selectAudio();
                btnSave.setEnabled(true);
            }else {
                ActivityCompat.requestPermissions(EditMetingActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},AUDIO_REQUEST);
            }
        });

        btnSave.setOnClickListener(view -> {
            if (haveConnection()){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditMetingActivity.this);
                alertDialogBuilder
                        .setTitle(R.string.save)
                        .setMessage(R.string.valid_data)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, id) -> createMeting(user,userId,idMeting,database))
                        .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                alertDialog.show();
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT ).show();
            }
        });
    }
    private void createMeting(FirebaseUser user, String userId, String idMeting, FirebaseDatabase database) {
        if (!validInfo() || !validDoc() || !validAudio()){
            return;
        }
        loadingProgress.startLoadingProgress();
        info = edtInfo.getText().toString();
        doc = edtAttachDoc.getText().toString();
        audio = edtAttachAudio.getText().toString();

        String idUser = user.getUid();
        if (idUser.equals(userId)){
            Map<String,Object> mapMeting = new HashMap<>();
            mapMeting.put("urlCover",urlCover);
            mapMeting.put("meting",meting);
            mapMeting.put("classId",classId);
            mapMeting.put("courses",courses);
            mapMeting.put("idMeting",idMeting);
            mapMeting.put("userId",userId);
            mapMeting.put("information",info);
            mapMeting.put("urlDocument",doc);
            mapMeting.put("urlAudio",audio);

            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).setValue(mapMeting).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    uploadImage();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(EditMetingActivity.this, getString(R.string.update_failed),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

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
            Toast.makeText(EditMetingActivity.this, getString(R.string.allow_permission_storage), Toast.LENGTH_SHORT).show();
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
            final StorageReference fileReference = storageReferenceCover.child(meting+classId+"."+ getFileExtension(uriImage));
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
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()){
                            uploadDoc();
                        }
                    });
                    Toast.makeText(EditMetingActivity.this, getString(R.string.create_picture), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.image_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("urlCover", urlCover);
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
                if (voidTask.isSuccessful()) {
                    uploadDoc();
                    Toast.makeText(getApplicationContext(), getString(R.string.blank_image), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadDoc() {
        if (uriDoc != null) {
            final StorageReference fileReference = storageReferenceDoc.child(meting+classId+ "." + getFileExtension(uriDoc));
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
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()) {
                            uploadAudio();
                        }
                    });
                    Toast.makeText(EditMetingActivity.this, getString(R.string.create_doc), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.doc_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("urlAudio", urlDocument);
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
                if (voidTask.isSuccessful()) {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.blank_audio), Toast.LENGTH_SHORT).show();
                    backActivity();
                }
            });
        }
    }

    private void uploadAudio() {
        if (uriAudio != null) {
            final StorageReference fileReference = storageReferenceAudio.child(tvMeting+classId+ "." + getFileExtension(uriAudio));
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
                    database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
                        if (voidTask.isSuccessful()) {
                            backActivity();
                            loadingProgress.dismissLoadingProgress();
                        }
                    });
                    Toast.makeText(EditMetingActivity.this, getString(R.string.create_audio), Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgress.dismissLoadingProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.audio_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("urlAudio", urlAudio);
            database.getReference(getString(R.string.name_class)).child(classId).child(getString(R.string.meting)).child(meting).updateChildren(map).addOnCompleteListener(voidTask -> {
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
}