package com.example.aiproject;

import com.example.aiproject.Classes.DatasetReader;
import javafx.application.Application;

public class Launcher {

    // To main data set
    public static DatasetReader.Data data;

    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}