package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.io.File;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;

public interface OutputFileNameResolver {

	public String resolveOutputFileName(File imageFile, FieldValueRepository fieldValues);

}
