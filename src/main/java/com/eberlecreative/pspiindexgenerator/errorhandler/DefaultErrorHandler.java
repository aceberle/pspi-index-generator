package com.eberlecreative.pspiindexgenerator.errorhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class DefaultErrorHandler implements ErrorHandler {

    private final boolean strict;

    private final Logger logger;

    public DefaultErrorHandler(Logger logger) {
        this(logger, false);
    }

    public DefaultErrorHandler(Logger logger, boolean strict) {
        this.strict = strict;
        this.logger = logger;
    }

    @Override
    public void handleError(String message, Object...objects) {
        if(strict) {
            final String fullMessage = String.format(message, objects);
            throw new RuntimeException(fullMessage);
        }
        logger.logError(message, objects);
    }
    
}
