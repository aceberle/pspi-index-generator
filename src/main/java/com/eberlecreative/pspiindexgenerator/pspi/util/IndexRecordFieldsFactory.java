package com.eberlecreative.pspiindexgenerator.pspi.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.eberlecreative.pspiindexgenerator.record.RecordField;

public class IndexRecordFieldsFactory {

    private final Map<String, String> headersByField;

    public IndexRecordFieldsFactory() {
        headersByField = new LinkedHashMap<>();
        // Adding the 13 standard fields
        addField(IndexRecordFields.VOLUME_NAME, "Volume Name");
        addField(IndexRecordFields.IMAGE_FOLDER, "Image Folder");
        addField(IndexRecordFields.IMAGE_FILE_NAME, "Image File Name");
        addField(IndexRecordFields.GRADE, "Grade");
        addField(IndexRecordFields.LAST_NAME, "Last Name");
        addField(IndexRecordFields.FIRST_NAME, "First Name");
        addField(IndexRecordFields.HOME_ROOM, "Home Room");
        addField("period", "Period");
        addField("teacherName", "Teacher Name");
        addField("track", "Track");
        addField("department", "Department");
        addField("title", "Title");
        addField(IndexRecordFields.IMAGE_SIZE, "Image Size");
    }
    
    public Collection<RecordField> getIndexRecordFields() {
        return headersByField.entrySet().stream().map(entry -> new RecordField(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public void addField(String field, String header) {
        headersByField.put(field, header);
    }

}
