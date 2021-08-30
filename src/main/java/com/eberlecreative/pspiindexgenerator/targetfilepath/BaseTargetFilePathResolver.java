package com.eberlecreative.pspiindexgenerator.targetfilepath;

import com.eberlecreative.pspiindexgenerator.util.FileUtils;

public abstract class BaseTargetFilePathResolver implements TargetFilePathResolver {

    protected final FileUtils fileUtils;
    
    public BaseTargetFilePathResolver(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

}
