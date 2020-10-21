package com.eberlecreative.pspiindexgenerator.imagecopier;

import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandler;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifier;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifierFactory;
import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class ImageCopierFactory {

    private float compressionQuality;

    public ImageCopierFactory(float compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    public ImageCopierFactory compressionQuality(float compressionQuality) {
        this.compressionQuality = compressionQuality;
        return this;
    }

    public ImageCopier getImageCopier(Logger logger, ErrorHandler errorHandler, ImageModifierFactory imageModifierFactory) {
        final ImageModifier imageModifier = imageModifierFactory.getImageModifier(logger);
        ImageCopier rootCopier = null;
        if(imageModifier == null) {
            rootCopier = new SimpleImageCopier(logger);
        } else {
            rootCopier = new ImageModifyingCopier(imageModifier, compressionQuality);
        }
        return new UniqueImageNameTrackingImageCopierFilter(rootCopier, errorHandler);
    }
    
}
