package com.job.monitoring.controllers;

import com.job.monitoring.ui.TileButton;
import com.job.monitoring.utils.RefreshJobStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StatusPageController implements Initializable {
    // UI Elements
    @FXML
    private HBox headerBox;

    @FXML
    private SplitPane contentSplitBox;

    @FXML
    private AnchorPane anchorPane1;

    @FXML
    private ScrollPane scrollPane1;

    @FXML
    private AnchorPane anchorPane2;

    @FXML
    private ScrollPane scrollPane2;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private Button closeAppBtn;

    @FXML
    public HBox statusBar;

    // Non UI elements
    private List<String> jobList;           // This is set by reading Job names from the Job name file.
    private String logDir;
    private List<TileButton> jobs;           // List of Buttons, each button represents a job.
    ScheduledExecutorService executor;
    private String appServerCmdTemplate;
    private Stage loginStage;

    private Label statusMsg;
    private String jobLogLocation;

    /**
     * This method gets executed first when the Controller is created.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jobs = new ArrayList<>();
        executor = Executors.newScheduledThreadPool(2);

        // Add a Status message field to the Status bar
        Label statusMsg = new Label();
        statusMsg.getStyleClass().add("text-field");
        this.statusMsg = statusMsg;

        statusBar.getChildren().addAll(statusMsg);
    }

    /**
     * This method is called from other controller. It sets the "jobList" and Location of Log directory which
     * are read from the UI from User. To read log files from the App Server these two are required.
     * <p>
     * This method also creates a Button for each of the jobs. Each Button's name is Job name. Each button also
     * has a "jobLog" property which is to store the Log of that job. When a Button is clicked, it log will be
     * shown on the Text Area.
     *
     * @param jobList
     * @param logDir
     */
    public void setJobList(Stage loginStage,
                           String jobLogLocation,
                           List<String> jobList,
                           List<String> statuses,
                           String logDir,
                           String appServerCmdTemplate) {
        this.jobList = jobList;
        this.logDir = logDir;
        this.appServerCmdTemplate = appServerCmdTemplate;
        this.loginStage = loginStage;
        this.jobLogLocation = jobLogLocation;

        // Create a Tile pane
        TilePane tilePane = new TilePane(Orientation.HORIZONTAL);
        tilePane.setHgap(10.0);
        tilePane.setVgap(10.0);
        tilePane.setPadding(new Insets(20, 20, 20, 20));
//        tilePane.setPrefColumns(3);
        tilePane.setMaxWidth(Region.USE_PREF_SIZE);

        boolean showProgressIndicator = false;
        String btnClass = "";

        for (int i = 0; i < jobList.size(); i++) {
            String jobStatus = statuses.get(i).toUpperCase();

            if (jobStatus.equalsIgnoreCase("not-started")) {
                showProgressIndicator = false;
                btnClass = "job-not-started";
            } else if (jobStatus.equalsIgnoreCase("success")) {
                showProgressIndicator = false;
                btnClass = "job-success";
            } else if (jobStatus.equalsIgnoreCase("failed")) {
                showProgressIndicator = false;
                btnClass = "job-failed";
            } else if (jobStatus.equalsIgnoreCase("running")) {
                showProgressIndicator = true;
                btnClass = "job-running";
            }

            TileButton tile = new TileButton(jobList.get(i), "BLACK", "yellow", jobStatus, showProgressIndicator, btnClass);
            tilePane.getChildren().addAll(tile);

            // Store all Button references
            jobs.add(tile);

            // When the button is clicked, populate text area with Job log.
            tile.getStatusBtn().setOnAction(ActionEvent -> {
                resultTextArea.setText(tile.getJobLog());
            });
        }

        scrollPane1.setContent(tilePane);
        scrollPane1.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane1.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        refreshJobLogs();
    }

    /**
     * Read job logs on App Server and updates the background color of the Buttons. In addition to this, sets
     * "jobLog" property of each Button with actual job log.
     */
    private void refreshJobLogs() {
        // Monitor the jobs
        String logDir = this.logDir;
        String logFile = this.jobLogLocation;

        RefreshJobStatus task = new RefreshJobStatus(this.jobs, logFile, logDir, this.appServerCmdTemplate, statusMsg);
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(task, 1, 10, TimeUnit.SECONDS);

        resultTextArea.getStyleClass().add("result-area");
    }

    @FXML
    private void stopApplication(ActionEvent event) {
        executor.shutdown();

        // Close the Window
        Stage stage = (Stage) closeAppBtn.getScene().getWindow();
        stage.close();

        // Close the first window
        loginStage.close();

        System.exit(0);
    }
}