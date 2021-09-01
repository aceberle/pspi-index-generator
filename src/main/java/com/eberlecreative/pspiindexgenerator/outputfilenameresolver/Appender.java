package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.util.Map;

@FunctionalInterface
public interface Appender {

    void append(StringBuilder builder, Map<String, String> fieldValues);
    
}
