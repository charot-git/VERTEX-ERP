package com.vertex.vos;

import com.vertex.vos.Constructors.SalesOrderHeader;
import javafx.collections.ObservableList;

public class DragContainer {
    private final ObservableList<SalesOrderHeader> data;

    public DragContainer(ObservableList<SalesOrderHeader> data) {
        this.data = data;
    }

    public ObservableList<SalesOrderHeader> getData() {
        return data;
    }
}
