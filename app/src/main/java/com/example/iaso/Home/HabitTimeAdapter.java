package com.example.iaso.Home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
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
import java.util.List;

public class HabitTimeAdapter extends RecyclerView.Adapter<HabitTimeAdapter.HabitTimeViewHolder> {

    private List<HabitTimeData> habitTimeList;
    private Context context;

    public HabitTimeAdapter(Context context) {
        this.context = context;
        this.habitTimeList = new ArrayList<>();
    }

    public void updateData(List<HabitTimeData> newData) {
        this.habitTimeList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.habit_time_card, parent, false);
        return new HabitTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitTimeViewHolder holder, int position) {
        HabitTimeData data = habitTimeList.get(position);

        holder.habitTitle.setText(data.getHabitName());
        holder.habitMinutes.setText(String.valueOf(data.getTotalMinutes()));

        // Animate habitMinutes with blur lift effect
        animateHabitMinutes(holder.habitMinutes);

        // Load image using Glide
        int imageRes = context.getResources().getIdentifier(data.getImageName(), "drawable", context.getPackageName());
        Glide.with(context).load(imageRes).circleCrop().into(holder.habitImage);
    }

    // Animates the habit minutes text with a blur lift effect
    private void animateHabitMinutes(TextView view) {
        float startBlur = 25f;
        float endBlur = 0f;

        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.getPaint().setMaskFilter(new BlurMaskFilter(startBlur, BlurMaskFilter.Blur.NORMAL));

        ValueAnimator blurAnimator = ValueAnimator.ofFloat(startBlur, endBlur);
        blurAnimator.setDuration(600);
        blurAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (value <= 0f) {
                view.getPaint().setMaskFilter(null);
            } else {
                view.getPaint().setMaskFilter(new BlurMaskFilter(value, BlurMaskFilter.Blur.NORMAL));
            }
            view.invalidate();
        });

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start();

        blurAnimator.start();
    }

    @Override
    public int getItemCount() {
        return habitTimeList.size();
    }

    static class HabitTimeViewHolder extends RecyclerView.ViewHolder {
        ImageView habitImage;
        TextView habitTitle;
        TextView habitMinutes;

        public HabitTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            habitImage = itemView.findViewById(R.id.habitImage);
            habitTitle = itemView.findViewById(R.id.habitTitle);
            habitMinutes = itemView.findViewById(R.id.habitMinutes);
        }
    }
}
