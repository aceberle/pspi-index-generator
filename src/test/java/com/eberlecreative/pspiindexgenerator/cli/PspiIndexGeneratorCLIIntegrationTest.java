package com.eberlecreative.pspiindexgenerator.cli;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.arakelian.faker.model.Person;
import com.arakelian.faker.service.RandomData;
import com.arakelian.faker.service.RandomPerson;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageSize;
import com.eberlecreative.pspiindexgenerator.pspi.generator.OutputDirectoryContainsValidPspiPackageException;
import com.eberlecreative.pspiindexgenerator.pspi.generator.OutputDirectoryIsNotEmptyException;
import com.eberlecreative.pspiindexgenerator.pspi.util.PspiImageSize;

public class PspiIndexGeneratorCLIIntegrationTest {
    
    private static final String TEST_IMAGE_FILENAME = "John_Doe.jpg";

    private static final String TEST_IMAGE_FOLDER = "11_Smith";
    
    private static final String TEST_IMAGE_FOLDER_WITHOUT_HOMEROOM = "11_Grade";

    private static final File TEST_INPUT_DIR_ROOT = new File("target/test-input-directories/");
    
    private static final File TEST_OUTPUT_DIR_ROOT = new File("target/test-output-directories/");
    
    private static final int NUM_IMAGES_PER_FOLDER = 10;
    
    private static final int NUM_FOLDERS = 3;

    private static final File SOURCE_COPYRIGHT_FILE = new File("src/main/resources/COPYRIGHT.TXT");
    
    private TestDataGenerator testDataGenerator;
    
    private TestWorkbookGenerator testWorkbookGenerator;
    
    private File inputDirectory;

    private File outputDirectory;
    
    private File expectedInputFile;

    private File actualInputFile;
    
    private Exception thrownException;

    private File actualCopyRightFile;

    private File dataFile;
    
    private String outputFileFormat;

    private PspiImageSize imageSize;

    private String currentDirectoryName;

    private boolean forceOutput;

    private boolean appendOutput;
    
    @Before
    public void init() {
        testDataGenerator = new TestDataGenerator();
        testWorkbookGenerator = new TestWorkbookGenerator();
        currentDirectoryName = null;
        outputDirectory = null;
        inputDirectory = null;
        outputFileFormat = null;
        imageSize = null;
        dataFile = null;
        forceOutput = false;
        appendOutput = false;
    }
    
    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndHasLargeImage() {
        givenCleanDirectoryName("volume1");
        givenLargeImage();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatActualCopyRightFileContentsMatchExpectedWhenMainIsExecutedAndHasLargeImage() {
        givenCleanDirectoryName("volume1");
        givenLargeImage();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualCopyRightFileContentsMatchExpected();
    }

    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedAndPictureHasInvalidAspectRatio() {
        givenCleanDirectoryName("volume2");
        givenImageWithInvalidAspectRatio();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenActualIndexFileContentsMatchExpected();
    }

    @Test
    public void specifyThatExceptionIsThrownWhenMainIsExecutedAndPictureHasInvalidAspectRatio() {
        givenCleanDirectoryName("volume2");
        givenImageWithInvalidAspectRatio();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenExceptionIsThrown(RuntimeException.class, String.format("Invalid image found! Expected aspect ratio of 0.8 but found 850px / 1000px = 0.85, image at: %2$s%1$s11_Smith%1$sJohn_Doe.jpg", File.separator, outputDirectory.getAbsolutePath()));
    }

    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndPictureHasInvalidAspectRatioButResizeIsEnabled() {
        givenCleanDirectoryName("volume3");
        givenImageWithInvalidAspectRatio();
        givenResizeEnabled(PspiImageSize.LARGE);
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatActualIndexFileContentsMatchExpectedWhenMainIsExecutedAndHasSmallImage() {
        givenCleanDirectoryName("volume4");
        givenSmallImage();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatActualIndexFileNumEntriesMatchExpectedWhenMainIsExecutedAndManyValidImagesExist() {
        givenCleanDirectoryName("volume5");
        givenManyValidImages();
        givenResizeEnabled(PspiImageSize.SMALL);
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContainsNumRows(1 + (NUM_FOLDERS * NUM_IMAGES_PER_FOLDER));
    }
    
    @Test
    public void specifyThatExceptionIsThrownWhenMainIsExecutedAndImageIsLargerThan10MB() {
        givenCleanDirectoryName("volume6");
        givenImageLargerThan10MB();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenExceptionIsThrown(RuntimeException.class, String.format("Invalid image found! Expected size to be less than 10000000 bytes but found size of %3$s bytes, image at: %2$s%1$s11_Smith%1$sJohn_Doe.jpg", File.separator, outputDirectory.getAbsolutePath(), getTestImageFileSize()));
    }
    
    @Test
    public void specifyThatGradeIsUsedForHomeRoomWhenMainIsExecutedAndHomeRoomIsNotAvailable() {
        givenCleanDirectoryName("volume7");
        givenLargeImageInFolderWithoutHomeRoom();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatUsingDataFileWorks() {
        givenCleanDirectoryName("volume8");
        givenTestImage("001.jpg", PspiImageSize.SMALL);
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenTestImage("003.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("1", "Stephanie", "Helsabeck", "100304", "5", "HR-5th-1: Caputa");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        givenExcelFileRow("3", "", "", "100102", "3", "HR-5th-2: Reid", "", "Bynum Meredith");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatUsingOutputFileFormatWorks() {
        givenCleanDirectoryName("volume9");
        givenTestImage("001.jpg", PspiImageSize.SMALL);
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenTestImage("003.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("1", "Stephanie", "Helsabeck", "100304", "5", "HR-5th-1: Caputa");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        givenExcelFileRow("3", "", "", "100102", "3", "HR-5th-2: Reid", "", "Bynum Meredith");
        givenOutputFileFormat("<lastName>_<firstName>_<id>.jpg");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatUsingOutputFileFormatWorksWithFileNameDataSource() {
        givenCleanDirectoryName("volume10");
        givenLargeImage();
        givenOutputFileFormat("<lastName>_<firstName>.jpg");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatOutputDirectoryContainsValidPspiPackageExceptionIsThrownIfOutputDirectoryAlreadyHasValidPspiFiles() {
        givenCleanDirectoryName("volume11");
        givenTestImage("001.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("1", "Stephanie", "Helsabeck", "100304", "5", "HR-5th-1: Caputa");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        givenInputDirectory("volume11a");
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenExceptionIsThrown(OutputDirectoryContainsValidPspiPackageException.class, String.format("Output directory already contains a valid PSPI package!: %s", outputDirectory.getAbsolutePath()));
    }
    
    @Test
    public void specifyThatForceOutputResultsInSecondRunWinningIfOutputDirectoryAlreadyHasValidPspiFiles() {
        givenCleanDirectoryName("volume12");
        givenTestImage("001.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("1", "Stephanie", "Helsabeck", "100304", "5", "HR-5th-1: Caputa");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        givenInputDirectory("volume12a");
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        givenOutputForced();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatOutputDirectoryIsNotEmptyExceptionIsThrownWhenOutputDirectoryExistsAndIsNotEmpty() {
        givenCleanDirectoryName("volume13");
        givenOutputDirectoryExistsAndIsNotEmpty();
        givenInputDirectory("volume13a");
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenExceptionIsThrown(OutputDirectoryIsNotEmptyException.class, String.format("Output directory is not empty!: %s", outputDirectory.getAbsolutePath()));
    }
    
    @Test
    public void specifyThatForceOutputResultsInSecondRunWinningWhenOutputDirectoryExistsAndIsNotEmpty() {
        givenCleanDirectoryName("volume14");
        givenOutputDirectoryExistsAndIsNotEmpty();
        givenInputDirectory("volume14a");
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        givenOutputForced();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    @Test
    public void specifyThatAppendOutputResultsInSecondRunAppendingIfOutputDirectoryAlreadyHasFiles() {
        givenCleanDirectoryName("volume15");
        givenTestImage("001.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("1", "Stephanie", "Helsabeck", "100304", "5", "HR-5th-1: Caputa");
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        givenInputDirectory("volume15a");
        givenTestImage("002.jpg", PspiImageSize.SMALL);
        givenExcelFileWithColumnHeaders("Image Number", "First Name", "Last Name", "ID", "Grade", "Home Room", "First_Last", "Last_First");
        givenExcelFileRow("2", "", "", "100269", "4", "HR-5th-2: Reid", "Beatriz Gurgel");
        givenOutputFileFormat("<lastName>_<firstName>.jpg");
        givenOutputAppended();
        whenTestDataIsGeneratedAndMainIsExecuted();
        thenNoExceptionIsThrown();
        thenActualIndexFileContentsMatchExpected();
    }
    
    private void givenOutputAppended() {
        this.appendOutput = true;
    }

    private void givenOutputDirectoryExistsAndIsNotEmpty() {
        try {
            outputDirectory.mkdirs();
            new File(outputDirectory, "test.txt").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenOutputForced() {
        this.forceOutput = true;
    }

    private void givenOutputFileFormat(String outputFileFormat) {
        this.outputFileFormat = outputFileFormat;
    }

    private void givenExcelFileRow(String...rowData) {
        this.testWorkbookGenerator.addRow(rowData);
    }

    private void givenExcelFileWithColumnHeaders(String...headers) {
        dataFile = new File(TEST_INPUT_DIR_ROOT, String.format("%s.xlsx", currentDirectoryName));
        this.testWorkbookGenerator.setColumnHeaders(headers);
    }

    private void givenManyValidImages() {
        final List<Person> teachers = RandomPerson.get().listOf(NUM_FOLDERS);
        final Random random = new Random();
        final ImageSize[] imageSizes = new ImageSize[] {PspiImageSize.SMALL, PspiImageSize.LARGE, new DefaultImageSize(500, 700)};
        for(Person teacher : teachers) {
            final String grade = nextGrade();
            final String homeRoomName = camel(teacher.getLastName());
            System.out.println(String.format("Generating Teacher: %s Grade: %s", homeRoomName, grade));
            final String folderName = String.format("%s_%s", grade, homeRoomName);
            final List<Person> students = RandomPerson.get().listOf(NUM_IMAGES_PER_FOLDER);
            for(Person student : students) {
                final String firstName = camel(student.getFirstName());
                final String lastName = camel(student.getLastName());
                System.out.println(String.format("Generating Student: %s %s", firstName, lastName));
                final String imageFileName = String.format("%s_%s.jpg", firstName, lastName);
                final ImageSize imageSize = imageSizes[random.nextInt(imageSizes.length)];
                testDataGenerator.addTestImage(folderName, imageFileName, imageSize);
            }
        }
    }

    private void givenImageLargerThan10MB() {
        final PspiImageSize imageSize = PspiImageSize.LARGE;
        givenTestImageWithDimensions(imageSize.getWidth() * 15, imageSize.getHeight() * 15);
    }

    private void thenActualIndexFileContainsNumRows(int expectedRows) {
        assertEquals(expectedRows, readAllLines(actualInputFile.toPath()).size());
    }

    private String camel(String string) {
        final int length = string.length();
        return length > 0 ? length > 1 ? string.substring(0,1).toUpperCase().concat(string.substring(1).toLowerCase()) : string.substring(0,1) : string;
    }

    private String nextGrade() {
        return RandomData.get().nextString("grade");
    }

    private void thenNoExceptionIsThrown() {
        if(thrownException != null) {
            throw new AssertionError("Unexpected exception occurred!", thrownException);
        }
    }

    private void givenResizeEnabled(PspiImageSize imageSize) {
        this.imageSize = imageSize;
    }

    private void givenImageWithInvalidAspectRatio() {
        givenTestImageWithDimensions(850, 1000);
    }

    private void givenTestImageWithDimensions(final int width, final int height) {
        givenTestImage(TEST_IMAGE_FOLDER, TEST_IMAGE_FILENAME, width, height);
    }

    private void givenLargeImageInFolderWithoutHomeRoom() {
        givenTestImage(TEST_IMAGE_FOLDER_WITHOUT_HOMEROOM, TEST_IMAGE_FILENAME, PspiImageSize.LARGE);
    }

    private long getTestImageFileSize() {
        final File imageFile = new File(outputDirectory, String.format("%s/%s", TEST_IMAGE_FOLDER, TEST_IMAGE_FILENAME));
        assertTrue(imageFile.isFile());
        return imageFile.length();
    }

    private void givenSmallImage() {
        givenImage(PspiImageSize.SMALL);
    }

    private void givenLargeImage() {
        givenImage(PspiImageSize.LARGE);
    }

    private void givenImage(final PspiImageSize imageSize) {
        givenTestImage(TEST_IMAGE_FOLDER, TEST_IMAGE_FILENAME, imageSize);
    }

    private void thenExceptionIsThrown(Class<? extends Throwable> expectedClass, String expectedMessage) {
        assertNotNull(thrownException);
        final Class<? extends Exception> actualClass = thrownException.getClass();
        if(expectedClass != actualClass) {
            throw new AssertionError(String.format("Expected exception type \"%s\" to be thrown but instead was \"%s\"", expectedClass, actualClass), thrownException);
        }
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    private void whenTestDataIsGeneratedAndMainIsExecuted() {
        testDataGenerator.generateTestImages(inputDirectory);
        testWorkbookGenerator.generateWorkbook(dataFile);
        whenMainIsExecuted();
    }

    private void whenMainIsExecuted() {
        final List<String> commandArguments = new ArrayList<>();
        commandArguments.add("-v");
        commandArguments.add("-s");
        commandArguments.add("-i");
        commandArguments.add(inputDirectory.getAbsolutePath());
        commandArguments.add("-o");
        commandArguments.add(outputDirectory.getAbsolutePath());
        if(outputFileFormat != null) {
            commandArguments.add("-p");
            commandArguments.add(outputFileFormat);
        }
        if(imageSize != null) {
            commandArguments.add("-r");
            commandArguments.add(imageSize.name());
        }
        if(dataFile != null) {
            commandArguments.add("-d");
            commandArguments.add(dataFile.getAbsolutePath());
        }
        if(forceOutput) {
            commandArguments.add("-f");
        }
        if(appendOutput) {
            commandArguments.add("-a");
        }
        thrownException = null;
        try {
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

    private void thenActualCopyRightFileContentsMatchExpected() {
        final String expectedContent = readString(SOURCE_COPYRIGHT_FILE.toPath());
        final String actualContent = readString(actualCopyRightFile.toPath());
        assertEquals(expectedContent, actualContent);
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path).replaceAll("\\r\\n|\\r|\\n", "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenTestImage(String imageName, PspiImageSize imageSize) {
        testDataGenerator.addTestImage(imageName, imageSize);
    }

    private void givenTestImage(String folderName, String imageName, PspiImageSize imageSize) {
        testDataGenerator.addTestImage(folderName, imageName, imageSize);
    }
    
    private void givenTestImage(String folderName, String imageName, int width, int height) {
        testDataGenerator.addTestImage(folderName, imageName, width, height);
    }

    private void givenCleanDirectoryName(String folderName) {
        givenDirectoryNamed(folderName);
        cleanOutputDirectory();
    }

    private void cleanOutputDirectory() {
        if(outputDirectory.exists()) {
            try {
                FileUtils.cleanDirectory(outputDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Unable to clean output directory: " + outputDirectory, e);
            }
        }
    }

    private void givenDirectoryNamed(String directoryName) {
        currentDirectoryName = directoryName;
        givenInputDirectory(directoryName);
        outputDirectory = new File(TEST_OUTPUT_DIR_ROOT, directoryName);
        expectedInputFile = new File("src/test/directories/" + directoryName + "_expected_input.txt");
        actualInputFile = new File(outputDirectory, "INDEX.TXT");
        actualCopyRightFile = new File(outputDirectory, "COPYRIGHT.TXT");
    }

    private void givenInputDirectory(String folderName) {
        inputDirectory = new File(TEST_INPUT_DIR_ROOT, folderName);
    }

}
