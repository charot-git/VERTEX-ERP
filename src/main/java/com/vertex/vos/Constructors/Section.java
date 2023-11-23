package com.vertex.vos.Constructors;

public class Section {
    private int section_id;
    private String section_name;

    public Section(int section_id, String section_name) {
        this.section_id = section_id;
        this.section_name = section_name;
    }

    public int getSection_id() {
        return section_id;
    }

    public String getSection_name() {
        return section_name;
    }
}
