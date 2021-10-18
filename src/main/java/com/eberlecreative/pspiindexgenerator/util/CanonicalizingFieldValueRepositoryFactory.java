package com.eberlecreative.pspiindexgenerator.util;

public class CanonicalizingFieldValueRepositoryFactory implements FieldValueRepositoryFactory {

	private final FieldNameCanonicalizer fieldNameCanonicalizer;

	public CanonicalizingFieldValueRepositoryFactory(FieldNameCanonicalizer fieldNameCanonicalizer) {
		this.fieldNameCanonicalizer = fieldNameCanonicalizer;
	}

	@Override
	public FieldValueRepository newFieldValueRepository() {
		return new CanonicalizingFieldValueRepository(fieldNameCanonicalizer);
	}

}
