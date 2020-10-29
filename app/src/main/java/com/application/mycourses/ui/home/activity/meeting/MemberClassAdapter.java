package com.application.mycourses.ui.home.activity.meeting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class MemberClassAdapter extends RecyclerView.Adapter<MemberClassAdapter.ViewHolder> {

    private Context memberContext;
    private List<ModelUser> modelUsers = new ArrayList<>();

    public MemberClassAdapter(Context memberContext, List<ModelUser> modelUsersList) {
        this.memberContext = memberContext;
        if (modelUsers == null)
            return;
        modelUsers.clear();
        modelUsers.addAll(modelUsersList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(memberContext).inflate(R.layout.items_member,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelUser modelUser = modelUsers.get(position);
        holder.bind(modelUser,memberContext);

    }

    @Override
    public int getItemCount() {
        return modelUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(ModelUser modelUser, Context memberContext) {

        }
    }
}
