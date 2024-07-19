package com.vertex.vos.Objects;

public class Section {
    private final int section_id;
    private final String section_name;

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
