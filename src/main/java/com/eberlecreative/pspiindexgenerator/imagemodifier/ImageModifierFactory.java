package com.eberlecreative.pspiindexgenerator.imagemodifier;

import java.util.ArrayList;
import java.util.List;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class ImageModifierFactory  {
    
    private ImageSize targetSize;

    private CropAnchor cropAnchor;
    
    public ImageModifierFactory(CropAnchor cropAnchor) {
        this.cropAnchor = cropAnchor;
    }

    public ImageModifierFactory cropAnchor(CropAnchor cropAnchor) {
        this.cropAnchor = cropAnchor;
        return this;
    }

    public ImageModifierFactory resizeImages(ImageSize imageSize) {
        this.targetSize = imageSize;
        return this;
    }

    public ImageModifier getImageModifier(Logger logger) {
        final List<ImageModifier> modifiers = new ArrayList<>();
        if(targetSize != null) {
            modifiers.add(new CropImageModifier(targetSize, cropAnchor, logger));
            modifiers.add(new ResizeImageModifier(targetSize, logger));
        }
        if(modifiers.isEmpty()) {
            return null;
        }
        return new CompositeImageModifier(modifiers);
    }

}
