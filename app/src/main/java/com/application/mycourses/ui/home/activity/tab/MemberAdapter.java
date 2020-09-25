package com.application.mycourses.ui.home.activity.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelUser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> implements Filterable {

    private Context memberContext;
    private List<ModelUser> modelUsers = new ArrayList<>();
    private List<ModelUser> getModelUsers = new ArrayList<>();

    public MemberAdapter(Context memberContext, List<ModelUser> modelUser) {
        this.memberContext = memberContext;

        if (modelUsers == null) return;
        modelUsers.clear();
        modelUsers.addAll(modelUser);

        if (getModelUsers == null) return;
        getModelUsers.clear();
        getModelUsers.addAll(modelUser);
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
        holder.bind(memberContext,modelUser);
    }

    @Override
    public int getItemCount() {
        return modelUsers.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ModelUser> filterData = new ArrayList<>();
            if (charSequence.toString().isEmpty()){
                filterData.addAll(getModelUsers);
            } else {
                for (ModelUser modelUser : getModelUsers){
                    if (modelUser.getUserName().toUpperCase().contains(charSequence.toString().toLowerCase())){
                        filterData.add(modelUser);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterData;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            modelUsers.clear();
            modelUsers.addAll((Collection<? extends ModelUser>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView imgViewMember;
        final TextView tvName,tvNoPhone/*,tvStatus*/,tvOnline;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgViewMember = itemView.findViewById(R.id.img_viewMember);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvNoPhone = itemView.findViewById(R.id.tv_item_no_phone);
            /*tvStatus = itemView.findViewById(R.id.tv_item_status);*/
            tvOnline = itemView.findViewById(R.id.tv_item_online);
        }

        public void bind(final Context memberContext, final ModelUser modelUsers) {

            if (modelUsers.getUrlPicture().equals("urlCover")){
                Glide.with(memberContext)
                        .load(R.mipmap.ic_launcher_round)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgViewMember);
            } else {
                Glide.with(memberContext)
                        .load(modelUsers.getUrlPicture())
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                        .into(imgViewMember);
            }

            tvName.setText(modelUsers.getUserName());
            tvNoPhone.setText(modelUsers.getNumberPhone());
            /*tvStatus.setText(modelUsers.getStatus());*/

            if (modelUsers.getUserOnline().equals(true)){
                tvOnline.setText(memberContext.getString(R.string.online));
            }else {
                tvOnline.setText(String.format("%s %s %s %s", memberContext.getResources().getString(R.string.last_seen), modelUsers.getLastDate(),memberContext.getResources().getString(R.string.at),modelUsers.getLastTime()));
                tvOnline.setTextColor(memberContext.getResources().getColor(R.color.colorPrimary)); }
        }
    }
}
