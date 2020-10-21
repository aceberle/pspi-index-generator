package com.eberlecreative.pspiindexgenerator.record;

public class RecordField {
    
    private final String name;

    private final String label;

    public RecordField(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "RecordField [label=" + label + ", name=" + name + "]";
    }

}
