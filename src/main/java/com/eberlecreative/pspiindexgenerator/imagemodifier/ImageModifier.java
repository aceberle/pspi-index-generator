package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageModifier {

    public BufferedImage modifyImage(File imageFile, BufferedImage image);

}
