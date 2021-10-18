package com.eberlecreative.pspiindexgenerator.util;

import java.io.InputStream;

public class ResourceUtils {

	private static ResourceUtils instance = new ResourceUtils();

	public static ResourceUtils getInstance() {
		return instance;
	}

	public InputStream getResourceAsStream(String resourceName) {
		return this.getClass().getResourceAsStream(resourceName);
	}

}
