package com.job.monitoring.utils;

import com.job.monitoring.ui.TileButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.job.monitoring.utils.AppLogging.logger;
import static com.job.monitoring.utils.Utils.logStackTrace;

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

        /* Read the log file and identify Job names and their Statuses. */
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
                logger.debug("Background thread has been Interrupted!!");
            }

            final String targetJob = tile.getJobName();

            try {
                final boolean showProgressIndicator;
                final String btnClass;
                final String jobStatus = logFileMap.get(targetJob).toUpperCase();

                if (jobStatus.equalsIgnoreCase("not-started")) {
                    showProgressIndicator = false;
                    btnClass = "job-not-started";
                } else if (jobStatus.equalsIgnoreCase("success")) {
                    showProgressIndicator = false;
                    btnClass = "job-success";
                } else if (jobStatus.equalsIgnoreCase("failed")) {
                    showProgressIndicator = false;
                    btnClass = "job-failed";
                } else {
                    showProgressIndicator = true;
                    btnClass = "job-running";
                }

                /* Update UI */
                Platform.runLater(() -> {
                    tile.getStatusBtn().setText(jobStatus);

                    /* Button's first class is "Button" [This is Default], second one is "tile-button",
                      third one changes based on Job status. In this case, remove the third class, and a new one
                      based on job's current status which is derived above.
                     */
                    tile.getStatusBtn().getStyleClass().remove(2);
                    tile.getStatusBtn().getStyleClass().add(btnClass);
                    tile.getProgressIndicator().setVisible(showProgressIndicator);
                });
            } catch (Exception ex) {
                logStackTrace(ex);
            }

            // Fetch Job log and store it in the Tile member variable.
            try {
                cmd = appServerCmdTemplate + "'cat " + logDir + "/" + targetJob + "'";
                String logFileOutput = SSHConnection.executeRemoteCommand(cmd);

                Platform.runLater(() -> {
                    tile.setJobLog(logFileOutput);
                });
            } catch (Exception ex) {
                logStackTrace(ex);
            }
        }
    }
}