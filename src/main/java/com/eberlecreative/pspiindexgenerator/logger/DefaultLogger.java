package com.eberlecreative.pspiindexgenerator.logger;

import java.io.PrintStream;

public class DefaultLogger implements Logger {

    private final boolean verbose;
    
    private final PrintStream out;
    
    private final PrintStream err;

    public DefaultLogger() {
        this(false);
    }

    public DefaultLogger(boolean verbose) {
        this(verbose, System.out, System.err);
    }

    public DefaultLogger(boolean verbose, PrintStream out, PrintStream err) {
        this.verbose = verbose;
        this.out = out;
        this.err = err;
    }

    @Override
    public void logInfo(String message, Object... objects) {
        if(verbose) {
            out.println(String.format(message, objects));
        }
    }

    @Override
    public void logError(String message, Object... objects) {
        err.println(String.format(message, objects));
    }
    
}
