package com.job.monitoring.controllers;

import com.job.monitoring.ui.JobDetail;
import com.job.monitoring.utils.RefreshJobStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private VBox jobListView;

    @FXML
    private AnchorPane anchorPane2;

    @FXML
    private ScrollPane scrollPane2;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private Button monitorBtn;

    @FXML
    private Button closeAppBtn;

    @FXML
    private Label statusMsg;

    @FXML
    private ProgressBar progressBar;

    // Non UI elements
    private List<String> jobList;           // This is set by reading Job names from the Job name file.
    private String logDir;
    private List<JobDetail> jobs;           // List of Buttons, each button represents a job.
    ScheduledExecutorService executor;
    private String appServerCmdTemplate;
    private Stage loginStage;

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
    public void setJobList(Stage loginStage, List<String> jobList, String logDir, String appServerCmdTemplate) {
        this.jobList = jobList;
        this.logDir = logDir;
        this.appServerCmdTemplate = appServerCmdTemplate;
        this.loginStage = loginStage;

        try {
            jobList.forEach(name -> {
                JobDetail btn = new JobDetail(name, "");
                btn.getStyleClass().add("button-raised");

                jobListView.getChildren().add(btn);

                // Store all Button references
                jobs.add(btn);

                btn.setOnAction(event -> {
                    resultTextArea.setText(btn.getJobLog());
                });
            });

        } catch (Exception ex) {
            statusMsg.setText(ex.getMessage());
            return;
        }

        // Start monitoring jobs right away. No need to wait for User action. Don't show this button.
        // Trigger an event to start it.
        monitorBtn.setVisible(false);
        refreshJobLogs(new ActionEvent());
    }


    /**
     * Read job logs on App Server and updates the background color of the Buttons. In addition to this, sets
     * "jobLog" property of each Button with actual job log.
     *
     * @param actionEvent
     */
    @FXML
    private void refreshJobLogs(ActionEvent actionEvent) {
        // Monitor the jobs
        String jobLogsLocation = this.logDir;
        RefreshJobStatus task = new RefreshJobStatus(jobLogsLocation, this.jobs, this.resultTextArea, appServerCmdTemplate, this.progressBar);
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(task, 1, 10, TimeUnit.SECONDS);

        // Disable the button so that user does not click again and again.
        monitorBtn.setDisable(true);
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