package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;

public interface ImageCopier {
    
    public void copyImage(File sourceFile, File targetFile) throws IOException;

}
