package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageCopier {
    
    public void copyImage(Path sourcePath, Path targetPath) throws IOException;

}
