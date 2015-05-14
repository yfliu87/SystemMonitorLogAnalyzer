package com.SystemMonitor.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatchVersionDefinition {
	
	public static final String Main2013 = "4.0.9163";
	public static final String SP12013DNM = "13075";
	public static final String SP12013WL = "13393";
	public static final String SP22013 = "18214";
	public static final String SP32013 = "22480";
	public static final String SP42013 = "26570";
	public static final String Main2014 = "5.0.29600";
	public static final String SP12014 = "5.1.33858";
	
	public static final List<String> patches = new ArrayList<String>(
			Arrays.asList("4.0.9163", "13393","18214","22480","26570","5.0.29600","5.1.33858"));
	
	public static String stringToDigit(String input){
		
		if (input == null)
			return "empty";
		
		else if (input.equals("2013 SP1 D&M"))
			return SP12013DNM;
		else if (input.equals("2013 SP1 WL"))
			return SP12013WL;
		else if (input.equals("2013 SP2"))
			return SP22013;
		else if (input.equals("2013 SP3"))
			return SP32013;
		else if (input.equals("2013 SP4"))
			return SP42013;
		else if (input.equals("2014 SP1"))
			return SP12014;
		else
			return input;
	}
	
	public static String digitToString(String digit){
		if (digit == null)
			return "";
		else if (digit.contains("13075"))
			return "2013 DnM SP1";
		else if (digit.contains("13393"))
			return "2013 WL SP1";
		else if (digit.contains("18214"))
			return "2013 SP2";
		else if (digit.contains("22480"))
			return "2013 SP3";
		else if (digit.contains("26570"))
			return "2013 SP4";
		else if (digit.contains("33858"))
			return "2014 SP1";
		else 
			return "";
					
		
	}
}
