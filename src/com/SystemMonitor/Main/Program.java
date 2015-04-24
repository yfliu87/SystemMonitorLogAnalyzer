package com.SystemMonitor.Main;

import com.SystemMonitor.Indexer.LogIndexer;
import com.SystemMonitor.LogParser.ParserWrapper;
import com.SystemMonitor.Util.ConfigHelper;
import com.SystemMonitor.Util.SystemMonitorException;

public class Program {

	public static void main(String[] args) {
		ConfigHelper config = ConfigHelper.getInstance("D:\\NotBackedUp\\SystemMonitorLogAnalyzer\\SystemMonitorLogAnalyzer\\config.txt");
		
		String logFilePath = config.getLogLocation();
		String featureMappingFilePath = config.getFeatureMappingFileLocation();
		
		try{
			ParserWrapper csvParser = new ParserWrapper(logFilePath, featureMappingFilePath);
//			csvParser.parseCSV_multithread();
//			csvParser.filterTargetJobByType("D&M");
//			System.out.println("filter done");
//			
			csvParser.parseCSV_singlethread();
//			
			String indexFilePath = config.getIndexLocation();
			LogIndexer indexer = new LogIndexer(logFilePath, indexFilePath, csvParser);
//			
			indexer.index();
			System.out.println("index finished");
		}catch(Exception e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}

}
