package com.application.mycourses.ui.home;

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
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.application.mycourses.ui.home.activity.SemesterActivity;
import com.application.mycourses.ui.main.edit.EditActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_home_join, parent, false);
            return new HomeAdapter.ViewHolder(view);
        }
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
            if (modelHomes.get(position).getIdClass().equals(firebaseUser.getUid())){
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
        /*final ImageButton imgBtnLeft;*/
        final CircleImageView imgViewHome;
        final TextView titleCourses,titleUniversity,titleFaculty,titleStudy,titleSemester;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            titleCourses = itemView.findViewById(R.id.tv_item_courses);
            imgBtnSettings = itemView.findViewById(R.id.imgBtnSettings);
            /*imgBtnLeft = itemView.findViewById(R.id.imgBtnLeft);*/
            imgViewHome = itemView.findViewById(R.id.img_view_home);
            titleUniversity = itemView.findViewById(R.id.tv_item_university);
            titleFaculty = itemView.findViewById(R.id.tv_item_faculty);
            titleStudy = itemView.findViewById(R.id.tv_item_study_program);
            titleSemester = itemView.findViewById(R.id.tv_item_semester);
        }

        public void bind(Context contextHome, ModelHome modelHome, HomeFragmentCallback callback) {
            if (modelHome.getUrlCover().equals("urlCover")){
                Glide.with(contextHome)
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher))
                        .into(imgViewHome);
            } else {
                Glide.with(contextHome)
                        .load(modelHome.getUrlCover())
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher))
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
                            editIdClass(modelHome,firebaseUser);
                            return true;
                        case R.id.action_delete:
                            deleteIdClass(database,modelHome,firebaseUser);
                            return true;
                        default:
                            return false;
                    }
                });
                popup.inflate(R.menu.home_items);
                popup.show();
            });

            /*imgBtnLeft.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(contextHome,view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_share:
                            shareIdClass(modelHome,callback);
                            return true;
                        case R.id.action_left:
                            LeftIdClass(database,firebaseUser);
                            return true;
                        default:
                            return false;
                    }
                });
                popup.inflate(R.menu.home_items_left);
                popup.show();
            });*/

            titleCourses.setText(modelHome.getCourses());
            titleUniversity.setText(modelHome.getUniversity());
            titleFaculty.setText(modelHome.getFaculty());
            titleStudy.setText(modelHome.getStudy());
            titleSemester.setText(String.format("Semester %s",modelHome.getSemester()));

            itemView.setOnClickListener(item -> {
                Intent intent = new Intent(contextHome, SemesterActivity.class);
                intent.putExtra("urlCover",modelHome.getUrlCover());
                intent.putExtra("university",modelHome.getUniversity());
                intent.putExtra("faculty",modelHome.getFaculty());
                intent.putExtra("study",modelHome.getStudy());
                intent.putExtra("semester",modelHome.getSemester());
                intent.putExtra("courses",modelHome.getCourses());
                contextHome.startActivity(intent);
                Activity activity = (Activity) contextHome;
                activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            });
        }

        private void LeftIdClass(FirebaseDatabase database, FirebaseUser firebaseUser) {
            Toast.makeText(contextHome, "Exit in Class",Toast.LENGTH_SHORT).show();
        }

        private void shareIdClass(ModelHome modelHome, HomeFragmentCallback callback) {
            callback.onShareClick(modelHome);
        }

        private void editIdClass(ModelHome modelHome, FirebaseUser firebaseUser) {
            if (firebaseUser!=null){
                Intent intent = new Intent(contextHome, EditActivity.class);
                intent.putExtra("urlCover",modelHome.getUrlCover());
                intent.putExtra("university",modelHome.getUniversity());
                intent.putExtra("faculty",modelHome.getFaculty());
                intent.putExtra("study",modelHome.getStudy());
                intent.putExtra("semester",modelHome.getSemester());
                intent.putExtra("courses",modelHome.getCourses());
                contextHome.startActivity(intent);
                Activity activity = (Activity) contextHome;
                activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            }
        }
        private void deleteIdClass(FirebaseDatabase database, ModelHome modelHome, FirebaseUser firebaseUser) {
            String userId = firebaseUser.getUid();
            database.getReference("Class").child(userId).child(modelHome.getCourses()).removeValue().addOnCompleteListener(task -> {
              if (task.isSuccessful()){
                  Toast.makeText(contextHome,R.string.delete_class,Toast.LENGTH_SHORT).show();
              } else {
                  Toast.makeText(contextHome,R.string.delete_class_failed,Toast.LENGTH_SHORT).show();
              }});
        }
    }
}
