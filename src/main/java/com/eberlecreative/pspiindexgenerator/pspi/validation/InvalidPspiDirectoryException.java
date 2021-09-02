package com.eberlecreative.pspiindexgenerator.pspi.validation;

public class InvalidPspiDirectoryException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -668126077827978757L;

    public InvalidPspiDirectoryException(String message) {
        super(message);
    }

    public InvalidPspiDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
