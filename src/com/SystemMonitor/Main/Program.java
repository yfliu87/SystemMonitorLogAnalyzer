package com.SystemMonitor.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

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
			ParserWrapper csvParser = new ParserWrapper(logFilePath, featureMappingFilePath, config.getCrashDetailPath(), config.getCallStackDepth());
//			csvParser.parseCSV_multithread();
//			csvParser.filterTargetJobByType("D&M");
//			System.out.println("filter done");

//			csvParser.parseCSV_singlethread();
//			System.out.println("parse finished");

//			crashFromFPGrowth(config.getCrashDetailPath());
//			System.out.println("FPGrowth statistics finished");
			
			crashFromPrefixTree(config.getCrashDetailPath(), config.getStatisticFilePath(), config.getCrashThreshold(), 
					config.getTargetConsole(), config.getTargetVersion());
			System.out.println("prefix tree statistic finished");

//			new LogIndexer(logFilePath, config.getIndexLocation(), csvParser).index();
//			System.out.println("index finished");
			
		}catch(Exception e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}

	private static void crashFromPrefixTree(String crashDetailPath, String statisticFilePath, int crashThreshold, List<String> targetConsoles, List<String> targetVersions) {
		CallstackAnalyzer analyzer = new CallstackAnalyzer(targetVersions);
		analyzer.buildTree(crashDetailPath);
		analyzer.printResult(statisticFilePath, crashThreshold, targetConsoles);
	}

	private static void crashFromFPGrowth(String crashDetailPath) {
		try {
			FPGrowth growth = new FPGrowth(new File(crashDetailPath), 3);

		} catch (FileNotFoundException e) {
			SystemMonitorException.logException(Thread.currentThread(), e, e.getMessage());
		}
	}

}
