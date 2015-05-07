package com.SystemMonitor.LogParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.SystemMonitor.Model.CrashDetail;
import com.SystemMonitor.Model.JobInformation;
import com.SystemMonitor.Util.CSVConverter;
import com.SystemMonitor.Util.DateHelper;
import com.SystemMonitor.Util.SystemMonitorException;

public class EXCELParser implements Runnable{
	private File _file;
	private FeatureAnalyzer _featureAnalyzer;
	private List<String> _filterJobName ;
	private BufferedWriter _crashDetailFile;
	private Hashtable<String, Integer> _operations;
	private Hashtable<String, Integer> _components;
	private int _callstackDepth;

	public EXCELParser(File file, FeatureAnalyzer analyzer, BufferedWriter crashDetailFile, Hashtable<String, Integer> operations, Hashtable<String, Integer> components, int callstackDepth){
		this._file = file;
		this._featureAnalyzer = analyzer;
		this._filterJobName = new ArrayList<String>(Arrays.asList(
				"MCAL","MasterCal","Master Calibration","Master_Cal","Master_Calibration",
				"BCAL","BeforeCal","Before Cal","Before Calibration","Before_Cal","Before_Calibration",
				"calibration","CalCheck","op check","opchekc","op chk","OP_CHECK","op_chk",
				"Simulitor=N","test","shop","wellsite","KLC","OPCK","Yanjiao"));
		this._crashDetailFile = crashDetailFile;
		this._operations = operations;
		this._components = components;
		this._callstackDepth = callstackDepth;
	}

	@Override
	public void run() {
		HSSFWorkbook workbook = null;
		try{
			String xlsFile = CSVConverter.convertToXLS(this._file);
			workbook = new HSSFWorkbook(new FileInputStream(xlsFile));

			HSSFSheet sheet = workbook.getSheetAt(0);
			Calendar jobStopDate = null;
			
			JobInformation jobInfo = null;
			Map<String, Set<String>> featureUsage = null;
			List<String> tools = null;
			
			List<JobInformation> jobs = new ArrayList<JobInformation>();
			List<Map<String, Set<String>>> featureUsages = new ArrayList<Map<String, Set<String>>>();
			List<Map<String, List<String>>> toolsList = new ArrayList<Map<String, List<String>>>();

			boolean isNewJob = false;
			int rowIndex = 0;
			for (Iterator<Row> iterRow =(Iterator<Row>)sheet.rowIterator(); iterRow.hasNext();){ 
				
				Row row = iterRow.next();
				if (row == null)
					continue;

				if (!isNewJob && jobStart(row)){
					jobInfo = new JobInformation();
					tools = new ArrayList<String>();
					featureUsage = new HashMap<String, Set<String>>();
					
					readJobInfo(row, jobInfo);
					isNewJob = true;
				}
				
				if (isNewJob && jobStop(row)){
					jobStopDate = readDateInfo(row);
					jobInfo.setJobStopDate(jobStopDate);
					jobInfo.setJobDuration(this.getJobDuration(jobInfo.getJobStartDate(), jobInfo.getJobStopDate()));
					
					this._crashDetailFile.write(jobInfo.getCrashDetail());

					Set<String> featureFound = detectFeature(featureUsage);
					updateJobInfo(jobInfo, jobs, featureFound, featureUsages, tools, toolsList);
					
					isNewJob = false;
				}
				
				if (isNewJob){
					updateFeature(row, featureUsage);
					
					if (isNewRunStarted(row)){
						getToolstring(row, tools);
					}
					
					if (containsCrashInfo(row)){
						recordCrashInfo(row, jobInfo);
						
						traceback(sheet, row, jobInfo);
					}
				}
			}
			
			updateSummary(workbook, new File(xlsFile), jobs, featureUsages, toolsList);	
			
		} catch (Exception e) {
			SystemMonitorException.logException(Thread.currentThread(), e, this._file);
		} finally {
			closeWorkbook(workbook);
		}
	}
	
	private void traceback(HSSFSheet sheet, Row row, JobInformation jobInfo) throws IOException {
		int rowIndex = row.getRowNum();
		CrashDetail detail = null;
		Stack<CrashDetail> detailStack = new Stack<CrashDetail>();
		
		int index = rowIndex - this._callstackDepth;
		while (index < rowIndex) {
			Row reasonRow = sheet.getRow(index);
			++index;
			if (reasonRow == null)
				continue;
		
			Cell cell = reasonRow.getCell(LogColumnDefinition.PROCESS.ordinal());
			if (cell == null)
				continue;

			Cell componentCell = reasonRow.getCell(LogColumnDefinition.COMPONENT.ordinal());
			Cell operationCell = reasonRow.getCell(LogColumnDefinition.OPERATION.ordinal());
			
			String value = (componentCell!=null) ? componentCell.getStringCellValue() : "";
			value += (operationCell != null) ? " - " + operationCell.getStringCellValue() : "";

			detail = new CrashDetail();
			detail.updateProcessOperation(value);

			value = "";
			Cell contextCell = reasonRow.getCell(LogColumnDefinition.CONTEXT.ordinal());
			if (contextCell != null)
				value = contextCell.getStringCellValue();
			
			if (!value.equals("")){
				String component = null;
				if (value.contains("ComponentCode"))
					component = value.substring(value.indexOf("ComponentCode"));
				
				String target = "";
				if (component != null && component.contains(";"))
					target = component.substring(0, component.indexOf(";"));
			
				detail.updateComponent(target);
				
				updatePool(componentCell.getStringCellValue(), operationCell.getStringCellValue(), target);
			}
			detailStack.push(detail);
		}
		jobInfo.updateCrashDetails(detailStack);	
	}

	private void updatePool(String process, String operation, String component) {
		if (this._operations.containsKey(operation)){
			this._operations.put(operation, this._operations.get(operation) + 1);
		}else{
			this._operations.put(operation, 1);
		}
		
		if (this._components.containsKey(component)){
			this._components.put(component, this._components.get(component) + 1);
		}else{
			this._components.put(component, 1);
		}
	}

	private void recordCrashInfo(Row row, JobInformation jobInfo) {
		jobInfo.setCrashed(true);
		
		Cell cell = row.getCell(LogColumnDefinition.PROCESS.ordinal());
		if (cell != null){
			String crashProcess = cell.getStringCellValue();
			
			if (crashProcess.startsWith("Rig Floor") && crashProcess.contains("-"))
				crashProcess = crashProcess.substring(0, crashProcess.lastIndexOf("-") - 1);
			
			jobInfo.updateCrashProcess(crashProcess);
		}
		else
			jobInfo.updateCrashProcess("unknown");
	}

	private void updateJobInfo(JobInformation jobInfo, List<JobInformation> jobs, 
			Set<String> featureFound, List<Map<String, Set<String>>> featureUsages, 
			List<String> tools, List<Map<String, List<String>>> toolsList) {
		
		if (_filterJobName.contains(jobInfo.getJobName()) || jobInfo.isSimulation() ||
				(jobInfo.getWorkflow().equals("D&M") && (jobInfo.getJobDuration() < 1.0 || jobInfo.getJobDuration() > 2500.0)) ||
				(jobInfo.getWorkflow().equals("Wireline") && (jobInfo.getJobDuration() < 1.0 || jobInfo.getJobDuration() > 400.0)))
			return;
			
		jobs.add(jobInfo);
		Map<String, Set<String>> jobFeature = new HashMap<String, Set<String>>();
		jobFeature.put(jobInfo.getJobName() + "_" + DateHelper.formatDate(jobInfo.getJobStartDate(), true), featureFound);
		featureUsages.add(jobFeature);
		
		Map<String, List<String>> jobTool = new HashMap<String, List<String>>();
		jobTool.put(jobInfo.getJobName() + "_" + DateHelper.formatDate(jobInfo.getJobStartDate(), true), tools);
		toolsList.add(jobTool);
	}

	private boolean jobStop(Row row) {
		Cell cell = row.getCell(LogColumnDefinition.OPERATION.ordinal());
		if (cell != null){
			return cell.getStringCellValue().equals("Stop Job");
		}
		return false;
	}

	private boolean jobStart(Row row) {
		Cell cell = row.getCell(LogColumnDefinition.OPERATION.ordinal());
		if (cell != null){
			return cell.getStringCellValue().equals("Start Job");
		}
		return false;
	}

	private boolean containsCrashInfo(Row row) {
		return row.getCell(LogColumnDefinition.CONTEXT.ordinal()).getStringCellValue().toLowerCase().contains("crash");
	}

	private void readJobInfo(Row row, JobInformation jobInfo) {
		Calendar jobStartDate = readDateInfo(row);
		jobInfo.setJobStartDate(jobStartDate);
		String[] values = null;
		
		if (row.getCell(LogColumnDefinition.CONTEXT.ordinal()).getCellType() == Cell.CELL_TYPE_NUMERIC){
			values = String.valueOf(row.getCell(LogColumnDefinition.CONTEXT.ordinal()).getNumericCellValue()).split(";");
		}
		else{
			values = row.getCell(LogColumnDefinition.CONTEXT.ordinal()).getStringCellValue().split(";");		
		}
		
		for (String value : values){
			if (value.startsWith("JobName")) {
				jobInfo.setJobName(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("WellName")) {
				jobInfo.setWellName(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("ClientName")) {
				jobInfo.setClientName(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("Workflow")) {
				jobInfo.setWorkflow(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("Simulator")) {
				jobInfo.setSimulation(value.substring(value.indexOf("=") + 1)
						.toLowerCase().equals("true"));
			} else if (value.startsWith("UnitSystem")) {
				jobInfo.setUnitSystem(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("JobSize")) {
				jobInfo.setJobSize(Double.parseDouble(value.substring(value
						.indexOf("=") + 1)));
			} else if (value.startsWith("MWVersion")){
				jobInfo.setMWVersion(value.substring(value.indexOf("=") + 1));
			} else if (value.startsWith("Patch")){
				jobInfo.updatePatchVersion(value.substring(value.indexOf("=") + 1));
			}
		}
	}

	private void getToolstring(Row row, List<String> tools){
		Cell cell = row.getCell(LogColumnDefinition.CONTEXT.ordinal());
		String[] vals = cell.getStringCellValue().split(";");
		
		for (String val : vals){
			if (val.startsWith("DownholeEquipments")){
				tools.add(val.substring(val.indexOf("=") + 1));
			}
		}
	}
	
	private boolean isNewRunStarted(Row row){
		String cellValue = row.getCell(LogColumnDefinition.OPERATION.ordinal()).getStringCellValue();
		return cellValue.contains("Start Run") || 
			   cellValue.contains("Associate toolstring to run") ||
			   cellValue.contains("Activate Run");
	}

	private Calendar readDateInfo(Row row) {
		Cell cell = row.getCell(LogColumnDefinition.TIME.ordinal());
		
		if (cell != null){
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
				return DateUtil.getJavaCalendarUTC(cell.getNumericCellValue(), false);
			}
			else{
				return DateUtil.getJavaCalendarUTC(Double.parseDouble(cell.getStringCellValue()), false);
			}
		}
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

	private void updateSummary(HSSFWorkbook workbook, File targetFile,
			List<JobInformation> jobs,
			List<Map<String, Set<String>>> featureUsages,
			List<Map<String, List<String>>> toolsList) {
		
		FileOutputStream fileOut = null;
		try {

			Sheet summarySheet = workbook.getSheet(LogSheet.JOB_SUMMARY);
			if (summarySheet != null) {
				workbook.removeSheetAt(workbook.getSheetIndex(summarySheet));
			}
			summarySheet = workbook.createSheet(LogSheet.JOB_SUMMARY);

			updateJobInfo(summarySheet, jobs);
			
			Sheet toolSheet = workbook.getSheet(LogSheet.TOOL_SUMMARY);
			if (toolSheet != null) {
				workbook.removeSheetAt(workbook.getSheetIndex(toolSheet));
			}
			toolSheet = workbook.createSheet(LogSheet.TOOL_SUMMARY);
			updateTools(toolSheet, toolsList);

			Sheet featureSheet = workbook.getSheet(LogSheet.FEATURE_SUMMARY);
			if (featureSheet != null) {
				workbook.removeSheetAt(workbook.getSheetIndex(featureSheet));
			}

			featureSheet = workbook.createSheet(LogSheet.FEATURE_SUMMARY);
			updateFeatures(featureSheet, featureUsages);

			fileOut = new FileOutputStream(targetFile);
			workbook.write(fileOut);
		} catch (Exception e) {
			SystemMonitorException.logException(Thread.currentThread(), e, this._file);
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					SystemMonitorException.logException(Thread.currentThread(), e);
				}
			}
		}
	}

	private void updateJobInfo(Sheet newSheet, List<JobInformation> jobs) {
		setValue(newSheet, 0, Cell.CELL_TYPE_STRING, jobs.size() + "_jobs");
		
		int baseRowIndex = 1;
		for (int i = 0; i < jobs.size(); i++){
			JobInformation info = jobs.get(i);
			
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.MWVERSION.ordinal(), Cell.CELL_TYPE_STRING, info.getMWVersion());
			setPatchValues(newSheet, baseRowIndex + JobSummaryRowDefinition.PATCHVERSION.ordinal(), 0, Cell.CELL_TYPE_STRING, info.getPatches());
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.JOB_NAME.ordinal(), Cell.CELL_TYPE_STRING, info.getJobName() + "_" + DateHelper.formatDate(info.getJobStartDate(), true));
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.WELL_NAME.ordinal(), Cell.CELL_TYPE_STRING, info.getWellName());
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.CLIENT_NAME.ordinal(), Cell.CELL_TYPE_STRING, info.getClientName());
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.WORKFLOW.ordinal(), Cell.CELL_TYPE_STRING, info.getWorkflow());
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.UNITSYSTEM.ordinal(), Cell.CELL_TYPE_STRING, info.getUnitSystem());
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.STARTDATE.ordinal(), Cell.CELL_TYPE_STRING, DateHelper.formatDate(info.getJobStartDate(), true));
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.STOPDATE.ordinal(), Cell.CELL_TYPE_STRING, DateHelper.formatDate(info.getJobStopDate(), true));
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.DURATION.ordinal(), Cell.CELL_TYPE_STRING, String.valueOf(getJobDuration(info.getJobStartDate(), info.getJobStopDate())));
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.JOBSIZE.ordinal(), Cell.CELL_TYPE_STRING, String.valueOf(info.getJobSize()));
			setValue(newSheet, baseRowIndex + JobSummaryRowDefinition.CRASHED.ordinal(), Cell.CELL_TYPE_STRING, info.isCrashed()?"crashed" : "not crashed");
			
			if (info.isCrashed()){
				setCrashValues(newSheet, baseRowIndex + JobSummaryRowDefinition.CRASHED.ordinal(), 1, Cell.CELL_TYPE_STRING, info.getCrashProcess());
			}
			baseRowIndex += JobSummaryRowDefinition.CRASHED.ordinal() + 2;
		}
	}

	private void updateTools(Sheet newSheet, List<Map<String, List<String>>> toolsList){
		int rowToBeAdded = 0;
		for (Map<String, List<String>>tools : toolsList){
			Set<String> jobName = tools.keySet();
			
			for (String job : jobName){
				List<String> jobTool = tools.get(job);
				
				Row newRow = newSheet.createRow(rowToBeAdded++);
				Cell newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
				newCell.setCellValue(job);
				
				for (String tool : jobTool){
					newRow = newSheet.createRow(rowToBeAdded++);
					newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
					newCell.setCellValue(tool);
				}
				newRow = newSheet.createRow(rowToBeAdded++);
			}
		}
	}
	
	private void updateFeatures(Sheet newSheet, List<Map<String, Set<String>>> featureFound) {
		int rowToBeAdded = 0;

		for (Map<String, Set<String>>jobFeature : featureFound) {
			Set<String> jobs = jobFeature.keySet();
			
			for (String jobName : jobs){
				Set<String> features = jobFeature.get(jobName);
				
				Row newRow = newSheet.createRow(rowToBeAdded++);
				Cell newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
				newCell.setCellValue(jobName);
				
				for (String feature : features){
					newRow = newSheet.createRow(rowToBeAdded++);
					newCell = newRow.createCell(0, Cell.CELL_TYPE_STRING);
					newCell.setCellValue(feature);
				}
				newRow = newSheet.createRow(rowToBeAdded++);
			}
		}
	}
	
	/*
	 * Create new row for crashed information
	 */
	private void setValue(Sheet newSheet, int rowIndex, int cellType, String targetValue){
		Row newRow = newSheet.createRow(rowIndex);
		Cell newCell = newRow.createCell(0, cellType);
		newCell.setCellValue(targetValue);
	}
	
	private void setPatchValues(Sheet newSheet, int rowIndex, int cellStartIndex,
			int cellTypeString, List<String> patches) {
		Row targetRow = newSheet.createRow(rowIndex);
		for (String patch : patches){
			try{
				Cell newCell = targetRow.createCell(cellStartIndex++);
				newCell.setCellValue(patch);
			}catch(Exception e){
				SystemMonitorException.logException(Thread.currentThread(), e);
			}
		}
	}
	
	private void setCrashValues(Sheet newSheet, int rowIndex, int cellStartIndex,
			int cellTypeString, List<String> crashes) {
		Row targetRow = newSheet.getRow(rowIndex);
		for (String crashe : crashes){
			try{
				Cell newCell = targetRow.createCell(cellStartIndex++);
				newCell.setCellValue(crashe);
			}catch(Exception e){
				SystemMonitorException.logException(Thread.currentThread(), e);
			}
		}
	}
	
	private double getJobDuration(Calendar startDate, Calendar stopDate){
		return ((stopDate.getTimeInMillis() - startDate.getTimeInMillis()) / (60 * 60 * 1000.0));
	}
	
	private void closeWorkbook(HSSFWorkbook workbook){
		if (workbook != null){
			try {
				workbook.close();
			} catch (IOException e) {
				SystemMonitorException.logException(Thread.currentThread(), e);
			}
		}
	}

	public void filter(String filter) {
		// TODO Auto-generated method stub
		
	}
}
