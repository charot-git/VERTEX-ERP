package com.vertex.vos;

import javafx.collections.ObservableList;
import org.apache.poi.ss.formula.functions.T;

public class DragContainer {
    private final ObservableList<T> data;

    public DragContainer(ObservableList<T> data) {
        this.data = data;
    }

    public ObservableList<T> getData() {
        return data;
    }
}
