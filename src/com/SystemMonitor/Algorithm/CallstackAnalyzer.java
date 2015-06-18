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
import java.util.stream.Collectors;

import com.SystemMonitor.Util.SystemMonitorException;

public class CallstackAnalyzer {

	private CallstackTree _root;
	private String _targetVersion;
	
	public CallstackAnalyzer(String targetVersion){
		this._root = new CallstackTree("null");
		this._targetVersion = targetVersion;
	}

	public void buildTree(String crashDetailPath) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(crashDetailPath));

			String message = null;
			while((message = br.readLine()) != null){
				if (desiredVersion(message, _targetVersion))
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

	private boolean desiredVersion(String message, String targetVersion) {
		return message.substring(0, message.indexOf(";")).contains(targetVersion);
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
			
			List<CallstackTree> levelNode = new ArrayList<CallstackTree>();
			
			updateQueue(levelNode, order(this._root.getChildTree()));

			int levelCount = levelNode.size();
			int depth = 0;

			while (!levelNode.isEmpty()) {

				levelNode = levelNode.parallelStream().
						sorted((a,b) -> b.getOperationCount() - a.getOperationCount()).
						collect(Collectors.toList());
				
				levelCount = levelNode.size();
				if (depth == 0)
					fileWriter.write("\r\nAll Crash Console : ");
				else
					fileWriter.write("\r\n\r\nOperationStack Depth: " + depth);

				while(levelCount != 0){

					CallstackTree node = levelNode.remove(0);

					if (depth == 0){
						//if (node.getOperation().toLowerCase().startsWith(targetConsole)){
							fileWriter.write("\r\n\tCrash Count: " + node.getOperationCount() + "\tVersion: " + node.getOperation());
							
							levelNode.addAll(node.getChildTree().values());
						//}
					}else{
						if (desiredOperation(node, crashThreshold, targetConsole)){
							fileWriter.write(buildOutputMessage(node, depth));

							levelNode.addAll(node.getChildTree().values());
						}
					}
					
					--levelCount;
				}
				if (depth == 0)
					fileWriter.write("\r\n\r\n" + targetConsole + " details: ");
				
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

	private boolean desiredOperation(CallstackTree node, int crashThreshold, String targetConsole) {
		String targetString = node.getOperation().toLowerCase();
		targetString = targetString.substring(targetString.indexOf(";") + 1);
		return node.getOperationCount() >= crashThreshold && targetString.startsWith(targetConsole);
	}

//	private String buildOutputMessage(CallstackTree node) {
//		String msg = node.getOperation();
//		String crashConsole = msg.substring(0, msg.indexOf(";"));
//		String operation = msg.substring(msg.indexOf(";") + 1);
//
//		StringBuilder ret = new StringBuilder();
//		ret.append("\r\n\tCrash Count: " + node.getOperationCount());
//		ret.append("\tCrash Console: " + crashConsole);
//		ret.append("\tOperations: " + operation);
//
//		return ret.toString();
//	}
	
	private String buildOutputMessage(CallstackTree node, int depth) {
		String msg = node.getOperation();
		String[] msgs = msg.split(";");
		
		StringBuilder ret = new StringBuilder();
		ret.append("\r\n\tCrash Count: " + node.getOperationCount());
		
		if (depth == 1){
			ret.append("\tVersion: " + msgs[0]);
			ret.append("\tCrash Console: " + msgs[1]);
		}
		
		if (depth > 1){
			ret.append("\r\n\t\tOperations: " );
			for (int i = 1; i < msgs.length; i++)
				ret.append(msgs[i] + "\r\n\t\t\t\t\t");
		}
		
		return ret.toString();
	}

	private void updateQueue(List<CallstackTree> levelNode, List<Entry<String, CallstackTree>> orderedTree) {
		orderedTree.stream().forEach(e -> levelNode.add(e.getValue()));
	}

	private List<Entry<String, CallstackTree>> order(HashMap<String, CallstackTree> childTree) {
		List<Entry<String, CallstackTree>> ret = new ArrayList<Entry<String, CallstackTree>>(childTree.entrySet());
		
		return ret.parallelStream().
				sorted((a,b) -> b.getValue().getOperationCount() - a.getValue().getOperationCount()).
				collect(Collectors.toList());
	}
}
