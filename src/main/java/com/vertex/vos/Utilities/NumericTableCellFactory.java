package com.vertex.vos.Utilities;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.Locale;

public class NumericTableCellFactory<S, T extends Number> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    private final StringConverter<T> converter;

    public NumericTableCellFactory() {
        this(Locale.getDefault());
    }

    public NumericTableCellFactory(Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        this.converter = new StringConverter<T>() {
            @Override
            public String toString(T value) {
                if (value == null) {
                    return "";
                }
                return format.format(value);
            }

            @Override
            public T fromString(String string) {
                // Not needed for read-only conversion
                throw new UnsupportedOperationException("Conversion from String is not supported.");
            }
        };
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(converter.toString(item));
                }
            }
        };
    }
}
