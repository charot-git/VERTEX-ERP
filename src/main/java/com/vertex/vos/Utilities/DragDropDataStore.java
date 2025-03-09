package com.vertex.vos.Utilities;

import java.util.ArrayList;
import java.util.List;

public class DragDropDataStore<T> {
    public static List<?> draggedItems = new ArrayList<>();

    public static <T> void setDraggedItems(List<T> items) {
        draggedItems = new ArrayList<>(items);
    }

    public static <T> List<T> getDraggedItems() {
        return (List<T>) draggedItems;
    }

    public static void clear() {
        draggedItems = new ArrayList<>();
    }
}
