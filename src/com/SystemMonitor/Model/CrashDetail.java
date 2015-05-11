package com.SystemMonitor.Model;

public class CrashDetail {

	private String _crashProcessName;
	private String _customMessage;
	private String _processOperation;
	private String _componentName;
	
	public void updateProcessOperation(String value) {
		this._processOperation = value;
	}
	
	public void updateComponent(String component){
		this._componentName = component;
	}
	
	public void updateCrashProcessName(String process){
		this._crashProcessName = process;
	}
	
	public void updateDetailMessage(String msg) {
		this._customMessage = msg;
	}

	public int compare(Object o1, Object o2){
		return 0;
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(this._crashProcessName == null ? "" : this._crashProcessName);
		ret.append(this._customMessage == null ? "" : " - " + this._customMessage);
		ret.append(this._processOperation == null ? "" : this._processOperation);
		ret.append(this._componentName == null || this._componentName.isEmpty() ? "" : " - " + this._componentName);
		return ret.toString();
	}

}
