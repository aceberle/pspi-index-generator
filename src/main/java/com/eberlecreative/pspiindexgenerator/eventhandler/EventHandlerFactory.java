package com.eberlecreative.pspiindexgenerator.eventhandler;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public interface EventHandlerFactory {

	public EventHandler getEventHandler(Logger logger);

}
