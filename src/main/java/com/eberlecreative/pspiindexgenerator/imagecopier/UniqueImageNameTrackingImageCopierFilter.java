package com.eberlecreative.pspiindexgenerator.imagecopier;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;

public class UniqueImageNameTrackingImageCopierFilter extends ImageCopierFilter {

	private final EventHandler eventHandler;

	private final Set<String> processedImageNames;

	public UniqueImageNameTrackingImageCopierFilter(ImageCopier source, EventHandler eventHandler,
			Set<String> processedImageNames) {
		super(source);
		this.eventHandler = eventHandler;
		this.processedImageNames = processedImageNames == null ? new HashSet<>() : new HashSet<>(processedImageNames);
	}

	@Override
	public void copyImage(File sourceFile, File targetFile) throws IOException {
		final String name = sourceFile.getName();
		if (processedImageNames.contains(name)) {
			eventHandler.error(
					"Image with name \"%s\" has already been processed!  PSPI Guidelines specify that image names should be unique!",
					name);
		}
		processedImageNames.add(name);
		super.copyImage(sourceFile, targetFile);
	}

}