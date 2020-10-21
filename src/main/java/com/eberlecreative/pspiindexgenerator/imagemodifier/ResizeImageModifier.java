package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class ResizeImageModifier implements ImageModifier {

    private final ImageSize targetSize;

    private final Logger logger;

    public ResizeImageModifier(ImageSize targetSize, Logger logger) {
        this.targetSize = targetSize;
        this.logger = logger;
    }

    @Override
    public BufferedImage modifyImage(Path imagePath, BufferedImage origImage) {
        final int origWidth = origImage.getWidth();
        final int origHeight = origImage.getHeight();
        final int targetWidth = targetSize.getWidth();
        final int targetHeight = targetSize.getHeight();
        if(origWidth == targetWidth && origHeight == targetHeight) {
            logger.logInfo("Skipping scaling for image \"%s\"", imagePath.getFileName());
            return origImage;
        }
        logger.logInfo("Resizing image \"%s\" from %sx%s to %sx%s", imagePath.getFileName(), origWidth, origHeight, targetWidth, targetHeight);
        final double origRatio = ((double)origWidth) / origHeight;
        final double targetRatio = ((double)targetWidth) / targetHeight;
        //https://stackoverflow.com/a/9090575
        if(!almostEqual(origRatio, targetRatio, 0.001)) {
            logger.logError("Warning: Original aspect ratio %s does not match target aspect ratio %s, image will be warped! Consider enabling auto-croping!", origRatio, targetRatio);
        }
        final Image tmpImage = origImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        final BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics g = newImage.getGraphics();
        g.drawImage(tmpImage, 0, 0, null);
        g.dispose();
        return newImage;
    }
    
    private static boolean almostEqual(double a, double b, double eps){
        return Math.abs(a-b)<eps;
    }
    
}
