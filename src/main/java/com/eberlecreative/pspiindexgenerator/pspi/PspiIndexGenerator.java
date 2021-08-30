package com.eberlecreative.pspiindexgenerator.pspi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.eberlecreative.pspiindexgenerator.datafileparser.DataFileParser;
import com.eberlecreative.pspiindexgenerator.errorhandler.DefaultErrorHandlerFactory;
import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandler;
import com.eberlecreative.pspiindexgenerator.errorhandler.ErrorHandlerFactory;
import com.eberlecreative.pspiindexgenerator.imagecopier.ImageCopier;
import com.eberlecreative.pspiindexgenerator.imagecopier.ImageCopierFactory;
import com.eberlecreative.pspiindexgenerator.imagemodifier.CropAnchor;
import com.eberlecreative.pspiindexgenerator.imagemodifier.CropAnchors;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifierFactory;
import com.eberlecreative.pspiindexgenerator.logger.DefaultLogger;
import com.eberlecreative.pspiindexgenerator.logger.Logger;
import com.eberlecreative.pspiindexgenerator.record.RecordField;
import com.eberlecreative.pspiindexgenerator.record.RecordWriter;
import com.eberlecreative.pspiindexgenerator.record.RecordWriterFactory;
import com.eberlecreative.pspiindexgenerator.record.TabDelimitedRecordWriterFactory;
import com.eberlecreative.pspiindexgenerator.targetfilepath.TargetFilePathResolver;
import com.eberlecreative.pspiindexgenerator.targetfilepath.TargetFilePathResolverFactory;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;
import com.eberlecreative.pspiindexgenerator.util.ResourceUtils;

public class PspiIndexGenerator {

    public static final String DEFAULT_CROP_ANCHOR = "top-middle";

    public static final float DEFAULT_COMPRESSION_QUALITY = 0.9f;

    public static final String DEFAULT_IMAGE_FILE_PATTERN = "(?<firstName>.*)_(?<lastName>.*)\\.jpg";

    public static final String DEFAULT_IMAGE_FOLDER_PATTERN = "(?<grade>[0-9a-zA-Z]+)(?:_Grade)?(?:_(?<homeRoom>.*))?";

    private static final long MAX_IMAGE_SIZE = (long)1E+7;

    private FileUtils fileUtils = FileUtils.getInstance();
    
    private ImageUtils imageUtils = ImageUtils.getInstance();
    
    private ResourceUtils resourceUtils = ResourceUtils.getInstance();

    private Logger logger = new DefaultLogger();

    private ErrorHandlerFactory errorHandlerFactory = new DefaultErrorHandlerFactory();

    private IndexRecordFieldsFactory indexRecordFieldsFactory = new IndexRecordFieldsFactory();

    private RecordWriterFactory indexRecordWriterFactory = new TabDelimitedRecordWriterFactory();

    private Pattern imageFolderPattern = Pattern.compile(DEFAULT_IMAGE_FOLDER_PATTERN);

    private Pattern imageFilePattern = Pattern.compile(DEFAULT_IMAGE_FILE_PATTERN);

    private ImageCopierFactory imageCopierFactory = new ImageCopierFactory(DEFAULT_COMPRESSION_QUALITY);

    private ImageModifierFactory imageModifierFactory = new ImageModifierFactory(CropAnchors.parseCropAnchor(DEFAULT_CROP_ANCHOR));

    private DataFileParser dataFileParser = new DataFileParser();
    
    private TargetFilePathResolverFactory targetFilePathResolverFactory = new TargetFilePathResolverFactory();
    
    private PspiDirectoryValidator validator = new PspiDirectoryValidator();

    private boolean forceOutput;

    private String dataFilePath;

    public static class Builder {

        private PspiIndexGenerator instance = new PspiIndexGenerator();

        public Builder logger(Logger logger) {
            instance.logger = logger;
            return this;
        }

        public Builder verboseLogging(boolean verbose) {
            return logger(new DefaultLogger(verbose));
        }

        public Builder verboseLogging(boolean verbose, PrintStream out, PrintStream err) {
            return logger(new DefaultLogger(verbose, out, err));
        }

        public Builder verboseLogging() {
            return verboseLogging(true);
        }

        public Builder indexRecordWriterFactory(RecordWriterFactory indexRecordWriterFactory) {
            instance.indexRecordWriterFactory = indexRecordWriterFactory;
            return this;
        }

        public Builder errorHandlerFactory(ErrorHandlerFactory errorHandlerFactory) {
            instance.errorHandlerFactory = errorHandlerFactory;
            return this;
        }

        public Builder indexRecordFieldsFactory(IndexRecordFieldsFactory indexRecordFieldsFactory) {
            instance.indexRecordFieldsFactory = indexRecordFieldsFactory;
            return this;
        }

        public Builder dataFile(String dataFilePath) {
            instance.dataFilePath = dataFilePath;
            return this;
        }

        public Builder outputFilePattern(String outputFilePattern) {
            instance.targetFilePathResolverFactory.outputFilePattern(outputFilePattern);
            return this;
        }

        public Builder imageFolderPattern(String imageFolderPattern) {
            return imageFolderPattern(Pattern.compile(imageFolderPattern));
        }

        public Builder imageFilePattern(String imageFilePattern) {
            return imageFilePattern(Pattern.compile(imageFilePattern));
        }

        public Builder imageCopierFactory(ImageCopierFactory imageCopierFactory) {
            instance.imageCopierFactory = imageCopierFactory;
            return this;
        }

        public Builder forceOutput(boolean forceOutput) {
            instance.forceOutput = forceOutput;
            return this;
        }

        public Builder forceOutput() {
            return forceOutput(true);
        }

        public Builder strict(boolean strict) {
            return errorHandlerFactory(new DefaultErrorHandlerFactory(strict));
        }

        public Builder strict() {
            return strict(true);
        }

        public Builder cropImages(CropAnchor cropAnchor) {
            instance.imageModifierFactory.cropAnchor(cropAnchor);
            return this;
        }

        public Builder resizeImages(PspiImageSize imageSize) {
            instance.imageModifierFactory.resizeImages(imageSize);
            return this;
        }

        public Builder compressionQuality(float compressionQuality) {
            instance.imageCopierFactory.compressionQuality(compressionQuality);
            return this;
        }

        public Builder imageFolderPattern(Pattern pattern) {
            instance.imageFolderPattern = pattern;
            return this;
        }

        public Builder imageFilePattern(Pattern pattern) {
            instance.imageFilePattern = pattern;
            return this;
        }

        public PspiIndexGenerator build() {
            return instance;
        }

    }

    public void generate(File inputDirectory, File outputDirectory) throws Exception {
        logger.logInfo("Starting generation...");
        fileUtils.assertIsDirectory(inputDirectory);
        validateAndInitOutputDirectory(outputDirectory);
        final File indexFile = new File(outputDirectory, PspiConstants.INDEX_FILE_NAME);
        final Collection<RecordField> recordFields = indexRecordFieldsFactory.getIndexRecordFields();
        final ErrorHandler errorHandler = errorHandlerFactory.getErrorHandler(logger);
        final TargetFilePathResolver targetFilePathResolver = targetFilePathResolverFactory.getTargetFilePathResolver();
        final ImageCopier imageCopier = imageCopierFactory.getImageCopier(logger, errorHandler, imageModifierFactory);
        final FileProcessingStrategy processingStrategy = dataFilePath == null ? getPathPatternProcessingStrategy() : getDataFileProcessingStrategy();
        try (RecordWriter indexRecordWriter = indexRecordWriterFactory.getRecordWriter(indexFile, recordFields)) {
            indexRecordWriter.writeHeaders();
            processingStrategy.processFiles(indexRecordWriter, inputDirectory, outputDirectory, errorHandler, imageCopier, recordFields, targetFilePathResolver);
            logger.logInfo("Creating COPYRIGHT.TXT file...");
            fileUtils.save(resourceUtils.getResourceAsStream("/"+PspiConstants.COPYRIGHT_FILE_NAME), new File(outputDirectory, PspiConstants.COPYRIGHT_FILE_NAME));
            logger.logInfo("Generation completed!");
        }
        logger.logInfo("Validating output directory...");
        validator.validatePspiDirectory(outputDirectory);
        logger.logInfo("Validation completed!");
    }

    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^(\\d+)\\.(jpg|jpeg|mpg|mpeg|gif|png)$");
    
    private static final Pattern FIRST_LAST_PATTERN = Pattern.compile("^(.+)\\s+([^\\s]+)$");
    
    private static final Pattern LAST_FIRST_PATTERN = Pattern.compile("^([^\\s]+)\\s+(.+)$");
    
    private static final String IMAGES_DIR_NAME = "images";

    private static final String CSV_HEADER_LAST_FIRST = "lastFirst";
    
    private static final String CSV_HEADER_FIRST_LAST = "firstLast";
    
    private FileProcessingStrategy getDataFileProcessingStrategy() {
        return (indexRecordWriter, inputDirectory, outputDirectory, errorHandler, imageCopier, recordFields, targetFilePathResolver) -> {
            final List<Map<String, String>> rawData = dataFileParser.parseDataFile(dataFilePath);
            final Map<Long, Map<String, String>> dataByImageNumber = getDataByImageNumber(rawData, errorHandler);
            final File imageOutputDirectory = new File(outputDirectory, IMAGES_DIR_NAME);
            final String volumeName = outputDirectory.getName();
            for (File file : sort(inputDirectory.listFiles())) {
                if(file.isHidden()) {
                    continue;
                }
                logger.logInfo("Processing path: " + file);
                if (!file.isFile()) {
                    errorHandler.handleError("Expected path to be a file: " + file);
                } else {
                    final String imageFileName = file.getName();
                    final Matcher imageFileMatcher = IMAGE_NAME_PATTERN.matcher(imageFileName);
                    if (!imageFileMatcher.matches()) {
                        errorHandler.handleError("Encountered unexpected file name \"%s\" while processing file: %s", imageFileName, file);
                    } else {
                        final Long imageNumber = Long.parseLong(imageFileMatcher.group(1));
                        final Map<String, String> rowData = dataByImageNumber.get(imageNumber);
                        updateNames(rowData);
                        final Path newImageFilePath = targetFilePathResolver.getTargetFilePath(inputDirectory, imageOutputDirectory, file, rowData);
                        fileUtils.makeParentDirectory(newImageFilePath);
                        imageCopier.copyImage(file.toPath(), newImageFilePath);
                        if(rowData == null) {
                            errorHandler.handleError("Could not find row data for image number %s while processing image file: %s", imageNumber, file);
                        } else {
                            final Map<String, String> indexRecord = new HashMap<>();
                            for (RecordField field : recordFields) {
                                final String fieldName = field.getName();
                                final String valueFromMatchers = rowData.get(fieldName);
                                if (valueFromMatchers != null) {
                                    indexRecord.put(fieldName, valueFromMatchers);
                                }
                            }
                            useAliasValueIfEmpty(indexRecord, IndexRecordFields.HOME_ROOM, IndexRecordFields.GRADE);
                            indexRecord.put(IndexRecordFields.VOLUME_NAME, volumeName);
                            indexRecord.put(IndexRecordFields.IMAGE_FOLDER, IMAGES_DIR_NAME);
                            indexRecord.put(IndexRecordFields.IMAGE_FILE_NAME, newImageFilePath.toFile().getName());
                            indexRecord.put(IndexRecordFields.IMAGE_SIZE, getImageSize(newImageFilePath.toFile(), errorHandler));
                            logger.logInfo("Logging index record: %s", indexRecord);
                            indexRecordWriter.writeRecord(indexRecord);
                        }
                    }
                }
            }
        };
    }
    
    private void updateNames(Map<String, String> rowData) {
        final String lastName = rowData.get(IndexRecordFields.LAST_NAME);
        final String firstName = rowData.get(IndexRecordFields.FIRST_NAME);
        if(StringUtils.isAnyBlank(lastName, firstName)) {
            final String lastFirst = rowData.get(CSV_HEADER_LAST_FIRST);
            final String firstLast = rowData.get(CSV_HEADER_FIRST_LAST);
            if(StringUtils.isNotBlank(lastFirst)) {
                final Matcher matcher = LAST_FIRST_PATTERN.matcher(rowData.get(CSV_HEADER_LAST_FIRST));
                if(matcher.matches()) {
                    rowData.put(IndexRecordFields.LAST_NAME, matcher.group(1));
                    rowData.put(IndexRecordFields.FIRST_NAME, matcher.group(2));
                }
            } else if(StringUtils.isNotBlank(firstLast)) {
                final Matcher matcher = FIRST_LAST_PATTERN.matcher(rowData.get(CSV_HEADER_FIRST_LAST));
                if(matcher.matches()) {
                    rowData.put(IndexRecordFields.FIRST_NAME, matcher.group(1));
                    rowData.put(IndexRecordFields.LAST_NAME, matcher.group(2));
                }
            }
        }
    }

    private static final String IMAGE_NUMBER_HEADER = "imageNumber";

    private Map<Long, Map<String, String>> getDataByImageNumber(List<Map<String, String>> rawData, ErrorHandler errorHandler) {
        final Map<Long, Map<String, String>> results = new HashMap<>();
        for(Map<String, String> data : rawData) {
            final String rawImageNumber = data.get(IMAGE_NUMBER_HEADER);
            if(rawImageNumber == null) {
                errorHandler.handleError("Unable to locate image number in row information: %s", data);
            }
            try {
                final long imageNumber = Long.parseLong(rawImageNumber);
                if(results.containsKey(imageNumber)) {
                    errorHandler.handleError("Image number %s has been defined in multiple rows!", imageNumber);
                } else {
                    results.put(imageNumber, data);
                }
            } catch (NumberFormatException e) {
                errorHandler.handleError("An exception occured while trying to parse image number string \"%s\": %s", rawImageNumber, e.getMessage());
            }
        }
        return results;
    }

    private FileProcessingStrategy getPathPatternProcessingStrategy() {
        return (indexRecordWriter, inputDirectory, outputDirectory, errorHandler, imageCopier, recordFields, targetFilePathResolver) -> {
            final String volumeName = outputDirectory.getName();
            for (File file : sort(inputDirectory.listFiles())) {
                if(file.isHidden()) {
                    continue;
                }
                logger.logInfo("Processing path: " + file);
                final String imageFolderName = file.getName();
                final Matcher imageFolderMatcher = imageFolderPattern.matcher(imageFolderName);
                if (!file.isDirectory()) {
                    errorHandler.handleError("Expected path to be a directory: " + file);
                } else if (!imageFolderMatcher.matches()) {
                    errorHandler.handleError("Encountered unexpected directory name: " + imageFolderName);
                } else {
                    for (File imageFile : sort(file.listFiles())) {
                        logger.logInfo("Processing image file: " + imageFile);
                        final String imageFileName = imageFile.getName();
                        final Matcher imageFileMatcher = imageFilePattern.matcher(imageFileName);
                        if (!imageFile.isFile()) {
                            errorHandler.handleError("Unexpected directory found: " + imageFile);
                        } else if (!imageFileMatcher.matches()) {
                            errorHandler.handleError("Encountered unexpected file name: " + imageFileName);
                        } else {
                            final Map<String, String> fieldValues = getFromMatchers(imageFileMatcher, imageFolderMatcher);
                            final Path newImageFilePath = targetFilePathResolver.getTargetFilePath(inputDirectory, outputDirectory, imageFile, fieldValues);
                            fileUtils.makeParentDirectory(newImageFilePath.toFile());
                            imageCopier.copyImage(imageFile.toPath(), newImageFilePath);
                            final Map<String, String> indexRecord = new HashMap<>();
                            for (RecordField field : recordFields) {
                                final String fieldName = field.getName();
                                final String valueFromMatchers = fieldValues.get(fieldName);
                                if (valueFromMatchers != null) {
                                    indexRecord.put(fieldName, valueFromMatchers);
                                }
                            }
                            useAliasValueIfEmpty(indexRecord, IndexRecordFields.HOME_ROOM, IndexRecordFields.GRADE);
                            indexRecord.put(IndexRecordFields.VOLUME_NAME, volumeName);
                            indexRecord.put(IndexRecordFields.IMAGE_FOLDER, imageFolderName);
                            indexRecord.put(IndexRecordFields.IMAGE_FILE_NAME, imageFileName);
                            indexRecord.put(IndexRecordFields.IMAGE_SIZE, getImageSize(newImageFilePath.toFile(), errorHandler));
                            logger.logInfo("Logging index record: %s", indexRecord);
                            indexRecordWriter.writeRecord(indexRecord);
                        }
                    }
                }
            }
        };
    }

    private void useAliasValueIfEmpty(Map<String, String> indexRecord, String ifValueEmpty, String thenUseValue) {
        if(indexRecord.get(ifValueEmpty) == null) {
            indexRecord.put(ifValueEmpty, indexRecord.get(thenUseValue));
        }
    }

    private void validateAndInitOutputDirectory(File outputDirectory) {
        if (outputDirectory.exists()) {
            if (outputDirectory.isDirectory() && outputDirectory.listFiles().length > 0) {
                if (forceOutput) {
                    logger.logInfo("Cleaning output directory at: " + outputDirectory);
                    fileUtils.cleanDirectory(outputDirectory);
                } else {
                    throw new RuntimeException("Output directory is not empty!: " + outputDirectory);
                }
            } else if (outputDirectory.isFile()) {
                throw new RuntimeException("Output directory path points to a file!: " + outputDirectory);
            }
        }
        outputDirectory.mkdirs();
    }

    private File[] sort(File[] files) {
        Arrays.sort(files, (f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName()));
        return files;
    }
    
    private Map<String, String> getFromMatchers(Matcher...matchers) {
       final Map<String, String> results = new HashMap<>();
       for(Matcher matcher : matchers) {
           final Set<String> names = getNamedGroupCandidates(matcher.pattern().pattern());
           for(String name : names) {
               try {
                   results.put(name, matcher.group(name));
               } catch (IllegalArgumentException e) {
                   // ignore
               }
           }
       }
       return results;
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

    private String getImageSize(File imageFile, ErrorHandler errorHandler) throws IOException {
        final long fileSize = imageFile.length();
        if(fileSize > MAX_IMAGE_SIZE) {
            errorHandler.handleError("Invalid image found! Expected size to be less than %s bytes but found size of %s bytes, image at: %s", MAX_IMAGE_SIZE, fileSize, imageFile);
        }
        final BufferedImage bimg = imageUtils.readImage(imageFile);
        final int width = bimg.getWidth();
        final int height = bimg.getHeight();
        if (width == 320 && height == 400) {
            return "SMALL";
        } else if (width == 640 && height == 800) {
            return "LARGE";
        }
        final double ratio = ((double) width) / height;
        if (ratio != 0.8) {
            errorHandler.handleError("Invalid image found! Expected aspect ratio of 0.8 but found %spx / %spx = %s, image at: %s", width, height, ratio, imageFile);
        }
        return "OTHER";
    }

}
