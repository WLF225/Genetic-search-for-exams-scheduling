package com.example.aiproject.Classes;

import java.util.ArrayList;
import java.util.List;

// To record and replay every crossover event for the visualizer tab
public class Crossover {

    // To store the full history of crossover steps produced during one GA run
    private static final List<CrossoverStep> history = new ArrayList<>();

    // To expose the recorded steps to the UI for playback
    public static List<CrossoverStep> getHistory() {
        return history;
    }

    // To perform two-point crossover on c1 and c2 and save a snapshot for the viewer
    public static void crossover(Chromosome c1, Chromosome c2, int generation) {
        byte[][] gene1 = c1.getGenes();
        byte[][] gene2 = c2.getGenes();

        int crossOverPoint1 = gene1.length / 3;
        int crossOverPoint2 = gene1.length - crossOverPoint1;

        // To capture the parents before any genes are swapped
        byte[][] p1Before = deepCopy(gene1);
        byte[][] p2Before = deepCopy(gene2);

        double p1Fitness = c1.fitness().getFitness();
        double p2Fitness = c2.fitness().getFitness();

        byte[][] newGenes1 = new byte[gene1.length][2];
        byte[][] newGenes2 = new byte[gene2.length][2];

        // To build the child chromosomes by swapping the middle segment
        for (int i = 0; i < gene1.length; i++) {
            if (i < crossOverPoint1 || i >= crossOverPoint2) {
                newGenes1[i] = gene1[i];
                newGenes2[i] = gene2[i];
            } else {
                newGenes1[i] = gene2[i];
                newGenes2[i] = gene1[i];
            }
        }

        c1.setGenes(newGenes1);
        c2.setGenes(newGenes2);


        history.add(new CrossoverStep(
                generation,
                p1Before, p2Before,
                deepCopy(newGenes1), deepCopy(newGenes2),
                p1Fitness, p2Fitness,
                c1.fitness().getFitness(), c2.fitness().getFitness(),
                crossOverPoint1, crossOverPoint2
        ));

    }

    // To clone a gene matrix so stored snapshots are independent of later mutations
    private static byte[][] deepCopy(byte[][] src) {
        byte[][] copy = new byte[src.length][2];
        for (int i = 0; i < src.length; i++) {
            copy[i][0] = src[i][0];
            copy[i][1] = src[i][1];
        }
        return copy;
    }

    public static void clearHistory() {
        history.clear();
    }

    // To bundle all data needed to draw one crossover frame in the viewer
    public static class CrossoverStep {
        public final int generation;
        public final byte[][] parent1Before, parent2Before;
        public final byte[][] child1After, child2After;
        public final double parent1Fitness, parent2Fitness;
        public final double child1Fitness, child2Fitness;
        public final int crossoverPoint1, crossoverPoint2;

        public CrossoverStep(int generation, byte[][] parent1Before, byte[][] parent2Before,
                             byte[][] child1After, byte[][] child2After, double parent1Fitness,
                             double parent2Fitness, double child1Fitness, double child2Fitness,
                             int crossoverPoint1, int crossoverPoint2) {
            this.generation = generation;
            this.parent1Before = parent1Before;
            this.parent2Before = parent2Before;
            this.child1After = child1After;
            this.child2After = child2After;
            this.parent1Fitness = parent1Fitness;
            this.parent2Fitness = parent2Fitness;
            this.child1Fitness = child1Fitness;
            this.child2Fitness = child2Fitness;
            this.crossoverPoint1 = crossoverPoint1;
            this.crossoverPoint2 = crossoverPoint2;
        }
    }
}
