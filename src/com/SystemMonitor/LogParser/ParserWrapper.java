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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import com.SystemMonitor.Model.SystemMonitorException;

public class ParserWrapper {
	private FeatureAnalyzer _featureAnalyzer;
	private String _logFilePath;
	
	public ParserWrapper(){}
	
	public ParserWrapper(String logFilePath, String featureMappingFilePath){
		_logFilePath = logFilePath;
		_featureAnalyzer = new FeatureAnalyzer(featureMappingFilePath);
	}
	
	public void parseCSV_multithread() throws InterruptedException{
		File fileFolder = new File(_logFilePath);
		File[] files = fileFolder.listFiles();

		ThreadPoolExecutor executor = new ThreadPoolExecutor(3, files.length, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		for (File file : files){
			executor.execute(new EXCELParser(file, _featureAnalyzer));
		}
		
		Thread.sleep(1000);
		executor.shutdown();
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

	public String getJobStartDate(File file) {		
		XSSFWorkbook workbook = null;
		try{
			workbook = new XSSFWorkbook(file.getAbsolutePath());
			XSSFSheet sheet = workbook.getSheet("job summary");

			for (int rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++){
				XSSFRow row = sheet.getRow(rowIndex);

				return row.getCell(0).getStringCellValue();
			}
		}catch (IOException e){
			SystemMonitorException.recordException(e.getMessage());
		}finally{
			closeWorkbook(workbook);
		}
		return null;
	}
	
	public List<String> getFeatures(File file) {
		XSSFWorkbook workbook = null;
		List<String> ret = new ArrayList<String>();
		
		try{
			workbook = new XSSFWorkbook(file.getAbsolutePath());
			XSSFSheet sheet = workbook.getSheet("feature summary");

			for (int rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++){
				XSSFRow row = sheet.getRow(rowIndex);

				ret.add(row.getCell(0).getStringCellValue());
			}
		}catch (IOException e){
			SystemMonitorException.recordException(e.getMessage());
		}finally{
			closeWorkbook(workbook);
		}
		return ret;
	}
	
	private void closeWorkbook(XSSFWorkbook workbook){
		if (workbook != null){
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}