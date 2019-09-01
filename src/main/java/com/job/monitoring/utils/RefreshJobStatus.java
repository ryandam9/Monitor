package com.job.monitoring.utils;

import com.job.monitoring.ui.JobDetail;
import javafx.application.Platform;

import java.util.List;

public class RefreshJobStatus implements Runnable {
    private final String jobLogsLocation;
    private List<JobDetail> jobs;

    public RefreshJobStatus(String jobLogsLocation, List<JobDetail> jobs) {
        this.jobLogsLocation = jobLogsLocation;
        this.jobs = jobs;
    }

    @Override
    public void run() {
        for (JobDetail job : jobs) {
            String cmd = "cat " + jobLogsLocation + "/" + job.getText() + ".dat";

            try {
                String log = SSHConnection.executeRemoteCommand(cmd);
                job.setJobLog(log);

                String status = "success";
                final String style;

                // Determine Success or failure & Change Color.
                if (status.equals("success"))
                    style = "-fx-background-color: GREEN";
                else if (status.equals("failure"))
                    style = "-fx-background-color: RED";
                else
                    style = "-fx-background-color: GRAY";

                // Background thread cannot access UI elements. Following stmt creates a task and executes
                // on the JavaFX application thread later.
                Platform.runLater(() -> job.setStyle(style));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}