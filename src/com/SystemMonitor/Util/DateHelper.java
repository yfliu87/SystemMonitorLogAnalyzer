package com.SystemMonitor.Util;

/*
 * This class is designed for date related issues
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateHelper {
	public static List<String> getDatesInRange(String startDate, String stopDate) {
		List<String> ret = new ArrayList<String>();
		
		Calendar begin = Calendar.getInstance();
		begin.clear();
		initByDate(begin, startDate);
		
		Calendar end = Calendar.getInstance();
		end.clear();
		initByDate(end, stopDate);
		
		if (begin.compareTo(end) > 0)
			return ret;
		
		while(begin.get(Calendar.YEAR) <= end.get(Calendar.YEAR)){
			
			while(begin.get(Calendar.YEAR) < end.get(Calendar.YEAR) || 
					(begin.get(Calendar.YEAR) == end.get(Calendar.YEAR) &&
					begin.get(Calendar.MONTH) <= end.get(Calendar.MONTH))){
				
				while(true){
					if (begin.compareTo(end) <= 0){
						ret.add(formatDate(begin));
					}
					else
						break;

					int nextDay = begin.get(Calendar.DAY_OF_MONTH) + 1;
					if (nextDay > 31)
						break;

					begin.set(Calendar.DAY_OF_MONTH, nextDay);
				}
				
				if (begin.get(Calendar.MONTH) + 1 == 12)
					break;
				else{
					begin.set(Calendar.DAY_OF_MONTH, 1);
					begin.set(Calendar.MONTH, begin.get(Calendar.MONTH) + 1);
				}
			}
			
			begin.set(Calendar.DAY_OF_MONTH, 1);
			begin.set(Calendar.MONTH, 0);
			begin.set(Calendar.YEAR, begin.get(Calendar.YEAR) + 1);
		}
		
		return ret;
	}
	
	private static void initByDate(Calendar cal, String date) {
		int beginYear = Integer.parseInt(date.substring(0, 4));
		int beginMonth = Integer.parseInt(date.substring(4, 6));
		int beginDay = Integer.parseInt(date.substring(6, 8));
		
		cal.set(Calendar.DAY_OF_MONTH, beginDay);
		cal.set(Calendar.MONTH, beginMonth - 1);
		cal.set(Calendar.YEAR, beginYear);
	}

	public static String formatDate(Calendar date){
		StringBuilder ret = new StringBuilder();
		ret.append("" + date.get(Calendar.YEAR));
		
		String month = String.valueOf(date.get(Calendar.MONTH) + 1);
		if (month.length() == 1)
			month = "0" + month;
		
		ret.append(month);
		
		String day = String.valueOf(date.get(Calendar.DATE) + 1);
		if (day.length() == 1)
			day = "0" + day;
		
		ret.append(day);
		return ret.toString();
	}
}
