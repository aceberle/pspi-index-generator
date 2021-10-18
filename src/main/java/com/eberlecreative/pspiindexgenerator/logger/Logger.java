package com.eberlecreative.pspiindexgenerator.logger;

public interface Logger {

	public void info(String message, Object... objects);

	public void error(String message, Object... objects);

}
