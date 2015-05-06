package com.SystemMonitor.Algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
		
		String current = (pre.isEmpty()? "" : pre + ";") + message.substring(0, message.indexOf(";"));
		
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
		
		updateQueue(levelNode, order(this._root.getChildTree()));

		int levelCount = levelNode.size();
		int depth = 1;

		while (!levelNode.isEmpty()) {

			levelCount = levelNode.size();
			System.out.println("\r\nDepth: " + depth);

			while(levelCount != 0){

				CallstackTree node = levelNode.remove(0);

				System.out.println("\toperation: " + node.getOperation() + ", count: " + node.getOperationCount());

				updateQueue(levelNode, order(node.getChildTree()));
				
				--levelCount;
			}
			++depth;
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
