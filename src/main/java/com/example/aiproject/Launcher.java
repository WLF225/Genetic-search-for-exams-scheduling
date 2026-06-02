package com.example.aiproject;

import com.example.aiproject.Classes.*;
import java.util.Scanner;

public class Launcher {

    public static DatasetReader.Data data;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        data = DatasetReader.readDataset("ga_exam_timetable_dataset.xlsx");

        System.out.print("Population size [default 100]: ");
        int population = readInt(sc, 100);

        System.out.print("Max generations [default 200]: ");
        int generations = readInt(sc, 200);

        System.out.print("Mutation rate [default 0.1]: ");
        double mutation = readDouble(sc, 0.1);

        System.out.print("Power (p) [default 0.85]: ");
        double power = readDouble(sc, 0.85);

        System.out.println("\n─────────────────────────────────────────");
        System.out.println("  GA Exam Scheduler");
        System.out.printf ("  Population: %d  |  Generations: %d%n", population, generations);
        System.out.printf ("  Mutation: %.3f  |  Power: %.2f%n", mutation, power);
        System.out.println("─────────────────────────────────────────\n");

        GeneticAlgorithm ga = new GeneticAlgorithm(population, generations, mutation, power, 1000, 500, 100);


        Chromosome best = ga.run();

        FitnessSnapshot snap = best.getFitnessSnapshot();
        System.out.printf("Fitness: %4.2f  |  Same time: %d  |  More than 2 in a day: %d  |" +
                        "  4 in 2days: %d  |  2 same day: %d  |  Days used: %d%n",
                snap.getFitness(),
                snap.getSameTime(),
                snap.getMoreThan2InDay(),
                snap.getFourInTwoDays(),
                snap.getTwoSameDay(),
                snap.getDaysUsed()
        );

        // Print final schedule
        System.out.println("\n─────────────────────────────────────────");
        System.out.println("  Best Schedule Found");
        System.out.println("─────────────────────────────────────────");
        String[] times = {"09:00-11:00", "12:00-14:00", "15:00-17:00"};
        byte[][] genes = best.getGenes();
        Course[] courses = data.getCourses();
        System.out.printf("%-12s  %-6s  %s%n", "Course", "Day", "Time");
        System.out.println("─────────────────────────────────────────");
        for (int i = 0; i < genes.length; i++) {
            System.out.printf("%-12s  Day %d   %s%n",
                    courses[i].getName(), genes[i][0], times[genes[i][1] - 1]);
        }

        // Save CSVs
        ga.saveConvergenceToFile("convergence_run.csv");
        ga.saveScheduleToFile("schedule_result.csv", best, courses);
        System.out.println("\nSaved: convergence_run.csv  |  schedule_result.csv");
    }

    private static int readInt(Scanner sc, int def) {
        String line = sc.nextLine().trim();
        return line.isEmpty() ? def : Integer.parseInt(line);
    }

    private static double readDouble(Scanner sc, double def) {
        String line = sc.nextLine().trim();
        return line.isEmpty() ? def : Double.parseDouble(line);
    }
}