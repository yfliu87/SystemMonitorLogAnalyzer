package com.SystemMonitor.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class is designed for reading config files which includes below info:
 * 1. feature mapping config file location
 * 2. log file location 
 * 3. index folder location
 */
public class ConfigHelper {
	Map<String, String> filePathMap = new HashMap<String, String>();
	static ConfigHelper _configHelper = null;
	
	private ConfigHelper(String configFilePath) {
		File file = new File(configFilePath);
		if (file.isFile() && file.exists()){
			InputStreamReader isReader = null;
			BufferedReader bReader = null; 
			try {
				isReader = new InputStreamReader(new FileInputStream(file), "GBK");
				bReader = new BufferedReader(isReader);
				String msg = null;

					while ((msg = bReader.readLine()) != null) {
						if (msg.indexOf("logs") != -1) {
							filePathMap.put("logs", bReader.readLine());
						} else if (msg.indexOf("feature") != -1) {
							filePathMap.put("featureMap", bReader.readLine());
						} else if (msg.indexOf("index") != -1) {
							filePathMap.put("index", bReader.readLine());
						} else if (msg.indexOf("crashfile") != -1) {
							filePathMap.put("crashfile", bReader.readLine());
						} else if (msg.indexOf("statisticfile") != -1) {
							filePathMap.put("statisticfile", bReader.readLine());
						} else if (msg.indexOf("callstack_depth") != -1){
							filePathMap.put("callstack_depth", bReader.readLine());
						} else if (msg.indexOf("crash_threshold") != -1){
							filePathMap.put("crash_threshold", bReader.readLine());
						} else if (msg.indexOf("target_console") != -1){
							filePathMap.put("target_console", bReader.readLine());
						} else if (msg.indexOf("target_version") != -1){
							filePathMap.put("target_version", bReader.readLine());
						}
					}
			} catch (IOException e) {
				SystemMonitorException.logException(Thread.currentThread(), e);
			}finally{
				closeResource(bReader);
				closeResource(isReader);
			}
		}
	}
	
	public static ConfigHelper getInstance(String configPath){
		if (_configHelper == null){
			_configHelper = new ConfigHelper(configPath);
		}
		return _configHelper;
	}
	
	private void closeResource(Reader reader){
		
		if (reader != null){
			try {
				reader.close();
			} catch (IOException e) {
				SystemMonitorException.logException(Thread.currentThread(), e);
			}
		}
	}

	public String getFeatureMappingFileLocation(){
		return this.filePathMap.get("featureMap");
	}
	
	public String getIndexLocation(){
		return this.filePathMap.get("index");
	}
	
	public String getLogLocation(){
		return this.filePathMap.get("logs");
	}
	
	public String getCrashDetailPath(){
		return this.filePathMap.get("crashfile");
	}
	
	public String getStatisticFilePath(){
		return this.filePathMap.get("statisticfile");
	}
	
	public int getCallStackDepth(){
		return Integer.parseInt(this.filePathMap.get("callstack_depth"));
	}
	
	public int getCrashThreshold(){
		return Integer.parseInt(this.filePathMap.get("crash_threshold"));
	}
	
	public List<String> getTargetConsole(){
		return Arrays.asList(this.filePathMap.get("target_console").split(","));
	}

	public List<String> getTargetVersion() {
		return Arrays.asList(this.filePathMap.get("target_version").split(","));
	}
}
