package com.job.monitoring.controllers;

import com.job.monitoring.utils.SSHConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private Button monitorBtn;

    @FXML
    private Label statusMsg;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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

        // Verify if any entry is not keyed in
        if (jumpboxUserIdVal.trim().length() == 0 ||
                jumpboxIpAddressVal.trim().length() == 0 || locJumpboxPrivKeyFileVal.trim().length() == 0 ||
                appServerUserIdVal.trim().length() == 0 || appServerIpAddressVal.trim().length() == 0 || locappServerPrivKeyFileVal.trim().length() == 0 ||
                jobFileLocation.trim().length() == 0) {
            statusMsg.setText("Make sure to key in all the required fields !");
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

        // TODO: Here, verify the connectivity from Jumpbox to App Server. The command should get the contents of the
        //       Job list file from App Server.
        try {
            ssh.executeRemoteCommand("ssh -i " + locappServerPrivKeyFileVal + " " + appServerUserIdVal + "@" + appServerIpAddressVal);
        } catch (Exception e) {
            e.printStackTrace();
            statusMsg.setText(e.getMessage());
            return;
        }

        loadStatusWindow();
    }

    void loadStatusWindow() {
        // TODO: First close the first window and then render the Results Window.
        try {
            URL url = new File("resources/ui/status.fxml").toURI().toURL();
            System.out.println("URL = " + url.getPath());

            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();
            StatusPageController statusPageController = loader.getController();
            System.out.println(statusPageController);

            // Pass the Jobs list file name to the next Controller.
            statusPageController.setJobFileLocation(jobFile.getText());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Job Monitor");
            stage.setScene(new Scene(parent));
            stage.setResizable(false);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}