package com.example.gymlove;
public class User {
    private String id;
    private String name;
    private String description;
    private boolean trainer;

    public User(String id, String name, String description, boolean trainer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.trainer = trainer;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isTrainer() { return trainer; }
}
