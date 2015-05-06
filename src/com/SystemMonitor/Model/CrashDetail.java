package com.SystemMonitor.Model;

public class CrashDetail {

	private String _processOperation = "";
	private String _componentName = "";
	private String _templateName = "";
	
	public void updateProcessOperation(String value) {
		this._processOperation = value;
	}
	
	public void updateComponent(String component){
		this._componentName = component;
	}
	
	public void updateTemplate(String template){
		this._templateName = template;
	}
	
	public int compare(Object o1, Object o2){
		return 0;
	}

	public String toString(){
		return this._processOperation + "," + this._componentName;
	}
}
