package com.job.monitoring.utils;

import com.job.monitoring.ui.JobDetail;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import java.util.List;

/**
 * This Thread is executed by the Executor Server, every X period units. For each job, the thread fetches
 * the job's contents from the App Server, and stores in the Button's "jobLog" property.
 *
 * Uses "Platform.runLater()" to control UI elements.
 */

public class RefreshJobStatus implements Runnable {
    private final String jobLogsLocation;
    private List<JobDetail> jobs;
    private TextArea textArea;
    private String appServerCmdTemplate;
    private ProgressIndicator progressIndicator;
    private Label status;

    public RefreshJobStatus(String jobLogsLocation, List<JobDetail> jobs, TextArea textArea, String appServerCmdTemplate,
                            ProgressIndicator progressIndicator,
                            Label status) {
        this.jobLogsLocation = jobLogsLocation;
        this.jobs = jobs;
        this.textArea = textArea;
        this.appServerCmdTemplate = appServerCmdTemplate;
        this.progressIndicator = progressIndicator;
        this.status = status;
    }

    @Override
    public void run() {
        for (JobDetail job : jobs) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Background thread has been Interrupted!!");
            }

            String cmd = appServerCmdTemplate + "'cat " + jobLogsLocation + "/" + job.getText() + ".dat' ";

            try {
                // Read log file from the App Server
                String log = SSHConnection.executeRemoteCommand(cmd);
                job.setJobLog(log);

                Platform.runLater(() -> {
                    status.setText("Monitoring job: " + job.getText() + ", Log has been retrieved from App Server");
                });

                // Verify the log for any errors.
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
                Platform.runLater(() -> {
                    job.setStyle(style);
                });
            } catch (IllegalArgumentException e) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                e.printStackTrace(pw);
//                String sStackTrace = sw.toString(); // stack trace as a string
//                Platform.runLater(() -> textArea.setText(cmd + "\n" + sStackTrace));
                Platform.runLater(() -> {
                    status.setText("Monitoring Job: " + job.getText() + ": " + e.getMessage());
                });

                Platform.runLater(() -> job.setStyle("-fx-background-color: GRAY"));
            }
        }
    }
}