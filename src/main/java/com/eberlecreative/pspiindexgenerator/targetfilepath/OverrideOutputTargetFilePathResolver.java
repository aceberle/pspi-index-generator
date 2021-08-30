package com.eberlecreative.pspiindexgenerator.targetfilepath;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class OverrideOutputTargetFilePathResolver extends BaseTargetFilePathResolver {
    
    private final List<Appender> appenders;
    
    public OverrideOutputTargetFilePathResolver(FileUtils fileUtils, List<Appender> appenders) {
        super(fileUtils);
        this.appenders = appenders;
    }

    @Override
    public Path getTargetFilePath(File currentBaseDir, File newBaseDir, File currentFilePath, Map<String, String> fieldValues) {
        final Path path = fileUtils.getRelativePath(currentBaseDir, newBaseDir, currentFilePath);
        final StringBuilder newFileNameBuilder = new StringBuilder();
        appenders.forEach(appender -> appender.append(newFileNameBuilder, fieldValues));
        final String newFileName = newFileNameBuilder.toString();
        return path.resolveSibling(newFileName);
    }

}
