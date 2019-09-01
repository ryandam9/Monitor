package com.job.monitoring.ui;

import javafx.scene.control.Button;

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
