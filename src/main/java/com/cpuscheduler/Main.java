package com.cpuscheduler;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.List;
import java.util.Map;

public class Main extends Application {
    private VBox processInputArea;
    private VBox ganttChartArea;
    private VBox metricsArea;
    private ComboBox<String> algorithmSelector;
    private TableView<Process> processTable;
    private SchedulerSimulator scheduler;
    private TextField quantumField;

    @Override
    public void start(Stage primaryStage) {
        scheduler = new SchedulerSimulator();
        
        BorderPane mainLayout = new BorderPane();
        
        VBox topArea = createTopArea();
        mainLayout.setTop(topArea);
        
        processInputArea = createProcessInputArea();
        mainLayout.setCenter(processInputArea);
        
        VBox bottomArea = createBottomArea();
        mainLayout.setBottom(bottomArea);
        
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setTitle("CPU Scheduling Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createTopArea() {
        VBox topArea = new VBox(10);
        topArea.setPadding(new Insets(10));
        
        Label titleLabel = new Label("CPU Scheduling Algorithm Simulator");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        algorithmSelector = new ComboBox<>(FXCollections.observableArrayList(
            "First Come First Serve (FCFS)",
            "Shortest Job First (SJF)",
            "Shortest Remaining Time First (SRTF)",
            "Round Robin"
        ));
        algorithmSelector.setValue("First Come First Serve (FCFS)");
        
        HBox algorithmBox = new HBox(10);
        quantumField = new TextField();
        quantumField.setPromptText("Time Quantum");
        quantumField.setVisible(false);
        
        algorithmSelector.setOnAction(e -> {
            quantumField.setVisible(algorithmSelector.getValue().contains("Round Robin"));
        });
        
        algorithmBox.getChildren().addAll(
            new Label("Select Algorithm:"), 
            algorithmSelector,
            quantumField
        );
        
        topArea.getChildren().addAll(titleLabel, algorithmBox);
        return topArea;
    }

    private VBox createProcessInputArea() {
        VBox inputArea = new VBox(10);
        inputArea.setPadding(new Insets(10));
        
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(5);
        
        TextField processIdField = new TextField();
        TextField arrivalTimeField = new TextField();
        TextField burstTimeField = new TextField();
        
        inputGrid.addRow(0, new Label("Process ID:"), processIdField);
        inputGrid.addRow(1, new Label("Arrival Time:"), arrivalTimeField);
        inputGrid.addRow(2, new Label("Burst Time:"), burstTimeField);
        
        Button addProcessButton = new Button("Add Process");
        addProcessButton.setOnAction(e -> {
            try {
                String processId = processIdField.getText();
                int arrivalTime = Integer.parseInt(arrivalTimeField.getText());
                int burstTime = Integer.parseInt(burstTimeField.getText());
                
                Process process = new Process(processId, arrivalTime, burstTime);
                scheduler.addProcess(process);
                updateProcessTable();
                
                processIdField.clear();
                arrivalTimeField.clear();
                burstTimeField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for arrival and burst times.");
            }
        });
        
        processTable = new TableView<>();
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Process, String> idColumn = new TableColumn<>("Process ID");
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProcessId()));
        
        TableColumn<Process, Integer> arrivalColumn = new TableColumn<>("Arrival Time");
        arrivalColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getArrivalTime()).asObject());
        
        TableColumn<Process, Integer> burstColumn = new TableColumn<>("Burst Time");
        burstColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getBurstTime()).asObject());
        
        processTable.getColumns().add(idColumn);
        processTable.getColumns().add(arrivalColumn);
        processTable.getColumns().add(burstColumn);
        
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            scheduler.clearProcesses();
            updateProcessTable();
            clearVisualization();
        });
        
        Button simulateButton = new Button("Run Simulation");
        simulateButton.setOnAction(e -> runSimulation());
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addProcessButton, clearButton, simulateButton);
        
        inputArea.getChildren().addAll(
            new Label("Add Process Details:"),
            inputGrid,
            buttonBox,
            new Separator(),
            processTable
        );
        
        return inputArea;
    }

    private VBox createBottomArea() {
        VBox bottomArea = new VBox(10);
        bottomArea.setPadding(new Insets(10));
        
        ganttChartArea = new VBox();
        ganttChartArea.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        ganttChartArea.setPadding(new Insets(10));
        
        metricsArea = new VBox(5);
        metricsArea.setPadding(new Insets(10));
        metricsArea.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        
        bottomArea.getChildren().addAll(
            new Label("Gantt Chart:"),
            ganttChartArea,
            new Separator(),
            new Label("Metrics:"),
            metricsArea
        );
        
        return bottomArea;
    }

    private void updateProcessTable() {
        ObservableList<Process> processes = FXCollections.observableArrayList(scheduler.getProcesses());
        processTable.setItems(processes);
    }

    private void clearVisualization() {
        ganttChartArea.getChildren().clear();
        metricsArea.getChildren().clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void runSimulation() {
        if (scheduler.getProcesses().isEmpty()) {
            showAlert("Error", "Please add at least one process.");
            return;
        }

        clearVisualization();
        List<SchedulerSimulator.ExecutionStep> executionSteps = null;
        
        String selectedAlgorithm = algorithmSelector.getValue();
        try {
            switch (selectedAlgorithm) {
                case "First Come First Serve (FCFS)":
                    executionSteps = scheduler.runFCFS();
                    break;
                case "Shortest Job First (SJF)":
                    executionSteps = scheduler.runSJF();
                    break;
                case "Shortest Remaining Time First (SRTF)":
                    executionSteps = scheduler.runSRTF();
                    break;
                case "Round Robin":
                    try {
                        int quantum = Integer.parseInt(quantumField.getText());
                        if (quantum <= 0) throw new NumberFormatException();
                        executionSteps = scheduler.runRoundRobin(quantum);
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Input", "Please enter a valid time quantum (positive integer).");
                        return;
                    }
                    break;
            }
            
            if (executionSteps != null) {
                visualizeGanttChart(executionSteps);
                displayMetrics();
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred during simulation: " + e.getMessage());
        }
    }

    private void visualizeGanttChart(List<SchedulerSimulator.ExecutionStep> steps) {
        HBox ganttChart = new HBox(2);
        ganttChart.setPadding(new Insets(10));
        
        int totalTime = steps.stream()
            .mapToInt(SchedulerSimulator.ExecutionStep::getEndTime)
            .max()
            .orElse(0);
            
        double unitWidth = Math.min(50, (800.0 - 100) / totalTime);
        
        for (SchedulerSimulator.ExecutionStep step : steps) {
            double width = (step.getEndTime() - step.getStartTime()) * unitWidth;
            
            VBox processBox = new VBox(2);
            processBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            Rectangle rect = new Rectangle(width, 40);
            rect.setFill(Color.LIGHTBLUE);
            rect.setStroke(Color.BLACK);
            
            Text processId = new Text(step.getProcessId());
            processId.setTextAlignment(TextAlignment.CENTER);
            
            Text timeRange = new Text(step.getStartTime() + "-" + step.getEndTime());
            timeRange.setTextAlignment(TextAlignment.CENTER);
            
            processBox.getChildren().addAll(rect, processId, timeRange);
            ganttChart.getChildren().add(processBox);
        }
        
        ScrollPane scrollPane = new ScrollPane(ganttChart);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefViewportHeight(100);
        scrollPane.setStyle("-fx-background-color: white;");
        
        ganttChartArea.getChildren().add(scrollPane);
    }

    private void displayMetrics() {
        Map<String, Double> metrics = scheduler.getMetrics();
        metricsArea.getChildren().clear();
        
        metrics.forEach((metric, value) -> {
            String formattedValue = String.format("%.2f", value);
            Label metricLabel = new Label(metric + ": " + formattedValue);
            metricsArea.getChildren().add(metricLabel);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
} 