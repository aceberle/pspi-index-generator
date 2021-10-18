package com.eberlecreative.pspiindexgenerator.logger;

import java.io.PrintStream;

public class StreamLogger implements Logger {

	private final boolean verbose;

	private final PrintStream out;

	private final PrintStream err;

	public StreamLogger() {
		this(false);
	}

	public StreamLogger(boolean verbose) {
		this(verbose, System.out, System.err);
	}

	public StreamLogger(boolean verbose, PrintStream out, PrintStream err) {
		this.verbose = verbose;
		this.out = out;
		this.err = err;
	}

	@Override
	public void info(String message, Object... objects) {
		if (verbose) {
			out.println(String.format(message, objects));
		}
	}

	@Override
	public void error(String message, Object... objects) {
		err.println(String.format(message, objects));
	}

}
