package com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.FileProcessor;
import com.eberlecreative.pspiindexgenerator.record.RecordField;

@FunctionalInterface
public interface DirectoryProcessor {

    void processDirectory(File inputDirectory, File outputDirectory, Collection<RecordField> recordFields, FileProcessor fileProcessor) throws IOException;

}
