package com.SystemMonitor.Algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.SystemMonitor.Util.SystemMonitorException;

public class CallstackAnalyzer {

	private CallstackTree _root;
	
	public CallstackAnalyzer(){
		this._root = new CallstackTree("null");
	}

	public void buildTree(String crashDetailPath) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(crashDetailPath));

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
		
		String current = (pre.isEmpty()? "" : pre + ";") + message.substring(0, message.indexOf(";"));
		
		if (childTree.containsKey(current)){
			CallstackTree child = childTree.get(current);
			child.updateOperationCount();
		}else{
			childTree.put(current, new CallstackTree(current));
		}

		buildTreeRecursively(message.substring(message.indexOf(";") + 1), childTree.get(current).getChildTree(), current);
	}

	public void printResult(String statisticFile, int crashThreshold, String targetConsole) {
		BufferedWriter fileWriter = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(statisticFile));
			
			ArrayList<CallstackTree> levelNode = new ArrayList<CallstackTree>();
			
			updateQueue(levelNode, order(this._root.getChildTree()));

			int levelCount = levelNode.size();
			int depth = 0;

			while (!levelNode.isEmpty()) {

				Collections.sort(levelNode, new Comparator<CallstackTree>(){
					public int compare(CallstackTree t1, CallstackTree t2){
						return t2.getOperationCount() - t1.getOperationCount();
					}
				});
				
				levelCount = levelNode.size();
				if (depth == 0)
					fileWriter.write("\r\n\r\nCrash Console Detail: ");
				else
					fileWriter.write("\r\n\r\nStep Count: " + depth);

				while(levelCount != 0){

					CallstackTree node = levelNode.remove(0);

					if (depth == 0){
						if (node.getOperation().toLowerCase().startsWith(targetConsole)){
							fileWriter.write("\r\n\tCrash Count: "
									+ node.getOperationCount() + "\r Console: " + node.getOperation());
							
							for (String subtree : node.getChildTree().keySet()) {
								levelNode.add(node.getChildTree().get(subtree));
							}
						}
					}else{
						if (node.getOperationCount() >= crashThreshold && node.getOperation().toLowerCase().startsWith(targetConsole)){
							fileWriter.write("\r\n\tCrash Count: "
									+ node.getOperationCount() + " --- operation: " + node.getOperation());

							for (String subtree : node.getChildTree().keySet()) {
								levelNode.add(node.getChildTree().get(subtree));
							}
						}
					}
					
					--levelCount;
				}
				++depth;
			}
		} catch (IOException e) {
			SystemMonitorException.logException(Thread.currentThread(), e, e.getMessage());
		} finally{
			if (fileWriter != null){
				try {
					fileWriter.close();
				} catch (IOException e) {
					SystemMonitorException.logException(Thread.currentThread(), e, e.getMessage());
				}
			}
		}
	}

	private void updateQueue(ArrayList<CallstackTree> levelNode, List<Entry<String, CallstackTree>> orderedTree) {
		for (Entry<String, CallstackTree> node : orderedTree){
			levelNode.add(node.getValue());
		}	
	}

	private List<Entry<String, CallstackTree>> order(HashMap<String, CallstackTree> childTree) {
		List<Entry<String, CallstackTree>> ret = new ArrayList<Entry<String, CallstackTree>>(childTree.entrySet());
		
		Collections.sort(ret, new Comparator<Entry<String, CallstackTree>>(){
			public int compare(Entry<String, CallstackTree> t1, Entry<String, CallstackTree> t2){
				return t2.getValue().getOperationCount() - t1.getValue().getOperationCount();
			}
		});
		
		return ret;
	}
}
