package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.Point;

public interface CropAnchor {

	public Point calculateCropOffset(int oldWidth, int oldHeight, int newWidth, int newHeight);

}
