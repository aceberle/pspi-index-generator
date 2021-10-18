package com.eberlecreative.pspiindexgenerator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class DefaultFieldNameCanonicalizer implements FieldNameCanonicalizer {

	private static final Pattern VALUES_TO_KEEP = Pattern.compile("[a-z0-9]+", Pattern.CASE_INSENSITIVE);

	@Override
	public String canonicalizeFieldName(String value) {
		if (value == null) {
			throw new InvalidFieldNameException(getExceptionMessage(value));
		}
		final StringBuilder builder = new StringBuilder();
		final Matcher matcher = VALUES_TO_KEEP.matcher(value);
		while (matcher.find()) {
			builder.append(matcher.group().toLowerCase());
		}
		final String canonicalizedValue = builder.toString();
		if (StringUtils.isBlank(canonicalizedValue)) {
			throw new InvalidFieldNameException(getExceptionMessage(value));
		}
		return canonicalizedValue;

	}

	private String getExceptionMessage(String value) {
		return String.format("Field name \"%s\" is invalid!", value);
	}

}
