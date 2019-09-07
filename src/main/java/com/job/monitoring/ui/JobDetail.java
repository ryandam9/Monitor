package com.job.monitoring.ui;

import javafx.scene.control.Button;

/**
 * Each Job is represented as a Button. It has additional property called "jobLog" that stores
 * the contents of the job's log file.
 */
public class JobDetail extends Button {
    private String jobLog;

    public JobDetail(String name, String jobLog) {
        super(name);
        this.jobLog = jobLog;
    }

    public void setJobLog(String jobLog) {
        this.jobLog = jobLog;
    }

    public String getJobLog() {
        return jobLog;
    }
}
