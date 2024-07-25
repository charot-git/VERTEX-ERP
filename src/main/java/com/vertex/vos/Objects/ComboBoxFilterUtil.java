package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ComboBoxFilterUtil {

    private static final Logger LOGGER = Logger.getLogger(ComboBoxFilterUtil.class.getName());

    public static <T> void setupComboBoxFilter(ComboBox<T> comboBox, ObservableList<T> originalList) {

        if (comboBox == null || originalList == null) {
            return;
        }

        if (originalList.isEmpty()) {
            return;
        }
        if (comboBox.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        final ObservableList<T> data = FXCollections.observableArrayList(originalList);

        comboBox.setEditable(true);
        comboBox.getEditor().focusedProperty().addListener(observable -> {
            if (0 > comboBox.getSelectionModel().getSelectedIndex()) {
                comboBox.getEditor().setText(null);
            }
        });

        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> comboBox.hide());
        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

            private boolean moveCaretToPos = false;
            private int caretPos;

            @Override
            public void handle(KeyEvent event) {
                try {
                    switch (event.getCode()) {
                        case DOWN:
                            if (!comboBox.isShowing()) {
                                comboBox.show();
                            }
                        case UP:
                            caretPos = -1;
                            moveCaret(comboBox.getEditor().getText().length());
                            return;
                        case BACK_SPACE:
                        case DELETE:
                            moveCaretToPos = true;
                            caretPos = comboBox.getEditor().getCaretPosition();
                            break;
                    }

                    if (KeyCode.RIGHT == event.getCode() || KeyCode.LEFT == event.getCode()
                            || event.isControlDown() || KeyCode.HOME == event.getCode()
                            || KeyCode.END == event.getCode() || KeyCode.TAB == event.getCode()) {
                        return;
                    }

                    final ObservableList<T> list = FXCollections.observableArrayList();
                    for (T aData : data) {
                        if (shouldDataBeAddedToInput(aData)) {
                            list.add(aData);
                        }
                    }
                    final String text = comboBox.getEditor().getText();

                    comboBox.setItems(list);
                    comboBox.getEditor().setText(text);
                    if (!moveCaretToPos) {
                        caretPos = -1;
                    }
                    moveCaret(text.length());
                    if (!list.isEmpty()) {
                        comboBox.show();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error occurred while filtering ComboBox", e);
                    // Notify the user of the error (e.g., display an alert dialog)
                    // Provide a fallback mechanism or revert to the original list
                    // Disable or gracefully handle the filtering functionality
                }
            }

            private boolean shouldDataBeAddedToInput(T aData) {
                final String dataValue = aData.toString().toLowerCase();
                final String inputValue = comboBox.getEditor().getText().toLowerCase();
                return dataValue.contains(inputValue);
            }

            private void moveCaret(int textLength) {
                if (-1 == caretPos) {
                    comboBox.getEditor().positionCaret(textLength);
                } else {
                    comboBox.getEditor().positionCaret(caretPos);
                }
                moveCaretToPos = false;
            }
        });
    }
}
