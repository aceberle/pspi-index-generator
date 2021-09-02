package com.eberlecreative.pspiindexgenerator.record;

import java.io.IOException;
import java.util.Map;

public interface RecordWriter extends AutoCloseable {

    public void initializeFile(boolean append) throws IOException;

    public void writeRecord(Map<String, String> indexRecord) throws IOException;
    
}
