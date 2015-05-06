package com.SystemMonitor.Model;

public class CrashDetail {

	private String _processOperation = "";
	private String _componentName = "";
	
	public void updateProcessOperation(String value) {
		this._processOperation = value;
	}
	
	public void updateComponent(String component){
		this._componentName = component;
	}
	
	public int compare(Object o1, Object o2){
		return 0;
	}

	public String toString(){
		return this._processOperation + (this._componentName.isEmpty() ? "" : " - " + this._componentName);
	}
}
