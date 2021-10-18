package com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.eberlecreative.pspiindexgenerator.datafileparser.DataFileParser;
import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;
import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.FileProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.util.IndexRecordFields;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class DataFileBasedDirectoryProcessor implements DirectoryProcessor {

    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^(\\d+)\\.(jpg|jpeg|mpg|mpeg|gif|png)$");
    
    private static final Pattern FIRST_LAST_PATTERN = Pattern.compile("^(.+)\\s+([^\\s]+)$");
    
    private static final Pattern LAST_FIRST_PATTERN = Pattern.compile("^([^\\s]+)\\s+(.+)$");
    
    private static final String IMAGES_DIR_NAME = "images";

    private static final String CSV_HEADER_LAST_FIRST = "lastFirst";
    
    private static final String CSV_HEADER_FIRST_LAST = "firstLast";
    
    private final FileUtils fileUtils;
    
    private final EventHandler eventHandler;

    private final DataFileParser dataFileParser;
    
    private final File dataFilePath;

    public DataFileBasedDirectoryProcessor(FileUtils fileUtils, EventHandler eventHandler, DataFileParser dataFileParser, File dataFilePath) {
        this.fileUtils = fileUtils;
        this.eventHandler = eventHandler;
        this.dataFileParser = dataFileParser;
        this.dataFilePath = dataFilePath;
    }

    @Override
    public void processDirectory(File inputDirectory, File outputDirectory, Collection<RecordField> recordFields, FileProcessor fileProcessor) throws IOException {
        final List<FieldValueRepository> rawData = dataFileParser.parseDataFile(dataFilePath);
        final Map<Long, FieldValueRepository> dataByImageNumber = getDataByImageNumber(rawData, eventHandler);
        final Queue<File> files = new LinkedList<>();
        files.addAll(getFiles(inputDirectory));
        while (!files.isEmpty()) {
        	final File file = files.poll();
            if(file.isHidden()) {
                continue;
            }
            eventHandler.info("Processing path: " + file);
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else if(file.isFile()) {
                final String imageFileName = file.getName();
                final Matcher imageFileMatcher = IMAGE_NAME_PATTERN.matcher(imageFileName);
                if (!imageFileMatcher.matches()) {
                    eventHandler.error("Encountered unexpected file name \"%s\" while processing file: %s", imageFileName, file);
                } else {
                    final Long imageNumber = Long.parseLong(imageFileMatcher.group(1));
                    final FieldValueRepository fieldValues = dataByImageNumber.get(imageNumber);
                    if(fieldValues == null) {
                        eventHandler.error("Could not find row data for image number %s while processing image file: %s", imageNumber, file);
                    } else {
                        updateNames(fieldValues);
                        fileProcessor.processFile(inputDirectory, outputDirectory, IMAGES_DIR_NAME, file, fieldValues);
                    }
                }
            } else {
            	eventHandler.info("Skipping path that is not a file or a directory: " + file);
            }
        }
    }

	private List<File> getFiles(File inputDirectory) {
		return Arrays.asList(fileUtils.sort(inputDirectory.listFiles()));
	}
    
    private void updateNames(FieldValueRepository rowData) {
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

    private Map<Long, FieldValueRepository> getDataByImageNumber(List<FieldValueRepository> rawData, EventHandler eventHandler) {
        final Map<Long, FieldValueRepository> results = new HashMap<>();
        for(FieldValueRepository data : rawData) {
            final String rawImageNumber = data.get(IMAGE_NUMBER_HEADER);
            if(rawImageNumber == null) {
                eventHandler.error("Unable to locate image number in row information: %s", data);
            }
            try {
                final long imageNumber = (long) Math.floor(Double.parseDouble(rawImageNumber));
                if(results.containsKey(imageNumber)) {
                    eventHandler.error("Image number %s has been defined in multiple rows!", imageNumber);
                } else {
                    results.put(imageNumber, data);
                }
            } catch (NumberFormatException e) {
                eventHandler.error("An exception occured while trying to parse image number string \"%s\": %s", rawImageNumber, e);
            }
        }
        return results;
    }

}
