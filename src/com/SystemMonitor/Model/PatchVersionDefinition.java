package com.SystemMonitor.Model;

public class PatchVersionDefinition {

//	private static final String SP12013DNM = "Patch-SP-10767_13075-4.0.9163.3001";
//	private static final String SP12013WL = "Patch-SP-10767_13393-4.0.9163.3001";
//	private static final String SP22013 = "Patch-SP-10767_18214-4.0.9163.3001";
//	private static final String SP32013 = "Patch-SP-10767_22480-4.0.9163.3001";
//	private static final String SP42013 = "Patch-SP-10767_26570-4.0.9163.3001";
	
	private static final String SP12013DNM = "13075";
	private static final String SP12013WL = "13393";
	private static final String SP22013 = "18214";
	private static final String SP32013 = "22480";
	private static final String SP42013 = "26570";
	
	public static String convert(String input){
		
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
//		
//		else if (input.equals(SP12013DNM) || input.equals("2013 SP1 D&M"))
//			return SP12013DNM;
//		else if (input.equals(SP12013WL) || input.equals("2013 SP1 WL"))
//			return SP12013WL;
//		else if (input.equals(SP22013) || input.equals("2013 SP2"))
//			return SP22013;
//		else if (input.equals(SP32013) || input.equals("2013 SP3"))
//			return SP32013;
//		else if (input.equals(SP42013) || input.equals("2013 SP4"))
//			return SP42013;
		else
			return input;
	}
}
