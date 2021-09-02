package com.eberlecreative.pspiindexgenerator.pspi.fileprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;
import com.eberlecreative.pspiindexgenerator.imagecopier.ImageCopier;
import com.eberlecreative.pspiindexgenerator.outputfilenameresolver.OutputFileNameResolver;
import com.eberlecreative.pspiindexgenerator.pspi.util.IndexRecordFields;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.record.RecordWriter;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;

public class PspiCopyAndWriteRecordFileProcessor implements FileProcessor {

    private static final long MAX_IMAGE_SIZE = (long)1E+7;
    
    private final EventHandler eventHandler;
    
    private final FileUtils fileUtils;
    
    private final ImageCopier imageCopier;
    
    private final OutputFileNameResolver outputFileNameResolver;

    private final ImageUtils imageUtils;
    
    private final RecordWriter indexRecordWriter;

    private final Collection<RecordField> recordFields;

    public PspiCopyAndWriteRecordFileProcessor(EventHandler eventHandler, FileUtils fileUtils, ImageCopier imageCopier, OutputFileNameResolver outputFileNameResolver, ImageUtils imageUtils, RecordWriter indexRecordWriter, Collection<RecordField> recordFields) {
        this.eventHandler = eventHandler;
        this.fileUtils = fileUtils;
        this.imageCopier = imageCopier;
        this.outputFileNameResolver = outputFileNameResolver;
        this.imageUtils = imageUtils;
        this.indexRecordWriter = indexRecordWriter;
        this.recordFields = recordFields;
    }

    @Override
    public void processFile(File inputDirectory, File outputDirectory, String imageFolderName, File imageFile, Map<String, String> fieldValues) throws IOException {
        final String outputFileName = outputFileNameResolver.resolveOutputFileName(imageFile, fieldValues);
        final File newImageFile = outputDirectory.toPath().resolve(imageFolderName).resolve(outputFileName).toFile();
        fileUtils.mkdirs(newImageFile);
        imageCopier.copyImage(imageFile, newImageFile);
        final Map<String, String> indexRecord = new HashMap<>();
        for (RecordField field : recordFields) {
            final String fieldName = field.getName();
            final String valueFromMatchers = fieldValues.get(fieldName);
            if (valueFromMatchers != null) {
                indexRecord.put(fieldName, valueFromMatchers);
            }
        }
        useAliasValueIfEmpty(indexRecord, IndexRecordFields.HOME_ROOM, IndexRecordFields.GRADE);
        indexRecord.put(IndexRecordFields.VOLUME_NAME, outputDirectory.getName());
        indexRecord.put(IndexRecordFields.IMAGE_FOLDER, imageFolderName);
        indexRecord.put(IndexRecordFields.IMAGE_FILE_NAME, newImageFile.getName());
        indexRecord.put(IndexRecordFields.IMAGE_SIZE, getImageSize(newImageFile));
        eventHandler.info("Logging index record: %s", indexRecord);
        indexRecordWriter.writeRecord(indexRecord);
    }
    
    private void useAliasValueIfEmpty(Map<String, String> indexRecord, String ifValueEmpty, String thenUseValue) {
        if(indexRecord.get(ifValueEmpty) == null) {
            indexRecord.put(ifValueEmpty, indexRecord.get(thenUseValue));
        }
    }

    private String getImageSize(File imageFile) throws IOException {
        final long fileSize = imageFile.length();
        if(fileSize > MAX_IMAGE_SIZE) {
            eventHandler.error("Invalid image found! Expected size to be less than %s bytes but found size of %s bytes, image at: %s", MAX_IMAGE_SIZE, fileSize, imageFile);
        }
        final BufferedImage bimg = imageUtils.readImage(imageFile);
        final int width = bimg.getWidth();
        final int height = bimg.getHeight();
        if (width == 320 && height == 400) {
            return "SMALL";
        } else if (width == 640 && height == 800) {
            return "LARGE";
        }
        final double ratio = ((double) width) / height;
        if (ratio != 0.8) {
            eventHandler.error("Invalid image found! Expected aspect ratio of 0.8 but found %spx / %spx = %s, image at: %s", width, height, ratio, imageFile);
        }
        return "OTHER";
    }

}
