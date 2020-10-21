package com.eberlecreative.pspiindexgenerator.cli;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eberlecreative.pspiindexgenerator.pspi.PspiImageSize;

public class PspiIndexGeneratorCLIIntegrationTest {
    
    private static final File TEST_INPUT_DIR_ROOT = new File("target/test-input-directories/");
    
    private static final File TEST_OUTPUT_DIR_ROOT = new File("target/test-output-directories/");
    
    private TestDataGenerator testDataGenerator;
    
    private File inputDirectory;

    private File outputDirectory;
    
    private File expectedInputFile;

    private File actualInputFile;

    private List<String> commandArguments;
    
    private Exception thrownException;
    
    @Before
    public void init() {
        testDataGenerator = new TestDataGenerator();
        inputDirectory = null;
        commandArguments = new ArrayList<>();
        commandArguments.add("-f");
        commandArguments.add("-v");
        commandArguments.add("-s");
    }
    
    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndHasLargeImage() {
        givenDirectoryName("volume1");
        givenLargeImage();
        whenMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }

    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedAndPictureHasInvalidAspectRatio() {
        givenDirectoryName("volume2");
        givenImageWithInvalidAspectRatio();
        whenMainIsExecuted();
        thenActualIndexFileContentsMatchExpected();
    }

    @Test
    public void specifyThatExceptionIsThrownWhenMainIsExecutedAndPictureHasInvalidAspectRatio() {
        givenDirectoryName("volume2");
        givenImageWithInvalidAspectRatio();
        whenMainIsExecuted();
        thenExceptionIsThrown(RuntimeException.class, String.format("Invalid image found! Expected aspect ratio of 0.8 but found 850px / 1000px = 0.85, image at: %2$s%1$s11_Smith%1$sJohn_Doe.jpg", File.separator, outputDirectory.getAbsolutePath()));
    }

    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndPictureHasInvalidAspectRatioButResizeIsEnabled() {
        givenDirectoryName("volume3");
        givenImageWithInvalidAspectRatio();
        givenResizeEnabled(PspiImageSize.LARGE);
        whenMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndHasSmallImage() {
        givenDirectoryName("volume4");
        givenSmallImage();
        whenMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }

    private void thenNoExceptionIsThrown() {
        if(thrownException != null) {
            throw new AssertionError("Unexpected exception occurred!", thrownException);
        }
    }

    private void givenResizeEnabled(PspiImageSize imageSize) {
        commandArguments.add("-r");
        commandArguments.add(imageSize.name());
    }

    private void givenImageWithInvalidAspectRatio() {
        givenTestImage("11_Smith", "John_Doe.jpg", 850, 1000);
    }

    private void givenSmallImage() {
        givenImage(PspiImageSize.SMALL);
    }

    private void givenLargeImage() {
        givenImage(PspiImageSize.LARGE);
    }

    private void givenImage(final PspiImageSize imageSize) {
        givenTestImage("11_Smith", "John_Doe.jpg", imageSize);
    }

    private void thenExceptionIsThrown(Class<? extends Throwable> expectedClass, String expectedMessage) {
        assertNotNull(thrownException);
        final Class<? extends Exception> actualClass = thrownException.getClass();
        if(expectedClass != actualClass) {
            throw new AssertionError(String.format("Expected exception type \"%s\" to be thrown but instead was \"%s\"", expectedClass, actualClass), thrownException);
        }
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    private void whenMainIsExecuted() {
        thrownException = null;
        try {
            testDataGenerator.generateTestImages(inputDirectory);
            PspiIndexGeneratorCLI.main(commandArguments.toArray(new String[commandArguments.size()]));
        } catch(Exception e) {
            thrownException = e;
        }
    }

    private void thenActualIndexFileContentsMatchExpected() {
        final String expectedContent = readString(expectedInputFile.toPath());
        final String actualContent = readString(actualInputFile.toPath());
        assertEquals(expectedContent, actualContent);
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path).replaceAll("\\r\\n|\\r|\\n", "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenTestImage(String folderName, String imageName, PspiImageSize imageSize) {
        testDataGenerator.addTestImage(folderName, imageName, imageSize);
    }
    
    private void givenTestImage(String folderName, String imageName, int width, int height) {
        testDataGenerator.addTestImage(folderName, imageName, width, height);
    }

    private void givenDirectoryName(String folderName) {
        inputDirectory = new File(TEST_INPUT_DIR_ROOT, folderName);
        outputDirectory = new File(TEST_OUTPUT_DIR_ROOT, folderName);
        expectedInputFile = new File("src/test/directories/" + folderName + "_expected_input.txt");
        actualInputFile = new File(outputDirectory + "/INDEX.TXT");
        commandArguments.add("-i");
        commandArguments.add(inputDirectory.getAbsolutePath());
        commandArguments.add("-o");
        commandArguments.add(outputDirectory.getAbsolutePath());
    }

}
