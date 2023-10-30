package com.vertex.vos.Constructors;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class SharedFunctions {
    private static TextField searchBar;
    private static AnchorPane contentPane;

    public static TextField getSearchBar() {
        return searchBar;
    }

    public static AnchorPane getContentPane() {
        return contentPane;
    }

    public static void setSearchBar(TextField searchBar) {
        SharedFunctions.searchBar = searchBar;
    }

    public static void setContentPane(AnchorPane contentPane) {
        SharedFunctions.contentPane = contentPane;
    }
}
