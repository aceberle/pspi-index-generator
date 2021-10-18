package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverridePatternParser {

    private static final Pattern TOKENS = Pattern.compile("<([^>]*)>");

	public List<Appender> parseOverridePattern(String overridePattern) {
        List<Appender> results = new ArrayList<>();
        final Matcher matcher = TOKENS.matcher(overridePattern);
        int lastMatchEndIdx = 0;
        while(matcher.find()) {
            final String fieldName = matcher.group(1);
            final int matchStartIdx = matcher.start();
            if(lastMatchEndIdx < matchStartIdx) {
                final String beforeToken = overridePattern.substring(lastMatchEndIdx, matchStartIdx);
                results.add((stringBuilder, fieldValues) -> {
                    stringBuilder.append(beforeToken);
                });
            }
            lastMatchEndIdx = matcher.end();
            results.add((stringBuilder, fieldValues) -> {
                final String tokenValue = fieldValues.get(fieldName);
                if(tokenValue == null) {
                    throw new RuntimeException(String.format("Unable to find value for field name \"%s\" to insert into file pattern: %s", fieldName, overridePattern));
                }
                stringBuilder.append(tokenValue);
            });
        }
        if(lastMatchEndIdx < overridePattern.length()) {
            final String tail = overridePattern.substring(lastMatchEndIdx, overridePattern.length());
            results.add((stringBuilder, fieldValues) -> {
                stringBuilder.append(tail);
            });
        }
        return results;
    }
    
}
