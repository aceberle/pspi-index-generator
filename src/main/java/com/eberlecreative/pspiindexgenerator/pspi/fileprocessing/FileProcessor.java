package com.eberlecreative.pspiindexgenerator.pspi.fileprocessing;

import java.io.File;
import java.io.IOException;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;

@FunctionalInterface
public interface FileProcessor {

	void processFile(File inputDirectory, File outputDirectory, String imageFolderName, File imageFile,
			FieldValueRepository fieldValues) throws IOException;

}