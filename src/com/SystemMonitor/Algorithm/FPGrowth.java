package com.SystemMonitor.Algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class FPGrowth {

    int threshold;
    //fp-tree constructing fileds
    Vector<FPTree> headerTable;
    FPTree fptree;
    //fp-growth
    Map<String, Integer> frequentPatterns;

    public FPGrowth(File file, int threshold) throws FileNotFoundException {
        this.threshold = threshold;
        fptree(file);
        fpgrowth(fptree, threshold, headerTable);
        print();

    }

    private FPTree conditional_fptree_constructor(Map<String, Integer> conditionalPatternBase, Map<String, Integer> conditionalItemsMaptoFrequencies, int threshold, Vector<FPTree> conditional_headerTable) {
        //FPTree constructing
        //the null node!
    	FPTree conditional_fptree = new FPTree("null");
        conditional_fptree.item = null;
        conditional_fptree.root = true;
        //remember our transactions here has oredering and non-frequent items for condition items
        for (String pattern : conditionalPatternBase.keySet()) {
            //adding to tree
            //removing non-frequents and making a vector instead of string
            Vector<String> pattern_vector = new Vector<String>();
            StringTokenizer tokenizer = new StringTokenizer(pattern, ";");
            
            //replacement
            //StringTokenizer tokenizer = new StringTokenizer(pattern, " ");
            while (tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                if (conditionalItemsMaptoFrequencies.get(item) >= threshold) {
                    pattern_vector.addElement(item);
                }
            }
            //the insert method
            insert(pattern_vector, conditionalPatternBase.get(pattern), conditional_fptree, conditional_headerTable);
            //end of insert method
        }
        return conditional_fptree;
    }

    private void fptree(File file) throws FileNotFoundException {
        //preprocessing fields
        Map<String, Integer> itemsMaptoFrequencies = new HashMap<String, Integer>();
        
        Scanner input = new Scanner(file);
        
        //replacement
        input.useDelimiter("[;\r\n]");
        List<String> sortedItemsbyFrequencies = new LinkedList<String>();
        Vector<String> itemstoRemove = new Vector<String>();
        preProcessing(file, itemsMaptoFrequencies, input, sortedItemsbyFrequencies, itemstoRemove);
        construct_fpTree(file, itemsMaptoFrequencies, input, sortedItemsbyFrequencies, itemstoRemove);

    }

    private void preProcessing(File file, Map<String, Integer> itemsMaptoFrequencies, Scanner input, List<String> sortedItemsbyFrequencies, Vector<String> itemstoRemove) throws FileNotFoundException {
        while (input.hasNext()) {
            String temp = input.next();
            if (temp.equals(""))
            	continue;
            
            if (itemsMaptoFrequencies.containsKey(temp)) {
                int count = itemsMaptoFrequencies.get(temp);
                itemsMaptoFrequencies.put(temp, count + 1);
            } else {
                itemsMaptoFrequencies.put(temp, 1);
            }
        }
        input.close();
        //orderiiiiiiiiiiiiiiiiiiiiiiiiiiiing
        //also elimating non-frequents

        for (String key : itemsMaptoFrequencies.keySet()){
        	System.out.println("key:" + key + " value: " + itemsMaptoFrequencies.get(key));
        }
        
        //for breakpoint for comparison
        sortedItemsbyFrequencies.add("null");
        itemsMaptoFrequencies.put("null", 0);
        for (String item : itemsMaptoFrequencies.keySet()) {
            int count = itemsMaptoFrequencies.get(item);
            // System.out.println( count );
            int i = 0;
            for (String listItem : sortedItemsbyFrequencies) {
                if (itemsMaptoFrequencies.get(listItem) < count) {
                    sortedItemsbyFrequencies.add(i, item);
                    break;
                }
                i++;
            }
        }
        //removing non-frequents
        //this pichidegi is for concurrency problem in collection iterators
        for (String listItem : sortedItemsbyFrequencies) {
            if (itemsMaptoFrequencies.get(listItem) < threshold) {
                itemstoRemove.add(listItem);
            }
        }
        for (String itemtoRemove : itemstoRemove) {
            sortedItemsbyFrequencies.remove(itemtoRemove);
        }
        //printttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
        //     for ( String key : list )
        //        System.out.printf( "%-10s%10s\n", key, items.get( key ) );

    }

    private void construct_fpTree(File file, Map<String, Integer> itemsMaptoFrequencies, Scanner input, List<String> sortedItemsbyFrequencies, Vector<String> itemstoRemove) throws FileNotFoundException {
        //HeaderTable Creation
        // first elements use just as pointers
        headerTable = new Vector<FPTree>();
        for (String itemsforTable : sortedItemsbyFrequencies) {
            headerTable.add(new FPTree(itemsforTable));
        }
        //FPTree constructing
		input = new Scanner(file);
		
		//replacement
        input.useDelimiter("[;\r\n]");
        //the null node!
        fptree = new FPTree("null");
        fptree.item = null;
        fptree.root = true;
        //ordering frequent items transaction
        while (input.hasNextLine()) {
            String line = input.nextLine();
            StringTokenizer tokenizer = new StringTokenizer(line, ";");
            
            //replacement
            //StringTokenizer tokenizer = new StringTokenizer(line, " ");
            Vector<String> transactionSortedbyFrequencies = new Vector<String>();
            while (tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                if (itemstoRemove.contains(item)) {
                    continue;
                }
                int index = 0;
                for (String vectorString : transactionSortedbyFrequencies) {
                    //some lines of condition is for alphabetically check in equals situatioans
                    if (itemsMaptoFrequencies.get(vectorString) < itemsMaptoFrequencies.get(item) || ((itemsMaptoFrequencies.get(vectorString) == itemsMaptoFrequencies.get(item)) && (vectorString.compareToIgnoreCase(item) < 0 ? true : false))) {
                        transactionSortedbyFrequencies.add(index, item);
                        break;
                    }
                    index++;
                }
                if (!transactionSortedbyFrequencies.contains(item)) {
                    transactionSortedbyFrequencies.add(item);
                }
            }
            //printing transactionSortedbyFrequencies
            /*
            for (String vectorString : transactionSortedbyFrequencies) {
            System.out.printf("%-10s%10s ", vectorString, itemsMaptoFrequencies.get(vectorString));
            }
            System.out.println();
             *
             */
            //printing transactionSortedbyFrequencies
            /*
            for (String vectorString : transactionSortedbyFrequencies) {
            System.out.printf("%-10s%10s ", vectorString, itemsMaptoFrequencies.get(vectorString));
            }
            System.out.println();
             *
             */
            //adding to tree
            insert(transactionSortedbyFrequencies, fptree, headerTable);
            transactionSortedbyFrequencies.clear();
        }
        //headertable reverse ordering
        //first calculating item frequencies in tree
        for (FPTree item : headerTable) {
            int count = 0;
            FPTree itemtemp = item;
            while (itemtemp.next != null) {
                itemtemp = itemtemp.next;
                count += itemtemp.count;
            }
            item.count = count;
        }
        Comparator c = new FrequencyComparitorinHeaderTable();
        Collections.sort(headerTable, c);
        input.close();
    }

    void insert(Vector<String> transactionSortedbyFrequencies, FPTree fptree, Vector<FPTree> headerTable) {
        if (transactionSortedbyFrequencies.isEmpty()) {
            return;
        }
        String itemtoAddtotree = transactionSortedbyFrequencies.firstElement();
        FPTree newNode = null;
        boolean ifisdone = false;
        for (FPTree child : fptree.children) {
            if (child.item.equals(itemtoAddtotree)) {
                newNode = child;
                child.count++;
                ifisdone = true;
                break;
            }
        }
        if (!ifisdone) {
            newNode = new FPTree(itemtoAddtotree);
            newNode.count = 1;
            newNode.parent = fptree;
            fptree.children.add(newNode);
            for (FPTree headerPointer : headerTable) {
                if (headerPointer.item.equals(itemtoAddtotree)) {
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                }
            }
        }
        transactionSortedbyFrequencies.remove(0);
        insert(transactionSortedbyFrequencies, newNode, headerTable);
    }

    private void fpgrowth(FPTree fptree, int threshold, Vector<FPTree> headerTable) {
        frequentPatterns = new HashMap<String, Integer>();
        FPgrowth(fptree, null, threshold, headerTable, frequentPatterns);
    }

    void FPgrowth(FPTree fptree, String base, int threshold, Vector<FPTree> headerTable, Map<String, Integer> frequentPatterns) {
        for (FPTree iteminTree : headerTable) {
            //replacement
        	//String currentPattern = (base != null ? base : "") + (base != null ? " " : "") + iteminTree.item;
            String currentPattern = (base != null ? base : "") + (base != null ? ";" : "") + iteminTree.item;
            int supportofCurrentPattern = 0;
            Map<String, Integer> conditionalPatternBase = new HashMap<String, Integer>();
            while (iteminTree.next != null) {
                iteminTree = iteminTree.next;
                supportofCurrentPattern += iteminTree.count;
                String conditionalPattern = null;
                FPTree conditionalItem = iteminTree.parent;

                while (!conditionalItem.isRoot()) {
                	conditionalPattern = conditionalItem.item + ";" + (conditionalPattern != null ? conditionalPattern : "");
                    
                    //conditionalPattern = conditionalItem.item + " " + (conditionalPattern != null ? conditionalPattern : "");
                    conditionalItem = conditionalItem.parent;
                }
                if (conditionalPattern != null) {
                    conditionalPatternBase.put(conditionalPattern, iteminTree.count);
                }
            }
            frequentPatterns.put(currentPattern, supportofCurrentPattern);
            //counting frequencies of single items in conditional pattern-base
            Map<String, Integer> conditionalItemsMaptoFrequencies = new HashMap<String, Integer>();
            for (String conditionalPattern : conditionalPatternBase.keySet()) {
                StringTokenizer tokenizer = new StringTokenizer(conditionalPattern, ";");
                
                //replacement
                //StringTokenizer tokenizer = new StringTokenizer(conditionalPattern, " ");
                while (tokenizer.hasMoreTokens()) {
                    String item = tokenizer.nextToken();
                    if (conditionalItemsMaptoFrequencies.containsKey(item)) {
                        int count = conditionalItemsMaptoFrequencies.get(item);
                        count += conditionalPatternBase.get(conditionalPattern);
                        conditionalItemsMaptoFrequencies.put(item, count);
                    } else {
                        conditionalItemsMaptoFrequencies.put(item, conditionalPatternBase.get(conditionalPattern));
                    }
                }
            }
            //conditional fptree
            //HeaderTable Creation
            // first elements are being used just as pointers
            // non conditional frequents also will be removed
            Vector<FPTree> conditional_headerTable = new Vector<FPTree>();
            for (String itemsforTable : conditionalItemsMaptoFrequencies.keySet()) {
                int count = conditionalItemsMaptoFrequencies.get(itemsforTable);
                if (count < threshold) {
                    continue;
                }
                FPTree f = new FPTree(itemsforTable);
                f.count = count;
                conditional_headerTable.add(f);
            }
            FPTree conditional_fptree = conditional_fptree_constructor(conditionalPatternBase, conditionalItemsMaptoFrequencies, threshold, conditional_headerTable);
            //headertable reverse ordering
            Collections.sort(conditional_headerTable, new FrequencyComparitorinHeaderTable());
            //
            if (!conditional_fptree.children.isEmpty()) {
                FPgrowth(conditional_fptree, currentPattern, threshold, conditional_headerTable, frequentPatterns);
            }
        }
    }

    private void insert(Vector<String> pattern_vector, int count_of_pattern, FPTree conditional_fptree, Vector<FPTree> conditional_headerTable) {
        if (pattern_vector.isEmpty()) {
            return;
        }
        String itemtoAddtotree = pattern_vector.firstElement();
        FPTree newNode = null;
        boolean ifisdone = false;
        for (FPTree child : conditional_fptree.children) {
            if (child.item.equals(itemtoAddtotree)) {
                newNode = child;
                child.count += count_of_pattern;
                ifisdone = true;
                break;
            }
        }
        if (!ifisdone) {
            for (FPTree headerPointer : conditional_headerTable) {
                //this if also gurantees removing og non frequets
                if (headerPointer.item.equals(itemtoAddtotree)) {
                    newNode = new FPTree(itemtoAddtotree);
                    newNode.count = count_of_pattern;
                    newNode.parent = conditional_fptree;
                    conditional_fptree.children.add(newNode);
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                }
            }
        }
        pattern_vector.remove(0);
        insert(pattern_vector, count_of_pattern, newNode, conditional_headerTable);
    }

    private void print() throws FileNotFoundException {
        /*
        Vector<String> sortedItems = new Vector<String>();
        sortedItems.add("null");
        frequentPatterns.put("null", 0);
        for (String item : frequentPatterns.keySet()) {
            int count = frequentPatterns.get(item);

            int i = 0;
            for (String listItem : sortedItems) {
                if (frequentPatterns.get(listItem) < count) {
                    sortedItems.add(i, item);
                    break;
                }
                i++;
            }
        }
         * 
         */
        //Formatter output = new Formatter("a.out");
    	
    	splitResult(frequentPatterns);

//    	
//        for (String frequentPattern : frequentPatterns.keySet()) {
//        	System.out.println("\r\nfrequent pattern:" + frequentPattern + ", count: " + frequentPatterns.get(frequentPattern));
//            //output.format("%s\t%d\n", frequentPattern,frequentPatterns.get(frequentPattern));
//        }
    }

	private void splitResult(Map<String, Integer> frequentPatterns) {
		
		printOriginalFrequentPattern(frequentPatterns);
		
		sortByOperation(frequentPatterns);
//		
//		int flag = 0;
//		Map<String, Integer> newMap = new TreeMap<String, Integer>();
//		for (String key : map.keySet()){
//			System.out.println("@@@key: " + key);
//			//replacement
//			//int currentKeyLength = key.split(" ").length;
//			int currentKeyLength = key.split(";").length;
//			
//			if (currentKeyLength >= flag){
//				System.out.println("operation:" + flag);
//				
//				for (String item : newMap.keySet()){
//					System.out.println(item + " : " + newMap.get(item));
//				}
//				flag = currentKeyLength;
//				
//				newMap = new TreeMap<String, Integer>();				
//			}
//			
//			newMap.put(key, map.get(key));
//		}
		
	}

	private void sortByOperation(Map<String, Integer> frequentPatterns) {
		
		int maxSteps = 0;
		for (String ops : frequentPatterns.keySet()){
			if (ops.split(";").length > maxSteps)
				maxSteps = ops.split(";").length;
		}
		
		int count = 1;
		while(count <= maxSteps){
			
			Map<String, Integer> map = new HashMap<String, Integer>();
			
			for (String ops : frequentPatterns.keySet()){
				if (ops.split(";").length == count)
					map.put(ops, frequentPatterns.get(ops));
			}
			
			orderByFrequency(map);
			
			++count;
		}	
	}

	private void orderByFrequency(Map<String, Integer> map) {
		List<Entry<String, Integer>> result = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		
		Collections.sort(result, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
				if (o1.getValue() > o2.getValue())
					return 1;
				else if (o1.getValue() < o2.getValue())
					return -1;
				else
					return 0;
			}
		});

		for (Map.Entry<String, Integer> item : result){
			System.out.print("\r\n\r\nop count: " + item.getKey().split(";").length + " --- key: " + item.getKey() +" ------ count: " + item.getValue());
		}
		
	}

	private void printOriginalFrequentPattern(Map<String, Integer> frequentPatterns) {
    	List<Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(frequentPatterns.entrySet());
    	Collections.sort(entryList, new Comparator<Entry<String, Integer>>(){
    		public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
    			return o1.getValue().compareTo(o2.getValue());
    		}
    	});
    	
    	for (Entry<String, Integer> item : entryList){
    		System.out.println("\r\n%%%%%frequent pattern:" + item.getKey() + ", count: " + item.getValue());
    	}
		
	}
}
