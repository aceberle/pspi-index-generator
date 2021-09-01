package com.eberlecreative.pspiindexgenerator.pspi.generator;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.regex.Pattern;

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
import com.eberlecreative.pspiindexgenerator.outputfilenameresolver.OutputFileNameResolver;
import com.eberlecreative.pspiindexgenerator.outputfilenameresolver.OutputFileNameResolverFactory;
import com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing.DataFileBasedDirectoryProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing.DirectoryProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.directoryprocessing.FilePathPatternBasedDirectoryProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.FileProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.fileprocessing.PspiCopyAndWriteRecordFileProcessor;
import com.eberlecreative.pspiindexgenerator.pspi.util.IndexRecordFieldsFactory;
import com.eberlecreative.pspiindexgenerator.pspi.util.PspiConstants;
import com.eberlecreative.pspiindexgenerator.pspi.util.PspiImageSize;
import com.eberlecreative.pspiindexgenerator.pspi.validation.PspiDirectoryValidator;
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

    public static final String DEFAULT_IMAGE_FOLDER_PATTERN = "(?<grade>[0-9a-zA-Z]+)(?:_Grade)?(?:_(?<homeRoom>.*))?";

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
    
    private OutputFileNameResolverFactory outputFileNameResolverFactory = new OutputFileNameResolverFactory();
    
    private PspiDirectoryValidator validator = new PspiDirectoryValidator();

    private boolean forceOutput;

    private File dataFilePath;

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

        public Builder dataFile(File dataFilePath) {
            instance.dataFilePath = dataFilePath;
            return this;
        }

        public Builder outputFilePattern(String outputFilePattern) {
            instance.outputFileNameResolverFactory.outputFilePattern(outputFilePattern);
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
        final OutputFileNameResolver outputFileNameResolver = outputFileNameResolverFactory.getOutputFileNameResolver();
        final ImageCopier imageCopier = imageCopierFactory.getImageCopier(logger, errorHandler, imageModifierFactory);
        final DirectoryProcessor processingStrategy = getDirectoryProcessingStrategy(errorHandler);
        try (RecordWriter indexRecordWriter = indexRecordWriterFactory.getRecordWriter(indexFile, recordFields)) {
            indexRecordWriter.writeHeaders();
            final FileProcessor fileProcessor = new PspiCopyAndWriteRecordFileProcessor(logger, errorHandler, fileUtils, imageCopier, outputFileNameResolver, imageUtils, indexRecordWriter, recordFields);
            processingStrategy.processDirectory(inputDirectory, outputDirectory, recordFields, fileProcessor);
            logger.logInfo("Creating COPYRIGHT.TXT file...");
            fileUtils.save(resourceUtils.getResourceAsStream("/"+PspiConstants.COPYRIGHT_FILE_NAME), new File(outputDirectory, PspiConstants.COPYRIGHT_FILE_NAME));
            logger.logInfo("Generation completed!");
        }
        logger.logInfo("Validating output directory...");
        validator.validatePspiDirectory(outputDirectory);
        logger.logInfo("Validation completed!");
    }

    private DirectoryProcessor getDirectoryProcessingStrategy(ErrorHandler errorHandler) {
        if(dataFilePath == null) {
            return new FilePathPatternBasedDirectoryProcessor(fileUtils, logger, errorHandler, imageFolderPattern, imageFilePattern);
        }
        return new DataFileBasedDirectoryProcessor(fileUtils, logger, errorHandler, new DataFileParser(), dataFilePath);
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

}
