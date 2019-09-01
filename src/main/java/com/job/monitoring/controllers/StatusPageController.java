package com.job.monitoring.controllers;

import com.job.monitoring.ui.JobDetail;
import com.job.monitoring.utils.RefreshJobStatus;
import com.job.monitoring.utils.SSHConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    // Non UI elements
    private String jobFileLocation;
    private List<JobDetail> jobs;
    ScheduledExecutorService executor;

    public void setJobFileLocation(String loc) {
        this.jobFileLocation = loc;
        System.out.println("setJobFileLocation() is set to " + loc);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jobs = new ArrayList<>();
        executor = Executors.newScheduledThreadPool(2);
    }

    @FXML
    private void refreshJobLogs(ActionEvent actionEvent) {
        // TODO: Trigger the background thread that does the following activities:
        //   1. Read the Jobs list file and create one Button for each Job.
        //   2. For every given X number of unit periods:
        //        a. Read the Job log file and set the 'jobLog' member of each button.
        //        b. Identify the success/failure of the job from the log and change the bg color of the button.
        //        c. If no job log is found, it means Job has not started.
        //             GREEN | RED | GRAY

        // Read the jobs list file and identify the jobs to be monitored
        try {
            String jobListFileContents = SSHConnection.executeRemoteCommand("cat " + jobFileLocation);
            List<String> jobList = new ArrayList<>();

            // Process the strings and extract Job names.
            jobList.add("Job 1");
            jobList.add("Job 2");
            jobList.add("Job 3");
            jobList.add("Job 4");
            jobList.add("Job 5");
            jobList.add("Job 6");
            jobList.add("Job 7");
            jobList.add("Job 8");
            jobList.add("Job 9");
            jobList.add("Job 10");

            jobList.forEach(name -> {
                JobDetail btn = new JobDetail(name, "");
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

        // Monitor the jobs
        String jobLogsLocation = "";
        RefreshJobStatus task = new RefreshJobStatus(jobLogsLocation, jobs);
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(task, 1, 30, TimeUnit.SECONDS);

        // Disable the button so that user does not click again and again.
        monitorBtn.setDisable(true);
    }

    @FXML
    private void stopApplication(ActionEvent event) {
        executor.shutdown();
    }
}