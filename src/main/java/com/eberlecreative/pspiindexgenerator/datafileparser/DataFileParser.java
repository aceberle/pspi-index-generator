package com.eberlecreative.pspiindexgenerator.datafileparser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.CaseUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataFileParser {

    public List<Map<String, String>> parseDataFile(String dataFilePath) {
        final List<Map<String, String>> results = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(dataFilePath);
                XSSFWorkbook workbook = new XSSFWorkbook (fis);) {
            final XSSFSheet sheet = workbook.getSheetAt(0);
            final Iterator<Row> rowIterator = sheet.iterator();
            if(!rowIterator.hasNext()) {
                throw new RuntimeException("Data file does not have any rows! " + dataFilePath);
            }
            final Row headerRow = rowIterator.next();
            final List<String> headers = new ArrayList<>();
            final Iterator<Cell> headerCellIterator = headerRow.cellIterator();
            while(headerCellIterator.hasNext()) {
                final Cell cell = headerCellIterator.next();
                final String cellValue = cell.getStringCellValue();
                final String header = canonicalizeHeader(cellValue);
                headers.add(header);
            }
            while(rowIterator.hasNext()) {
                final Map<String, String> rowData = new HashMap<>();
                results.add(rowData);
                final Row row = rowIterator.next();
                final Iterator<Cell> cellIterator = row.cellIterator();
                for(int i = 0; i < headers.size() && cellIterator.hasNext(); i++) {
                    final Cell cell = cellIterator.next();
                    final String cellValue = cell.getStringCellValue();
                    final String header = headers.get(i);
                    rowData.put(header, cellValue);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while processing data file at: " + dataFilePath, e);
        }
        return results;
    }

    private String canonicalizeHeader(String value) {
        return CaseUtils.toCamelCase(value, false, ' ', '_');
    }

}
