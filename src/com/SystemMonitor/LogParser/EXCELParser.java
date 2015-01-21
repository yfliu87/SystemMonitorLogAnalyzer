package com.SystemMonitor.LogParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.SystemMonitor.Model.JobInfo;
import com.SystemMonitor.Model.SystemMonitorException;
import com.SystemMonitor.Util.DateHelper;

public class EXCELParser implements Runnable{
	private File _file;
	private FeatureAnalyzer _featureAnalyzer;

	public EXCELParser(File file, FeatureAnalyzer analyzer){
		this._file = file;
		this._featureAnalyzer = analyzer;
	}

	@Override
	public void run() {
		HSSFWorkbook workbook = null;
		try{
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(this._file));
			workbook = new HSSFWorkbook(fs);

			HSSFSheet sheet = workbook.getSheetAt(0);
			Calendar jobStartDate = null, jobStopDate = null;
			JobInfo info = new JobInfo();

			Map<String, Set<String>> featureUsage = new HashMap<String, Set<String>>();

			for (Iterator<Row> iterRow = (Iterator<Row>) sheet.rowIterator(); iterRow
					.hasNext();) {
				Row row = iterRow.next();

				if (row == null)
					continue;

				updateFeature(row, featureUsage);

				if (row.getRowNum() == sheet.getFirstRowNum() + 1){
					jobStartDate = readDateInfo(row);
					updateJobInfo(row, info);
				}

				if (row.getRowNum() == sheet.getLastRowNum())
					jobStopDate = readDateInfo(row);
			}
			// detect the features according to components and predefined
			// feature component mapping
			Set<String> featureFound = detectFeature(featureUsage);

			updateSummary(workbook, _file, info, jobStartDate, jobStopDate, featureFound);
		} catch (Exception e) {
			SystemMonitorException.recordException(e.getMessage());
		} finally {
			closeWorkbook(workbook);
		}
	}
	
	private void updateJobInfo(Row row, JobInfo info) {
		Cell cell = row.getCell(LogColumnDefinition.CONTEXT.ordinal());
		
		if (cell != null){
			String context = cell.getStringCellValue();
			String[] content = context.split(";");
			
			for (String msg : content){
				int idx = msg.lastIndexOf("=");

				if (msg.startsWith("JobName")){
					info.setJobName(msg.substring(idx, msg.length()));
				}else if (msg.startsWith("WellName")){
					info.setWellName(msg.substring(idx, msg.length()));
				}else if (msg.startsWith("ClientName")){
					info.setClientName(msg.substring(idx, msg.length()));
				}else if (msg.startsWith("Workflow")){
					info.setWorkflow(msg.substring(idx, msg.length()));
				}else if (msg.startsWith("Simulator")){
					info.setSimulator(msg.substring(idx, msg.length()).equals("true"));
				}else if (msg.startsWith("UnitSystem")){
					info.setUnitSystem(msg.substring(idx, msg.length()));
				}else if (msg.startsWith("JobSize")){
					info.setJobSize(Double.parseDouble(msg.substring(idx, msg.length())));
				}
			}
		}
	}

	private Calendar readDateInfo(Row row) {
		Cell cell = row.getCell(LogColumnDefinition.TIME.ordinal());
		
		if (cell != null)
			return DateUtil.getJavaCalendarUTC(cell.getNumericCellValue(), false);
		
		return null;
	}

	/*
	 * This method outputs the features applied in current job according to the components found in log.
	 * The components found in log will have corresponding feature predefined
	 * It will check the feature mapped by components which detected in log
	 * Then find the intersect of components predefined and found in current log
	 * If similarity is over 90 % then this feature is considered to be used.
	 */
	private Set<String> detectFeature(Map<String, Set<String>> featureUsage) {
		Set<String> featureApplied = new HashSet<String>();
		Set<String> featureCandidates = featureUsage.keySet();

		for (String feature : featureCandidates){
			Set<String> components = featureUsage.get(feature);
			Set<String> predefinedComponents = _featureAnalyzer.queryComponent(feature);
			
			if (similar(components, predefinedComponents)){
				featureApplied.add(feature);
			}
		}
		return featureApplied;
	}

	private boolean similar(Set<String> components, Set<String> predefinedComponents) {
		int count = predefinedComponents.size();
		predefinedComponents.retainAll(components);
		
		return predefinedComponents.size()/count > 0.9;
	}

	private void updateFeature(Row row, Map<String, Set<String>> featureUsage) {
		String componentName = row.getCell(
				LogColumnDefinition.COMPONENT.ordinal()).getStringCellValue();
		if (componentName == null)
			return;

		Set<String> features = _featureAnalyzer.queryFeature(componentName);

		for (String feature : features) {
			if (featureUsage.containsKey(feature)) {
				Set<String> components = featureUsage.get(feature);
				components.add(componentName);
				featureUsage.put(feature, components);
			} else {
				Set<String> components = new HashSet<String>();
				components.add(componentName);
				featureUsage.put(feature, components);
			}
		}
	}

	private void updateSummary(HSSFWorkbook workbook, File subfile, JobInfo info,
			Calendar jobStartDate, Calendar jobStopDate,
			Set<String> featureFound) {

		int sheetIndex = 0;
		if ((sheetIndex = workbook.getSheetIndex("job summary")) != -1){
			workbook.removeSheetAt(sheetIndex);
		}
		
		Sheet newSheet = workbook.createSheet("job summary");
		int rowToBeAdded = 0;
		updateJobStartDate(newSheet, jobStartDate, rowToBeAdded++);
		updateJobDuration(newSheet, jobStartDate, jobStopDate, rowToBeAdded++);
		updateJobinfo(newSheet, info, rowToBeAdded);

		if ((sheetIndex = workbook.getSheetIndex("feature summary")) != -1){
			workbook.removeSheetAt(sheetIndex);
		}
		
		Sheet featureSheet = workbook.createSheet("feature summary");
		updateFeatures(featureSheet, featureFound);

		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(subfile);
			workbook.write(fileOut);
		}catch(Exception e){
			SystemMonitorException.recordException(e.getMessage());
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					SystemMonitorException.recordException(e.getMessage());
				}
			}
		}
	}

	private void updateJobinfo(Sheet newSheet, JobInfo info, int rowIndex) {
		
		Row newRow = newSheet.createRow(rowIndex++);
		Cell newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
		newCell.setCellValue(info.getJobName());
		
		newRow = newSheet.createRow(rowIndex++);
		newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
		newCell.setCellValue(info.getWellName());

		newRow = newSheet.createRow(rowIndex++);
		newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
		newCell.setCellValue(info.getClientName());

		newRow = newSheet.createRow(rowIndex++);
		newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
		newCell.setCellValue(info.getWorkflow());

		newRow = newSheet.createRow(rowIndex++);
		newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
		newCell.setCellValue(info.getUnitSystem());

		newRow = newSheet.createRow(rowIndex++);
		newCell = newRow.createCell(0, Cell.CELL_TYPE_NUMERIC);
		newCell.setCellValue(info.getJobSize());
	}

	private void updateJobDuration(Sheet newSheet, Calendar jobStartDate, Calendar jobStopDate, int rowIndex) {

		Row durationRow = newSheet.createRow(rowIndex);
		Cell durationCell = durationRow.createCell(0, Cell.CELL_TYPE_NUMERIC);
		double duration = ((jobStopDate.getTimeInMillis() - jobStartDate
				.getTimeInMillis()) / (60 * 60 * 1000.0));
		durationCell.setCellValue(duration);
	}

	private void updateJobStartDate(Sheet newSheet, Calendar jobStartDate, int rowIndex) {
		Row startDateRow = newSheet.createRow(rowIndex);
		
		Cell startDateCell = startDateRow.createCell(0, Cell.CELL_TYPE_STRING);
		startDateCell.setCellValue(DateHelper.formatDate(jobStartDate));
	}

	private void updateFeatures(Sheet newSheet, Set<String> featureFound) {
		int rowToBeAdded = 0;

		for (String feature : featureFound) {
			Row durationRow = newSheet.createRow(rowToBeAdded++);
			Cell durationCell = durationRow
					.createCell(0, Cell.CELL_TYPE_STRING);
			durationCell.setCellValue(feature);
		}
	}

	
	private void closeWorkbook(HSSFWorkbook workbook){
		if (workbook != null){
			try {
				workbook.close();
			} catch (IOException e) {
				SystemMonitorException.recordException(e.getMessage());
			}
		}
	}
}
