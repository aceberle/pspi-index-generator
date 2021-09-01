package com.eberlecreative.pspiindexgenerator.outputfilenameresolver;

import java.io.File;
import java.util.Map;

public class SameNameOutputFileNameResolver implements OutputFileNameResolver {

    @Override
    public String resolveOutputFileName(File imageFile, Map<String, String> fieldValues) {
        return imageFile.getName();
    }

}
