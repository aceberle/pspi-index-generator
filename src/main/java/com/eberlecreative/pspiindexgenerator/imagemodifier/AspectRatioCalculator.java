package com.eberlecreative.pspiindexgenerator.imagemodifier;

public class AspectRatioCalculator {

    private static AspectRatioCalculator instance = new AspectRatioCalculator();
    
    public static AspectRatioCalculator getInstance() {
        return instance;
    }
    
    public double calculateAspectRatio(ImageSize imageSize) {
        return ((double)imageSize.getWidth()) / imageSize.getHeight();
    }
    
}
