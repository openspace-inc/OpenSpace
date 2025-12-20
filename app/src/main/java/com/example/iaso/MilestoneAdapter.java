package com.example.iaso;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying milestones in a RecyclerView.
 * Each milestone shows a card with name and time, with a timeline on the left.
 * The last item shows a checkmark instead of a circle.
 */
public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder> {

    private List<Milestone> milestones = new ArrayList<>();
    private OnMilestoneClickListener clickListener;

    /**
     * Interface for handling milestone card clicks
     */
    public interface OnMilestoneClickListener {
        void onMilestoneClick(int position, Milestone milestone);
    }

    public MilestoneAdapter() {
    }

    public void setOnMilestoneClickListener(OnMilestoneClickListener listener) {
        this.clickListener = listener;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
        notifyDataSetChanged();
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void updateMilestone(int position, Milestone milestone) {
        if (position >= 0 && position < milestones.size()) {
            milestones.set(position, milestone);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_milestone, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        Milestone milestone = milestones.get(position);
        boolean isFirst = position == 0;
        boolean isLast = position == milestones.size() - 1;

        holder.bind(milestone, isFirst, isLast, clickListener, position);
    }

    @Override
    public int getItemCount() {
        return milestones.size();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        private final ImageView milestoneIcon;
        private final TextView milestoneName;
        private final TextView milestoneTime;
        private final View lineTop;
        private final View lineBottom;
        private final CardView milestoneCard;

        public MilestoneViewHolder(@NonNull View itemView) {
            super(itemView);
            milestoneIcon = itemView.findViewById(R.id.milestone_icon);
            milestoneName = itemView.findViewById(R.id.milestone_name);
            milestoneTime = itemView.findViewById(R.id.milestone_time);
            lineTop = itemView.findViewById(R.id.line_top);
            lineBottom = itemView.findViewById(R.id.line_bottom);
            milestoneCard = itemView.findViewById(R.id.milestone_card);
        }

        public void bind(Milestone milestone, boolean isFirst, boolean isLast,
                         OnMilestoneClickListener listener, int position) {
            milestoneName.setText(milestone.getName());
            milestoneTime.setText(milestone.getTime());

            // Show checkmark for last item, circle for others
            if (isLast) {
                milestoneIcon.setImageResource(R.drawable.checkmarkv3);
            } else {
                milestoneIcon.setImageResource(R.drawable.os_circle);
            }

            // Hide top line for first item
            lineTop.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);

            // Hide bottom line for last item
            lineBottom.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);

            // Set click listener on the card
            milestoneCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMilestoneClick(position, milestone);
                }
            });
        }
    }
}
