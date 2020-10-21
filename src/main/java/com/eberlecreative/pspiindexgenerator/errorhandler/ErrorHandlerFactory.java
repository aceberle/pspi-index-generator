package com.eberlecreative.pspiindexgenerator.errorhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public interface ErrorHandlerFactory {
    
    public ErrorHandler getErrorHandler(Logger logger);

}
