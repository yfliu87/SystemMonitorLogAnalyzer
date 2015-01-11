package com.myapp.Model;

public class SpringException extends RuntimeException{
	
	private String exceptionMsg;
	
	public SpringException(String msg){
		this.setExceptionMsg(msg);
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}
	

}
