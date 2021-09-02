package com.eberlecreative.pspiindexgenerator.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TabDelimitedRecordWriter implements RecordWriter {

    private final File indexFile;

    private final Collection<RecordField> recordFields;

    private PrintWriter writer;

    public TabDelimitedRecordWriter(File indexFile, Collection<RecordField> recordFields) {
        this.indexFile = indexFile;
        this.recordFields = recordFields;
    }

    @Override
    public void initializeFile(boolean append) throws IOException {
        writer = new PrintWriter(new FileWriter(indexFile, append), true);
        if(!append) {
            writeRecord(RecordField::getLabel, Function.identity());
        }
    }

    @Override
    public void writeRecord(Map<String, String> record) throws IOException {
        writeRecord(RecordField::getName, record::get);
    }

    private void writeRecord(Function<RecordField, String> recordExtractor, Function<String, String> valueConverter) {
        final Queue<String> queue = recordFields.stream().map(recordExtractor).collect(Collectors.toCollection(LinkedList::new));
        while(!queue.isEmpty()) {
            final String key = queue.poll();
            final String converted = valueConverter.apply(key);
            if(converted != null) {
                writer.print(converted);
            }
            if(!queue.isEmpty()) {
                writer.print("\t");
            }
        }
        writer.print("\r\n");
    }

    @Override
    public void close() throws Exception {
        if(writer != null) {
            writer.close();
        }
    }
    
}
