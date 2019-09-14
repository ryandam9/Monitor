package com.job.monitoring.utils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.util.Map;

import static com.job.monitoring.utils.Utils.logStackTrace;

public class ValidateInputsThread extends Thread {
    private Map<String, String> details;
    private Label statusMsg;
    private ProgressIndicator progressIndicator;
    private Button verifyBtn;
    private Button monitorBtn;

    public ValidateInputsThread(Map<String, String> details,
                                Label statusMsg,
                                ProgressIndicator progressIndicator,
                                Button verifyBtn,
                                Button monitorBtn) {
        this.details = details;
        this.statusMsg = statusMsg;
        this.progressIndicator = progressIndicator;
        this.verifyBtn = verifyBtn;
        this.monitorBtn = monitorBtn;
    }

    @Override
    public void run() {
        super.run();

        String jumpBoxUserId = details.get("jumpBoxUserId");
        String jumpBoxIpAddress = details.get("jumpBoxIpAddress");
        String jumpBoxPrivateKeyFile = details.get("jumpBoxPrivateKeyFile");

        String appServerUserId = details.get("appServerUserId");
        String appServerIpAddress = details.get("appServerIpAddress");
        String appServerPrivateKeyFile = details.get("appServerPrivateKeyFile");

        String jobLog = details.get("jobLog");

        // Check If the Credentials working or not. The Requirement is to connect from [Local Machine] -> Jump Box -> App Server.
        SSHConnection.userId = jumpBoxUserId;
        SSHConnection.hostIpAddress = jumpBoxIpAddress;
        SSHConnection.privateKeyFile = jumpBoxPrivateKeyFile;

        // If connection to Jumpbox cannot be established, Show the error message.
        try {
            SSHConnection.createSession();
        } catch (Exception e) {
            logStackTrace(e);
            Platform.runLater(() -> statusMsg.setText(e.getMessage()));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            return;
        }

        String msg1 = "Connection established to Jumpbox: " + jumpBoxIpAddress;
        String output;

        // Execute a command on Jumpbox
        try {
            output = SSHConnection.executeRemoteCommand("uname -o");
        } catch (Exception e) {
            logStackTrace(e);
            Platform.runLater(() -> statusMsg.setText("Unable to execute command on the Jumpbox: " + e.getMessage()));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            return;
        }

        msg1 = msg1 + ". Jumpbox OS: " + output.trim();

        // Verify the connectivity from Jumpbox to App Server. Just get the OS Version of the app Server.
        try {
            String cmd = "ssh -i " + appServerPrivateKeyFile + " " + appServerUserId + "@" + appServerIpAddress + " " + "'uname -a'";
            output = SSHConnection.executeRemoteCommand(cmd);
            msg1 = msg1 + "\n" + "Connectivity from Jumpbox to App Server has been established. App Server OS: " + output;
        } catch (IllegalArgumentException e) {
            logStackTrace(e);
            String msg = msg1 + "\n" + "Unable to connect from " + jumpBoxIpAddress + " to " + appServerIpAddress;
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            return;
        }

        // Read the Contents of the Jobs file from App server.
        try {
            String cmd = "ssh -i " + appServerPrivateKeyFile + " " + appServerUserId + "@" + appServerIpAddress + " " + "'cat " + jobLog + "'";
            SSHConnection.executeRemoteCommand(cmd);
            String msg = msg1 + "\n" + "Job file: " + jobLog + " is available on App server!";
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            Platform.runLater(() -> verifyBtn.setDisable(true));
            Platform.runLater(() -> monitorBtn.setDisable(false));
        } catch (IllegalArgumentException e) {
            logStackTrace(e);
            String msg = msg1 + "\n" + "Unable to read job file: " + jobLog + " on App Server!!! Verify !!!";
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));

            return;
        }

        // This is same as Clicking the 'Monitor' button.
        Platform.runLater(() -> monitorBtn.fire());
    }
}