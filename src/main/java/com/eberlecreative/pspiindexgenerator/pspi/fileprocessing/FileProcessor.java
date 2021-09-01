package com.eberlecreative.pspiindexgenerator.pspi.fileprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface FileProcessor {
    
    void processFile(File inputDirectory, File outputDirectory, String imageFolderName, File imageFile, Map<String, String> fieldValues) throws IOException;
    
}