package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;

public class ResizeImageModifier implements ImageModifier {

    private final ImageSize targetSize;

    private final EventHandler eventHandler;

    public ResizeImageModifier(ImageSize targetSize, EventHandler eventHandler) {
        this.targetSize = targetSize;
        this.eventHandler = eventHandler;
    }

    @Override
    public BufferedImage modifyImage(File imageFile, BufferedImage origImage) {
        final int origWidth = origImage.getWidth();
        final int origHeight = origImage.getHeight();
        final int targetWidth = targetSize.getWidth();
        final int targetHeight = targetSize.getHeight();
        if(origWidth == targetWidth && origHeight == targetHeight) {
            eventHandler.info("Skipping scaling for image \"%s\"", imageFile.getName());
            return origImage;
        }
        eventHandler.info("Resizing image \"%s\" from %sx%s to %sx%s", imageFile.getName(), origWidth, origHeight, targetWidth, targetHeight);
        final double origRatio = ((double)origWidth) / origHeight;
        final double targetRatio = ((double)targetWidth) / targetHeight;
        //https://stackoverflow.com/a/9090575
        if(!almostEqual(origRatio, targetRatio, 0.001)) {
            eventHandler.error("Warning: Original aspect ratio %s does not match target aspect ratio %s, image will be warped! Consider enabling auto-croping!", origRatio, targetRatio);
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
