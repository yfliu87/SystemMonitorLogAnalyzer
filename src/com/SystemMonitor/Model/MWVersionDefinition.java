package com.SystemMonitor.Model;

public class MWVersionDefinition {

	public static String digitToString(String digit){
		if (digit == null)
			return "";
		else if (digit.contains("4.0.9163"))
			return "MW 2013";
		else if (digit.contains("5.0.29600"))
			return "MW 2014";
		else if (digit.contains("5.1.33858"))
			return "MW 2014 - 2014 SP1";
		else
			return digit;
		
	}
}
