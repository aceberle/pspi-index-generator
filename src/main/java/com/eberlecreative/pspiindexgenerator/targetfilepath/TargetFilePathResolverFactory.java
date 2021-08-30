package com.eberlecreative.pspiindexgenerator.targetfilepath;

import java.util.List;

import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class TargetFilePathResolverFactory {
    
    private FileUtils fileUtils = FileUtils.getInstance();

    private String overrideFilePattern;

    public TargetFilePathResolverFactory outputFilePattern(String overrideFilePattern) {
        this.overrideFilePattern = overrideFilePattern;
        return this;
    }
    
    public TargetFilePathResolver getTargetFilePathResolver() {
        if(overrideFilePattern != null) {
            final OverridePatternParser parser = new OverridePatternParser();
            final List<Appender> appenders = parser.parseOverridePattern(overrideFilePattern);
            return new OverrideOutputTargetFilePathResolver(fileUtils, appenders);
        }
        return new SameNameTargetFilePathResolver(fileUtils);
    }
    
}
