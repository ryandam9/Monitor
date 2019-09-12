package com.job.monitoring.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class TileButton extends VBox {
    private Label label;
    private ProgressIndicator progressIndicator;
    private Button button;
    private String jobLog;

    public TileButton(String jobName, String tileColor, String fontColor) {
        setPrefSize(100, 100);
        setSpacing(10);
        setStyle("-fx-background-color: " + tileColor + ";");
        setPadding(new Insets(10, 10, 10, 10));

        // Job name
        Label l1 = createLabel(jobName, fontColor);

        // Progress Indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(70, 70);
        progressIndicator.setVisible(false);

        // Job Status Color
        Button statusBtn = new Button("");

        this.getChildren().addAll(l1, progressIndicator, statusBtn);
    }

    private Label createLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + color);
        return label;
    }

    private Button makeButton(String name) {
        Button button = new Button(name);
        button.getStyleClass().add("button-raised");
        return button;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public Button getButton() {
        return button;
    }

    public void setJobLog(String log) {
        this.jobLog = log;
    }

    public String getJobLog() {
        return this.jobLog;
    }
}