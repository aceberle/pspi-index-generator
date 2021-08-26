package com.eberlecreative.pspiindexgenerator.cli;

import java.awt.Graphics;
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
    
    private static final String ROOT = "%ROOT%";

    private FileUtils fileUtils = FileUtils.getInstance();
    
    private ImageUtils imageUtils = ImageUtils.getInstance();

    private Map<String, Map<String, ImageSize>> testData = new HashMap<>();
    
    public void addTestImage(String imageName, ImageSize spec) {
        addTestImage(null, imageName, spec);
    }
    
    public void addTestImage(String folderName, String imageName, ImageSize spec) {
        getTestFolder(folderName).put(imageName, spec);
    }
    
    public void addTestImage(String imageName, int width, int height) {
        addTestImage(null, imageName, width, height);
    }
    
    public void addTestImage(String folderName, String imageName, int width, int height) {
        addTestImage(folderName, imageName, new DefaultImageSize(width, height));
    }

    private Map<String, ImageSize> getTestFolder(String folderName) {
        final String actualFolderName = folderName == null ? ROOT : folderName;
        Map<String, ImageSize> folderData = testData.get(actualFolderName);
        if(folderData == null) {
            folderData = new HashMap<>();
            testData.put(actualFolderName, folderData);
        }
        return folderData;
    }
    
    public void generateTestImages(File targetDir) throws IOException {
        final BufferedImage sourceImage = imageUtils.readImage(new File("src/test/resources/profile.jpg"));
        final int origWidth = sourceImage.getWidth();
        final int origHeight = sourceImage.getHeight();
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
            final String testDirName = testDirEntry.getKey();
            final File testDirFile = ROOT.equals(testDirName) ? targetDir : new File(targetDir, testDirName);
            System.out.println("Creating directory at: " + testDirFile);
            testDirFile.mkdirs();
            for(Entry<String, ImageSize> testImageEntry : testDirEntry.getValue().entrySet()) {
                final File testImageFile = new File(testDirFile, testImageEntry.getKey());
                System.out.println("Creating file at: " + testImageFile);
                final ImageSize targetSize = testImageEntry.getValue();
                final int targetWidth = targetSize.getWidth();
                final int targetHeight = targetSize.getHeight();
                BufferedImage targetImage;
                if(targetWidth > origWidth || targetHeight > origHeight) {
                    targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    final Graphics g = targetImage.getGraphics();
                    for(int y = 0; y < targetHeight; y += origHeight) {
                        for(int x = 0; x < targetWidth; x += origWidth) {
                            g.drawImage(sourceImage, x, y, null);
                        }
                    }
                    g.dispose();
                } else {
                    modifierFactory.resizeImages(targetSize);
                    final ImageModifier modifier = modifierFactory.getImageModifier(noOpLogger);
                    targetImage = modifier.modifyImage(testImageFile.toPath(), sourceImage);
                }
                ImageIO.write(targetImage, "jpg", testImageFile);
            }
        }
    }

}
