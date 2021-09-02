package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.io.File;
import java.util.Map;

public interface OutputFileNameResolver {
    
    public String resolveOutputFileName(File imageFile, Map<String, String> fieldValues);
    
}
