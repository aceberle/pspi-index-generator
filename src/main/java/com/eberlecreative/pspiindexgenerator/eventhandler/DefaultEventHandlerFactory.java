package com.eberlecreative.pspiindexgenerator.eventhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class DefaultEventHandlerFactory implements EventHandlerFactory {

    private final boolean strict;

    public DefaultEventHandlerFactory() {
        this(false);
    }

    public DefaultEventHandlerFactory(boolean strict) {
        this.strict = strict;
    }

    @Override
    public EventHandler getEventHandler(Logger logger) {
        return new DefaultEventHandler(logger, strict);
    }
    
}
