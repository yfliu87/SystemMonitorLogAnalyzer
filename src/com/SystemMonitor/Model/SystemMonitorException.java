package com.SystemMonitor.Model;

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
	

}
