package com.application.mycourses.ui.home.activity;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelMeting;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MetingAdapter extends RecyclerView.Adapter<MetingAdapter.ViewHolder> {

    private Context metingContext;
    private List<ModelMeting> modelMetings = new ArrayList<>();

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
        final ImageButton btnSettings;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            imageView = itemView.findViewById(R.id.img_view_meting);
            tvTitle = itemView.findViewById(R.id.tv_item_titleMeting);
            tvInfo = itemView.findViewById(R.id.tv_item_informationMeting);
            btnSettings = itemView.findViewById(R.id.imgBtnSettings);
        }

        public void bind(Context metingContext, ModelMeting modelMeting) {
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

            btnSettings.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(metingContext,view);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.action_down_doc:
                            downloadDoc(user,modelMeting,database);
                            return true;
                        case R.id.action_del_Doc:
                            deleteDoc(user,modelMeting,database);
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.inflate(R.menu.meting_items);
                popupMenu.show();
            });

        }

        private void downloadDoc(FirebaseUser user, ModelMeting modelMeting, FirebaseDatabase database) {

        }

        private void deleteDoc(FirebaseUser user, ModelMeting modelMeting, FirebaseDatabase database) {

        }
    }
}
