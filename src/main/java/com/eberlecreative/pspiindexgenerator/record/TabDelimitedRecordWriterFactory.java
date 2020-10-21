package com.eberlecreative.pspiindexgenerator.record;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class TabDelimitedRecordWriterFactory implements RecordWriterFactory {

    @Override
    public TabDelimitedRecordWriter getRecordWriter(File targetFile, Collection<RecordField> recordFields) throws IOException {
        return new TabDelimitedRecordWriter(targetFile, recordFields);
    }
    
}
