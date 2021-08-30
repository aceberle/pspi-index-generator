package com.eberlecreative.pspiindexgenerator.targetfilepath;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class SameNameTargetFilePathResolver extends BaseTargetFilePathResolver {
    
    public SameNameTargetFilePathResolver(FileUtils fileUtils) {
        super(fileUtils);
    }
    
    @Override
    public Path getTargetFilePath(File currentBaseDir, File newBaseDir, File currentFilePath, Map<String, String> fieldValues) {
        return fileUtils.getRelativePath(currentBaseDir, newBaseDir, currentFilePath);
    }

}
