package com.SystemMonitor.Main;

import java.io.File;
import java.io.FileNotFoundException;

import com.SystemMonitor.Algorithm.CallstackAnalyzer;
import com.SystemMonitor.Algorithm.FPGrowth;
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
			ParserWrapper csvParser = new ParserWrapper(logFilePath, featureMappingFilePath, config.getCrashDetailPath());
//			csvParser.parseCSV_multithread();
//			csvParser.filterTargetJobByType("D&M");
//			System.out.println("filter done");

			csvParser.parseCSV_singlethread();
			
//			crashFromFPGrowth(config.getCrashDetailPath());
			
			crashFromPrefixTree(config.getCrashDetailPath(), config.getStatisticFilePath());
//			
			String indexFilePath = config.getIndexLocation();
			new LogIndexer(logFilePath, indexFilePath, csvParser).index();
			System.out.println("index finished");
		}catch(Exception e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}

	private static void crashFromPrefixTree(String crashDetailPath, String statisticFile) {
		CallstackAnalyzer analyzer = new CallstackAnalyzer();
		analyzer.buildTree(crashDetailPath);
		analyzer.printResult(statisticFile);
	}

	private static void crashFromFPGrowth(String crashDetailPath) {
		try {
			FPGrowth growth = new FPGrowth(new File(crashDetailPath), 3);

		} catch (FileNotFoundException e) {
			SystemMonitorException.logException(Thread.currentThread(), e, e.getMessage());
		}
	}

}
