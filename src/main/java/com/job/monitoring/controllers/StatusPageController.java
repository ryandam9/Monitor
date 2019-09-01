package com.job.monitoring.controllers;

import com.job.monitoring.ui.JobDetail;
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
    private Label statusMsg;

    // Non UI elements
    private String jobFileLocation;

    public void setJobFileLocation(String loc) {
        this.jobFileLocation = loc;
        System.out.println("setJobFileLocation() is set to " + loc);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
        List<String> jobList = new ArrayList<>();
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
            JobDetail btn = new JobDetail(name, name);
            jobListView.getChildren().add(btn);

            btn.setOnAction(event -> {
                resultTextArea.setText(btn.getJobLog());
            });
        });

        statusMsg.setText("Job File on App Server: " + jobFileLocation);
    }
}