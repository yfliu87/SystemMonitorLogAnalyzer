package com.SystemMonitor.Model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemMonitorException extends RuntimeException{
	
	private String exceptionMsg;
	
	public SystemMonitorException(String msg){
		this.setExceptionMsg(msg);
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}
	
	public static void recordException(String msg){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(new Date());
		String exceptionFileName = "/usr/local/apache-tomcat-7/logs/SystemMonitorException" + date;
		BufferedWriter bwriter = null;
		try{
			bwriter = new BufferedWriter(new FileWriter(exceptionFileName));
			bwriter.write(msg);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (bwriter != null){
				try {
					bwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
