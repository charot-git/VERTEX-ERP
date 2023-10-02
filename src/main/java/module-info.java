module com.example.vos {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.zaxxer.hikari;
    requires java.sql;

    opens com.example.vos to javafx.fxml;
    exports com.example.vos;
}