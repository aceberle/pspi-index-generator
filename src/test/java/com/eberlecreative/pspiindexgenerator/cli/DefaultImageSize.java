package com.eberlecreative.pspiindexgenerator.cli;

import com.eberlecreative.pspiindexgenerator.imagemodifier.AbstractImageSize;

public class DefaultImageSize extends AbstractImageSize {

	private final int width;

	private final int height;

	public DefaultImageSize(int width, int height) {
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

}
