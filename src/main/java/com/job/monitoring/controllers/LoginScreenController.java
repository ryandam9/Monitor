package com.job.monitoring.controllers;

import com.job.monitoring.utils.SSHConnection;
import com.job.monitoring.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginScreenController implements Initializable {
    @FXML
    private TextField jumpboxUserId;

    @FXML
    private TextField jumpboxIpAddress;

    @FXML
    private TextField locJumpboxPrivKeyFile;

    @FXML
    private TextField appServerUserId;

    @FXML
    private TextField appServerIpAddress;

    @FXML
    private TextField locappServerPrivKeyFile;

    @FXML
    private TextField jobFile;

    @FXML
    private TextField logDirectory;

    @FXML
    private Button monitorBtn;

    @FXML
    private Label statusMsg;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Properties properties = Utils.loadProperties();

        String jumpboxUserIdVal = properties.getProperty("jumpboxUserIdVal", "");
        String jumpboxIpAddressVal = properties.getProperty("jumpboxIpAddressVal", "");
        String locJumpboxPrivKeyFileVal = properties.getProperty("locJumpboxPrivKeyFileVal", "");

        String appServerUserIdVal = properties.getProperty("appServerUserIdVal", "");
        String appServerIpAddressVal = properties.getProperty("appServerIpAddressVal", "");
        String locappServerPrivKeyFileVal = properties.getProperty("locappServerPrivKeyFileVal", "");

        String jobFileLocation = properties.getProperty("jobFileLocation", "");
        String logDirectoryLoc = properties.getProperty("logDirectoryLoc", "");

        jumpboxUserId.setText(jumpboxUserIdVal);
        jumpboxIpAddress.setText(jumpboxIpAddressVal);
        locJumpboxPrivKeyFile.setText(locJumpboxPrivKeyFileVal);
        appServerUserId.setText(appServerUserIdVal);
        appServerIpAddress.setText(appServerIpAddressVal);
        locappServerPrivKeyFile.setText(locappServerPrivKeyFileVal);
        jobFile.setText(jobFileLocation);
        logDirectory.setText(logDirectoryLoc);
    }

    @FXML
    private void validateUserInputs(ActionEvent actionEvent) {
        System.out.println(System.getProperty("java.class.path"));
        statusMsg.setText("");

        Boolean exceptionOccurred = false;

        // Fetch Jump Box Connection Details
        String jumpboxUserIdVal = jumpboxUserId.getText();
        String jumpboxIpAddressVal = jumpboxIpAddress.getText();
        String locJumpboxPrivKeyFileVal = locJumpboxPrivKeyFile.getText();

        // Fetch App Server Connection Details
        String appServerUserIdVal = appServerUserId.getText();
        String appServerIpAddressVal = appServerIpAddress.getText();
        String locappServerPrivKeyFileVal = locappServerPrivKeyFile.getText();

        // The file that contains Jobs to be executed, available on App Server.
        String jobFileLocation = jobFile.getText();
        String logDirectoryLoc = logDirectory.getText();

        // Verify if any entry is not keyed in
        if (jumpboxUserIdVal.trim().length() == 0 ||
                jumpboxIpAddressVal.trim().length() == 0 || locJumpboxPrivKeyFileVal.trim().length() == 0 ||
                appServerUserIdVal.trim().length() == 0 || appServerIpAddressVal.trim().length() == 0 || locappServerPrivKeyFileVal.trim().length() == 0 ||
                jobFileLocation.trim().length() == 0 ||
                logDirectoryLoc.trim().length() == 0) {
            statusMsg.setText("Make sure to key in all the required fields !");
            //statusMsg.setStyle("-fx-background-color: #D4EDDA;");
            return;
        }

        // Check If the Credentials working or not. The Requirement is to connect
        // from [Local Machine] -> Jump Box -> App Server.
        SSHConnection ssh = new SSHConnection(jumpboxUserIdVal, jumpboxIpAddressVal, locJumpboxPrivKeyFileVal);

        try {
            ssh.createSession();
        } catch (Exception e) {
            e.printStackTrace();
            statusMsg.setText(e.getMessage());
            return;
        }
        String msg1 = "Connection established to Jumpbox: " + jumpboxIpAddressVal;
        String output;

        try {
            output = SSHConnection.executeRemoteCommand("uname -o");
        } catch (Exception e) {
            statusMsg.setText("Unable to execute command on the Jumpbox: " + e.getMessage());
            return;
        }

        msg1 = msg1 + ". Jumpbox OS: " + output.trim();
        statusMsg.setText(msg1);

        // Verify the connectivity from Jumpbox to App Server. Just get the OS Version of the app Server.
        try {
            String cmd = "ssh -i " + locappServerPrivKeyFileVal + " " + appServerUserIdVal + "@" + appServerIpAddressVal + " " + "'uname -a'";
            output = ssh.executeRemoteCommand(cmd);
            msg1 = msg1 + "\n" + "Connectivity from Jumpbox to App Server has been established. App Server OS: " + output;
            statusMsg.setText(msg1);
            System.out.println(msg1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            msg1 = msg1 + "\n" + "Unable to connect from " + jumpboxIpAddressVal + " to " + appServerIpAddressVal;
            statusMsg.setText(msg1);
            return;
        }

        // Read the Contents of the Jobs file from App server.
        try {
            String cmd = "ssh -i " + locappServerPrivKeyFileVal + " " + appServerUserIdVal + "@" + appServerIpAddressVal + " " + "'cat " + jobFileLocation + "'";
            output = ssh.executeRemoteCommand(cmd);
            msg1 = msg1 + "\n" + "Job file: " + jobFileLocation + " is available on App server!";
            statusMsg.setText(msg1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            msg1 = msg1 + "\n" + "Unable to read job file: " + jobFileLocation + " on App Server!!! Verify !!!";
            statusMsg.setText(msg1);
            return;
        }

        jumpboxUserId.setDisable(true);
        jumpboxIpAddress.setDisable(true);
        locJumpboxPrivKeyFile.setDisable(true);
        appServerUserId.setDisable(true);
        appServerIpAddress.setDisable(true);
        locappServerPrivKeyFile.setDisable(true);
        jobFile.setDisable(true);
        monitorBtn.setDisable(true);
        logDirectory.setDisable(true);

        String appServerCmdTemplate = "ssh -i " + locappServerPrivKeyFileVal + " " + appServerUserIdVal + "@" + appServerIpAddressVal + " ";
        loadStatusWindow(logDirectoryLoc, appServerCmdTemplate);
    }

    void loadStatusWindow(String logDirectoryLoc, String appServerCmdTemplate) {
        // TODO: First close the first window and then render the Results Window.
        try {
            URL url = new File("resources/ui/status.fxml").toURI().toURL();
            System.out.println("URL = " + url.getPath());

            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();
            StatusPageController statusPageController = loader.getController();
            System.out.println(statusPageController);

            List<String> l = new ArrayList<>();
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
            Stage loginStage = (Stage) jumpboxUserId.getScene().getWindow();

            // Pass the Jobs list file name to the next Controller.
            statusPageController.setJobList(loginStage, l, logDirectoryLoc, appServerCmdTemplate);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Job Monitor");
            stage.setScene(new Scene(parent));

            String cssFile = new File("resources/css/theme.css").toURI().toURL().toString();
            parent.getStylesheets().add(cssFile);

//            stage.setResizable(false);
            stage.getIcons().add(new Image(new File("resources/images/circle.png").toURI().toURL().toString()));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}