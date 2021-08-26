package com.eberlecreative.pspiindexgenerator.pspi;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandler;
import com.eberlecreative.pspiindexgenerator.imagecopier.ImageCopier;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.record.RecordWriter;

@FunctionalInterface
public interface FileProcessingStrategy {

    void processFiles(RecordWriter indexRecordWriter, File inputDirectory, File outputDirectory, ErrorHandler errorHandler, ImageCopier imageCopier, Collection<RecordField> recordFields) throws IOException;

}
