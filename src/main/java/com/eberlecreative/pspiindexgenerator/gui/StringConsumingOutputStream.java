package com.eberlecreative.pspiindexgenerator.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Adapted from: https://stackoverflow.com/a/10873260
 *
 */
public class StringConsumingOutputStream extends OutputStream {

	private final Consumer<String> stringConsumer;
	private final StringBuilder sb = new StringBuilder();

	public StringConsumingOutputStream(Consumer<String> stringConsumer) {
		this.stringConsumer = stringConsumer;
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	@Override
	public void write(int b) throws IOException {
		if (b == '\r')
			return;
		if (b == '\n') {
			final String text = sb.toString() + "\n";
			stringConsumer.accept(text);
			sb.setLength(0);
			return;
		}
		sb.append((char) b);
	}
}
