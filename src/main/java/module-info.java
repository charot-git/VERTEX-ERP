module com.example.vos {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.controlsfx.controls;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.json;

    opens com.vertex.vos to javafx.fxml;
    exports com.vertex.vos;
}