package com.eberlecreative.pspiindexgenerator.pspi.util;

import com.eberlecreative.pspiindexgenerator.imagemodifier.AspectRatioCalculator;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageSize;

public enum PspiImageSize implements ImageSize {
	SMALL(320, 400), LARGE(640, 800);

	private final int width;
	private final int height;

	private PspiImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public double getAspectRatio() {
		return AspectRatioCalculator.getInstance().calculateAspectRatio(this);
	}

	public static PspiImageSize fromString(String input) {
		for (PspiImageSize size : values()) {
			if (size.name().equalsIgnoreCase(input)) {
				return size;
			}
		}
		throw new IllegalArgumentException(
				String.format("Expected input value \"%s\" to match one of: %s", input, values()));
	}
}
