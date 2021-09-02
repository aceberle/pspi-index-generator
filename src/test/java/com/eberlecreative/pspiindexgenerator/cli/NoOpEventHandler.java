package com.eberlecreative.pspiindexgenerator.cli;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;

public class NoOpEventHandler implements EventHandler {

    @Override
    public void info(String message, Object... objects) {
        // do nothing
    }

    @Override
    public void error(String message, Object... objects) {
        // do nothing
    }

}
