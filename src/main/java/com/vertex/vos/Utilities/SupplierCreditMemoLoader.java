package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SupplierCreditMemoLoader implements MemoDataLoader {

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    @Override
    public void loadMemoData(TableView<CreditDebitMemo> memoTable) {
        ObservableList<CreditDebitMemo> memoList = FXCollections.observableList(supplierMemoDAO.getAllSupplierCreditMemo());
        memoTable.setItems(memoList);
    }
}
