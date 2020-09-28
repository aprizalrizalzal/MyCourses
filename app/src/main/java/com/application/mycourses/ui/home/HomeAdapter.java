package com.application.mycourses.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.application.mycourses.ui.home.activity.MetingActivity;
import com.application.mycourses.ui.main.edit.EditCoursesActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private static final int CLASS_LEFT=0;
    private static final int CLASS_RIGHT=1;
    private Context contextHome;
    private List<ModelHome> modelHomes = new ArrayList<>();
    private final HomeFragmentCallback callback;

    public HomeAdapter(Context contextHome, List<ModelHome> modelHomeList, HomeFragmentCallback callback) {
        this.contextHome = contextHome;
        this.callback = callback;

        if (modelHomes == null) return;
        modelHomes.clear();
        modelHomes.addAll(modelHomeList);
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CLASS_LEFT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_home, parent, false);
            return new HomeAdapter.ViewHolder(view);
        } else if (viewType == CLASS_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_home_join, parent, false);
            return new HomeAdapter.ViewHolder(view);
        }
        return createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        ModelHome modelHome = modelHomes.get(position);
        holder.bind(contextHome, modelHome, callback);
    }

    @Override
    public int getItemCount() {
        return modelHomes.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            if (modelHomes.get(position).getUserId().equals(firebaseUser.getUid())){
                return CLASS_LEFT;
            }else
                return CLASS_RIGHT;
        }
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final FirebaseAuth firebaseAuth;
        final FirebaseUser firebaseUser;
        final FirebaseDatabase database;
        final ImageButton imgBtnSettings;
        final CircleImageView imgViewHome;
        final TextView titleCourses,titleUniversity,titleFaculty,titleStudy,titleSemester;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            titleCourses = itemView.findViewById(R.id.tv_item_courses);
            imgBtnSettings = itemView.findViewById(R.id.imgBtnSettings);
            imgViewHome = itemView.findViewById(R.id.img_view_home);
            titleUniversity = itemView.findViewById(R.id.tv_item_university);
            titleFaculty = itemView.findViewById(R.id.tv_item_faculty);
            titleStudy = itemView.findViewById(R.id.tv_item_study_program);
            titleSemester = itemView.findViewById(R.id.tv_item_semester);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bind(Context contextHome, ModelHome modelHome, HomeFragmentCallback callback) {
            if (modelHome.getUrlCover().equals("urlCover")){
                Glide.with(contextHome)
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgViewHome);
            } else {
                Glide.with(contextHome)
                        .load(modelHome.getUrlCover())
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgViewHome);
            }

            imgBtnSettings.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(contextHome,view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_share:
                            shareIdClass(modelHome,callback);
                            return true;
                        case R.id.action_edit:
                            if (modelHome.getUserId().equals(firebaseUser.getUid())){
                                editIdClass(modelHome,firebaseUser);
                            } else {
                                Toast.makeText(contextHome, contextHome.getString(R.string.not_edit_class),Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case R.id.action_delete:
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(contextHome);
                            alertDialogBuilder
                                    .setTitle(contextHome.getString(R.string.delete))
                                    .setMessage(contextHome.getString(R.string.delete_your_class,modelHome.getCourses()))
                                    .setCancelable(false)
                                    .setPositiveButton(contextHome.getString(R.string.yes), (dialog, id) -> {
                                        deleteIdClass(modelHome,database,firebaseUser);
                                    })
                                    .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(contextHome.getDrawable(R.drawable.bg_costume));
                            alertDialog.show();
                            return true;
                        default:
                            return false;
                    }
                });
                popup.inflate(R.menu.home_items);
                popup.show();
            });

            titleCourses.setText(modelHome.getCourses());
            titleUniversity.setText(modelHome.getUniversity());
            titleFaculty.setText(modelHome.getFaculty());
            titleStudy.setText(modelHome.getStudy());
            titleSemester.setText(String.format("Semester %s",modelHome.getSemester()));

            itemView.setOnClickListener(item -> {
                Intent intent = new Intent(contextHome, MetingActivity.class);
                intent.putExtra("userId",modelHome.getUserId());
                intent.putExtra("urlCover",modelHome.getUrlCover());
                intent.putExtra("courses",modelHome.getCourses());
                intent.putExtra("classId",modelHome.getClassId());
                contextHome.startActivity(intent);
                Activity activity = (Activity) contextHome;
                activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                activity.finish();
            });
        }

        private void shareIdClass(ModelHome modelHome, HomeFragmentCallback callback) {
            callback.onShareClick(modelHome);
        }

        private void editIdClass(ModelHome modelHome, FirebaseUser firebaseUser) {
            String userId = firebaseUser.getUid();
            if (userId.equals(modelHome.getUserId())){
                Intent intent = new Intent(contextHome, EditCoursesActivity.class);
                intent.putExtra("classId",modelHome.getClassId());
                intent.putExtra("courses",modelHome.getCourses());
                intent.putExtra("faculty",modelHome.getFaculty());
                intent.putExtra("semester",modelHome.getSemester());
                intent.putExtra("study",modelHome.getStudy());
                intent.putExtra("university",modelHome.getUniversity());
                intent.putExtra("urlCover",modelHome.getUrlCover());
                contextHome.startActivity(intent);
                Activity activity = (Activity) contextHome;
                activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                activity.finish();
            }
        }
        private void deleteIdClass(ModelHome modelHome, FirebaseDatabase database, FirebaseUser firebaseUser) {
            String userId = firebaseUser.getUid();
            if (userId.equals(modelHome.getUserId())){
                database.getReference(contextHome.getString(R.string.name_class)).child(modelHome.getClassId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(contextHome, MainNavActivity.class);
                        Toast.makeText(contextHome,contextHome.getString(R.string.delete_class),Toast.LENGTH_SHORT).show();
                        contextHome.startActivity(intent);
                        Activity activity = (Activity) contextHome;
                        activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                        activity.finish();
                    } else {
                        Toast.makeText(contextHome,contextHome.getString(R.string.delete_class_failed),Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                database.getReference(contextHome.getString(R.string.name_class)).child(modelHome.getClassId()).child(contextHome.getString(R.string.name_class_member)).child(userId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(contextHome, MainNavActivity.class);
                        Toast.makeText(contextHome,contextHome.getString(R.string.delete_and_left_class),Toast.LENGTH_SHORT).show();
                        contextHome.startActivity(intent);
                        Activity activity = (Activity) contextHome;
                        activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                        activity.finish();
                    } else {
                        Toast.makeText(contextHome,contextHome.getString(R.string.delete_class_failed),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
