package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CropAnchors {

    private static final Pattern PATTERN_CROP_ALIGNMENT = Pattern.compile("^(top|center|bottom|t|c|b)-?(left|middle|right|l|m|r)$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidCropOffset(String string) {
        return getMatcher(string).matches();
    }
    
    public static void validateCropOffset(String string) {
        internalValidateCropOffset(string);
    }
    
    public static CropAnchor parseCropAnchor(String string) {
        final Matcher matcher = internalValidateCropOffset(string);
        final PointChifter vertical = findPointModifier(matcher.group(1));
        final PointChifter horizontal = findPointModifier(matcher.group(2));
        return (oldWidth, oldHeight, newWidth, newHeight) -> {
            final int x = horizontal.calculateOffset(oldWidth, newWidth);
            final int y = vertical.calculateOffset(oldHeight, newHeight);
            return new Point(x, y);
        };
    }

    private static Matcher internalValidateCropOffset(String string) {
        final Matcher matcher = getMatcher(string);
        if(!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Provided string \"%s\" does not match expected pattern: ", string, PATTERN_CROP_ALIGNMENT.pattern()));
        }
        return matcher;
    }

    private static Matcher getMatcher(String string) {
        return PATTERN_CROP_ALIGNMENT.matcher(string);
    }

    private interface PointChifter {
        int calculateOffset(int first, int second);
    }

    private static final PointChifter SAME = (first, second) -> 0;

    private static final PointChifter CENTER = (first, second) -> (first - second) / 2;

    private static final PointChifter FAR = (first, second) -> first - second;

    private static final Map<String, PointChifter> POINT_CONSUMERS_BY_NAME;
    static {
        POINT_CONSUMERS_BY_NAME = new HashMap<>();
        POINT_CONSUMERS_BY_NAME.put("top", SAME);
        POINT_CONSUMERS_BY_NAME.put("center", CENTER);
        POINT_CONSUMERS_BY_NAME.put("bottom", FAR);
        POINT_CONSUMERS_BY_NAME.put("t", SAME);
        POINT_CONSUMERS_BY_NAME.put("c", CENTER);
        POINT_CONSUMERS_BY_NAME.put("b", FAR);
        POINT_CONSUMERS_BY_NAME.put("left", SAME);
        POINT_CONSUMERS_BY_NAME.put("middle", CENTER);
        POINT_CONSUMERS_BY_NAME.put("right", FAR);
        POINT_CONSUMERS_BY_NAME.put("l", SAME);
        POINT_CONSUMERS_BY_NAME.put("m", CENTER);
        POINT_CONSUMERS_BY_NAME.put("r", FAR);
    }

    private static PointChifter findPointModifier(String name) {
        return POINT_CONSUMERS_BY_NAME.get(name.toLowerCase());
    }
    
}
