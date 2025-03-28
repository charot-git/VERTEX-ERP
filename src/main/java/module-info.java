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
    requires java.persistence;
    requires ant;
    requires org.apache.commons.collections4;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;

    opens com.vertex.vos to javafx.fxml;
    opens com.vertex.vos.Utilities to javafx.fxml;
    opens com.vertex.vos.Objects to javafx.fxml, com.google.gson; // ✅ FIXED
    opens com.vertex.vos.DAO to javafx.fxml;

    exports com.vertex.vos;
    exports com.vertex.vos.Utilities;
    exports com.vertex.vos.Objects;
    exports com.vertex.vos.DAO;
    exports com.vertex.vos.Enums;
    opens com.vertex.vos.Enums to com.google.gson, javafx.fxml;
}
