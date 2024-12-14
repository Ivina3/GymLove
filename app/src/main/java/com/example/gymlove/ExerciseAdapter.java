package com.example.gymlove;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Exercise> exerciseList;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exerciseList, OnExerciseClickListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        // simple_list_item_2 - стандартный layout c двумя текстовыми полями
        // Можно заменить на свой layout
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseAdapter.ViewHolder holder, int position) {
        Exercise ex = exerciseList.get(position);
        holder.nameTextView.setText(ex.getName());
        holder.muscleTextView.setText(ex.getMuscleGroup());
        holder.itemView.setOnClickListener(v -> listener.onExerciseClick(ex));
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, muscleTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(android.R.id.text1);
            muscleTextView = itemView.findViewById(android.R.id.text2);
        }
    }
}
