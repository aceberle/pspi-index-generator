package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.IOException;
import java.nio.file.Path;

public class ImageCopierFilter implements ImageCopier {

    private final ImageCopier source;

    public ImageCopierFilter(ImageCopier source) {
        this.source = source;
    }

    @Override
    public void copyImage(Path sourcePath, Path targetPath) throws IOException {
        this.source.copyImage(sourcePath, targetPath);
    }
    
}
