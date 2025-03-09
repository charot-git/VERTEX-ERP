package com.vertex.vos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.Serializable;
import java.util.List;

public class DragContainer<T> implements Serializable {
    private final ObservableList<T> data;

    public DragContainer(ObservableList<T> data) {
        this.data = FXCollections.observableArrayList(data);
    }

    public ObservableList<T> getData() {
        return data;
    }

    // Add this method to expose the data for Gson
    public List<T> getDataList() {
        return data;
    }
}
