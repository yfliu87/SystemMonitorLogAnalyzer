package com.SystemMonitor.Model;

/*
 * This class is the encapsulation of query conditions
 */
import java.util.ArrayList;
import java.util.List;

public class SystemMonitorQuery {
	private String toolString;
	private String jobStartDate;
	private String jobStopDate;
	private String jobDuration;
	private List<String> features = new ArrayList<String>();
	private boolean isCrashed;

	public String getToolString() {
		return toolString;
	}

	public void setToolString(String toolString) {
		this.toolString = toolString;
	}

	public String getJobStartDate() {
		return jobStartDate;
	}

	public void setJobStartDate(String jobStartDate) {
		this.jobStartDate = jobStartDate;
	}

	public String getJobStopDate() {
		return jobStopDate;
	}

	public void setJobStopDate(String jobStopDate) {
		this.jobStopDate = jobStopDate;
	}

	public String getJobDuration() {
		return jobDuration;
	}

	public void setJobDuration(String jobDuration) {
		this.jobDuration = jobDuration;
	}

	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}

	public boolean isCrashed() {
		return isCrashed;
	}

	public void setCrashed(boolean isCrashed) {
		this.isCrashed = isCrashed;
	}
}
