package com.eberlecreative.pspiindexgenerator.eventhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class DefaultEventHandler implements EventHandler {

    private final boolean strict;

    private final Logger logger;

    public DefaultEventHandler(Logger logger) {
        this(logger, false);
    }

    public DefaultEventHandler(Logger logger, boolean strict) {
        this.strict = strict;
        this.logger = logger;
    }

    @Override
    public void error(String message, Object...objects) {
        logger.error(message, objects);
        if(strict) {
            final String fullMessage = String.format(message, objects);
            throw new StrictRuleViolationException(fullMessage);
        }
    }

    @Override
    public void info(String message, Object... objects) {
        logger.info(message, objects);
    }
    
}
