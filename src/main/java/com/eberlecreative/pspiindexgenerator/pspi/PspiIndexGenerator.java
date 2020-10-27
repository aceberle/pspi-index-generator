package com.eberlecreative.pspiindexgenerator.pspi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.eberlecreative.pspiindexgenerator.util.FileUtils;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;
import com.eberlecreative.pspiindexgenerator.util.ResourceUtils;

public class PspiIndexGenerator {

    public static final String DEFAULT_CROP_ANCHOR = "top-middle";

    public static final float DEFAULT_COMPRESSION_QUALITY = 0.9f;

    public static final String DEFAULT_IMAGE_FILE_PATTERN = "(?<firstName>.*)_(?<lastName>.*)\\.jpg";

    public static final String DEFAULT_IMAGE_FOLDER_PATTERN = "(?<grade>[0-9pPkK]+)(?:_Grade)?(?:_(?<homeRoom>.*))?";

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

    private boolean forceOutput;

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
        validateInputDirectory(inputDirectory);
        validateAndInitOutputDirectory(outputDirectory);
        final String volumeName = outputDirectory.getName();
        final File indexFile = new File(outputDirectory, "INDEX.TXT");
        final Collection<RecordField> recordFields = indexRecordFieldsFactory.getIndexRecordFields();
        final ErrorHandler errorHandler = errorHandlerFactory.getErrorHandler(logger);
        final ImageCopier imageCopier = imageCopierFactory.getImageCopier(logger, errorHandler, imageModifierFactory);
        try (RecordWriter indexRecordWriter = indexRecordWriterFactory.getRecordWriter(indexFile, recordFields)) {
            indexRecordWriter.writeHeaders();
            for (File imageFolder : sort(inputDirectory.listFiles())) {
                if(imageFolder.isHidden()) {
                    continue;
                }
                logger.logInfo("Processing image folder: " + imageFolder);
                final String imageFolderName = imageFolder.getName();
                final Matcher imageFolderMatcher = imageFolderPattern.matcher(imageFolderName);
                if (!imageFolder.isDirectory()) {
                    errorHandler.handleError("Expected path to be a directory: " + imageFolder);
                } else if (!imageFolderMatcher.matches()) {
                    errorHandler.handleError("Encountered unexpected directory name: " + imageFolderName);
                } else {
                    for (File imageFile : sort(imageFolder.listFiles())) {
                        logger.logInfo("Processing image file: " + imageFile);
                        final String imageFileName = imageFile.getName();
                        final Matcher imageFileMatcher = imageFilePattern.matcher(imageFileName);
                        if (!imageFile.isFile()) {
                            errorHandler.handleError("Unexpected directory found: " + imageFile);
                        } else if (!imageFileMatcher.matches()) {
                            errorHandler.handleError("Encountered unexpected file name: " + imageFileName);
                        } else {
                            final Path newImageFilePath = fileUtils.getRelativePath(inputDirectory, outputDirectory, imageFile);
                            fileUtils.makeParentDirectory(newImageFilePath.toFile());
                            imageCopier.copyImage(imageFile.toPath(), newImageFilePath);
                            final Map<String, String> indexRecord = new HashMap<>();
                            for (RecordField field : recordFields) {
                                final String fieldName = field.getName();
                                final String valueFromMatchers = getFromMatchers(fieldName, imageFileMatcher, imageFolderMatcher);
                                if (valueFromMatchers != null) {
                                    indexRecord.put(fieldName, valueFromMatchers);
                                }
                            }
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
            logger.logInfo("Creating COPYRIGHT.TXT file...");
            fileUtils.save(resourceUtils.getResourceAsStream("/COPYRIGHT.TXT"), new File(outputDirectory, "COPYRIGHT.TXT"));
            logger.logInfo("Generation completed!");
        }
    }

    private void validateInputDirectory(File inputDirectory) {
        if (!inputDirectory.isDirectory()) {
            throw new RuntimeException("Expected path to exist: " + inputDirectory);
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

    private String getFromMatchers(String fieldName, Matcher... matchers) {
        for (Matcher matcher : matchers) {
            try {
                return matcher.group(fieldName);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return null;
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
