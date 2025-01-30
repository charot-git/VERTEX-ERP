package com.vertex.vos;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProductLedgerApp extends Application {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();



    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/ProductLedger.fxml"));
        Parent root = loader.load();

        ProductLedgerController controller = loader.getController();

        primaryStage.setTitle("Product Ledger");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
