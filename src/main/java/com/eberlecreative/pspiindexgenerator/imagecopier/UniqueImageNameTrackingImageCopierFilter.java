package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandler;

public class UniqueImageNameTrackingImageCopierFilter extends ImageCopierFilter {

    private final ErrorHandler errorHandler;

    private Set<String> processedImageNames;

    public UniqueImageNameTrackingImageCopierFilter(ImageCopier source, ErrorHandler errorHandler) {
        super(source);
        this.errorHandler = errorHandler;
        this.processedImageNames = new HashSet<>();
    }

    @Override
    public void copyImage(File sourceFile, File targetFile) throws IOException {
        final String name = sourceFile.getName();
        if(processedImageNames.contains(name)) {
            errorHandler.handleError("Image with name \"%s\" has already been processed!  PSPI Guidelines specify that image names should be unique!", name);
        }
        processedImageNames.add(name);
        super.copyImage(sourceFile, targetFile);
    }
    
}