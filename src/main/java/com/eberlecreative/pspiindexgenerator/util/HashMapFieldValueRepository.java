package com.eberlecreative.pspiindexgenerator.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HashMapFieldValueRepository implements FieldValueRepository {

	private Map<String, String> map = new HashMap<>();

	@Override
	public void put(String fieldName, String fieldValue) {
		map.put(fieldName, fieldValue);
	}

	@Override
	public String get(String fieldName) {
		return map.get(fieldName);
	}

	@Override
	public Collection<String> values() {
		return map.values();
	}

}