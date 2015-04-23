package com.SystemMonitor.Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.SystemMonitor.Indexer.LogIndexer;
import com.SystemMonitor.Indexer.IndexField;
import com.SystemMonitor.Model.JobInformation;
import com.SystemMonitor.Model.PatchVersionDefinition;
import com.SystemMonitor.Model.SystemMonitorQuery;
import com.SystemMonitor.Util.ConfigHelper;
import com.SystemMonitor.Util.DateHelper;
import com.SystemMonitor.Util.SystemMonitorException;

@Controller
public class LogAnalyzerController {
	private LogIndexer _indexer;
	private String _logFilePath;
	private int _jobCount;
	private String _configFilePath = "/home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer/config.txt";

	@RequestMapping(value = "/logAnalyzer", method = RequestMethod.GET)
	public ModelAndView logAnalyzerHandler(Model model) {
		
		init();

		ModelAndView mv = null;
		mv = new ModelAndView("/main");
		mv.addObject("jobCount", getTotalJob());
		mv.addObject(new SystemMonitorQuery());
		return mv;
	}

	@RequestMapping(value = "/logAnalyzer", method = RequestMethod.POST)
	public ModelAndView queryLogHandler(HttpServletRequest request, SystemMonitorQuery queryCondition){
		ModelAndView mv = null;
		
		if (invalidDate(queryCondition.getJobStartDate(), queryCondition.getJobStopDate())){
			mv = new ModelAndView("/invalidResult");
			mv.addObject("message", "invalid date range, start should be earlier than stop.");
			return mv;
		}
		
		List<JobInformation> result = new ArrayList<JobInformation>();
		String[] crashChecked = request.getParameterValues("crashChecked");
		boolean isCrashChecked = false;
		if (crashChecked != null)
			isCrashChecked = crashChecked[0].equals("on");
		
		result = this.searchByQuery(queryCondition, isCrashChecked);

		if (result == null || result.size() == 0){
			mv = new ModelAndView("/emptyResult");
		}else{
			organizeResult(result, request, queryCondition);

			if (isCrashChecked){
				addMoreIndex(result, request);
				
				if (queryCondition.getChartType().equals("BarChart"))
					mv = new ModelAndView("/CrashbarChartResult");
				else if (queryCondition.getChartType().equals("LineChart"))
					mv = new ModelAndView("/CrashlineChartResult");
				
				//mv = new ModelAndView("/RadarChartResult");
			} else if (queryCondition.getChartType().equals("BarChart")) {
				mv = new ModelAndView("/barChartResult");
			} else if (queryCondition.getChartType().equals("LineChart")) {
				mv = new ModelAndView("/lineChartResult");
			}			
		}
		
		return mv;
	}
	
	private void addMoreIndex(List<JobInformation> result,
			HttpServletRequest request) {
		HashMap<String, Integer> processCount = new HashMap<String, Integer>();
		
		for (JobInformation job : result){
			List<String> processes = job.getCrashProcess();
			
			for (String process : processes){
				if (processCount.containsKey(process))
					processCount.put(process, processCount.get(process) + 1);
				else
					processCount.put(process, 1);
			}
		}
		
		Set<String> keySet = processCount.keySet();
		String processName = "";
		String count = "";
		
		for (String process : keySet){
			processName += process + ",";
			count += processCount.get(process) + ",";
		}
		
		processName = processName.substring(0, processName.lastIndexOf(","));
		count = count.substring(0, count.lastIndexOf(","));

		request.setAttribute("processName", processName);
		request.setAttribute("count", count);
	}

	private String getTotalJob(){
		return "Totally " + _indexer.getTotalJobs() + " jobs in storage";
	}
	
	private boolean invalidDate(String jobStartDate, String jobStopDate) {
		Calendar start = DateHelper.formatCalendar(jobStartDate);
		Calendar stop = DateHelper.formatCalendar(jobStopDate);
		
		if (start == null || stop == null)
			return false;
		else
			return start.after(stop);
	}

	private void organizeResult(List<JobInformation> result, HttpServletRequest request, SystemMonitorQuery queryCondition) {
		String xValue = "";
		String yValue = "";
		if (result.size() == 0)
			return;
		
		Map<String, List<JobInformation>> resultMap = new HashMap<String, List<JobInformation>>();
		for (JobInformation job : result){
			String date = DateHelper.formatDate(job.getJobStartDate(), false);
			
			if (resultMap.containsKey(date)){
				List<JobInformation> jobList = resultMap.get(date);
				jobList.add(job);
				resultMap.put(date, jobList);
			}else{
				List<JobInformation> jobList = new ArrayList<JobInformation>();
				jobList.add(job);
				resultMap.put(date, jobList);
			}
		}

		Object[] keyArr = resultMap.keySet().toArray();
		Arrays.sort(keyArr);
		
		int totalCount = 0;
		StringBuilder fullJobInfo = new StringBuilder();
		for (Object key : keyArr){
			xValue += key + ",";
			
			List<JobInformation> jobs = resultMap.get(key);
			
			yValue += resultMap.get(key).size() + ",";
			for (JobInformation job : jobs){
				fullJobInfo.append(job.getJSONstring() + ";");
			}
			
			totalCount += resultMap.get(key).size();
		}
		
		xValue = xValue.substring(0, xValue.lastIndexOf(","));
		yValue = yValue.substring(0, yValue.lastIndexOf(","));

		request.setAttribute("xValue", xValue);
		request.setAttribute("yValue", yValue);
		request.setAttribute("TotalCount", "There are totally " + totalCount + "/" + this._jobCount + " results.");
		request.setAttribute("jobField", "MWVersion, Patch, Job Name, Well Name, Client Name, Workflow, UnitSystem, Start Date, Duration(hr), Crash");
		request.setAttribute("fullJobInfo", fullJobInfo.toString().substring(0, fullJobInfo.length() - 1));
	
	}
	
	public List<JobInformation> searchByQuery(SystemMonitorQuery query, boolean isCrashed) {
		List<JobInformation> retList = null;

		try{
			
			//
			
			String mwVersion = query.getMWVersion();
			if (!mwVersion.equals("none")){
				List<JobInformation> jobs = this.searchMWVersion(mwVersion);

				if (retList == null)
					retList = jobs;
				else
					retList.retainAll(jobs);
			}			

//			String patchVersion = query.getPatchVersion();
//			if (!patchVersion.equals("none")){
//				List<JobInformation> jobs = this.searchPatchVersion(patchVersion);
//
//				if (retList == null)
//					retList = jobs;
//				else
//					retList.retainAll(jobs);
//			}
			
			//
			
			String workflow = query.getWorkflow();
			if (!workflow.equals("none")){
				
				List<JobInformation> workflows = this.searchWorkflow(workflow);
				
				if (retList == null)
					retList = workflows;
				else
					retList.retainAll(workflows);
			}
		
			String toolName = query.getToolString();
			if (!toolName.isEmpty()) {
				List<JobInformation> toolList = this.searchToolUsage(toolName);
				
				if (retList == null)
					retList = toolList;
				else
					retList.retainAll(toolList);
			}

			String startDate = query.getJobStartDate();
			String stopDate = query.getJobStopDate();
			if (!startDate.isEmpty() && stopDate.isEmpty()) {
				List<JobInformation> startList = this.searchJobByDate(IndexField.STARTDATE, startDate);

				if (retList == null)
					retList = startList;
				else
					retList.retainAll(startList);
			}

			if (startDate.isEmpty() && !stopDate.isEmpty()) {
				List<JobInformation> stopList = this.searchJobByDate(IndexField.STOPDATE, stopDate);

				if (retList == null)
					retList = stopList;
				else
					retList.retainAll(stopList);
			}

			if (!startDate.isEmpty() && !stopDate.isEmpty()) {
				List<JobInformation> durationList = this.searchJobByTimeRange(startDate, stopDate);

				if (retList == null)
					retList = durationList;
				else
					retList.retainAll(durationList);
			}

			boolean isCrash = isCrashed;
			if (isCrash) {
				List<JobInformation> crashList = this.searchCrashJobs();

				if (retList == null)
					retList = crashList;
				else
					retList.retainAll(crashList);
			}
			
//			List<String> features = query.getFeatures();
//			for (String feature : features) {
//				List<JobInformation> jobs = this.searchFeatureUsage(feature);
//
//				if (retList == null)
//					retList = jobs;
//				else
//					retList.retainAll(jobs);
//			}
//			
			String duration = query.getJobDuration();
			if (!duration.isEmpty()){
				String sign = readSign(duration);
				String digit = readDigit(duration);
				
				if (retList == null)
					retList = this.searchAllDocs();
				
				retList = filterResult(sign, digit, retList);
			}
			
			if (retList == null){
				retList = this.searchAllDocs();
			}
			
		}catch (IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}

		return new ArrayList<JobInformation>(retList);
	}

	private void init() {
		ConfigHelper config = ConfigHelper.getInstance(_configFilePath);

		String logFilePath = config.getLogLocation();
		_logFilePath = logFilePath;

		String indexFilePath = config.getIndexLocation();
		_indexer = new LogIndexer(logFilePath, indexFilePath);
		
		_jobCount = _indexer.getTotalJobs();
	}

	private List<JobInformation> searchWorkflow(String workflow) {
		return _indexer.searchWorkflow(workflow);
	}

	public List<JobInformation> searchToolUsage(String toolName) throws IOException {
		return _indexer.searchToolUsage(toolName);
	}

	public List<JobInformation> searchCrashJobs() throws IOException {
		return _indexer.searchCrashJobs();
	}

	public List<JobInformation> searchFeatureUsage(String featureName)
			throws IOException {
		if (featureName == null)
			return new ArrayList<JobInformation>();

		return _indexer.searchJobFeature(featureName);
	}

	public List<JobInformation> searchJobByDate(String field, String date) throws IOException {
		List<JobInformation> ret = new ArrayList<JobInformation>();

		if (date == null)
			return ret;

		ret = _indexer.searchJobByDate(field, date);
		return ret;
	}

	public List<JobInformation> searchJobByTimeRange(String startDate, String stopDate)
			throws IOException {
		List<JobInformation> ret = new ArrayList<JobInformation>();

		if (startDate == null || stopDate == null)
			return ret;

		List<String> datesInRange = DateHelper.getDatesInRange(startDate,
				stopDate);
		List<JobInformation> result = null;
		Set<JobInformation> candidates = new HashSet<JobInformation>();

		for (String date : datesInRange) {
			result = _indexer.searchJobByDate(IndexField.STARTDATE, date);
			candidates.addAll(result);
		}

		return new ArrayList<JobInformation>(candidates);
	}

	public List<JobInformation> searchMWVersion(String version){
		if (version == null)
			return new ArrayList<JobInformation>();
		
		return _indexer.searchMWVersion(version);
	}
	
	public List<JobInformation> searchPatchVersion(String patch){
		if (patch == null || getPatchVersion(patch).equals("empty"))
			return new ArrayList<JobInformation>();
		
		return _indexer.searchPatchVersion(getPatchVersion(patch));
	}
		
	private String getPatchVersion(String patch) {
		return PatchVersionDefinition.convert(patch);
	}

	private String readDigit(String duration) {
		StringBuilder tempDigit = new StringBuilder();
		
		char[] chars = duration.toCharArray();
		for(char c : chars){
			if (c == '>' || c == '=' || c == '<'){
				continue;
			}
			else
				tempDigit.append(c);
		}
		return tempDigit.toString();
	}

	private String readSign(String duration) {
		StringBuilder tempSign = new StringBuilder();
		
		char[] chars = duration.toCharArray();
		for(char c : chars){
			if (c == '>' || c == '=' || c == '<'){
				tempSign.append(c);
			}
			else
				break;
		}
		return tempSign.toString();
	}

	private List<JobInformation> filterResult(String sign, String digit, List<JobInformation> retList) {
//		List<JobInformation> candidate = new ArrayList<JobInformation>(retList);
//		
//		
//		if (sign.equals("==")){
//			for (JobInformation job : candidate){
//				if (Math.abs(Double.parseDouble(digit) - job.getJobDuration()) > 0.01)
//					retList.remove(job);
//			}
//		}else if (sign.equals(">=")){
//			for (JobInformation job : candidate){
//				if (job.getJobDuration() < Double.parseDouble(digit))
//					retList.remove(job);
//			}
//		}else if (sign.equals(">")){
//			for (JobInformation job : candidate){
//				if (job.getJobDuration() <= Double.parseDouble(digit))
//					retList.remove(job);
//			}
//		}else if (sign.equals("<")){
//			for (JobInformation job : candidate){
//				if (job.getJobDuration() >= Double.parseDouble(digit))
//					retList.remove(job);
//			}
//		}else{
//			for (JobInformation job : candidate){
//				if (job.getJobDuration() > Double.parseDouble(digit))
//					retList.remove(job);
//			}
//		}
		
		
		List<JobInformation> candidate = new ArrayList<JobInformation>();
		
		if (sign.equals("==")){
			for (JobInformation job : retList){
				if (Math.abs(Double.parseDouble(digit) - job.getJobDuration()) < 0.01)
					candidate.add(job);
			}
		}else if (sign.equals(">=")){
			for (JobInformation job : retList){
				if (job.getJobDuration() >= Double.parseDouble(digit))
					candidate.add(job);
			}
		}else if (sign.equals(">")){
			for (JobInformation job : retList){
				if (job.getJobDuration() > Double.parseDouble(digit))
					candidate.add(job);
			}
		}else if (sign.equals("<")){
			for (JobInformation job : retList){
				if (job.getJobDuration() < Double.parseDouble(digit))
					candidate.add(job);
			}
		}else{
			for (JobInformation job : retList){
				if (job.getJobDuration() <= Double.parseDouble(digit))
					candidate.add(job);
			}
		}
		return candidate;
	}
	
	private List<JobInformation> searchAllDocs() {
		return this._indexer.SearchAllDocs();
	}
}
