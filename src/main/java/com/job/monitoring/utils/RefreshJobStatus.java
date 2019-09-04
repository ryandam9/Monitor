package com.job.monitoring.utils;

import com.job.monitoring.ui.JobDetail;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class RefreshJobStatus implements Runnable {
    private final String jobLogsLocation;
    private List<JobDetail> jobs;
    private TextArea textArea;
    private String appServerCmdTemplate;
    private ProgressBar progressBar;

    public RefreshJobStatus(String jobLogsLocation, List<JobDetail> jobs, TextArea textArea, String appServerCmdTemplate, ProgressBar progressBar) {
        this.jobLogsLocation = jobLogsLocation;
        this.jobs = jobs;
        this.textArea = textArea;
        this.appServerCmdTemplate = appServerCmdTemplate;
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        int i = 0;
        for (JobDetail job : jobs) {
            if (i > 10) {
                i = 0;
            }
            i++;
            final int j = i;

            System.out.println("Background Thread is executing.");
            String cmd = appServerCmdTemplate + "'cat " + jobLogsLocation + "/" + job.getText() + ".dat' ";

            try {
                String log = SSHConnection.executeRemoteCommand(cmd);
                job.setJobLog(log);

                String logUpper = log.toUpperCase();
                String status;

                if (logUpper.contains("ERROR"))
                    status = "failure";
                else
                    status = "success";

                final String style;

                // Determine Success or failure & Change Color.
                if (status.equals("success"))
                    style = "-fx-background-color: GREEN";
                else if (status.equals("failure"))
                    style = "-fx-background-color: #7D2996";
                else
                    style = "-fx-background-color: GRAY";

                // Background thread cannot access UI elements. Following stmt creates a task and executes
                // on the JavaFX application thread later.
                Platform.runLater(() -> job.setStyle(style));
                Platform.runLater(() -> progressBar.setProgress(j * 10));
            } catch (IllegalArgumentException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                Platform.runLater(() -> textArea.setText(cmd + "\n" + sStackTrace));
                Platform.runLater(() -> job.setStyle("-fx-background-color: GRAY"));
            }
        }
    }
}