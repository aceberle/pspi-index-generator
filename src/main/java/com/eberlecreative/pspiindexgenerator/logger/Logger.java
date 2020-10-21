package com.eberlecreative.pspiindexgenerator.logger;

public interface Logger {
    
    public void logInfo(String message, Object...objects);

    public void logError(String message, Object...objects);

}
