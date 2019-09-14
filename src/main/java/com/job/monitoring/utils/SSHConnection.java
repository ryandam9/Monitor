package com.job.monitoring.utils;

import com.jcraft.jsch.*;

import java.io.InputStream;

import static com.job.monitoring.utils.AppLogging.logger;
import static com.job.monitoring.utils.Utils.logStackTrace;

public class SSHConnection {
    public static String userId;
    public static String hostIpAddress;
    public static String privateKeyFile;
    public static Session session;

    public static void createSession() throws Exception {
        JSch jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        try {
            jsch.addIdentity(privateKeyFile);
            session = jsch.getSession(userId, hostIpAddress, 22);
            session.setConfig(config);
            session.connect();
        } catch (Exception ex) {
            logStackTrace(ex);
            String msg = "Unable to Create SSH Connection to Jumpbox : " + hostIpAddress + " Using Priv Key File: " + privateKeyFile;
            msg = msg + "; Verify the User name/Host IP/Private Key file !!!";
            throw new Exception(msg);
        }
    }

    public static String executeRemoteCommand(String command) throws IllegalArgumentException {
        int exitStatus = 0;

        StringBuffer result = new StringBuffer();

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            // Buffer to hold command result.
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result = result.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    exitStatus = channel.getExitStatus();
                    break;
                }
            }
        } catch (JSchException jschEx) {
            logStackTrace(jschEx);
            jschEx.getStackTrace();
            throw new IllegalArgumentException("Exception occurred when executing command " + command);
        } catch (Exception exception) {
            logStackTrace(exception);
            exception.printStackTrace();
            throw new IllegalArgumentException("Exception occurred when executing command " + command);
        }

        logger.debug("Remote Command: " + command);

        if (exitStatus == 0) {
            return result.toString().trim();
        } else {
            throw new IllegalArgumentException("Unable to execute command: " + command);
        }
    }
}