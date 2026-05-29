package com.example.aiproject.Classes;

// To represent a student and the indexes of courses they are enrolled in
public class Student {
    String name;
    int[] courseIndexes;

    public Student(String name, int[] courseIndexes) {
        this.name = name;
        this.courseIndexes = courseIndexes;
    }

    public int[]  getCourseIndexes() { return courseIndexes; }
}
