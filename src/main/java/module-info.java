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
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires static lombok;
    requires java.mail;
    requires org.apache.pdfbox;
    requires javafx.graphics;
    requires javafx.swing;
    requires commons.math3;

    opens com.vertex.vos to javafx.fxml;
    opens com.vertex.vos.Utilities to javafx.fxml;
    opens com.vertex.vos.Objects to javafx.fxml;
    opens com.vertex.vos.DAO to javafx.fxml;

    exports com.vertex.vos;
    exports com.vertex.vos.Utilities;
    exports com.vertex.vos.Objects;
    exports com.vertex.vos.DAO;
}
