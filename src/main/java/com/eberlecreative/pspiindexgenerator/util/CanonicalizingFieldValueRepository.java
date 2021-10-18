package com.eberlecreative.pspiindexgenerator.util;

import java.util.Collection;

public class CanonicalizingFieldValueRepository extends HashMapFieldValueRepository {

	private final FieldNameCanonicalizer fieldNameCanonicalizer;

	public CanonicalizingFieldValueRepository(FieldNameCanonicalizer fieldNameCanonicalizer) {
		this.fieldNameCanonicalizer = fieldNameCanonicalizer;
	}

	@Override
	public void put(String fieldName, String fieldValue) {
		super.put(fieldNameCanonicalizer.canonicalizeFieldName(fieldName), fieldValue);
	}

	@Override
	public String get(String fieldName) {
		return super.get(fieldNameCanonicalizer.canonicalizeFieldName(fieldName));
	}

	@Override
	public Collection<String> values() {
		return super.values();
	}

}
