package com.eberlecreative.pspiindexgenerator.cli;

import com.eberlecreative.pspiindexgenerator.logger.Logger;

public class NoOpLogger implements Logger {

    @Override
    public void logInfo(String message, Object... objects) {
        // do nothing
    }

    @Override
    public void logError(String message, Object... objects) {
        // do nothing
    }

}
