package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.util.Set;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifier;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifierFactory;

public class ImageCopierFactory {

    private float compressionQuality;

    public ImageCopierFactory(float compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    public ImageCopierFactory compressionQuality(float compressionQuality) {
        this.compressionQuality = compressionQuality;
        return this;
    }

    public ImageCopier getImageCopier(EventHandler eventHandler, ImageModifierFactory imageModifierFactory, Set<String> processedImages) {
        final ImageModifier imageModifier = imageModifierFactory.getImageModifier(eventHandler);
        ImageCopier rootCopier = null;
        if(imageModifier == null) {
            rootCopier = new SimpleImageCopier(eventHandler);
        } else {
            rootCopier = new ImageModifyingCopier(imageModifier, compressionQuality);
        }
        return new UniqueImageNameTrackingImageCopierFilter(rootCopier, eventHandler, processedImages);
    }
    
}
