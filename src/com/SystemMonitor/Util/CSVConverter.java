package com.SystemMonitor.Util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class CSVConverter {

	public static String convertToXLS(File source) throws Exception{
		return convert(source);
	}

	@SuppressWarnings("deprecation")
	private static String convert(File file){
		String newFileName = null;
		FileOutputStream fileout = null;
		HSSFWorkbook workbook = null;
		DataInputStream dis = null;
		try{
			ArrayList<ArrayList<String>> arList = new ArrayList<ArrayList<String>>();
			ArrayList<String> al = null;
			String fPath = file.getAbsolutePath();
			FileInputStream fis = new FileInputStream(fPath);
			dis = new DataInputStream(fis);
			String line = null;
			
			while((line = dis.readLine()) != null){
				al = new ArrayList<String>();
				String[] strs = line.split(",");
				for(String str : strs){
					al.add(str);
				}
				
				arList.add(al);
			}
			
			newFileName = createNewFileName(file);			
			File targetFile = new File(newFileName);
			if (targetFile.exists()){
				targetFile.delete();
			}
			
			workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("new sheet");
			
			for (int j = 0; j < arList.size(); j++) {
				ArrayList<String> ardata = (ArrayList<String>) arList.get(j);

				HSSFRow row = sheet.createRow(j);

				for (int k = 0; k < ardata.size(); k++) {
					HSSFCell cell = row.createCell(k);
					String data = ardata.get(k).toString();
					
					if (data.length() > 32767)
						continue;

					if (data.startsWith("=")) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						data = data.replaceAll("\"", "");
						data = data.replaceAll("=", "");
						cell.setCellValue(data);
					} else if (data.startsWith("\"")) {
						data = data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(data);
					} else {
						data = data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(data);
					}
				}
			}
			fileout = new FileOutputStream(newFileName);
			workbook.write(fileout);
		}catch(IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}finally{
			try{
				if (fileout != null)
					fileout.close();
				if (workbook != null)
					workbook.close();
				if (dis != null)
					dis.close();
			}catch (IOException e){
				SystemMonitorException.logException(Thread.currentThread(), e);
			}
		}

		return newFileName;
	}

	private static String createNewFileName(File source){
		String sourceName = source.getAbsolutePath();
		String newName = sourceName.substring(0, sourceName.lastIndexOf("."));
		return newName + ".xls";
	}
}