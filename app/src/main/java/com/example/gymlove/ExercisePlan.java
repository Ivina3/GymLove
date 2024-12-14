package com.example.gymlove;

import java.util.ArrayList;
import java.util.List;

public class ExercisePlan {
    private String key;
    private String name;
    private boolean fromTrainer;
    private String trainerId;
    private boolean expanded; // Добавляем флаг развёрнутости
    private List<ExerciseItem> exercises = new ArrayList<>();

    public ExercisePlan(String key, String name, boolean fromTrainer, String trainerId) {
        this.key = key;
        this.name = name;
        this.fromTrainer = fromTrainer;
        this.trainerId = trainerId;
        this.expanded = false;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public boolean isFromTrainer() { return fromTrainer; }
    public String getTrainerId() { return trainerId; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    public List<ExerciseItem> getExercises() { return exercises; }
    public void setExercises(List<ExerciseItem> exercises) { this.exercises = exercises; }
}
