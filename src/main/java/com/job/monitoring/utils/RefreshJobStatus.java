package com.job.monitoring.utils;

import com.job.monitoring.ui.TileButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Thread is executed by the Executor Server, every X period units. For each job, the thread fetches
 * the job's contents from the App Server, and stores in the Button's "jobLog" property.
 * <p>
 * Uses "Platform.runLater()" to control UI elements.
 */

public class RefreshJobStatus implements Runnable {
    private List<TileButton> jobs;
    private final String logFile;
    private final String logDir;
    private String appServerCmdTemplate;
    private TextArea result;
    private Label statusMsg;

    public RefreshJobStatus(List<TileButton> jobs, String logFile, String logDir, String appServerCmdTemplate,
                            Label statusMsg) {
        this.jobs = jobs;
        this.logFile = logFile;
        this.logDir = logDir;
        this.appServerCmdTemplate = appServerCmdTemplate;
        this.statusMsg = statusMsg;
    }

    @Override
    public void run() {
        String cmd = appServerCmdTemplate + "'cat " + logFile + "'";
        String jobFile = SSHConnection.executeRemoteCommand(cmd);

        Map<String, String> logFileMap = new HashMap<>();

        for (String line : jobFile.split("\n")) {
            String jobName = line.split(" ")[0];
            String status = line.split(" ")[3];
            logFileMap.put(jobName, status);
        }

        /* Now go through all the Tiles and update Status */
        for (final TileButton tile : jobs) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Background thread has been Interrupted!!");
            }

            final String targetJob = tile.getJobName();

            try {
                final boolean showProgressIndicator;
                String statusColor = "";
                final String jobStatus = logFileMap.get(targetJob);

                if (jobStatus.equalsIgnoreCase("not-started")) {
                    showProgressIndicator = false;
                    statusColor = "#F2F2F2";
                } else if (jobStatus.equalsIgnoreCase("success")) {
                    showProgressIndicator = false;
                    statusColor = "#1EB980";
                } else if (jobStatus.equalsIgnoreCase("failed")) {
                    showProgressIndicator = false;
                    statusColor = "#7D2996";
                } else {
                    showProgressIndicator = true;
                    statusColor = "#B4C1CC";
                }

                Platform.runLater(() -> {
                    tile.getStatusBtn().setText(jobStatus);
                    tile.getProgressIndicator().setVisible(showProgressIndicator);
                    statusMsg.setText(jobStatus);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                cmd = appServerCmdTemplate + "'cat " + logDir + "/" + targetJob + "'";
                String logFileOutput = SSHConnection.executeRemoteCommand(cmd);

                Platform.runLater(() -> {
                    tile.setJobLog(logFileOutput);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}