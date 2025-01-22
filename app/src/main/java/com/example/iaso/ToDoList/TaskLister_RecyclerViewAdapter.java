package com.example.iaso.ToDoList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.R;

import java.util.ArrayList;

public class TaskLister_RecyclerViewAdapter extends RecyclerView.Adapter<TaskLister_RecyclerViewAdapter.MyViewHolder>{

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<TaskItem> taskList;

    public TaskLister_RecyclerViewAdapter(Context context, ArrayList<TaskItem> taskList, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.taskList = taskList;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public TaskLister_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is where you inflate the layout. Giving a look to our rows.
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.each_task, parent, false);

        return new TaskLister_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskLister_RecyclerViewAdapter.MyViewHolder holder, int position) {
        //Assigning values to the views we created in the recycler_view_row layout file based on the position of the recycler view

        holder.taskTitle.setText(taskList.get(position).getName());
        holder.dateTitle.setText(taskList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        //Recycler View wants to know the number of items you want displayed on the screen.
        return taskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Kind of like an oncreate method

        TextView taskTitle;
        TextView dateTitle;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            taskTitle = itemView.findViewById(R.id.TaskNameId);
            dateTitle = itemView.findViewById(R.id.DateId);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                        }
                    }

                    return true;
                }
            });


        }
    }
}
