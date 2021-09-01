package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;

public class SimpleImageCopier implements ImageCopier {

    private final EventHandler eventHandler;

    public SimpleImageCopier(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void copyImage(File sourceFile, File targetFile) throws IOException {
        eventHandler.info("Copying image from \"%s\" to \"%s\"", sourceFile, targetFile);
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
    
}
