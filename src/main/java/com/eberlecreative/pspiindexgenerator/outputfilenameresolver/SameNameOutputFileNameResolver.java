package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.io.File;

import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;

public class SameNameOutputFileNameResolver implements OutputFileNameResolver {

    @Override
    public String resolveOutputFileName(File imageFile, FieldValueRepository fieldValues) {
        return imageFile.getName();
    }

}
