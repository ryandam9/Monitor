package com.job.monitoring.utils;

import com.job.monitoring.controllers.StatusPageController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValidateInputsThread extends Thread {
    private Map<String, String> details;
    private Label statusMsg;
    private ProgressIndicator progressIndicator;
    private Button button;

    public ValidateInputsThread(Map<String, String> details,
                                Label statusMsg,
                                ProgressIndicator progressIndicator,
                                Button button) {
        this.details = details;
        this.statusMsg = statusMsg;
        this.progressIndicator = progressIndicator;
        this.button = button;
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
        String jobLogsLocation = details.get("jobLogsLocation");

        // Check If the Credentials working or not. The Requirement is to connect from [Local Machine] -> Jump Box -> App Server.
        SSHConnection.userId = jumpBoxUserId;
        SSHConnection.hostIpAddress = jumpBoxIpAddress;
        SSHConnection.privateKeyFile = jumpBoxPrivateKeyFile;

        // If connection to Jumpbox cannot be established, Show the error message.
        try {
            SSHConnection.createSession();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            String msg = msg1 + "\n" + "Unable to connect from " + jumpBoxIpAddress + " to " + appServerIpAddress;
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            return;
        }

        // Read the Contents of the Jobs file from App server.
        try {
            String cmd = "ssh -i " + appServerPrivateKeyFile + " " + appServerUserId + "@" + appServerIpAddress + " " + "'cat " + jobLog + "'";
            output = SSHConnection.executeRemoteCommand(cmd);
            String msg = msg1 + "\n" + "Job file: " + jobLog + " is available on App server!";
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            Platform.runLater(() -> button.setDisable(true));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            String msg = msg1 + "\n" + "Unable to read job file: " + jobLog + " on App Server!!! Verify !!!";
            Platform.runLater(() -> statusMsg.setText(msg));
            Platform.runLater(() -> progressIndicator.setVisible(false));
            return;
        }

        // Open Status Window
        String appServerCmdTemplate = "ssh -i " + appServerPrivateKeyFile + " " + appServerUserId + "@" + appServerIpAddress + " ";
        loadStatusWindow(jobLog, jobLogsLocation, appServerCmdTemplate);
    }

    void loadStatusWindow(String jobLogLocation, String logDirectoryLoc, String appServerCmdTemplate) {

     Platform.runLater(() -> {
         try {
             URL url = new File("resources/ui/status.fxml").toURI().toURL();
             FXMLLoader loader = new FXMLLoader(url);
             Parent parent = (Parent) loader.load();
             StatusPageController statusPageController = loader.getController();
             System.out.println(statusPageController);

             List<String> l = new ArrayList<>();
             // Here read the jobLog and identify job names.
             l.add("job1");
             l.add("job2");
             l.add("job3");
             l.add("job4");
             l.add("job5");
             l.add("job6");
             l.add("job7");
             l.add("job8");
             l.add("job9");
             l.add("job10");
             l.add("job11");
             l.add("job12");
             l.add("job13");
             l.add("job14");

             // Initial Window Stage
             Stage loginStage = (Stage) statusMsg.getScene().getWindow();

             // Pass the Jobs list file name to the next Controller.
             statusPageController.setJobList(loginStage, jobLogLocation, l, logDirectoryLoc, appServerCmdTemplate);

             Stage stage = new Stage(StageStyle.DECORATED);
             stage.setTitle("Job Monitor");
             stage.setScene(new Scene(parent));

             String cssFile = new File("resources/css/theme.css").toURI().toURL().toString();
             parent.getStylesheets().add(cssFile);
             stage.getIcons().add(new Image(new File("resources/images/circle.png").toURI().toURL().toString()));
             stage.show();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     });
    }
}