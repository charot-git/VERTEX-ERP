package com.example.vos;

import javafx.scene.control.TextField;

public class SharedFunctions {
    private static TextField searchBar;

    public static TextField getSearchBar() {
        return searchBar;
    }

    public static void setSearchBar(TextField searchBar) {
        SharedFunctions.searchBar = searchBar;
    }
}
