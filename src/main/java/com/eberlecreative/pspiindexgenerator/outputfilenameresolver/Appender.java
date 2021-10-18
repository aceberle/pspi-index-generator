package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;

@FunctionalInterface
public interface Appender {

	void append(StringBuilder builder, FieldValueRepository fieldValues);

}
