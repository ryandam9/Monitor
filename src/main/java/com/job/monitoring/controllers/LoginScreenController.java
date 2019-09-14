package com.job.monitoring.controllers;

import com.job.monitoring.utils.SSHConnection;
import com.job.monitoring.utils.Utils;
import com.job.monitoring.utils.ValidateInputsThread;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.job.monitoring.utils.Utils.logStackTrace;

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
    private Button verifyBtn;

    @FXML
    public Button monitorBtn;

    @FXML
    private HBox statusBar;

    @FXML
    private Label statusMsg;

    private ProgressIndicator progressIndicator;

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

        // Add a Progress Indicator to the first page.
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        progressIndicator.setVisible(false);
        statusBar.getChildren().addAll(progressIndicator);

        monitorBtn.setDisable(true);
    }

    @FXML
    private void validateUserInputs(ActionEvent actionEvent) {
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
            return;
        }

        progressIndicator.setVisible(true);

        Map<String, String> details = new HashMap<>();
        details.put("jumpBoxUserId", jumpboxUserIdVal);
        details.put("jumpBoxIpAddress", jumpboxIpAddressVal);
        details.put("jumpBoxPrivateKeyFile", locJumpboxPrivKeyFileVal);
        details.put("appServerUserId", appServerUserIdVal);
        details.put("appServerIpAddress", appServerIpAddressVal);
        details.put("appServerPrivateKeyFile", locappServerPrivKeyFileVal);
        details.put("jobLog", jobFileLocation);
        details.put("jobLogsLocation", logDirectoryLoc);

        // Validate the Inputs and check whether SSH Connection to the hosts is possible or not
        // This is going to happen in the Background.
        Thread t = new ValidateInputsThread(details, statusMsg, progressIndicator, verifyBtn, monitorBtn);
        t.setDaemon(false);
        t.start();
    }

    @FXML
    private void loadStatusWindow(ActionEvent event) {
        // Fetch App Server Connection Details
        String appServerUserIdVal = appServerUserId.getText();
        String appServerIpAddressVal = appServerIpAddress.getText();
        String locappServerPrivKeyFileVal = locappServerPrivKeyFile.getText();

        String jobLogLocation = jobFile.getText();
        String logDirectoryLoc = logDirectory.getText();

        String appServerCmdTemplate = "ssh -i " + locappServerPrivKeyFileVal + " " + appServerUserIdVal + "@" + appServerIpAddressVal + " ";

        try {
            URL url = new File("resources/ui/status.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();
            String cssFile = new File("resources/css/tile_button.css").toURI().toURL().toString();
            parent.getStylesheets().add(cssFile);

            StatusPageController statusPageController = loader.getController();

            List<String> l = new ArrayList<>();
            List<String> statuses = new ArrayList<>();

            // Read the Job log file and identify job names to be monitored.
            String jobFile = SSHConnection.executeRemoteCommand(appServerCmdTemplate + "cat " + jobLogLocation);
            for (String line : jobFile.split("\n")) {
                String jobName = line.split(" ")[0];
                String status = line.split(" ")[3];

                l.add(jobName);
                statuses.add(status);
            }

            // Initial Window Stage
            Stage loginStage = (Stage) statusMsg.getScene().getWindow();

            // Pass the Jobs list file name to the next Controller.
            statusPageController.setJobList(loginStage, jobLogLocation, l, statuses, logDirectoryLoc, appServerCmdTemplate);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Job Monitor");
            stage.setScene(new Scene(parent));
            stage.getIcons().add(new Image(new File("resources/images/circle.png").toURI().toURL().toString()));
            stage.show();
        } catch (IOException ex) {
            logStackTrace(ex);
        }
    }
}