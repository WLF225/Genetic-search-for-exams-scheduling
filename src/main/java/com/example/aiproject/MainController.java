package com.example.aiproject;

import com.example.aiproject.Classes.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController implements Initializable {

    // To handle the Run tab where the user configures and launches a GA run
    @FXML private Spinner<Integer> spPopulation, spGenerations;
    @FXML private Spinner<Double>  spMutation, spPower;
    @FXML private Button btnRun, btnStop;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus, lblBestFitness, lblGeneration, lblSameTime,
            lblMoreThan2InDay, lblFourInTwoDays, lblTwoDaysConflict, lblDaysUsed;
    @FXML private TableView<ScheduleRow>    scheduleTable;
    @FXML private TableColumn<ScheduleRow, String>  colCourse, colTime;
    @FXML private TableColumn<ScheduleRow, Integer> colDay, colSlot;

    // To handle the Crossover viewer tab for step-by-step playback
    @FXML private Canvas crossoverCanvas;
    @FXML private Label  lblCrossoverStep, lblCrossoverGen, lblP1Fitness, lblP2Fitness,
            lblC1Fitness, lblC2Fitness, lblCrossoverNote;

    // To handle the Convergence chart tab for parameter-tuning experiments
    @FXML private ComboBox<String> cmbTuningParam;
    @FXML private LineChart<Number, Number> convergenceChart;
    @FXML private Label lblExperimentStatus;
    @FXML private TableView<ExperimentRow> experimentsTable;
    @FXML private TableColumn<ExperimentRow, String> colExpLabel;
    @FXML private TableColumn<ExperimentRow, Double> colExpFinal;
    @FXML private TableColumn<ExperimentRow, Integer> colExpGens, colExpPop;
    @FXML private TableColumn<ExperimentRow, Double>  colExpMut, colExpPow;


    // To track mutable UI state between user interactions
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private int crossoverIndex = 0;
    private int currentGeneration = 0;

    private static final String[] TIMES = {
        "09:00–11:00", "12:00–14:00", "15:00–17:00"
    };

    // To set up column factories, spinner ranges, and chart settings on startup
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // To bind each table column to the matching field of ScheduleRow
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().course));
        colDay.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().day).asObject());
        colSlot.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().slot).asObject());
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));
        styleColumn(colCourse); styleColumn(colDay); styleColumn(colSlot); styleColumn(colTime);

        // To bind each experiments column to the matching field of ExperimentRow
        colExpLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label));
        colExpFinal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().finalFitness).asObject());
        colExpGens.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().generations).asObject());
        colExpMut.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().mutation).asObject());
        colExpPow.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().power).asObject());
        colExpPop.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().population).asObject());

        // To populate the parameter-tuning dropdown with the available options
        cmbTuningParam.setItems(FXCollections.observableArrayList("Mutation rate", "Power (p)", "Population size"));

        cmbTuningParam.getSelectionModel().selectFirst();

        // To configure the convergence chart to draw clean lines without data-point symbols
        convergenceChart.setCreateSymbols(false);
        convergenceChart.setAnimated(false);

        // To configure the Double spinners with step sizes appropriate for each parameter
        spMutation.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.001, 0.5, 0.1, 0.005));
        spPower.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.05, 2.0, 0.85, 0.05));
    }

    // To start a background GA thread when the user clicks Run
    @FXML
    private void onRunGA() {
        stopFlag.set(false);
        btnRun.setDisable(true);
        btnStop.setDisable(false);
        progressBar.setProgress(0);
        lblStatus.setText("Running…");

        int pop = spPopulation.getValue();
        int gens = spGenerations.getValue();
        double mut = spMutation.getValue();
        double power = spPower.getValue();

        Thread t = new Thread(() -> {
            GeneticAlgorithm ga = new GeneticAlgorithm(pop, gens, mut, power,
                    1000, 100);

            Chromosome best = ga.run((gen, maxGen, bestFit, bestChr) -> {
                Platform.runLater(() -> {
                    progressBar.setProgress((double)(gen+1) / maxGen);
                    lblStatus.setText("Gen " + (gen+1) + " / " + maxGen);
                    lblGeneration.setText(String.valueOf(gen+1));
                    FitnessSnapshot snap = bestChr.fitness();
                    lblBestFitness.setText(String.format("%.1f", snap.getFitness()));
                    lblSameTime.setText(String.valueOf(snap.getSameTime()));
                    lblMoreThan2InDay.setText(String.valueOf(snap.getMoreThan2InDay()));
                    lblFourInTwoDays.setText(String.valueOf(snap.getFourInTwoDays()));
                    lblTwoDaysConflict.setText(String.valueOf(snap.getTwoSameDay()));
                    lblDaysUsed.setText(String.valueOf(snap.getDaysUsed()));
                });
                return !stopFlag.get();
            });

            Platform.runLater(() -> {
                btnRun.setDisable(false);
                btnStop.setDisable(true);
                progressBar.setProgress(1);
                lblStatus.setText("Done — best fitness: " + String.format("%.2f", ga.getBestEverFitness()));
                populateScheduleTable(best);
                // To populate the crossover viewer immediately after a run completes
                crossoverIndex = 0;
                currentGeneration = 0;
                renderCrossoverStep();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML private void onStop() { stopFlag.set(true); }

    private void populateScheduleTable(Chromosome chr) {
        byte[][] genes = chr.getGenes();
        Course[] courses = Launcher.data.getCourses();
        ObservableList<ScheduleRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < genes.length; i++) {
            int day  = genes[i][0];
            int slot = genes[i][1];
            rows.add(new ScheduleRow(courses[i].getName(), day, slot,
                "Day " + day + "  " + TIMES[slot - 1]));
        }
        rows.sort((a, b) -> a.day != b.day ? a.day - b.day : a.slot - b.slot);
        scheduleTable.setItems(rows);
    }

    // To navigate the crossover history and draw each step on the canvas
    @FXML private void onCrossoverFirst() { crossoverIndex = 0; currentGeneration = 0; renderCrossoverStep(); }
    @FXML private void onCrossoverLast()  {
        List<Crossover.CrossoverStep> h = Crossover.getHistory();
        if (!h.isEmpty()) crossoverIndex = h.size() - 1;
        renderCrossoverStep();
    }
    @FXML private void onCrossoverNext() {
        if (crossoverIndex < Crossover.getHistory().size() - 1) crossoverIndex++;
        renderCrossoverStep();
    }
    @FXML private void onCrossoverPrev() {
        if (crossoverIndex > 0) crossoverIndex--;
        renderCrossoverStep();
    }
    @FXML
    private void onNextGeneration() {
        List<Crossover.CrossoverStep> history = Crossover.getHistory();
        for (int i = crossoverIndex + 1; i < history.size(); i++) {
            if (history.get(i).generation > currentGeneration) {
                crossoverIndex = i;
                currentGeneration = history.get(i).generation;
                renderCrossoverStep();
                return;
            }
        }
    }

    @FXML
    private void onPrevGeneration() {
        List<Crossover.CrossoverStep> history = Crossover.getHistory();
        for (int i = crossoverIndex - 1; i >= 0; i--) {
            if (history.get(i).generation < currentGeneration) {
                int targetGen = history.get(i).generation;
                // To land on the first step of that generation, not the last
                while (i > 0 && history.get(i - 1).generation == targetGen) i--;
                crossoverIndex = i;
                currentGeneration = history.get(crossoverIndex).generation;
                renderCrossoverStep();
                return;
            }
        }
    }

    private void renderCrossoverStep() {
        List<Crossover.CrossoverStep> history = Crossover.getHistory();
        if (history.isEmpty()) {
            lblCrossoverNote.setText("Run the GA first to populate crossover steps.");
            lblCrossoverStep.setText("— / —");
            return;
        }

        lblCrossoverNote.setText("");
        lblCrossoverStep.setText((crossoverIndex + 1) + " / " + history.size());
        Crossover.CrossoverStep step = history.get(crossoverIndex);
        lblCrossoverGen.setText("Gen " + step.generation);
        lblP1Fitness.setText(String.format("%.1f", step.parent1Fitness));
        lblP2Fitness.setText(String.format("%.1f", step.parent2Fitness));
        lblC1Fitness.setText(String.format("%.1f", step.child1Fitness));
        lblC2Fitness.setText(String.format("%.1f", step.child2Fitness));

        drawCrossover(step);
    }

    private void drawCrossover(Crossover.CrossoverStep step) {
        GraphicsContext gc = crossoverCanvas.getGraphicsContext2D();
        int n = step.parent1Before.length;
        double w = crossoverCanvas.getWidth();
        double cellW = Math.min(40, (w - 20.0) / n);
        double cellH = 28;
        double rowGap = 4;   // gap between day and slot row of the same chromosome
        double groupGap = 14; // gap between chromosomes
        double y0 = 8;

        gc.clearRect(0, 0, w, crossoverCanvas.getHeight());
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 11));

        // To show both day and slot for each of the 4 chromosomes (8 rows total)
        // Each chromosome gets 2 rows: day row then slot row
        String[] labels = {
                "P1 day", "P1 slot",
                "P2 day", "P2 slot",
                "C1 day", "C1 slot",
                "C2 day", "C2 slot"
        };

        // To map each row to its gene matrix and which byte index ([0]=day, [1]=slot)
        byte[][][] geneSources = {
                step.parent1Before, step.parent1Before,
                step.parent2Before, step.parent2Before,
                step.child1After,   step.child1After,
                step.child2After,   step.child2After
        };
        int[] byteIndex = {0, 1, 0, 1, 0, 1, 0, 1};

        Color pColor = Color.web("#4a90d9");
        Color cColor = Color.web("#4ecca3");
        Color[] rowColors = {
                pColor, pColor,
                pColor, pColor,
                cColor, cColor,
                cColor, cColor
        };

        // To compute the y position of each row, grouping day+slot pairs together
        double[] yPositions = new double[8];
        double y = y0;
        for (int r = 0; r < 8; r++) {
            yPositions[r] = y;
            boolean isSlotRow = (r % 2 == 1);
            // To add a larger gap after each slot row to visually separate chromosomes
            y += cellH + (isSlotRow ? groupGap : rowGap);
        }

        for (int r = 0; r < 8; r++) {
            double ry = yPositions[r];
            gc.setFill(Color.web("#ccc"));
            gc.fillText(labels[r], 2, ry + cellH * 0.65);

            for (int i = 0; i < n; i++) {
                double x = 65 + i * cellW;
                boolean inSwap = (i >= step.crossoverPoint1 && i < step.crossoverPoint2);
                Color fill = inSwap ? Color.web("#e94560") : rowColors[r];
                gc.setFill(fill.deriveColor(0, 1, 1, 0.25));
                gc.fillRoundRect(x, ry, cellW - 2, cellH, 4, 4);
                gc.setStroke(fill);
                gc.setLineWidth(inSwap ? 2 : 1);
                gc.strokeRoundRect(x, ry, cellW - 2, cellH, 4, 4);
                gc.setFill(Color.WHITE);
                gc.fillText(String.valueOf(geneSources[r][i][byteIndex[r]]),
                        x + cellW * 0.3, ry + cellH * 0.65);
            }
        }

        // To draw dashed vertical lines spanning all 8 rows at the two cut points
        if (n > 0) {
            double totalHeight = yPositions[7] + cellH - y0;
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(1.5);
            gc.setLineDashes(4);
            for (int cp : new int[]{step.crossoverPoint1, step.crossoverPoint2}) {
                double x = 65 + cp * cellW - 1;
                gc.strokeLine(x, y0 - 4, x, y0 + totalHeight + 4);
            }
            gc.setLineDashes(0);
        }
    }

    // To run multiple GA variants and plot their convergence for comparison
    @FXML
    private void onAddExperiment() {
        String param = cmbTuningParam.getValue();
        if (param == null) return;

        // To read the current spinner values as the baseline for each experiment
        int basePop = spPopulation.getValue();
        int baseGens = spGenerations.getValue();
        double baseMut = spMutation.getValue();
        double basePow = spPower.getValue();

        // To create three test values that bracket the typical useful range for this parameter
        double[] variants;
        String[] variantLabels;
        switch (param) {
            case "Mutation rate":
                variants = new double[]{0.005, 0.02, 0.1};
                variantLabels = new String[]{"mut=0.005", "mut=0.02", "mut=0.1"};
                break;
            case "Power (p)":
                variants = new double[]{0.2, 0.35, 0.8};
                variantLabels = new String[]{"p=0.2", "p=0.35", "p=0.8"};
                break;
            default:
                variants = new double[]{30, 100, 300};
                variantLabels = new String[]{"pop=30", "pop=100", "pop=300"};
                break;
        }

        lblExperimentStatus.setText("Running experiments…");
        btnRun.setDisable(true);

        Thread t = new Thread(() -> {
            for (int v = 0; v < variants.length; v++) {
                final int pop = param.equals("Population size") ? (int)variants[v] : basePop;
                final double mut = param.equals("Mutation rate") ? variants[v] : baseMut;
                final double power = param.equals("Power (p)") ? variants[v] : basePow;
                final String label = variantLabels[v];

                GeneticAlgorithm ga = new GeneticAlgorithm(pop, baseGens, mut, power,
                        1000, 100);
                ga.run();

                List<Double> hist = ga.getConvergenceHistory();
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(label);
                for (int g = 0; g < hist.size(); g++) {
                    final int gi = g; final double fi = hist.get(g);
                    series.getData().add(new XYChart.Data<>(gi, fi));
                }

                final double finalFit = ga.getBestEverFitness();
                final int finalPop = pop;
                final double finalMut = mut;
                final double finalPow = power;

                Platform.runLater(() -> {
                    convergenceChart.getData().add(series);
                    experimentsTable.getItems().add(new ExperimentRow(
                        label, finalFit, hist.size(), finalMut, finalPow, finalPop));
                });
            }
            Platform.runLater(() -> {
                lblExperimentStatus.setText("Done.");
                btnRun.setDisable(false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onClearExperiments() {
        convergenceChart.getData().clear();
        experimentsTable.getItems().clear();
        lblExperimentStatus.setText("");
    }

    // To apply white text so column headers are readable on the dark background
    private <S, T> void styleColumn(TableColumn<S, T> col) {
        col.setStyle("-fx-text-fill: white;");
    }

    // To define lightweight row models for the schedule and experiments tables
    public static class ScheduleRow {
        final String course; final int day, slot; final String time;
        ScheduleRow(String c, int d, int s, String t) {
            course=c; day=d; slot=s; time=t;
        }
    }

    public static class ExperimentRow {
        final String label; final double finalFitness; final int generations;
        final double mutation,  power; final int population;
        ExperimentRow(String l, double ff, int g, double m, double p, int pop) {
            label=l;
            finalFitness=ff;
            generations=g;
            mutation=m;
            power=p;
            population=pop;
        }
    }
}
