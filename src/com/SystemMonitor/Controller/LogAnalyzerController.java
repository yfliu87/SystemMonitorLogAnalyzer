package com.SystemMonitor.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

			} else if (queryCondition.getChartType().equals("BarChart")) {
				mv = new ModelAndView("/barChartResult");
			} else if (queryCondition.getChartType().equals("LineChart")) {
				mv = new ModelAndView("/lineChartResult");
			}			
		}
		
		return mv;
	}
	
	private void addMoreIndex(List<JobInformation> result, HttpServletRequest request) {
		//map<month, map<process, crash_count>>
		HashMap<String, HashMap<String, Integer>> processMonthlyCrash = new HashMap<String, HashMap<String, Integer>>();

		for (JobInformation job : result){
			List<String> processes = job.getCrashProcess();
			
			String formatDate = DateHelper.getYearMonthFormat(job.getJobStartDate());
			//map<process, crash_count>
			HashMap<String, Integer> processCrashCount = new HashMap<String, Integer>();
			
			for (String process : processes){
				if (processCrashCount.containsKey(process))
					processCrashCount.put(process, processCrashCount.get(process) + 1);
				else
					processCrashCount.put(process, 1);
			}
			merge(formatDate, processCrashCount, processMonthlyCrash);
		}
		
		String months = "";
		String monthCount = "";
		//map<prcess, crash_count>
		HashMap<String, String> consoleCrashCount = new HashMap<String, String>();
		
		List<Map.Entry<String, HashMap<String, Integer>>> info = 
				new ArrayList<Map.Entry<String, HashMap<String, Integer>>>(processMonthlyCrash.entrySet());
		
		Collections.sort(info, new Comparator<Map.Entry<String, HashMap<String, Integer>>>(){
			public int compare(Map.Entry<String, HashMap<String, Integer>> o1, Map.Entry<String, HashMap<String, Integer>> o2){
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		int paddingZeroCount = 0;
		for (Map.Entry<String, HashMap<String, Integer>> crash: info){
			String month = crash.getKey();
			months += month + ",";
			int count = 0;
			
			//map<process, crash_count>
			HashMap<String, Integer> processCrash = crash.getValue();
			
			for (String process : processCrash.keySet()){
				int crashNumber = processCrash.get(process);
				count += crashNumber;
				
				if (consoleCrashCount.containsKey(process)){
					String previousValue = consoleCrashCount.get(process);
					
					int current = previousValue.substring(0, previousValue.lastIndexOf(",")).split(",").length;
					int zeroDiffCount = paddingZeroCount - current;
					
					while(zeroDiffCount > 0){
						previousValue += 0 + ",";
						--zeroDiffCount;
					}
					
					previousValue += crashNumber + ",";
					consoleCrashCount.put(process, previousValue);
				}else{
					String value = "";
					
					for (int i = 0; i < paddingZeroCount; i++){
						value += 0 + ",";
					}
					value += crashNumber + ",";
					consoleCrashCount.put(process, value);
				}
			}
			monthCount += count + ",";
			++paddingZeroCount;
		}
		
		months = months.substring(0, months.lastIndexOf(","));
		int totalMonth = months.split(",").length;
		
		String detailInfo = "";
		for (String process : consoleCrashCount.keySet()){
			String number = consoleCrashCount.get(process);
			
			int diff = totalMonth - number.substring(0, number.lastIndexOf(",")).split(",").length;
			if ( diff > 0){
				while(diff > 0){
					number += "0" + ",";
					diff--;
				}
				
			}
			detailInfo += process + "," + number.substring(0, number.lastIndexOf(",")) + ";";
		}
		monthCount = monthCount.substring(0, monthCount.lastIndexOf(","));
		detailInfo = detailInfo.substring(0, detailInfo.lastIndexOf(";"));

		String monthsWithProcessHeader = "Process," + months;
		
		request.setAttribute("month", months);
		request.setAttribute("monthWithProcessHeader", monthsWithProcessHeader);
		request.setAttribute("monthCount", monthCount);
		request.setAttribute("monthDetail", detailInfo);
	}
	
	private void merge(String date, HashMap<String, Integer> candidateCrash,HashMap<String, HashMap<String, Integer>> processMonthlyCrash ){
		if (!processMonthlyCrash.containsKey(date)){
			processMonthlyCrash.put(date, candidateCrash);
		}else{
			HashMap<String, Integer> processedCrash = processMonthlyCrash.get(date);
			
			for (String console : candidateCrash.keySet()){
				if (processedCrash.containsKey(console)){
					processedCrash.put(console, candidateCrash.get(console) + processedCrash.get(console));
				}else{
					processedCrash.put(console, candidateCrash.get(console));
				}
			}
		}
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
			String mwVersion = query.getMWVersion();
			if (!mwVersion.equals("none")){
				retList = this.searchMWVersion(mwVersion);
			}			
			
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
			
			String patchVersion = query.getPatchVersion();
			if (!patchVersion.equals("none")){
				
				if (retList != null)
					retList = filterByPatch(retList, patchVersion);
			}
			
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

	private List<JobInformation> filterByPatch(List<JobInformation> retList, String patchVersion) {
		
		String convertedPatch = PatchVersionDefinition.convert(patchVersion);
		List<JobInformation> ret = new ArrayList<JobInformation>();
		
		for (JobInformation job : retList){
			List<String> patches = job.getPatches();
			
			for (String patch : patches){
				if (patch.contains(convertedPatch)){
					ret.add(job);
					break;
				}
			}
		}
		return ret;
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

	public List<JobInformation> searchJobByTimeRange(String startDate, String stopDate) throws IOException {
		List<JobInformation> ret = new ArrayList<JobInformation>();

		if (startDate == null || stopDate == null)
			return ret;

		List<String> datesInRange = DateHelper.getDatesInRange(startDate, stopDate);
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
