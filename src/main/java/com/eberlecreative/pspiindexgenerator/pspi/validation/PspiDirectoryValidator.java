package com.eberlecreative.pspiindexgenerator.pspi.validation;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;

import com.eberlecreative.pspiindexgenerator.pspi.util.PspiConstants;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class PspiDirectoryValidator {
    
    private FileUtils fileUtils = FileUtils.getInstance();

    public void validatePspiDirectory(File directory) {
        fileUtils.assertIsDirectory(directory);
        final File copyrightFile = new File(directory, PspiConstants.COPYRIGHT_FILE_NAME);
        fileUtils.assertIsFileWithSize(copyrightFile);
        final File indexFile = new File(directory, PspiConstants.INDEX_FILE_NAME);
        fileUtils.assertIsFileWithSize(indexFile);
        final String directoryName = directory.getName();
        try (Reader in = new FileReader(indexFile)) {
            final Iterable<CSVRecord> records = Builder.create(CSVFormat.TDF).setHeader().setSkipHeaderRecord(true).build().parse(in);
            for(CSVRecord record : records) {
                final String volume = record.get("Volume Name");
                if(!directoryName.equals(volume)) {
                    throw new RuntimeException(String.format("Expected \"Volume\" to be \"%s\" but was \"%s\" in record #%s", directoryName, volume, record.getRecordNumber()));
                }
                final String folder = record.get("Image Folder");
                final String name = record.get("Image File Name");
                final File expectedFile = new File(directory, String.format("%s/%s", folder, name));
                fileUtils.assertIsFileWithSize(expectedFile);
            }
        } catch (Exception e) {
            throw new AssertionError("Exception occurred while verifying output directory", e);
        }
    }
    
}
