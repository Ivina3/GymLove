package com.example.gymlove;

public class Exercise {
    private String name;
    private String muscleGroup;
    private String description;

    public Exercise(String name, String muscleGroup, String description) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.description = description;
    }

    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getDescription() { return description; }
}
