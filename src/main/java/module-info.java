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
    requires org.apache.commons.lang3;
    requires com.google.gson;
    requires barbecue;
    requires java.desktop;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.websocket.server;
    requires org.eclipse.jetty.websocket.servlet;
    requires org.eclipse.jetty.websocket.api;
    requires itextpdf;
    requires mysql.connector.j;

    opens com.vertex.vos to javafx.fxml;
    exports com.vertex.vos;
    exports com.vertex.vos.Utilities;
    opens com.vertex.vos.Utilities to javafx.fxml;
    exports com.vertex.vos.Constructors;
    opens com.vertex.vos.Constructors to javafx.fxml;
}