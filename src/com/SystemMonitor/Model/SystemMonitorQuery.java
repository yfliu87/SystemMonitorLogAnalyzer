package com.SystemMonitor.Model;

/*
 * This class is the encapsulation of query conditions
 */
import java.util.ArrayList;
import java.util.List;

public class SystemMonitorQuery {
	private String workflow;
	private String toolString;
	private String jobStartDate;
	private String jobStopDate;
	private String jobDuration;
	private String MWVersion;
	private String patchVersion;
	private List<String> features = new ArrayList<String>();
	private boolean isCrashed;
	private String chartType;

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
	
	public void setMWVersion(String version){
		this.MWVersion = version;
	}

	public String getMWVersion(){
		return this.MWVersion;
	}
	
	public void setPatchVersion(String patch){
		this.patchVersion = patch;
	}
	
	public String getPatchVersion(){
		return this.patchVersion;
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

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}
}
