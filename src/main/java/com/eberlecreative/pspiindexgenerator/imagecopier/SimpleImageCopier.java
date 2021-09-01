package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class SimpleImageCopier implements ImageCopier {

    private final Logger logger;

    public SimpleImageCopier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void copyImage(File sourceFile, File targetFile) throws IOException {
        logger.logInfo("Copying image from \"%s\" to \"%s\"", sourceFile, targetFile);
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
    
}
