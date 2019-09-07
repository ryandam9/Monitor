package com.job.monitoring.utils;

import com.jcraft.jsch.*;

import java.io.InputStream;

public class SSHConnection {
    private String userId;
    private String HostIpAddress;
    private String privateKeyFile;

    private static Session session;

    public SSHConnection(String userId, String HostIpAddress, String privateKeyFile) {
        this.userId = userId;
        this.HostIpAddress = HostIpAddress;
        this.privateKeyFile = privateKeyFile;
    }

    public void createSession() throws Exception {
        JSch jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        try {
            jsch.addIdentity(privateKeyFile);
            session = jsch.getSession(userId, HostIpAddress, 22);
            session.setConfig(config);
            session.setTimeout(50);
            session.connect();
        } catch (Exception ex) {
            String msg = "Unable to Create SSH Connection to Jumpbox : " + HostIpAddress + " Using Priv Key File: " + privateKeyFile;
            msg = msg + "; Verify the User name/Host IP/Private Key file !!!";
            throw new Exception(msg);
        }
    }

    public static String executeRemoteCommand(String command) throws IllegalArgumentException {
        System.out.println("Command getting executed: " + command);
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
            System.out.println("Exception occurred when executing command " + command);
            jschEx.getStackTrace();
            throw new IllegalArgumentException("Exception occurred when executing command " + command);
        } catch (Exception exception) {
            System.out.println("Exception occurred when executing command " + command);
            exception.printStackTrace();
            throw new IllegalArgumentException("Exception occurred when executing command " + command);
        }

        System.out.println("Remote Command: " + command);
        System.out.println("Result: " + result.toString().trim());

        if (exitStatus == 0) {
            return result.toString().trim();
        } else {
            throw new IllegalArgumentException("Unable to execute command: " + command);
        }
    }
}
