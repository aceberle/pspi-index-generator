package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface ImageModifier {

    public BufferedImage modifyImage(Path imagePath, BufferedImage image);

}
