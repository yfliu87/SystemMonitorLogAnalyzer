package com.SystemMonitor.LogParser;

/*
 * This class helps to find the features applied in jobs via system monitor record
 * 1. use hashMap<featureName, hashSet<component>> to record the feature and component mapping relationship
 * 2. use hashMap<componentName, hashSet<featureName>> to record the component and feature mapping relationship
 * 3. read configuration file to initiate above hashMaps
 * 4. during parsing CSV file, try to detect the feature using this hashMap
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class FeatureAnalyzer {
	private Map<String, Set<String>> _featureComponentMapping;
	private Map<String, Set<String>> _componentFeatureMapping;
	private String _featureMappingFile;
	
	public FeatureAnalyzer(){}
	
	public FeatureAnalyzer(String featureMappingFile){
		_featureMappingFile = featureMappingFile;
		_featureComponentMapping = new HashMap<String, Set<String>>();
		_componentFeatureMapping = new HashMap<String, Set<String>>();
		init();
	}
	
	private void init(){
		HSSFWorkbook workbook = null;
		File file = new File(_featureMappingFile);
		File[] configFiles = file.listFiles();
		try{
			for (File configFile : configFiles){
				POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(configFile));
				workbook = new HSSFWorkbook(fs);
			
				int sheetNumber = 0;
				while (sheetNumber < workbook.getNumberOfSheets()) {
					HSSFSheet sheet = workbook.getSheetAt(sheetNumber);

					String featureName = sheet.getSheetName();

					for (Iterator<Row> iterRow = (Iterator<Row>) sheet.rowIterator(); iterRow.hasNext();) {
						Row row = iterRow.next();
						Cell cell = row.getCell(0);
						String componentName = cell.getStringCellValue();

						updateFeatureComponentMapping(featureName, componentName);
						updateComponentFeatureMapping(featureName, componentName);
					}
					++sheetNumber;
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateComponentFeatureMapping(String featureName, String componentName) {
		if (this._componentFeatureMapping.containsKey(componentName)) {
			Set<String> features = this._componentFeatureMapping.get(componentName);
			features.add(featureName);
			this._componentFeatureMapping.put(componentName, features);
		} else {
			Set<String> features = new HashSet<String>();
			features.add(featureName);
			this._componentFeatureMapping.put(componentName, features);
		}
	}

	private void updateFeatureComponentMapping(String featureName, String componentName) {
		if (this._featureComponentMapping.containsKey(featureName)) {
			Set<String> components = this._featureComponentMapping.get(featureName);
			components.add(componentName);
			this._featureComponentMapping.put(featureName, components);
		} else {
			Set<String> components = new HashSet<String>();
			components.add(componentName);
			this._featureComponentMapping.put(featureName, components);
		}		
	}

	public Set<String> queryFeature(String componentName) {
		if (_componentFeatureMapping.containsKey(componentName))
			return _componentFeatureMapping.get(componentName);
		else
			return new HashSet<String>();
	}
	
	public Set<String> queryComponent(String featureName){
		if (_featureComponentMapping.containsKey(featureName))
			return _featureComponentMapping.get(featureName);
		else
			return new HashSet<String>();
	}
}