package com.eberlecreative.pspiindexgenerator.util;

import java.util.Collection;

public interface FieldValueRepository {

	public void put(String fieldName, String fieldValue);

	public String get(String fieldName);

	public Collection<String> values();

}
