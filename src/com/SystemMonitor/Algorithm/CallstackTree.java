package com.SystemMonitor.Algorithm;

import java.util.HashMap;

public class CallstackTree {

	private String _operation;
	private int _count;
	private HashMap<String, CallstackTree> _operationStack;

	public CallstackTree(String operation){
		this._operation = operation;
		this._count = 1;
		this._operationStack = new HashMap<String, CallstackTree>();
	}
	
	public HashMap<String, CallstackTree> getChildTree() {
		return this._operationStack;
	}
	
	public String getOperation() {
		return this._operation;
	}

	public void updateOperationCount() {
		++this._count;
	}

	public int getOperationCount() {
		return this._count;
	}
}
