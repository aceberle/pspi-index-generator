package com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.eberlecreative.pspiindexgenerator.datafileparser.DataFileParser;
import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandler;
import com.eberlecreative.pspiindexgenerator.logger.Logger;
import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.FileProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.util.IndexRecordFields;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class DataFileBasedDirectoryProcessor implements DirectoryProcessor {

    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^(\\d+)\\.(jpg|jpeg|mpg|mpeg|gif|png)$");
    
    private static final Pattern FIRST_LAST_PATTERN = Pattern.compile("^(.+)\\s+([^\\s]+)$");
    
    private static final Pattern LAST_FIRST_PATTERN = Pattern.compile("^([^\\s]+)\\s+(.+)$");
    
    private static final String IMAGES_DIR_NAME = "images";

    private static final String CSV_HEADER_LAST_FIRST = "lastFirst";
    
    private static final String CSV_HEADER_FIRST_LAST = "firstLast";
    
    private final FileUtils fileUtils;
    
    private final Logger logger;
    
    private final ErrorHandler errorHandler;

    private final DataFileParser dataFileParser;
    
    private final File dataFilePath;

    public DataFileBasedDirectoryProcessor(FileUtils fileUtils, Logger logger, ErrorHandler errorHandler, DataFileParser dataFileParser, File dataFilePath) {
        this.fileUtils = fileUtils;
        this.logger = logger;
        this.errorHandler = errorHandler;
        this.dataFileParser = dataFileParser;
        this.dataFilePath = dataFilePath;
    }

    @Override
    public void processDirectory(File inputDirectory, File outputDirectory, Collection<RecordField> recordFields, FileProcessor fileProcessor) throws IOException {
        final List<Map<String, String>> rawData = dataFileParser.parseDataFile(dataFilePath);
        final Map<Long, Map<String, String>> dataByImageNumber = getDataByImageNumber(rawData, errorHandler);
        for (File file : fileUtils.sort(inputDirectory.listFiles())) {
            if(file.isHidden()) {
                continue;
            }
            logger.logInfo("Processing path: " + file);
            if (!file.isFile()) {
                errorHandler.handleError("Expected path to be a file: " + file);
            } else {
                final String imageFileName = file.getName();
                final Matcher imageFileMatcher = IMAGE_NAME_PATTERN.matcher(imageFileName);
                if (!imageFileMatcher.matches()) {
                    errorHandler.handleError("Encountered unexpected file name \"%s\" while processing file: %s", imageFileName, file);
                } else {
                    final Long imageNumber = Long.parseLong(imageFileMatcher.group(1));
                    final Map<String, String> fieldValues = dataByImageNumber.get(imageNumber);
                    updateNames(fieldValues);
                    if(fieldValues == null) {
                        errorHandler.handleError("Could not find row data for image number %s while processing image file: %s", imageNumber, file);
                    } else {
                        fileProcessor.processFile(inputDirectory, outputDirectory, IMAGES_DIR_NAME, file, fieldValues);
                    }
                }
            }
        }
    }
    
    private void updateNames(Map<String, String> rowData) {
        final String lastName = rowData.get(IndexRecordFields.LAST_NAME);
        final String firstName = rowData.get(IndexRecordFields.FIRST_NAME);
        if(StringUtils.isAnyBlank(lastName, firstName)) {
            final String lastFirst = rowData.get(CSV_HEADER_LAST_FIRST);
            final String firstLast = rowData.get(CSV_HEADER_FIRST_LAST);
            if(StringUtils.isNotBlank(lastFirst)) {
                final Matcher matcher = LAST_FIRST_PATTERN.matcher(rowData.get(CSV_HEADER_LAST_FIRST));
                if(matcher.matches()) {
                    rowData.put(IndexRecordFields.LAST_NAME, matcher.group(1));
                    rowData.put(IndexRecordFields.FIRST_NAME, matcher.group(2));
                }
            } else if(StringUtils.isNotBlank(firstLast)) {
                final Matcher matcher = FIRST_LAST_PATTERN.matcher(rowData.get(CSV_HEADER_FIRST_LAST));
                if(matcher.matches()) {
                    rowData.put(IndexRecordFields.FIRST_NAME, matcher.group(1));
                    rowData.put(IndexRecordFields.LAST_NAME, matcher.group(2));
                }
            }
        }
    }

    private static final String IMAGE_NUMBER_HEADER = "imageNumber";

    private Map<Long, Map<String, String>> getDataByImageNumber(List<Map<String, String>> rawData, ErrorHandler errorHandler) {
        final Map<Long, Map<String, String>> results = new HashMap<>();
        for(Map<String, String> data : rawData) {
            final String rawImageNumber = data.get(IMAGE_NUMBER_HEADER);
            if(rawImageNumber == null) {
                errorHandler.handleError("Unable to locate image number in row information: %s", data);
            }
            try {
                final long imageNumber = (long) Math.floor(Double.parseDouble(rawImageNumber));
                if(results.containsKey(imageNumber)) {
                    errorHandler.handleError("Image number %s has been defined in multiple rows!", imageNumber);
                } else {
                    results.put(imageNumber, data);
                }
            } catch (NumberFormatException e) {
                errorHandler.handleError("An exception occured while trying to parse image number string \"%s\": %s", rawImageNumber, e);
            }
        }
        return results;
    }

}
