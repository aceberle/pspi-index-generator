package com.eberlecreative.pspiindexgenerator.targetfilepath;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public interface TargetFilePathResolver {

    public Path getTargetFilePath(File currentBaseDir, File newBaseDir, File currentFilePath, Map<String, String> fieldValues);
    
}
