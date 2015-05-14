package com.SystemMonitor.Model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

import org.json.JSONObject;

import com.SystemMonitor.Util.DateHelper;

public class JobInformation {

	private String jobName;
	private String wellName;
	private String clientName;
	private String workflow;
	private String unitSystem;
	private String mwVersion;
	private Calendar jobStartDate;
	private Calendar jobStopDate;
	private double jobDuration;
	private double jobSize;
	private boolean isSimulation;
	private boolean isCrashed;
	private List<String> crashedProcess;
	private List<Stack<CrashDetail>> crashDetails;
	private List<String> patchVersions;
	
	public void updateCrashDetails(Stack<CrashDetail> obj) {
		crashDetails.add(obj);
	}

	public List<Stack<CrashDetail>> getCrashDetails() {
		return this.crashDetails;
	}
	
	public JobInformation(){
		crashedProcess = new ArrayList<String>();
		crashDetails = new ArrayList<Stack<CrashDetail>>();
		patchVersions = new ArrayList<String>();
	}
	
	public double getJobDuration() {
		return jobDuration;
	}

	public void setJobDuration(double jobDuration) {
		this.jobDuration = jobDuration;
	}
	
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

	public String getMWVersion(){
		return mwVersion;
	}
	
	public void setMWVersion(String version){
		this.mwVersion = version;
	}
	
	public List<String> getPatches(){
		return this.patchVersions;
	}
	
	public void updatePatchVersion(String patch){
		this.patchVersions.add(patch);
	}
	
	public Calendar getJobStartDate() {
		return jobStartDate;
	}

	public void setJobStartDate(Calendar jobStartDate) {
		this.jobStartDate = jobStartDate;
	}

	public Calendar getJobStopDate() {
		return jobStopDate;
	}

	public void setJobStopDate(Calendar jobStopDate) {
		this.jobStopDate = jobStopDate;
	}

	public double getJobSize() {
		return jobSize;
	}

	public void setJobSize(double jobSize) {
		this.jobSize = jobSize;
	}

	public boolean isSimulation() {
		return isSimulation;
	}

	public void setSimulation(boolean isSimulation) {
		this.isSimulation = isSimulation;
	}

	public boolean isCrashed() {
		return isCrashed;
	}

	public void setCrashed(boolean isCrashed) {
		this.isCrashed = isCrashed;
	}
	
	public void updateCrashProcess(String component){
		this.crashedProcess.add(component);
	}
	
	public List<String> getCrashProcess(){
		return this.crashedProcess;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		result = prime * result
				+ ((jobStartDate == null) ? 0 : jobStartDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobInformation other = (JobInformation) obj;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		if (jobStartDate == null) {
			if (other.jobStartDate != null)
				return false;
		} else if (!jobStartDate.equals(other.jobStartDate))
			return false;
		return true;
	}
	
	public String getJSONstring(){
		StringBuilder ret = new StringBuilder();
		
		ret.append(this.getMWVersion() + ",");		
		ret.append(convertPatchVersion(this.getPatches()) + ",");
		ret.append(this.getJobName() + ",");
		ret.append(this.getWellName() + ",");
		ret.append(this.getClientName() + ",");
		ret.append(this.getWorkflow() + ",");
		ret.append(this.getUnitSystem() + ",");
		
		String start = DateHelper.formatDate(this.getJobStartDate() , false);
		ret.append(start == null ? "empty" : start);
		ret.append(",");

		ret.append(this.getJobDuration() + ",");
		ret.append(this.isCrashed ? "Yes" : "No");
		
		return ret.toString();

//		
//		JSONObject jsonobj = new JSONObject();
//		jsonobj.put("MWVersion", this.getMWVersion());
//		jsonobj.put("Job Name", this.getJobName());
//		jsonobj.put("Well Name", this.getWellName());
//		jsonobj.put("Client Name", this.getClientName());
//		jsonobj.put("Workflow", this.getWorkflow());
//		jsonobj.put("Unit System", this.getUnitSystem());
//		String start = DateHelper.formatDate(this.getJobStartDate() , false);
//			jsonobj.put("Start Date", start == null ? "empty" : start);
//		
//		String stop = DateHelper.formatDate(this.getJobStopDate(), false);
//			jsonobj.put("Stop Date", stop == null ? "empty" : stop);
//		
//		jsonobj.put("Duration", this.getJobDuration());
//		jsonobj.put("Job Size", this.getJobSize());
//		jsonobj.put("Simulation", this.isSimulation);
//		jsonobj.put("Crash", this.isCrashed);
//	
//		return jsonobj.toString();
	}

	private String convertPatchVersion(List<String> patches) {
		StringBuilder ret = new StringBuilder();
		for (String patch : patches){
			ret.append(PatchVersionDefinition.stringToDigit(patch));
			ret.append(",");
		}
		String temp = ret.toString();
		return temp.substring(0, temp.length() - 1);
	}

	public String getCrashDetail() {
		if (this.crashDetails.isEmpty())
			return "";
		
		StringBuilder ret = new StringBuilder(256);
		
		for (Stack<CrashDetail> detailStack : crashDetails){
			
			ret.append(this.mwVersion == null ? "" + ";" : this.mwVersion + ";");
			ret.append(this.patchVersions.isEmpty()? "" + ";" : PatchVersionDefinition.digitToString(this.patchVersions.get(0)) + ";");

			while(!detailStack.isEmpty())
				ret.append(detailStack.pop().toString() + ";");
			
			ret.append("\r\n");
		}
		return ret.toString();
	}
}
