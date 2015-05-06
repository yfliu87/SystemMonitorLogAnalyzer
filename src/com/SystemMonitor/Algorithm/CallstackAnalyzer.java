package com.SystemMonitor.Algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CallstackAnalyzer {

	private CallstackTree _root;
	
	public CallstackAnalyzer(){
		this._root = new CallstackTree("null");
	}

	public void buildTree(String crashDetailPath) {
		BufferedReader br = null;
		try {
			FileReader crashfile = new FileReader(crashDetailPath);
			br = new BufferedReader(crashfile);

			String message = null;
			while((message = br.readLine()) != null){
				buildTreeRecursively(message, this._root.getChildTree(), "");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void buildTreeRecursively(String message, HashMap<String, CallstackTree> childTree, String pre) {
		if (message == null || message.length() == 0 || !message.contains(";"))
			return;
		
		String current = pre + message.substring(0, message.indexOf(";") - 1);
		
		if (childTree.containsKey(current)){
			CallstackTree child = childTree.get(current);
			child.updateOperationCount();
		}else{
			childTree.put(current, new CallstackTree(current));
		}

		buildTreeRecursively(message.substring(message.indexOf(";") + 1), childTree.get(current).getChildTree(), current);
	}

	public void printResult() {
		//level traverse multitree
		ArrayList<CallstackTree> levelNode = new ArrayList<CallstackTree>();
		
		for (String ops : this._root.getChildTree().keySet()){
			levelNode.add(this._root.getChildTree().get(ops));
		}
		
		int levelCount = levelNode.size();
		int depth = 1;

		while (!levelNode.isEmpty()) {

			levelCount = levelNode.size();
			System.out.println("\r\nDepth: " + depth);

			while(levelCount != 0){

				CallstackTree node = levelNode.remove(0);

				System.out.println("\toperation: " + node.getOperation()
						+ ", count: " + node.getOperationCount());

				for (String ops : node.getChildTree().keySet()) {
					levelNode.add(node.getChildTree().get(ops));
				}
				
				--levelCount;
			}
			++depth;
		}
	}
}
