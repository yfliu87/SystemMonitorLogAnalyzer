package com.SystemMonitor.LogParser;

/*
 * This class is targeted for pre-precessing system monitor logs in below aspects:
 * 1. get the job start/stop time
 * 2. get job duration time
 * 3. get feature usage info
 * 
 * All the above info will be recorded in a new sheet named "job summay" which will
 * also be indexed as log messges.
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.SystemMonitor.Model.JobInformation;
import com.SystemMonitor.Util.DateHelper;
import com.SystemMonitor.Util.SystemMonitorException;

public class ParserWrapper {
	private FeatureAnalyzer _featureAnalyzer;
	private String _logFilePath;
	
	public ParserWrapper(String logFilePath, String featureMappingFilePath){
		_logFilePath = logFilePath;
		_featureAnalyzer = new FeatureAnalyzer(featureMappingFilePath);
	}
	
	public void parseCSV_singlethread(){
		File fileFolder = new File(_logFilePath);
		File[] files = fileFolder.listFiles();
		
		for (File file : files){
			if(file.getAbsolutePath().endsWith("xls"))
				continue;
			
			EXCELParser parser = new EXCELParser(file, _featureAnalyzer);
			parser.run();
		}
	}
	
	public void parseCSV_multithread() throws InterruptedException{
		File fileFolder = new File(_logFilePath);
		File[] files = fileFolder.listFiles();
		List<File> smallFiles = new ArrayList<File>();
		List<File> tenMBFiles = new ArrayList<File>();
		List<File> twentyMBFiles = new ArrayList<File>();
		List<File> fiftyMBFiles = new ArrayList<File>();
		List<File> hundredMBFiles = new ArrayList<File>();
		List<File> overhundredMBFiles = new ArrayList<File>();
		
		for (File file : files){
			if(file.getAbsolutePath().endsWith("xls"))
				continue;
			
			if (file.length() <= 5000000){
				smallFiles.add(file);
			}
			else if (file.length() > 5000000 && file.length() <= 10000000){
				tenMBFiles.add(file);
			}
			else if (file.length() > 10000000 && file.length() <= 30000000){
				twentyMBFiles.add(file);
			}
			else if (file.length() > 30000000 && file.length() <= 50000000){
				fiftyMBFiles.add(file);
			}
			else if (file.length() > 50000000 && file.length() <= 100000000){
				hundredMBFiles.add(file);
			}
			else if (file.length() > 100000000){
				overhundredMBFiles.add(file);
			}
		}
		
		if (smallFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					smallFiles.size(), smallFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : smallFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(30000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
		if (tenMBFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					tenMBFiles.size(), tenMBFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : tenMBFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(60000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
		if (twentyMBFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					twentyMBFiles.size(), twentyMBFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : twentyMBFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(60000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
		if (fiftyMBFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					fiftyMBFiles.size(), fiftyMBFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : fiftyMBFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(60000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
		if (hundredMBFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					hundredMBFiles.size(), hundredMBFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : hundredMBFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(60000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
		if (overhundredMBFiles.size() > 0){
			ThreadPoolExecutor hugeexecutor = new ThreadPoolExecutor(
					overhundredMBFiles.size(), overhundredMBFiles.size(), 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			for (File file : overhundredMBFiles) {
				hugeexecutor.execute(new EXCELParser(file, _featureAnalyzer));
			}
			Thread.sleep(60000);
			hugeexecutor.shutdown();
			
			hugeexecutor.awaitTermination(60000, TimeUnit.SECONDS);
		}
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private void parseCSV_futuretask() throws InterruptedException{
		ExecutorService pool = Executors.newCachedThreadPool();
		List<Future<Integer>> future = new ArrayList<Future<Integer>>();
		
		File fileFolder = new File(_logFilePath);
		File[] files = fileFolder.listFiles();
		for (File subfile : files){
			Runnable r = new EXCELParser(subfile, _featureAnalyzer);
			Future<Integer> f = (Future<Integer>) pool.submit(r);
			future.add(f);
		}
		Thread.sleep(1000);
		pool.shutdown();
	}

	public Map<String, List<String>> getTools(File file) {
		HSSFWorkbook workbook = null;
		
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		
		try{
			workbook = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = workbook.getSheet(LogSheet.TOOL_SUMMARY);

			if (null == sheet){
				closeWorkbook(workbook);
				return ret;
			}
			
			for (Iterator<Row> iterRow = (Iterator<Row>)sheet.rowIterator(); iterRow.hasNext();){
				List<String> tools = new ArrayList<String>();

				Row row = iterRow.next();
				if (null == row)
					continue;
				
				Cell namecell = row.getCell(0);
				if (namecell == null)
					continue;
				
				String jobName = namecell.getStringCellValue();
				
				while(iterRow.hasNext()){
					row = iterRow.next();
					Cell cell = row.getCell(0);
					if (cell == null)
						break;
					
					String toolString = cell.getStringCellValue();
					if (toolString.isEmpty())
						break;
					
					tools.add(toolString);
				}
				
				if (ret.containsKey(jobName)){
					List<String> existTool = ret.get(jobName);
					existTool.retainAll(tools);
					ret.put(jobName, existTool);
				}
				else{
					ret.put(jobName, tools);
				}
			}
		}catch (IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e, file);
		}finally{
			closeWorkbook(workbook);
		}
		return ret;
	}
	
	public Map<String, List<String>> getFeatures(File file) {
		HSSFWorkbook workbook = null;
		
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		
		try{
			workbook = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = workbook.getSheet(LogSheet.FEATURE_SUMMARY);

			if (null == sheet){
				closeWorkbook(workbook);
				return ret;
			}
			
			for (Iterator<Row> iterRow = (Iterator<Row>)sheet.rowIterator(); iterRow.hasNext();){
				List<String> features = new ArrayList<String>();

				Row row = iterRow.next();
				String jobName = row.getCell(0).getStringCellValue();
				
				while(iterRow.hasNext()){
					row = iterRow.next();
					Cell cell = row.getCell(0);
					if (cell == null)
						break;
					
					String feature = cell.getStringCellValue();
					if (feature.isEmpty())
						break;
					
					features.add(feature);
				}
				
				if (ret.containsKey(jobName)){
					List<String> existFeature = ret.get(jobName);
					existFeature.retainAll(features);
					ret.put(jobName, existFeature);
				}
				else{
					ret.put(jobName, features);
				}
			}
		}catch (IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e, file);
		}finally{
			closeWorkbook(workbook);
		}
		return ret;
	}
	
	private void closeWorkbook(HSSFWorkbook workbook){
		if (workbook != null){
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<JobInformation> getJobInfo(File file) {
		HSSFWorkbook workbook = null;
		JobInformation jobInfo = null;
		List<JobInformation> ret = new ArrayList<JobInformation>();
		
		try{
			workbook = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = workbook.getSheet(LogSheet.JOB_SUMMARY);
			
			if (null == sheet){
				closeWorkbook(workbook);
				return ret;
			}
			
			int baseRowIndex = 0;
			Row row = sheet.getRow(baseRowIndex++);
			String jobsValue = row.getCell(0).getStringCellValue();
			int jobCount = Integer.parseInt(jobsValue.substring(0, jobsValue.indexOf("_")));
			
			for (int idx = 0; idx < jobCount; idx++){

				try {
					jobInfo = new JobInformation();

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.MWVERSION.ordinal());
					jobInfo.setMWVersion(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.PATCHVERSION.ordinal());
					readPatchVersion(row, jobInfo);

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.JOB_NAME.ordinal());
					jobInfo.setJobName(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.WELL_NAME.ordinal());
					jobInfo.setWellName(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.CLIENT_NAME.ordinal());
					jobInfo.setClientName(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.WORKFLOW.ordinal());
					jobInfo.setWorkflow(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.UNITSYSTEM.ordinal());
					jobInfo.setUnitSystem(row.getCell(0).getStringCellValue());

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.STARTDATE.ordinal());
					jobInfo.setJobStartDate(DateHelper.formatCalendar(row
							.getCell(0).getStringCellValue()));

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.DURATION.ordinal());
					jobInfo.setJobDuration(Double.parseDouble(row.getCell(0)
							.getStringCellValue()));

					row = sheet.getRow(baseRowIndex
							+ JobSummaryRowDefinition.CRASHED.ordinal());
					jobInfo.setCrashed(row.getCell(0).getStringCellValue()
							.equals("crashed"));

					if (jobInfo.isCrashed()) {
						readCrashProcess(row, jobInfo);
					}

					baseRowIndex += JobSummaryRowDefinition.CRASHED.ordinal() + 2;

					ret.add(jobInfo);
				} catch (Exception e) {
					SystemMonitorException.logException(Thread.currentThread(), e, String.valueOf(idx));
				}
			}

		}catch(IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}finally{
			closeWorkbook(workbook);
		}
		return ret;
	}

	private void readPatchVersion(Row row, JobInformation jobInfo) {
		Iterator<Cell> iterCell =(Iterator<Cell>)row.cellIterator();
		while(iterCell.hasNext()){ 
			Cell cell = iterCell.next();
			
			if (cell != null)
				jobInfo.updatePatchVersion(cell.getStringCellValue());
		}
	}

	private void readCrashProcess(Row row, JobInformation jobInfo) {
		Iterator<Cell> iterCell =(Iterator<Cell>)row.cellIterator();
		iterCell.next();
		while(iterCell.hasNext()){ 
			Cell cell = iterCell.next();
			
			if (cell != null)
				jobInfo.updateCrashProcess(cell.getStringCellValue());
		}
	}

	public void filterTargetJobByType(String filter) {
		// TODO Auto-generated method stub
		File fileFolder = new File(_logFilePath);
		File[] files = fileFolder.listFiles();
		
		for (File file : files){
			if(file.getAbsolutePath().endsWith("xls"))
				continue;
			
			DataInputStream dis = null;
			try{
				ArrayList<ArrayList<String>> arList = new ArrayList<ArrayList<String>>();
				ArrayList<String> al = null;
				String fPath = file.getAbsolutePath();
				FileInputStream fis = new FileInputStream(fPath);
				dis = new DataInputStream(fis);
				String line = null;
				
				boolean shouldDelete = false;
				while((line = dis.readLine()) != null){
					al = new ArrayList<String>();
					String[] strs = line.split(",");
					for(String str : strs){
						if (str.contains("Workflow=Wireline") ||
							str.contains("Workflow=PerfoExpress") ||
							str.contains("Workflow=WL Recorder") ||
							str.contains("Workflow=Coiled Tubing")){
							shouldDelete = true;
							break;
						}
						al.add(str);
					}
					
					if (shouldDelete){
						break;
					}
					arList.add(al);
				}
				
				if (shouldDelete){
					al.clear();
					fis.close();
					file.delete();
				}
			}catch(Exception e){
				
			}
		}	
	}
}
