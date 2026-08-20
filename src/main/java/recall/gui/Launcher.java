package recall.gui;

import javafx.application.Application;

/**
 * A launcher class. Running the JavaFX {@link MainApp} directly from a shaded
 * ("fat") JAR causes classpath issues, so this plain non-Application class is
 * used as the entry point and simply hands off to {@code Application.launch}.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
