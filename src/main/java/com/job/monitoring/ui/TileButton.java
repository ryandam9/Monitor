package com.job.monitoring.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public final class TileButton extends VBox {
    private final String jobName;
    private final Label label;
    private final ProgressIndicator progressIndicator;
    private final Button statusBtn;
    private String jobLog;

    public TileButton(String jobName, String tileColor, String fontColor, String jobStatus, boolean showProgressIndicator, String statusColor) {
        this.getStyleClass().add("tile-box");
        setStyle("-fx-background-color: " + tileColor + ";");

        this.jobName = jobName;

        // Job name
        label = createLabel(jobName, fontColor);
        label.getStyleClass().add("tile-label");

        // Progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(showProgressIndicator);

        // Job Status Color
        statusBtn = makeButton(jobStatus, statusColor);

        getChildren().addAll(label, progressIndicator, statusBtn);
    }

    private Label createLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + color);
        return label;
    }

    private Button makeButton(String name, String statusColor) {
        Button button = new Button(name);
        button.getStyleClass().add("tile-button");
        button.setStyle("-fx-background-color:" + statusColor);
        return button;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public Button getStatusBtn() {
        return statusBtn;
    }

    public void setJobLog(String log) {
        this.jobLog = log;
    }

    public String getJobLog() {
        return this.jobLog;
    }

    public String getJobName() {
        return jobName;
    }
}