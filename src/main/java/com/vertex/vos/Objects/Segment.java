package com.vertex.vos.Objects;

public class Segment {
    private final int segment_id;
    private final String segment_name;

    public Segment(int segment_id, String segment_name) {
        this.segment_id = segment_id;
        this.segment_name = segment_name;
    }

    public int getSegment_id() {
        return segment_id;
    }

    public String getSegment_name() {
        return segment_name;
    }
}
