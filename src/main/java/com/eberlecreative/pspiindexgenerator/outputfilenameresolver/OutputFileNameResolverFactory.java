package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.util.List;

public class OutputFileNameResolverFactory {

    private String overrideFilePattern;

    public OutputFileNameResolverFactory outputFilePattern(String overrideFilePattern) {
        this.overrideFilePattern = overrideFilePattern;
        return this;
    }
    
    public OutputFileNameResolver getOutputFileNameResolver() {
        if(overrideFilePattern != null) {
            final OverridePatternParser parser = new OverridePatternParser();
            final List<Appender> appenders = parser.parseOverridePattern(overrideFilePattern);
            return new FieldValueBasedOverridingOutputFileNameResolver(appenders);
        }
        return new SameNameOutputFileNameResolver();
    }
    
}
