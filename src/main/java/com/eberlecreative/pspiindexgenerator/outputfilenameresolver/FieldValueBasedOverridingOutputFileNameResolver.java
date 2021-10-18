package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.io.File;
import java.util.List;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;

public class FieldValueBasedOverridingOutputFileNameResolver implements OutputFileNameResolver {

	private final List<Appender> appenders;

	public FieldValueBasedOverridingOutputFileNameResolver(List<Appender> appenders) {
		this.appenders = appenders;
	}

	@Override
	public String resolveOutputFileName(File imageFile, FieldValueRepository fieldValues) {
		final StringBuilder newFileNameBuilder = new StringBuilder();
		appenders.forEach(appender -> appender.append(newFileNameBuilder, fieldValues));
		return newFileNameBuilder.toString();
	}

}
