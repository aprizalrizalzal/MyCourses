package com.application.mycourses.ui.home.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelMeting;
import com.application.mycourses.ui.home.activity.detail.MetingDetailActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetingAdapter extends RecyclerView.Adapter<MetingAdapter.ViewHolder> {

    private Context metingContext;
    private List<ModelMeting> modelMetings = new ArrayList<>();
    private static final int DOWNLOAD_DOC_REQUEST = 10;

    public MetingAdapter(Context metingContext, List<ModelMeting> modelMetingList) {
        this.metingContext = metingContext;

        if (modelMetings == null)
            return;
        modelMetings.clear();
        modelMetings.addAll(modelMetingList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(metingContext).inflate(R.layout.items_meting,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelMeting modelMeting = modelMetings.get(position);
        holder.bind(metingContext,modelMeting);

    }

    @Override
    public int getItemCount() {
        return modelMetings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FirebaseAuth auth;
        final FirebaseUser user;
        final FirebaseDatabase database;
        final ImageView imageView;
        final TextView tvTitle, tvInfo;
        final ImageButton btnDoc,btnDelDoc;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            imageView = itemView.findViewById(R.id.img_view_meting);
            tvTitle = itemView.findViewById(R.id.tv_item_titleMeting);
            tvInfo = itemView.findViewById(R.id.tv_item_informationMeting);
            btnDoc = itemView.findViewById(R.id.btnDownloadDoc);
            btnDelDoc = itemView.findViewById(R.id.btnDeleteDoc);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bind(Context metingContext, ModelMeting modelMeting) {
            LoadingProgress loadingProgress = new LoadingProgress((Activity) metingContext);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference httpsReference = storage.getReferenceFromUrl(modelMeting.getUrlDocument());
            if (modelMeting.getUrlCover().equals("urlCover")){
                Glide.with(metingContext)
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imageView);
            } else {
                Glide.with(metingContext)
                        .load(modelMeting.getUrlCover())
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imageView);
            }
            tvTitle.setText(modelMeting.getMeting());
            tvInfo.setText(modelMeting.getInformation());

            final File file = new File(itemView.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), modelMeting.getIdMeting());

            if (file.exists()){
                btnDoc.setVisibility(View.INVISIBLE);
                btnDelDoc.setVisibility(View.VISIBLE);
            }else {
                btnDoc.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(view -> {
                if (file.exists()){
                    Intent intentDetail = new Intent(metingContext, MetingDetailActivity.class);
                    intentDetail.putExtra("idMeting",modelMeting.getIdMeting());
                    intentDetail.putExtra("meting",modelMeting.getMeting());
                    intentDetail.putExtra("urlCover",modelMeting.getUrlCover());
                    intentDetail.putExtra("urlAudio",modelMeting.getUrlAudio());
                    Activity activity = (Activity) metingContext;
                    metingContext.startActivity(intentDetail);
                    activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                } else {
                    Toast.makeText(metingContext, R.string.not_download_doc,Toast.LENGTH_SHORT).show();
                }
            });

            btnDoc.setOnClickListener(view -> {
                Toast.makeText(metingContext, R.string.download_doc,Toast.LENGTH_SHORT).show();
                btnDoc.setVisibility(View.INVISIBLE);
                if (haveConnection()){
                    if (ContextCompat.checkSelfPermission(metingContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        downloadDoc(metingContext,modelMeting,httpsReference,file,loadingProgress);
                    }else {
                        btnDoc.setVisibility(View.VISIBLE);
                        ActivityCompat.requestPermissions((Activity) metingContext,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},DOWNLOAD_DOC_REQUEST);
                    }
                }else {
                    btnDoc.setVisibility(View.VISIBLE);
                    Toast.makeText(metingContext, R.string.not_have_connection,Toast.LENGTH_SHORT).show();
                }
            });

            btnDelDoc.setOnClickListener(view -> {
                if (file.exists()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(metingContext);
                    alertDialogBuilder
                            .setTitle(R.string.delete)
                            .setMessage(R.string.delete_doc)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, (dialog, id) -> {
                                file.delete();
                                btnDelDoc.setVisibility(View.INVISIBLE);
                                btnDoc.setVisibility(View.VISIBLE);
                            })
                            .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(metingContext.getDrawable(R.drawable.bg_costume));
                    alertDialog.show();
                }
            });
        }

        private void downloadDoc(Context metingContext, ModelMeting modelMeting, StorageReference httpsReference, File file, LoadingProgress loadingProgress) {
            loadingProgress.startLoadingProgress();
            httpsReference.getFile(file).addOnSuccessListener(taskSnapshot -> {
                btnDoc.setVisibility(View.INVISIBLE);
                loadingProgress.dismissLoadingProgress();
            }).addOnCompleteListener(task -> {
                if (task.isComplete()){
                    Toast.makeText(metingContext,itemView.getResources().getString(R.string.download_doc_success,modelMeting.getMeting()),Toast.LENGTH_SHORT).show();
                    btnDelDoc.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(metingContext, itemView.getResources().getString(R.string.download_doc_failed,modelMeting.getMeting()),Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                loadingProgress.dismissLoadingProgress();
                btnDoc.setVisibility(View.VISIBLE);
            });
        }

        private boolean haveConnection() {
            ConnectivityManager cm = (ConnectivityManager) itemView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }
}
