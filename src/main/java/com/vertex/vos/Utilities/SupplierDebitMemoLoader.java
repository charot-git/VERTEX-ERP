package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SupplierDebitMemoLoader implements MemoDataLoader {
    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    @Override
    public void loadMemoData(TableView<CreditDebitMemo> memoTable) {
        ObservableList<CreditDebitMemo> memoList = FXCollections.observableList(supplierMemoDAO.getAllSupplierDebitMemo());
        if (memoList.isEmpty()) {
            memoTable.getColumns().clear();
            TableColumn<CreditDebitMemo, String> messageColumn = new TableColumn<>("No Data");
            messageColumn.setCellValueFactory(param -> new SimpleStringProperty("No credit/debit memos found."));
            memoTable.getColumns().add(messageColumn);
        } else {
            memoTable.setItems(memoList);
        }
    }
}
