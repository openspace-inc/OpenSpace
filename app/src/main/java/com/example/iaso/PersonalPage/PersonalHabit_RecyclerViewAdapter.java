package com.example.iaso.PersonalPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.R;
import com.example.iaso.ToDoList.RecyclerViewInterface;

import java.util.ArrayList;

public class PersonalHabit_RecyclerViewAdapter extends RecyclerView.Adapter<PersonalHabit_RecyclerViewAdapter.MyViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    Context context;
    ArrayList<DynamicHabit> dynamicHabitList;


    public PersonalHabit_RecyclerViewAdapter(Context context, ArrayList<DynamicHabit> dynamicHabitList,
                                             RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.dynamicHabitList = dynamicHabitList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public PersonalHabit_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.personal_habit_row, parent, false);

        return new PersonalHabit_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalHabit_RecyclerViewAdapter.MyViewHolder holder, int position) {
        DynamicHabit habit = dynamicHabitList.get(position); // Get the habit at the current position
        holder.habitName.setText(habit.getName3());
        holder.streakCount.setText(String.valueOf(habit.getStreak3()) + " days"); // Convert int to String
        holder.time.setText(String.valueOf(habit.getTime()));


        int blockCountAmount = habit.getBlocks3();
        if(blockCountAmount == 1){
            holder.blockCount.setText(String.valueOf(habit.getBlocks3())); // Convert int to String
        }
        else {
            holder.blockCount.setText(String.valueOf(habit.getBlocks3())); // Convert int to String
        }

        //Edit this to go from blocks to check marks
        holder.check.setImageResource(R.drawable.block1);
        holder.streak.setImageResource(R.drawable.streakv1);
        holder.habitImage.setImageResource(habit.getImageResId());
    }

    @Override
    public int getItemCount() {
        return dynamicHabitList.size();

    }

    //Class that I created (Like onCreate Method)👇🏼
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView check, streak, habitImage;
        TextView habitName, streakCount, habitCount, blockCount, time;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            habitName = itemView.findViewById(R.id.habitText);
            streakCount = itemView.findViewById(R.id.streakCount);
            blockCount = itemView.findViewById(R.id.blockCount);
            check = itemView.findViewById(R.id.blockImage);
            streak = itemView.findViewById(R.id.streakImage);
            time = itemView.findViewById(R.id.commitmentTime);
            habitImage = itemView.findViewById(R.id.dynamicHabitImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });

        }
    }
}
