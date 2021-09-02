package com.eberlecreative.pspiindexgenerator.pspi.generator;

import java.io.File;

public class OutputDirectoryIsNotEmptyException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 285294753829382301L;

    public OutputDirectoryIsNotEmptyException(File outputDirectory) {
        super(String.format("Output directory is not empty!: %s", outputDirectory));
    }

}
