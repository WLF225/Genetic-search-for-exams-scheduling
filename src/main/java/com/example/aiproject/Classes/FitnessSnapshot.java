package com.example.aiproject.Classes;

public class FitnessSnapshot {

    // The number of students with 2 exams in the same slot
    private int sameTime;
    // The number of students with more than 2 exams in one day
    private int moreThan2InDay;
    // The number of students with 4 or more exams over 2 consecutive days
    private int fourInTwoDays;
    // The number of students with 2 exams on the same day (soft constraint)
    private int twoSameDay;
    // The number of total distinct exam days used across all courses
    private int daysUsed;
    // The fitness score for this chromosome
    private double fitness;

    public FitnessSnapshot(int mustBePenalty, int preferablePenalty, int betterPenalty){
        fitness = mustBePenalty*3 + preferablePenalty + betterPenalty;
    }

    public int getSameTime() {
        return sameTime;
    }
    public int getMoreThan2InDay() {
        return moreThan2InDay;
    }
    public int getFourInTwoDays() {
        return fourInTwoDays;
    }
    public int getTwoSameDay() {
        return twoSameDay;
    }
    public int getDaysUsed() {
        return daysUsed;
    }
    public double getFitness() {
        return fitness;
    }

    public void incrementSameTime() {
        sameTime++;
    }
    public void incrementMoreThan2InDay() {
        moreThan2InDay++;
    }
    public void incrementFourInTwoDays() {
        fourInTwoDays++;
    }
    public void incrementTwoSameDay() {
        twoSameDay++;
    }
    public void incrementDaysUsed() {
        daysUsed++;
    }
    public void reduceFitness(double reduction) {
        fitness -= reduction;
    }
}
