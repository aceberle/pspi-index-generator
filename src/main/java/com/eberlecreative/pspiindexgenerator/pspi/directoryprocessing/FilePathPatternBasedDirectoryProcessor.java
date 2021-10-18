package com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eberlecreative.pspiindexgenerator.eventhandler.EventHandler;
import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.FileProcessor;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;
import com.eberlecreative.pspiindexgenerator.util.FieldValueRepositoryFactory;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public class FilePathPatternBasedDirectoryProcessor implements DirectoryProcessor {

    private final FileUtils fileUtils;
    
    private final EventHandler eventHandler;
    
    private final Pattern imageFolderPattern;
    
    private final Pattern imageFilePattern;
    
    private final FieldValueRepositoryFactory fieldValueRepositoryFactory;
    
    public FilePathPatternBasedDirectoryProcessor(FileUtils fileUtils, EventHandler eventHandler, Pattern imageFolderPattern, Pattern imageFilePattern, FieldValueRepositoryFactory fieldValueRepositoryFactory) {
        this.fileUtils = fileUtils;
        this.eventHandler = eventHandler;
        this.imageFolderPattern = imageFolderPattern;
        this.imageFilePattern = imageFilePattern;
        this.fieldValueRepositoryFactory = fieldValueRepositoryFactory;
    }

    @Override
    public void processDirectory(File inputDirectory, File outputDirectory, Collection<RecordField> recordFields, FileProcessor fileProcessor) throws IOException {
        for (File imageFolder : fileUtils.sort(inputDirectory.listFiles())) {
            if(imageFolder.isHidden()) {
                continue;
            }
            eventHandler.info("Processing path: " + imageFolder);
            final String imageFolderName = imageFolder.getName();
            final Matcher imageFolderMatcher = imageFolderPattern.matcher(imageFolderName);
            if (!imageFolder.isDirectory()) {
                eventHandler.error("Expected path to be a directory: " + imageFolder);
            } else if (!imageFolderMatcher.matches()) {
                eventHandler.error("Encountered unexpected directory name: " + imageFolderName);
            } else {
                for (File imageFile : fileUtils.sort(imageFolder.listFiles())) {
                    eventHandler.info("Processing image file: " + imageFile);
                    final String imageFileName = imageFile.getName();
                    final Matcher imageFileMatcher = imageFilePattern.matcher(imageFileName);
                    if (!imageFile.isFile()) {
                        eventHandler.error("Unexpected directory found: " + imageFile);
                    } else if (!imageFileMatcher.matches()) {
                        eventHandler.error("Encountered unexpected file name: " + imageFileName);
                    } else {
                        final FieldValueRepository fieldValues = getFromMatchers(imageFileMatcher, imageFolderMatcher);
                        fileProcessor.processFile(inputDirectory, outputDirectory, imageFolder.getName(), imageFile, fieldValues);
                    }
                }
            }
        }
    }
    
	private FieldValueRepository getFromMatchers(Matcher... matchers) {
		final FieldValueRepository results = fieldValueRepositoryFactory.newFieldValueRepository();
		for (Matcher matcher : matchers) {
			final Set<String> names = getNamedGroupCandidates(matcher.pattern().pattern());
			for (String name : names) {
				final String value = getValue(matcher, name);
				if (value != null) {
					final String existingValue = results.get(name);
					if(existingValue != null) {
						throw new RuntimeException(String.format("Image path already contains value for key '%s', existing value='%s', new value='%s'", name, existingValue, value));
					}
					results.put(name, value);
				}
			}
		}
		return results;
	}

	private String getValue(Matcher matcher, String name) {
		String value = null;
		try {
			value = matcher.group(name);
		} catch (IllegalArgumentException e) {
			// ignore
		}
		return value;
	}
    
    private static final Pattern NAMED_GROUPS = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
    
    private static Set<String> getNamedGroupCandidates(String regex) {
        final Set<String> namedGroups = new TreeSet<String>();
        final Matcher m = NAMED_GROUPS.matcher(regex);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }

}
