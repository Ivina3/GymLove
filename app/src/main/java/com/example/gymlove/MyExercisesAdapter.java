package com.example.gymlove;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyExercisesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PLAN = 0;
    private static final int TYPE_EXERCISE = 1;

    private List<Object> displayList = new ArrayList<>();
    private OnPlanActionListener listener;

    public interface OnPlanActionListener {
        void onDeletePlan(ExercisePlan plan);
    }

    private List<ExercisePlan> planList;

    public MyExercisesAdapter(List<ExercisePlan> planList, OnPlanActionListener listener) {
        this.planList = planList;
        buildDisplayList();
        this.listener = listener;
    }

    private void buildDisplayList() {
        displayList.clear();
        // Для каждого плана добавляем сам план
        // Если expanded = true, добавляем под ним упражнения
        for (ExercisePlan plan : planList) {
            displayList.add(plan); // план
            if (plan.isExpanded()) {
                // Добавляем упражнения
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
            return new PlanViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_in_plan, parent, false);
            return new ExerciseViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = displayList.get(position);
        if (obj instanceof ExercisePlan) {
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
                // Toggle expanded
                plan.setExpanded(!plan.isExpanded());
                buildDisplayList();
                notifyDataSetChanged();
            });

            pv.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePlan(plan);
                }
            });

        } else if (obj instanceof ExerciseItem) {
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
        View deleteButton;
        PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.planNameTextView);
            fromTrainerTextView = itemView.findViewById(R.id.fromTrainerTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseName, setsWeight;
        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            setsWeight = itemView.findViewById(R.id.setsWeight);
        }
    }
}
