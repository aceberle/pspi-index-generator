package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;

public class ImageCopierFilter implements ImageCopier {

    private final ImageCopier source;

    public ImageCopierFilter(ImageCopier source) {
        this.source = source;
    }

    @Override
    public void copyImage(File sourceFile, File targetFile) throws IOException {
        this.source.copyImage(sourceFile, targetFile);
    }
    
}
