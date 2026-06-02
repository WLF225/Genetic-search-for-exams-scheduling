package com.example.aiproject.Classes;

import com.example.aiproject.Launcher;

public class Chromosome {

    // To score each chromosome during fitness evaluation
    private double power;
    private int mustBePenalty;
    private int preferablePenalty;
    private int betterPenalty;
    private FitnessSnapshot fitnessSnapshot;

    private byte[][] genes;

    // To create a chromosome for the initial random population
    public Chromosome(int capacity, double power, int mustBePenalty, int preferablePenalty, int betterPenalty) {
        this.power = power;
        this.mustBePenalty = mustBePenalty;
        this.preferablePenalty = preferablePenalty;
        this.betterPenalty = betterPenalty;
        genes = new byte[capacity][2];
        // To assign each course a random day (1-6) and a random time slot (1-3)
        for (int i = 0; i < capacity; i++) {
            genes[i][0] = (byte) ((Math.random() * 6) + 1);
            genes[i][1] = (byte) ((Math.random() * 3) + 1);
        }
        fitnessSnapshot = fitness();
    }

    // To clone a chromosome without sharing its gene array reference
    public Chromosome(Chromosome other) {
        this.power = other.power;
        this.mustBePenalty = other.mustBePenalty;
        this.preferablePenalty = other.preferablePenalty;
        this.betterPenalty = other.betterPenalty;
        this.fitnessSnapshot = other.fitnessSnapshot;
        this.genes = new byte[other.genes.length][2];
        for (int i = 0; i < other.genes.length; i++) {
            this.genes[i][0] = other.genes[i][0];
            this.genes[i][1] = other.genes[i][1];
        }
    }

    public byte[][] getGenes() {
        return genes;
    }

    public void setGenes(byte[][] genes) {
        this.genes = genes;
        fitnessSnapshot = fitness();
    }

    public void mutate(double mutationRate) {
        fitnessSnapshot = null;
        for (int i = 0; i < genes.length; i++) {
            if (Math.random() < mutationRate) {
                genes[i][0] = (byte) ((Math.random() * 6) + 1);
                genes[i][1] = (byte) ((Math.random() * 3) + 1);
            }
        }
        fitnessSnapshot = fitness();
    }

    public FitnessSnapshot fitness() {

        FitnessSnapshot snapshot = new FitnessSnapshot(mustBePenalty, preferablePenalty, betterPenalty);

        // To find out which of the six days are actually used across all courses
        boolean[] weekDays = new boolean[6];
        for (byte[] gene : genes) {
            weekDays[gene[0] - 1] = true;
        }

        for (Student student : Launcher.data.getStudents()) {
            byte[][] slots = new byte[6][3];
            byte[] days = new byte[6];

            boolean moreThan2Bool = false, sameTimeBool = false, sameDayBool = false;

            for (int index : student.getCourseIndexes()) {
                int row = genes[index][0] - 1;
                int col = genes[index][1] - 1;

                days[row]++;

                // To increase the number of students who have more than 2 exams on the same day
                if (days[row] > 2 && !moreThan2Bool) {
                    snapshot.incrementMoreThan2InDay();
                    moreThan2Bool = true;
                }

                // To increase the number of students who have 2 exams on the same day
                if (days[row] == 2 && !sameDayBool) {
                    snapshot.incrementTwoSameDay();
                    sameDayBool = true;
                }

                slots[row][col]++;

                // To increase the number of students who have 2 exams on the same time slot
                if (slots[row][col] == 2 && !sameTimeBool) {
                    snapshot.incrementSameTime();
                    sameTimeBool = true;
                }
            }

            // To increase the number of students who have 4 exams on 2 days in a row
            boolean fourExamsBool = false;
            for (int i = 0; i < 5 && !fourExamsBool; i++) {
                if (days[i] + days[i + 1] >= 4) {
                    snapshot.incrementFourInTwoDays();
                    fourExamsBool = true;
                }
            }
        }

        //To calculate the number of distinct days used across all exams
        for (boolean used : weekDays) {
            if (used)
                snapshot.incrementDaysUsed();
        }

        double studentPop = Launcher.data.getStudents().length;

        // To calculate the fitness score of the chromosome
        snapshot.reduceFitness(mustBePenalty*Math.pow((double) snapshot.getSameTime() /studentPop, power));
        snapshot.reduceFitness(mustBePenalty*Math.pow((double) snapshot.getFourInTwoDays()/studentPop, power));
        snapshot.reduceFitness(mustBePenalty*Math.pow((double) snapshot.getMoreThan2InDay()/studentPop, power));
        snapshot.reduceFitness(betterPenalty * Math.pow((double) snapshot.getTwoSameDay()/studentPop, power));
        snapshot.reduceFitness(preferablePenalty * Math.pow(snapshot.getDaysUsed() / 6.0,power));

        return snapshot;
    }

    public FitnessSnapshot getFitnessSnapshot() {
        return fitnessSnapshot;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte[] gene : genes) sb.append(gene[0]).append(" ");
        sb.append("\n");
        for (byte[] gene : genes) sb.append(gene[1]).append(" ");
        return sb.toString();
    }
}
