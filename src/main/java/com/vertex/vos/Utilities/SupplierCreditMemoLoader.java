package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.SupplierCreditDebitMemo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SupplierCreditMemoLoader implements MemoDataLoader {

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    @Override
    public void loadMemoData(TableView<SupplierCreditDebitMemo> memoTable) {
        ObservableList<SupplierCreditDebitMemo> memoList = FXCollections.observableList(supplierMemoDAO.getAllSupplierCreditMemo());
        if (memoList.isEmpty()) {
            memoTable.getColumns().clear();
            TableColumn<SupplierCreditDebitMemo, String> messageColumn = new TableColumn<>("No Data");
            messageColumn.setCellValueFactory(param -> new SimpleStringProperty("No credit/debit memos found."));
            memoTable.getColumns().add(messageColumn);
        } else {
            memoTable.setItems(memoList);
        }
    }
}
