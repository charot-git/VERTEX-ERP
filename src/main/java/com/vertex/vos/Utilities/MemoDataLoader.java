package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.scene.control.TableView;

public interface MemoDataLoader {
    void loadMemoData(TableView<CreditDebitMemo> memoTable);
}
