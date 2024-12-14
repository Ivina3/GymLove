package com.example.gymlove.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymlove.model.ExerciseItem;
import com.example.gymlove.model.ExercisePlan;

import java.util.ArrayList;
import java.util.List;

public class MyExercisesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PLAN = 0;
    private static final int TYPE_EXERCISE = 1;

    private List<ExercisePlan> planList;
    private List<Object> displayList = new ArrayList<>();
    private OnPlanActionListener listener;

    public interface OnPlanActionListener {
        void onDeletePlan(ExercisePlan plan);
    }

    public MyExercisesAdapter(List<ExercisePlan> planList, OnPlanActionListener listener) {
        this.planList = planList;
        this.listener = listener;
        buildDisplayList();
    }

    private void buildDisplayList() {
        displayList.clear();
        for (ExercisePlan plan : planList) {
            displayList.add(plan);
            if (plan.isExpanded()) {
                displayList.addAll(plan.getExercises());
            }
        }
    }

    public void updateList(List<ExercisePlan> newList) {
        this.planList = newList;
        buildDisplayList();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = displayList.get(position);
        if (obj instanceof ExercisePlan) {
            return TYPE_PLAN;
        } else {
            return TYPE_EXERCISE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PLAN) {
            View v = LayoutInflater.from(parent.getContext()).inflate(com.example.gymlove.R.layout.item_plan, parent, false);
            return new PlanViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(com.example.gymlove.R.layout.item_exercise_in_plan, parent, false);
            return new ExerciseViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = displayList.get(position);
        if (holder instanceof PlanViewHolder) {
            ExercisePlan plan = (ExercisePlan) obj;
            PlanViewHolder pv = (PlanViewHolder) holder;
            pv.nameTextView.setText(plan.getName());
            if (plan.isFromTrainer()) {
                pv.fromTrainerTextView.setVisibility(View.VISIBLE);
                pv.fromTrainerTextView.setText("From Trainer");
            } else {
                pv.fromTrainerTextView.setVisibility(View.GONE);
            }

            pv.itemView.setOnClickListener(v -> {
                plan.setExpanded(!plan.isExpanded());
                buildDisplayList();
                notifyDataSetChanged();
            });

            pv.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePlan(plan);
                }
            });
        } else {
            ExerciseItem exercise = (ExerciseItem) obj;
            ExerciseViewHolder ev = (ExerciseViewHolder) holder;
            ev.exerciseName.setText(exercise.getName());
            ev.setsWeight.setText("Sets: " + exercise.getSets() + "  Weight: " + exercise.getWeight() + "kg");
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, fromTrainerTextView;
        ImageView deleteButton;
        PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(com.example.gymlove.R.id.planNameTextView);
            fromTrainerTextView = itemView.findViewById(com.example.gymlove.R.id.fromTrainerTextView);
            deleteButton = itemView.findViewById(com.example.gymlove.R.id.deleteButton);
        }
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseName, setsWeight;
        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(com.example.gymlove.R.id.exerciseName);
            setsWeight = itemView.findViewById(com.example.gymlove.R.id.setsWeight);
        }
    }
}
