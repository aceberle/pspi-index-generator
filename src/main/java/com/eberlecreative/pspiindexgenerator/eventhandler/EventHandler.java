package com.eberlecreative.pspiindexgenerator.eventhandler;

public interface EventHandler {
    
    public void info(String message, Object...objects);
    
    public void error(String message, Object...objects);

}
