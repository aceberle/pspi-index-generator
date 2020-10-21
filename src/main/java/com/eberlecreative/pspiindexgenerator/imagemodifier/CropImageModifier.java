package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class CropImageModifier implements ImageModifier {

    private final AspectRatioProvider targetAspectRatioProvider;

    private final CropAnchor cropAnchor;

    private final Logger logger;

    public CropImageModifier(AspectRatioProvider targetAspectRatioProvider, CropAnchor cropAnchor, Logger logger) {
        this.targetAspectRatioProvider = targetAspectRatioProvider;
        this.cropAnchor = cropAnchor;
        this.logger = logger;
    }

    @Override
    public BufferedImage modifyImage(Path imagePath, BufferedImage origImage) {
        final int origWidth = origImage.getWidth();
        final int origHeight = origImage.getHeight();
        final double origAspectRatio = ((double)origWidth)/origHeight;
        int newWidth = origWidth;
        int newHeight = origHeight;
        final double targetAspectRatio = targetAspectRatioProvider.getAspectRatio();
        if(origAspectRatio > targetAspectRatio) {
            newWidth = (int) Math.round(origHeight * targetAspectRatio);
        } else if(origAspectRatio < targetAspectRatio) {
            newHeight = (int) Math.round(origWidth / targetAspectRatio);
        } else {
            logger.logInfo("Skipping center-crop for image \"%s\"", imagePath.getFileName());
            return origImage;
        }
        final Point offsets = cropAnchor.calculateCropOffset(origWidth, origHeight, newWidth, newHeight);
        final int x = (int)offsets.getX();
        final int y = (int)offsets.getY();
        logger.logInfo("Center-cropping image \"%s\" from %sx%s to %sx%s with offset X = %s and offset Y = %s", imagePath.getFileName(), origWidth, origHeight, newWidth, newHeight, x, y);
        return origImage.getSubimage(x, y, newWidth, newHeight);
    }
    
}
