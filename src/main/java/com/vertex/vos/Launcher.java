package com.vertex.vos;

import com.vertex.vos.Utilities.LocationCache;
import javafx.application.Application;

public class Launcher {
    public static void main(String[]args){
        LocationCache.initialize();
        Application.launch(Main.class);
    }
}