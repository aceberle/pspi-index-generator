package com.eberlecreative.pspiindexgenerator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileUtils {

    private static FileUtils instance = new FileUtils();

    public static FileUtils getInstance() {
        return instance;
    }
    
    public boolean makeParentDirectory(File file) {
        return file.getParentFile().mkdirs();
    }
    
    public Path getRelativePath(File currentBaseDir, File newBaseDir, File currentFilePath) {
        return getResolvedPath(newBaseDir, getRelativePath(currentBaseDir, currentFilePath));
    }
    
    public Path getRelativePath(File baseDir, File file) {
        final String filePathString = file.getAbsolutePath();
        final String baseDirPathString = baseDir.getAbsolutePath();
        if(!filePathString.startsWith(baseDirPathString)) {
            throw new IllegalArgumentException(String.format("Expected file path \"%\" to start with directory path: \"%s\"", filePathString, baseDirPathString));
        }
        final Path filePath = file.toPath();
        final Path baseDirPath = baseDir.toPath();
        return baseDirPath.relativize(filePath);
    }
    
    public Path getResolvedPath(File newBaseDir, Path relativePath) {
        if(relativePath.isAbsolute()) {
            throw new RuntimeException(String.format("Provided relative path is already absolute!: %s", relativePath));
        }
        final Path newBaseDirPath = newBaseDir.toPath();
        return newBaseDirPath.resolve(relativePath);
    }
    
    public void cleanDirectory(File folder) {
        final File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    public void deleteFolder(File folder) {
        cleanDirectory(folder);
        folder.delete();
    }

    public void save(InputStream inputStream, File file) throws IOException {
        final byte[] buffer = new byte[1024];
        try (FileOutputStream out = new FileOutputStream(file)) {
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) > -1) {
                out.write(buffer, 0 , bytesRead);
            }
        }
    }

}
