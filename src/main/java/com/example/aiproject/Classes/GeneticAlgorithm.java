package com.example.aiproject.Classes;

import com.example.aiproject.Launcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// To encapsulate the full GA engine and make all parameters configurable from the UI
public class GeneticAlgorithm {

    // To hold all tunable parameters in one place for easy experimentation
    private final int populationSize;
    private final int maxGenerations;
    private final double mutationRate;
    private final double power;
    private final int mustBePenalty;
    private final int preferablePenalty;
    private final int betterPenality;


    // To track the evolving population and the best chromosome seen so far
    private Chromosome[] population;
    private Chromosome bestEver;
    private double bestEverFitness = -1;

    // To record the best fitness at each generation so the convergence chart can be drawn
    private final List<Double> convergenceHistory = new ArrayList<>();


    public GeneticAlgorithm(int populationSize, int maxGenerations, double mutationRate,
                            double power, int mustBePenalty, int preferablePenalty, int betterPenality){

        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.mutationRate = mutationRate;
        this.power = power;
        this.mustBePenalty = mustBePenalty;
        this.preferablePenalty = preferablePenalty;
        this.betterPenality = betterPenality;
    }

    public Chromosome run(){
        int courseCount = Launcher.data.getCourses().length;
        // To start fresh so the crossover viewer shows only this run's events
        convergenceHistory.clear();
        Crossover.clearHistory();


        // To seed the initial population with random chromosomes
        population = new Chromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            population[i] = new Chromosome(courseCount, power, mustBePenalty, preferablePenalty, betterPenality);
        }

        for (int gen = 0; gen < maxGenerations; gen++) {

            // To score every chromosome fitness in the current generation
            double[] fitnesses = evaluate(population);

            // To update the all-time best whenever a fitter chromosome is found
            for (int i = 0; i < populationSize; i++) {
                if (fitnesses[i] > bestEverFitness) {
                    bestEverFitness = fitnesses[i];
                    bestEver = new Chromosome(population[i]);
                }
            }
            convergenceHistory.add(bestEverFitness);


            // To build the next generation using selection crossover and mutation
            Chromosome[] nextGen = new Chromosome[populationSize];

            // To preserve the best chromosome unchanged
            nextGen[0] = new Chromosome(bestEver);

            for (int i = 1; i < populationSize; i += 2) {
                Chromosome p1 = selection(population, fitnesses);
                Chromosome p2 = selection(population, fitnesses);

                Chromosome c1 = new Chromosome(p1);
                Chromosome c2 = new Chromosome(p2);

                // To apply the crossover
                Crossover.crossover(c1, c2, gen);


                c1.mutate(mutationRate);
                c2.mutate(mutationRate);

                nextGen[i] = c1;
                if (i + 1 < populationSize) nextGen[i + 1] = c2;
            }

            population = nextGen;
        }

        return bestEver;
    }

    // To compute fitness scores for every chromosome in one pass
    private double[] evaluate(Chromosome[] pop){
        double[] f = new double[pop.length];

        for (int i = 0; i < pop.length; i++)
            f[i] = pop[i].getFitnessSnapshot().getFitness();

        return f;
    }

    private Chromosome selection(Chromosome[] pop, double[] fitnesses) {
        double temperature = 15;

        // To give each fitness to the power to make the 3000 have much more weight than 2900
        double[] scaled = new double[fitnesses.length];
        double total = 0;
        for (int i = 0; i < fitnesses.length; i++) {
            scaled[i] = Math.pow(fitnesses[i], temperature);
            total += scaled[i];
        }

        // To get the random value we will compare against
        double spin = Math.random() * total;

        double cumulative = 0;
        for (int i = 0; i < pop.length; i++) {
            cumulative += scaled[i];
            if (cumulative >= spin)
                return pop[i];
        }
        return pop[pop.length - 1];
    }

    // To save the convergence history to a CSV file after each run
    public void saveConvergenceToFile(String filePath) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Generation,BestFitness");
            DecimalFormat df = new DecimalFormat("0.00");
            for (int i = 0; i < convergenceHistory.size(); i++) {
                pw.println(i + "," + df.format(convergenceHistory.get(i)));
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not write convergence file: " + e.getMessage());
        }
    }

    // To save the best schedule to a CSV file after each run
    public void saveScheduleToFile(String filePath, Chromosome best, Course[] courses) {
        String[] times = {"09:00-11:00", "12:00-14:00", "15:00-17:00"};
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Course,Day,Slot,Time");
            byte[][] genes = best.getGenes();
            for (int i = 0; i < genes.length; i++) {
                int day  = genes[i][0];
                int slot = genes[i][1];
                pw.println(courses[i].getName() + ",Day " + day + "," + slot + "," + times[slot - 1]);
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not write schedule file: " + e.getMessage());
        }
    }

}
