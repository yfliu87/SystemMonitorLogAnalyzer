package com.SystemMonitor.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemMonitorException extends RuntimeException{
	
	public static void logException(Thread exceptionThread, Exception exception){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(new Date());
		String exceptionFileName = "/usr/local/apache-tomcat-7/logs/SystemMonitorException" + date;
		BufferedWriter bwriter = null;

		try{
			bwriter = new BufferedWriter(new FileWriter(exceptionFileName));
			
			String msg = "\t" + "Exception class: " + exceptionThread.getStackTrace()[2].getClassName();
			msg += "\r\n\t" + "Exception method: " + exceptionThread.getStackTrace()[2].getMethodName();
			msg += "\r\n\t\t" + "Message: " + exception.getMessage();

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
	
	public static void logException(Thread exceptionThread, Exception exception, File file){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(new Date());
		String exceptionFileName = "/usr/local/apache-tomcat-7/logs/SystemMonitorException" + date;
		BufferedWriter bwriter = null;

		try{
			bwriter = new BufferedWriter(new FileWriter(exceptionFileName));
			
			String msg = "\t" + "Exception class: " + exceptionThread.getStackTrace()[2].getClassName();
			msg += "\r\n\t" + "Exception method: " + exceptionThread.getStackTrace()[2].getMethodName();
			msg += "\r\n\t\t" + "Message: " + exception.getMessage();
			msg += "\r\n\t\t" + "File: " + file.getName();
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
	
	public static void logException(Thread exceptionThread, Exception exception, String message){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(new Date());
		String exceptionFileName = "/usr/local/apache-tomcat-7/logs/SystemMonitorException" + date;
		BufferedWriter bwriter = null;

		try{
			bwriter = new BufferedWriter(new FileWriter(exceptionFileName));
			
			String msg = "\t" + "Exception class: " + exceptionThread.getStackTrace()[2].getClassName();
			msg += "\r\n\t" + "Exception method: " + exceptionThread.getStackTrace()[2].getMethodName();
			msg += "\r\n\t\t" + "Message: " + exception.getMessage();
			msg += "\r\n\t\t" + message;
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
