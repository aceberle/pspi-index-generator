package com.eberlecreative.pspiindexgenerator.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestWorkbookGenerator {

	private String[] columnHeaders;

	private List<String[]> rowDatas = new ArrayList<>();

	public void reset() {
		columnHeaders = null;
		rowDatas.clear();
	}

	public void setColumnHeaders(String... headers) {
		this.columnHeaders = headers;
	}

	public void addRow(String... rowData) {
		rowDatas.add(rowData);
	}

	public void generateWorkbook(File inputFile) {
		if (columnHeaders != null) {
			try (FileOutputStream outputStream = new FileOutputStream(inputFile);
					XSSFWorkbook workbook = new XSSFWorkbook()) {
				final XSSFSheet sheet = workbook.createSheet();
				int rowCount = 0;
				final XSSFRow headerRow = sheet.createRow(rowCount++);
				addCells(headerRow, columnHeaders);
				for (String[] rowData : rowDatas) {
					final XSSFRow row = sheet.createRow(rowCount++);
					addCells(row, rowData);
				}
				workbook.write(outputStream);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				reset();
			}
		}
	}

	private static void addCells(XSSFRow row, String[] datas) {
		int columnCount = 0;
		for (String data : datas) {
			row.createCell(columnCount++).setCellValue(data);
		}
	}

}
