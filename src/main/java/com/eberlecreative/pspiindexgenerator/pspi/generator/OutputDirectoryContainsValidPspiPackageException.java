package com.eberlecreative.pspiindexgenerator.pspi.generator;

import java.io.File;

public class OutputDirectoryContainsValidPspiPackageException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6681105236591882852L;

    public OutputDirectoryContainsValidPspiPackageException(File outputDirectory) {
        super(String.format("Output directory already contains a valid PSPI package!: %s", outputDirectory));
    }

}
