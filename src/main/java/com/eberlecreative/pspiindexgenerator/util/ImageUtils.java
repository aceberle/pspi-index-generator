package com.eberlecreative.pspiindexgenerator.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Adapted from https://stackoverflow.com/a/36675321
 * 
 */
public class ImageUtils {

    private static ImageUtils instance = new ImageUtils();

    public static ImageUtils getInstance() {
        return instance;
    }

    public BufferedImage readImage(File file) throws IOException {
        return withReader(file, reader -> reader.read(0));
    }

    public void saveImageCopyMetaData(BufferedImage image, File sourceFile, File outputFile, float compressionQuality)
            throws IOException {
        final ImageWriter writer = getImageWriter(getExtension(outputFile));
        withReader(sourceFile, reader -> { 
            try (FileOutputStream fout = new FileOutputStream(outputFile);
                    ImageOutputStream iout = ImageIO.createImageOutputStream(fout)) {
                writer.setOutput(iout);
                final ImageWriteParam iwParam = writer.getDefaultWriteParam();
                iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwParam.setCompressionQuality(compressionQuality);
                //https://stackoverflow.com/a/62240696
                if (iwParam instanceof JPEGImageWriteParam) {
                    ((JPEGImageWriteParam) iwParam).setOptimizeHuffmanTables(true);
                }
                final IIOMetadata metadata = reader.getImageMetadata(0);
                writer.write(null, new IIOImage(image, null, metadata), iwParam);
                writer.dispose();
            }
            return null;
        });
    }

    private <E> E withReader(File file, ImageReaderHandler<E> handler) throws FileNotFoundException, IOException {
        ImageReader reader = null;
        try (FileInputStream fin = new FileInputStream(file);
                ImageInputStream in = ImageIO.createImageInputStream(fin)) {
            reader = getImageReader(getExtension(file));
            reader.setInput(in);
            return handler.handle(reader);
        } finally {
            if(reader != null) {
                reader.dispose();
            }
        }
    }

    private interface ImageReaderHandler<E> {

        E handle(ImageReader reader) throws IOException;

    }

    private String getExtension(File file) {
        final String name = file.getName();
        final int idx = name.lastIndexOf('.');
        if (idx > -1 && idx + 1 < name.length()) {
            return name.substring(idx + 1);
        }
        throw new RuntimeException("File has unknown extension: " + file);
    }

    private ImageReader getImageReader(String type) {
        return first(ImageIO.getImageReadersBySuffix(type));
    }

    private ImageWriter getImageWriter(String type) {
        return first(ImageIO.getImageWritersBySuffix(type));
    }

    private static <E> E first(Iterator<E> itr) {
        return itr.next();
    }

}
