package com.example.aiproject.Classes;

// To hold the course code so chromosomes can reference courses by index
public class Course {
    String name;

    public Course(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
