package com.eberlecreative.pspiindexgenerator.imagemodifier;

public abstract class AbstractImageSize implements ImageSize {

    @Override
    public double getAspectRatio() {
        return AspectRatioCalculator.getInstance().calculateAspectRatio(this);
    }

}
