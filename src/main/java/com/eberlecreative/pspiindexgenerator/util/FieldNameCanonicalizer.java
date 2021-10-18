package com.eberlecreative.pspiindexgenerator.util;

@FunctionalInterface
public interface FieldNameCanonicalizer {

	String canonicalizeFieldName(String value);

}