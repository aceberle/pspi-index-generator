package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifier;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;

public class ImageModifyingCopier implements ImageCopier {

    private ImageUtils imageUtils = ImageUtils.getInstance();

    private final ImageModifier imageModifier;

    private final float compressionQuality;

    public ImageModifyingCopier(ImageModifier imageModifier, float compressionQuality) {
        this.imageModifier = imageModifier;
        this.compressionQuality = compressionQuality;
    }

    @Override
    public void copyImage(File sourceFile, File targetFile) throws IOException {
        BufferedImage image = imageUtils.readImage(sourceFile);
        image = imageModifier.modifyImage(sourceFile, image);
        imageUtils.saveImageCopyMetaData(image, sourceFile, targetFile, compressionQuality);
    }
    
}
