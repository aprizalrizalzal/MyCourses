package com.application.mycourses.ui.home.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelMeting;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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
        final ImageView imageView;
        final TextView tvTitle, tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_view_meting);
            tvTitle = itemView.findViewById(R.id.tv_item_titleMeting);
            tvInfo = itemView.findViewById(R.id.tv_item_informationMeting);
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

        }
    }
}
