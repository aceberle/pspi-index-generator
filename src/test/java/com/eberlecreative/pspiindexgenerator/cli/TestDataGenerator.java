package com.eberlecreative.pspiindexgenerator.cli;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.eberlecreative.pspiindexgenerator.imagemodifier.CropAnchors;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifier;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageModifierFactory;
import com.eberlecreative.pspiindexgenerator.imagemodifier.ImageSize;
import com.eberlecreative.pspiindexgenerator.logger.Logger;
import com.eberlecreative.pspiindexgenerator.util.FileUtils;
import com.eberlecreative.pspiindexgenerator.util.ImageUtils;

public class TestDataGenerator {

    private FileUtils fileUtils = FileUtils.getInstance();
    
    private ImageUtils imageUtils = ImageUtils.getInstance();

    private Map<String, Map<String, ImageSize>> testData = new HashMap<>();
    
    public void addTestImage(String folderName, String imageName, ImageSize spec) {
        getTestFolder(folderName).put(imageName, spec);
    }
    
    public void addTestImage(String folderName, String imageName, int width, int height) {
        addTestImage(folderName, imageName, new DefaultImageSize(width, height));
    }

    private Map<String, ImageSize> getTestFolder(String folderName) {
        Map<String, ImageSize> folderData = testData.get(folderName);
        if(folderData == null) {
            folderData = new HashMap<>();
            testData.put(folderName, folderData);
        }
        return folderData;
    }
    
    public void generateTestImages(File targetDir) throws IOException {
        final BufferedImage sourceImage = imageUtils.readImage(new File("src/test/resources/profile.jpg"));
        final Logger noOpLogger = new NoOpLogger();
        final ImageModifierFactory modifierFactory = new ImageModifierFactory(CropAnchors.parseCropAnchor("top-middle"));
        if(!targetDir.exists()) {
            targetDir.mkdirs();
        } else if (!targetDir.isDirectory()) {
            throw new RuntimeException("Expected target dir to be a directory!: " + targetDir);
        } else {
            fileUtils.cleanDirectory(targetDir);
        }
        for(Entry<String, Map<String, ImageSize>> testDirEntry : testData.entrySet()) {
            final File testDirFile = new File(targetDir, testDirEntry.getKey());
            System.out.println("Creating directory at: " + testDirFile);
            testDirFile.mkdirs();
            for(Entry<String, ImageSize> testImageEntry : testDirEntry.getValue().entrySet()) {
                final File testImageFile = new File(testDirFile, testImageEntry.getKey());
                System.out.println("Creating file at: " + testImageFile);
                final ImageSize targetSize = testImageEntry.getValue();
                modifierFactory.resizeImages(targetSize);
                final ImageModifier modifier = modifierFactory.getImageModifier(noOpLogger);
                final BufferedImage targetImage = modifier.modifyImage(testImageFile.toPath(), sourceImage);
                ImageIO.write(targetImage, "jpg", testImageFile);
            }
        }
    }

}
