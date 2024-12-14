package com.example.gymlove.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymlove.adapter.MyExercisesAdapter;
import com.example.gymlove.model.ExerciseItem;
import com.example.gymlove.model.ExercisePlan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyExercisesActivity extends AppCompatActivity {

    private RecyclerView myExercisesRecyclerView;
    private FloatingActionButton addPlanFab;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private List<ExercisePlan> planList = new ArrayList<>();
    private MyExercisesAdapter adapter;

    private List<String> allExercises = new ArrayList<>();
    private List<String> selectedExercises = new ArrayList<>();
    private String currentPlanName;
    private EditText planNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.gymlove.R.layout.activity_my_exercises);

        myExercisesRecyclerView = findViewById(com.example.gymlove.R.id.myExercisesRecyclerView);
        addPlanFab = findViewById(com.example.gymlove.R.id.addPlanFab);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        initAllExercises();

        adapter = new MyExercisesAdapter(planList, plan -> {
            deletePlan(plan);
        });
        myExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myExercisesRecyclerView.setAdapter(adapter);

        loadPlans();

        addPlanFab.setOnClickListener(v -> {
            showAddPlanDialog();
        });
    }

    private void initAllExercises() {
        allExercises.clear();
        allExercises.add("Bicep Curl");
        allExercises.add("Triceps Pushdown");
        allExercises.add("Hammer Curl");
        allExercises.add("Dips");
        allExercises.add("Overhead Triceps Extension");
        allExercises.add("Squat");
        allExercises.add("Lunge");
        allExercises.add("Leg Press");
        allExercises.add("Leg Curl");
        allExercises.add("Calf Raise");
        allExercises.add("Bench Press");
        allExercises.add("Incline Dumbbell Press");
        allExercises.add("Chest Fly");
        allExercises.add("Push-up");
        allExercises.add("Cable Crossover");
        allExercises.add("Deadlift");
        allExercises.add("Lat Pulldown");
        allExercises.add("Seated Row");
        allExercises.add("Pull-up");
        allExercises.add("Face Pull");
    }

    private void loadPlans() {
        String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).child("myExercises")
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        planList.clear();
                        for (DataSnapshot planSnap : snapshot.getChildren()) {
                            String key = planSnap.getKey();
                            String name = planSnap.child("name").getValue(String.class);
                            Boolean fromTrainer = planSnap.child("fromTrainer").getValue(Boolean.class);
                            String trainerId = planSnap.child("trainerId").getValue(String.class);

                            if (name == null) name = "Unnamed Plan";
                            if (fromTrainer == null) fromTrainer = false;

                            ExercisePlan plan = new ExercisePlan(key, name, fromTrainer, trainerId);

                            // Загружаем упражнения
                            DataSnapshot exSnap = planSnap.child("exercises");
                            List<ExerciseItem> items = new ArrayList<>();
                            for (DataSnapshot e : exSnap.getChildren()) {
                                String exName = e.getKey();
                                int sets = 0;
                                int weight = 0;
                                if (e.child("sets").getValue(Integer.class) != null) {
                                    sets = e.child("sets").getValue(Integer.class);
                                }
                                if (e.child("weight").getValue(Integer.class) != null) {
                                    weight = e.child("weight").getValue(Integer.class);
                                }
                                ExerciseItem exItem = new ExerciseItem(exName, sets, weight, true);
                                items.add(exItem);
                            }
                            plan.setExercises(items);

                            planList.add(plan);
                        }
                        adapter.updateList(planList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showAddPlanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(com.example.gymlove.R.layout.dialog_add_plan, null);
        planNameEditText = dialogView.findViewById(com.example.gymlove.R.id.planNameEditText);
        View saveButton = dialogView.findViewById(com.example.gymlove.R.id.saveButton);
        View cancelButton = dialogView.findViewById(com.example.gymlove.R.id.cancelButton);
        View chooseExercisesButton = dialogView.findViewById(com.example.gymlove.R.id.chooseExercisesButton);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        chooseExercisesButton.setOnClickListener(v -> {
            showChooseExercisesDialog();
        });

        saveButton.setOnClickListener(v -> {
            String planName = planNameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(planName)) {
                Toast.makeText(MyExercisesActivity.this, "Enter plan name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExercises.isEmpty()) {
                Toast.makeText(MyExercisesActivity.this, "No exercises selected", Toast.LENGTH_SHORT).show();
                return;
            }

            currentPlanName = planName;
            showSetsWeightsDialog();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            selectedExercises.clear();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showChooseExercisesDialog() {
        String[] exercisesArray = allExercises.toArray(new String[0]);
        boolean[] checkedItems = new boolean[exercisesArray.length];

        for (int i = 0; i < exercisesArray.length; i++) {
            if (selectedExercises.contains(exercisesArray[i])) {
                checkedItems[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Exercises");
        builder.setMultiChoiceItems(exercisesArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedExercises.contains(exercisesArray[which])) {
                    selectedExercises.add(exercisesArray[which]);
                }
            } else {
                selectedExercises.remove(exercisesArray[which]);
            }
        });
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSetsWeightsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(com.example.gymlove.R.layout.dialog_sets_weights, null);

        LinearLayout exercisesContainer = dialogView.findViewById(com.example.gymlove.R.id.exercisesContainer);
        List<View> exerciseViews = new ArrayList<>();
        for (String exercise : selectedExercises) {
            View row = LayoutInflater.from(this).inflate(com.example.gymlove.R.layout.item_sets_weight, exercisesContainer, false);
            TextView exerciseNameTextView = row.findViewById(com.example.gymlove.R.id.exerciseNameTextView);
            EditText setsEditText = row.findViewById(com.example.gymlove.R.id.setsEditText);
            EditText weightEditText = row.findViewById(com.example.gymlove.R.id.weightEditText);

            exerciseNameTextView.setText(exercise);
            exercisesContainer.addView(row);
            exerciseViews.add(row);
        }

        View saveButton = dialogView.findViewById(com.example.gymlove.R.id.saveButton);
        View cancelButton = dialogView.findViewById(com.example.gymlove.R.id.cancelButton);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            Map<String,Object> exercisesMap = new HashMap<>();
            for (View row : exerciseViews) {
                TextView exerciseNameTextView = row.findViewById(com.example.gymlove.R.id.exerciseNameTextView);
                EditText setsEditText = row.findViewById(com.example.gymlove.R.id.setsEditText);
                EditText weightEditText = row.findViewById(com.example.gymlove.R.id.weightEditText);

                String exName = exerciseNameTextView.getText().toString();
                int sets = 0;
                int weight = 0;
                try {
                    sets = Integer.parseInt(setsEditText.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                try {
                    weight = Integer.parseInt(weightEditText.getText().toString().trim());
                } catch (NumberFormatException ignored) {}

                Map<String,Object> exData = new HashMap<>();
                exData.put("sets", sets);
                exData.put("weight", weight);
                exercisesMap.put(exName, exData);
            }

            addUserPlan(currentPlanName, exercisesMap);
            selectedExercises.clear();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            selectedExercises.clear();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addUserPlan(String planName, Map<String,Object> exercisesMap) {
        String userId = currentUser.getUid();
        String planKey = mDatabase.child("users").child(userId).child("myExercises").push().getKey();
        if (planKey == null) return;

        Map<String,Object> planData = new HashMap<>();
        planData.put("name", planName);
        planData.put("fromTrainer", false);
        planData.put("exercises", exercisesMap);

        mDatabase.child("users").child(userId).child("myExercises").child(planKey).setValue(planData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MyExercisesActivity.this, "Plan added", Toast.LENGTH_SHORT).show();
                        loadPlans();
                    } else {
                        Toast.makeText(MyExercisesActivity.this, "Error adding plan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePlan(ExercisePlan plan) {
        String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).child("myExercises").child(plan.getKey()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MyExercisesActivity.this, "Plan deleted", Toast.LENGTH_SHORT).show();
                        loadPlans();
                    } else {
                        Toast.makeText(MyExercisesActivity.this, "Error deleting plan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
