package com.example.gymlove.model;

public class ExerciseItem {
    private String name;
    private String description;
    private boolean isGroupHeader;
    private int sets;
    private int weight;
    private boolean fromPlan;

    // Конструктор для группы
    public ExerciseItem(String name, boolean isGroupHeader) {
        this.name = name;
        this.isGroupHeader = isGroupHeader;
        this.description = null;
        this.sets = 0;
        this.weight = 0;
        this.fromPlan = false;
    }

    // Конструктор для упражнения из WorkoutActivity (без планов)
    public ExerciseItem(String name, String description) {
        this.name = name;
        this.description = description;
        this.isGroupHeader = false;
        this.sets = 0;
        this.weight = 0;
        this.fromPlan = false;
    }

    // Конструктор для упражнения в плане
    public ExerciseItem(String name, int sets, int weight, boolean fromPlan) {
        this.name = name;
        this.description = null;
        this.isGroupHeader = false;
        this.sets = sets;
        this.weight = weight;
        this.fromPlan = fromPlan;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isGroupHeader() { return isGroupHeader; }
    public int getSets() { return sets; }
    public int getWeight() { return weight; }
    public boolean isFromPlan() { return fromPlan; }

    public void setSets(int sets) { this.sets = sets; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setFromPlan(boolean fromPlan) { this.fromPlan = fromPlan; }
}
