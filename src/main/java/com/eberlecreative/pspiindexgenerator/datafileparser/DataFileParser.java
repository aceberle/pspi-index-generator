package com.eberlecreative.pspiindexgenerator.datafileparser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;
import com.eberlecreative.pspiindexgenerator.util.FieldValueRepositoryFactory;

public class DataFileParser {
	
	private final FieldValueRepositoryFactory fieldValueRepositoryFactory;

	public DataFileParser(FieldValueRepositoryFactory fieldValueRepositoryFactory) {
		this.fieldValueRepositoryFactory = fieldValueRepositoryFactory;
	}

	public List<FieldValueRepository> parseDataFile(File dataFilePath) {
        final List<FieldValueRepository> results = new ArrayList<>();
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
                if(StringUtils.isBlank(cellValue)) {
                    break;
                }
                headers.add(cellValue);
            }
            final DataFormatter objDefaultFormat = new DataFormatter();
            while(rowIterator.hasNext()) {
                final FieldValueRepository rowData = fieldValueRepositoryFactory.newFieldValueRepository();
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

    private boolean isAllBlank(FieldValueRepository rowData) {
        return StringUtils.isAllBlank(rowData.values().toArray(new String[0]));
    }

}
