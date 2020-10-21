package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.IOException;
import java.nio.file.Path;

import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifier;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;

import java.awt.image.BufferedImage;

public class ImageModifyingCopier implements ImageCopier {

    private ImageUtils imageUtils = ImageUtils.getInstance();

    private final ImageModifier imageModifier;

    private final float compressionQuality;

    public ImageModifyingCopier(ImageModifier imageModifier, float compressionQuality) {
        this.imageModifier = imageModifier;
        this.compressionQuality = compressionQuality;
    }

    @Override
    public void copyImage(Path sourcePath, Path targetPath) throws IOException {
        BufferedImage image = imageUtils.readImage(sourcePath.toFile());
        image = imageModifier.modifyImage(sourcePath, image);
        imageUtils.saveImageCopyMetaData(image, sourcePath, targetPath, compressionQuality);
    }
    
}
