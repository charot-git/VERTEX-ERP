package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CustomerDebitMemoLoader implements MemoDataLoader {

    CustomerMemoDAO customerMemoDAO = new CustomerMemoDAO();
    @Override
    public void loadMemoData(TableView<CreditDebitMemo> memoTable) {
        ObservableList<CreditDebitMemo> memoList = FXCollections.observableList(customerMemoDAO.getAllCustomerDebitMemo());
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
