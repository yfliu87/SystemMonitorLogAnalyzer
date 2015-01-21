package com.SystemMonitor.Model;

import java.util.Date;

public class JobInfo {
	private String jobName;
	private String wellName;
	private String clientName;
	private String workflow;
	private String unitSystem;
	private double jobSize;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getWellName() {
		return wellName;
	}

	public void setWellName(String wellName) {
		this.wellName = wellName;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getUnitSystem() {
		return unitSystem;
	}

	public void setUnitSystem(String unitSystem) {
		this.unitSystem = unitSystem;
	}

	public double getJobSize() {
		return jobSize;
	}

	public void setJobSize(double jobSize) {
		this.jobSize = jobSize;
	}

	public boolean isSimulator() {
		return simulator;
	}

	public void setSimulator(boolean simulator) {
		this.simulator = simulator;
	}

	public Date getJobStartDate() {
		return jobStartDate;
	}

	public void setJobStartDate(Date jobStartDate) {
		this.jobStartDate = jobStartDate;
	}

	public Date getJobStopDate() {
		return jobStopDate;
	}

	public void setJobStopDate(Date jobStopDate) {
		this.jobStopDate = jobStopDate;
	}

	private boolean simulator;
	private Date jobStartDate;
	private Date jobStopDate;

}
