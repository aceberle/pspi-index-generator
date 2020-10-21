package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class SimpleImageCopier implements ImageCopier {

    private final Logger logger;

    public SimpleImageCopier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void copyImage(Path sourcePath, Path targetPath) throws IOException {
        logger.logInfo("Copying image from \"%s\" to \"%s\"", sourcePath, targetPath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
    
}
