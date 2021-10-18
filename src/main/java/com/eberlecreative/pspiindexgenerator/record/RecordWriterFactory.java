package com.eberlecreative.pspiindexgenerator.record;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface RecordWriterFactory {

	public RecordWriter getRecordWriter(File targetFile, Collection<RecordField> recordFields) throws IOException;

}
