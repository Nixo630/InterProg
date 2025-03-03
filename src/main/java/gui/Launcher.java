package gui;

import javafx.application.Platform;

public class Launcher {
    public static void main(String[] args) {
        Platform.startup(() ->{});
        App.main(args);
    }
}
