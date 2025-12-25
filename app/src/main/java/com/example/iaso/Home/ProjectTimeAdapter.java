package com.example.iaso.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iaso.R;

import java.util.ArrayList;
import java.util.Locale;

public class ProjectTimeAdapter extends RecyclerView.Adapter<ProjectTimeAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<ProjectTimeData> projectList;

    public ProjectTimeAdapter(Context context, ArrayList<ProjectTimeData> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project_time, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectTimeData project = projectList.get(position);

        holder.projectName.setText(project.getProjectName());

        int totalMinutes = (int) project.getTotalMinutes();
        holder.projectTime.setText(String.valueOf(totalMinutes));

        String imageName = project.getImageName();
        if (imageName != null && !imageName.isEmpty()) {
            int imageRes = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            if (imageRes != 0) {
                Glide.with(context)
                        .load(imageRes)
                        .circleCrop()
                        .into(holder.projectIcon);
            } else {
                holder.projectIcon.setImageResource(R.drawable.orb2);
            }
        } else {
            holder.projectIcon.setImageResource(R.drawable.orb2);
        }
    }

    @Override
    public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    public void updateData(ArrayList<ProjectTimeData> newData) {
        this.projectList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView projectIcon;
        TextView projectName;
        TextView projectTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectIcon = itemView.findViewById(R.id.projectIcon);
            projectName = itemView.findViewById(R.id.projectName);
            projectTime = itemView.findViewById(R.id.projectTime);
        }
    }
}
