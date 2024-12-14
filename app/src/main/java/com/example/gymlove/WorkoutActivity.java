package com.example.gymlove;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private RecyclerView workoutRecyclerView;
    private WorkoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        workoutRecyclerView = findViewById(R.id.workoutRecyclerView);

        List<ExerciseItem> items = createWorkoutList();
        adapter = new WorkoutAdapter(items, this::showExerciseDialog);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutRecyclerView.setAdapter(adapter);
    }

    private void showExerciseDialog(ExerciseItem exercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exercise_detail, null);
        TextView titleTextView = dialogView.findViewById(R.id.exerciseTitleTextView);
        TextView descTextView = dialogView.findViewById(R.id.exerciseDescriptionTextView);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        titleTextView.setText(exercise.getName());
        descTextView.setText(exercise.getDescription());

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private List<ExerciseItem> createWorkoutList() {
        List<ExerciseItem> list = new ArrayList<>();

        // Добавляем группу, затем упражнения
        list.add(new ExerciseItem("Arms", true));
        list.add(new ExerciseItem("Bicep Curl", "Do curls with dumbbells."));
        list.add(new ExerciseItem("Triceps Pushdown", "Use a cable machine to target triceps."));
        list.add(new ExerciseItem("Hammer Curl", "Neutral grip curls for brachialis."));
        list.add(new ExerciseItem("Dips", "Bodyweight exercise for triceps and chest."));
        list.add(new ExerciseItem("Overhead Triceps Extension", "Dumbbell overhead for triceps long head."));

        list.add(new ExerciseItem("Legs", true));
        list.add(new ExerciseItem("Squat", "Fundamental leg exercise."));
        list.add(new ExerciseItem("Lunge", "Targets quads, glutes, hamstrings."));
        list.add(new ExerciseItem("Leg Press", "Machine-based compound leg exercise."));
        list.add(new ExerciseItem("Leg Curl", "Isolation exercise for hamstrings."));
        list.add(new ExerciseItem("Calf Raise", "Focuses on calves, standing or seated."));

        list.add(new ExerciseItem("Chest", true));
        list.add(new ExerciseItem("Bench Press", "Classic chest builder with a barbell."));
        list.add(new ExerciseItem("Incline Dumbbell Press", "Targets upper chest."));
        list.add(new ExerciseItem("Chest Fly", "Isolation for chest with dumbbells or cables."));
        list.add(new ExerciseItem("Push-up", "Bodyweight chest and triceps exercise."));
        list.add(new ExerciseItem("Cable Crossover", "Cable isolation for inner chest."));

        list.add(new ExerciseItem("Back", true));
        list.add(new ExerciseItem("Deadlift", "Targets posterior chain."));
        list.add(new ExerciseItem("Lat Pulldown", "Machine exercise for lats."));
        list.add(new ExerciseItem("Seated Row", "Targets mid-back muscles."));
        list.add(new ExerciseItem("Pull-up", "Bodyweight back exercise."));
        list.add(new ExerciseItem("Bent-Over Row", "Barbell row for back thickness."));

        list.add(new ExerciseItem("Shoulders", true));
        list.add(new ExerciseItem("Overhead Press", "Shoulder press with barbell or dumbbells."));
        list.add(new ExerciseItem("Lateral Raise", "Isolation for lateral deltoids."));
        list.add(new ExerciseItem("Front Raise", "Focus on anterior deltoids."));
        list.add(new ExerciseItem("Upright Row", "Barbell or cable row for shoulders."));
        list.add(new ExerciseItem("Face Pull", "Good for rear delts and rotator cuff."));

        list.add(new ExerciseItem("Abs", true));
        list.add(new ExerciseItem("Crunch", "Basic abdominal exercise."));
        list.add(new ExerciseItem("Plank", "Core isometric hold."));
        list.add(new ExerciseItem("Bicycle Crunch", "Targets obliques and abs."));
        list.add(new ExerciseItem("Leg Raise", "Lower abs exercise."));
        list.add(new ExerciseItem("Ab Wheel Rollout", "Advanced core exercise."));

        list.add(new ExerciseItem("Glutes", true));
        list.add(new ExerciseItem("Hip Thrust", "Glute-focused barbell exercise."));
        list.add(new ExerciseItem("Glute Bridge", "Bodyweight glute exercise."));
        list.add(new ExerciseItem("Donkey Kick", "Bodyweight isolation for glutes."));
        list.add(new ExerciseItem("Bulgarian Split Squat", "Unilateral leg/glute builder."));
        list.add(new ExerciseItem("Step-Up", "Functional lower-body exercise."));

        list.add(new ExerciseItem("Calves", true));
        list.add(new ExerciseItem("Standing Calf Raise", "Target gastrocnemius."));
        list.add(new ExerciseItem("Seated Calf Raise", "Focus on soleus muscle."));
        list.add(new ExerciseItem("Calf Press", "Machine-based calf exercise."));
        list.add(new ExerciseItem("Single-Leg Calf Raise", "Unilateral calf exercise."));
        list.add(new ExerciseItem("Jump Rope", "Cardio and calves endurance."));

        return list;
    }
}
