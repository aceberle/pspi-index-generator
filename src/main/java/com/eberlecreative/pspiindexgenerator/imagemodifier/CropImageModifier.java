package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;

public class CropImageModifier implements ImageModifier {

	private final AspectRatioProvider targetAspectRatioProvider;

	private final CropAnchor cropAnchor;

	private final EventHandler eventHandler;

	public CropImageModifier(AspectRatioProvider targetAspectRatioProvider, CropAnchor cropAnchor,
			EventHandler eventHandler) {
		this.targetAspectRatioProvider = targetAspectRatioProvider;
		this.cropAnchor = cropAnchor;
		this.eventHandler = eventHandler;
	}

	@Override
	public BufferedImage modifyImage(File imageFile, BufferedImage origImage) {
		final int origWidth = origImage.getWidth();
		final int origHeight = origImage.getHeight();
		final double origAspectRatio = ((double) origWidth) / origHeight;
		int newWidth = origWidth;
		int newHeight = origHeight;
		final double targetAspectRatio = targetAspectRatioProvider.getAspectRatio();
		if (origAspectRatio > targetAspectRatio) {
			newWidth = (int) Math.round(origHeight * targetAspectRatio);
		} else if (origAspectRatio < targetAspectRatio) {
			newHeight = (int) Math.round(origWidth / targetAspectRatio);
		} else {
			eventHandler.info("Skipping center-crop for image \"%s\"", imageFile.getName());
			return origImage;
		}
		final Point offsets = cropAnchor.calculateCropOffset(origWidth, origHeight, newWidth, newHeight);
		final int x = (int) offsets.getX();
		final int y = (int) offsets.getY();
		eventHandler.info("Center-cropping image \"%s\" from %sx%s to %sx%s with offset X = %s and offset Y = %s",
				imageFile.getName(), origWidth, origHeight, newWidth, newHeight, x, y);
		return origImage.getSubimage(x, y, newWidth, newHeight);
	}

}
