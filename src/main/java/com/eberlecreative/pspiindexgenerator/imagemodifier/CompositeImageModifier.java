package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

public class CompositeImageModifier implements ImageModifier {

    private final List<ImageModifier> imageModifiers;

    public CompositeImageModifier(List<ImageModifier> imageModifiers) {
        this.imageModifiers = imageModifiers;
    }

    @Override
    public BufferedImage modifyImage(Path imagePath, BufferedImage origImage) {
        BufferedImage image = origImage;
        for(ImageModifier imageModifier : imageModifiers) {
            image = imageModifier.modifyImage(imagePath, image);
        }
        return image;
    }

}
