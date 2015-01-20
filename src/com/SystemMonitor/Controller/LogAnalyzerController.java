package com.SystemMonitor.Controller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.SystemMonitor.Indexer.LogIndexer;
import com.SystemMonitor.Model.SystemMonitorQuery;
import com.SystemMonitor.Util.ConfigHelper;
import com.SystemMonitor.Util.DateHelper;

@Controller
public class LogAnalyzerController {
	private LogIndexer _indexer;
	private String _logFilePath;
	private int _jobSize;
	private String _configFilePath = "/home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer/config.txt";

	@RequestMapping(value="/logAnalyzer",method=RequestMethod.GET)
	public String logAnalyzerHandler(Model model){
		init();
		
		model.addAttribute(new SystemMonitorQuery());
		return "showLogAnalyzer";
	}
	
	@RequestMapping(value="/logAnalyzer", method=RequestMethod.POST)
	public String queryLogHandler(SystemMonitorQuery queryCondition, Model resultModel){
		List<String> result = new ArrayList<String>();
		
		try {
			result = this.searchByQuery(queryCondition);
			resultModel.addAttribute("result", result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "queryResult";
	}

	private void init(){
		ConfigHelper config = ConfigHelper.getInstance(_configFilePath);

		String logFilePath = config.getLogLocation();
		_logFilePath = logFilePath;

		String indexFilePath = config.getIndexLocation();
		try {
			_indexer = new LogIndexer(logFilePath, indexFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		_jobSize = getJobSize();
	}
	
	
	private int getJobSize(){
		File fileFolder = new File(this._logFilePath);
		File[] files = fileFolder.listFiles();
		return files.length;
	}

	public int getTotalJobs() {
		return _jobSize;
	}

	public List<String> searchToolUsage(String toolName) throws IOException{
		return _indexer.searchToolUsage(toolName);
	}
	
	public List<String> searchCrashJobs() throws IOException{
		return _indexer.searchCrashJobs();
	}
	
	public List<String> searchFeatureUsage(String featureName) throws IOException{
		if (featureName == null)
			return new ArrayList<String>();

		return _indexer.searchJobFeature(featureName);
	}
	
	public List<String> searchJobByDate(String date) throws IOException{
		List<String> ret = new ArrayList<String>();
		
		if (date == null)
			return ret;
		
		ret = _indexer.searchJobByDate(date);
		return ret;
	}

	public List<String> searchJobByTimeRange(String startDate, String stopDate) throws IOException {
		List<String> ret = new ArrayList<String>();
		
		if (startDate == null || stopDate == null)
			return ret;
		
		List<String> datesInRange = DateHelper.getDatesInRange(startDate, stopDate);
		List<String> result = null;
		Set<String> candidates = new HashSet<String>();
		
		for (String date : datesInRange){
			result = _indexer.searchJobByDate(date);
			candidates.addAll(result);
		}
		
		return new ArrayList<String>(candidates);
	}

	public List<String> searchByQuery(SystemMonitorQuery query) throws IOException {
		String toolName = query.getToolString();
		String startDate = query.getJobStartDate();
		String stopDate = query.getJobStopDate();
		boolean isCrash = query.isCrashed();
		List<String> features = query.getFeatures();
		List<String> retList = null;

		if (!toolName.isEmpty()){
			retList = this.searchToolUsage(toolName); 
		}
		
		if (!startDate.isEmpty() && stopDate.isEmpty()){
			List<String> startList = this.searchJobByDate(startDate);
			
			if (retList == null)
				retList = startList;
			else
				retList.retainAll(startList);
		}

		if (startDate.isEmpty() && !stopDate.isEmpty()) {
			List<String> stopList = this.searchJobByDate(stopDate);
			
			if (retList == null)
				retList = stopList;
			else
				retList.retainAll(stopList);
		}
		
		if (!startDate.isEmpty() && !stopDate.isEmpty()){
			List<String> durationList = this.searchJobByTimeRange(startDate, stopDate);
			
			if (retList == null)
				retList = durationList;
			else
				retList.retainAll(durationList);
		}
		
		if (isCrash){
			List<String> crashList = this.searchCrashJobs();
			
			if (retList == null)
				retList = crashList;
			else
				retList.retainAll(crashList);
		}
		
		for(String feature : features){
			List<String> jobs = this.searchFeatureUsage(feature);
			
			if (retList == null)
				retList = jobs;
			else
				retList.retainAll(jobs);
		}
		
		return new ArrayList<String>(retList);
	}
}
