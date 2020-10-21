package com.eberlecreative.pspiindexgenerator.errorhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class DefaultErrorHandlerFactory implements ErrorHandlerFactory {

    private final boolean strict;

    public DefaultErrorHandlerFactory() {
        this(false);
    }

    public DefaultErrorHandlerFactory(boolean strict) {
        this.strict = strict;
    }

    @Override
    public ErrorHandler getErrorHandler(Logger logger) {
        return new DefaultErrorHandler(logger, strict);
    }
    
}
