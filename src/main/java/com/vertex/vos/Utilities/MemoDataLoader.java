package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.SupplierCreditDebitMemo;
import javafx.scene.control.TableView;

public interface MemoDataLoader {
    void loadMemoData(TableView<SupplierCreditDebitMemo> memoTable);
}
