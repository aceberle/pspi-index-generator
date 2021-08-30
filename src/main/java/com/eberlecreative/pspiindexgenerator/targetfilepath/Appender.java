package com.eberlecreative.pspiindexgenerator.targetfilepath;

import java.util.Map;

@FunctionalInterface
public interface Appender {

    void append(StringBuilder builder, Map<String, String> fieldValues);
    
}
