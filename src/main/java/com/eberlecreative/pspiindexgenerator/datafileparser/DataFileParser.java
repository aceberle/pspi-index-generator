package com.eberlecreative.pspiindexgenerator.datafileparser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataFileParser {

    public List<Map<String, String>> parseDataFile(File dataFilePath) {
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
                if(StringUtils.isBlank(header)) {
                    break;
                }
                headers.add(header);
            }
            final DataFormatter objDefaultFormat = new DataFormatter();
            while(rowIterator.hasNext()) {
                final Map<String, String> rowData = new HashMap<>();
                final Row row = rowIterator.next();
                final Iterator<Cell> cellIterator = row.cellIterator();
                for(int i = 0; i < headers.size() && cellIterator.hasNext(); i++) {
                    final String header = headers.get(i);
                    final Cell cell = cellIterator.next();
                    final String cellValue = objDefaultFormat.formatCellValue(cell);
                    rowData.put(header, cellValue);
                }
                if(isAllBlank(rowData)) {
                    break;
                }
                results.add(rowData);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while processing data file at: " + dataFilePath, e);
        }
        return results;
    }

    private boolean isAllBlank(Map<String, String> rowData) {
        return StringUtils.isAllBlank(rowData.values().toArray(new String[0]));
    }

    private String canonicalizeHeader(String value) {
        return CaseUtils.toCamelCase(value, false, ' ', '_');
    }

}
