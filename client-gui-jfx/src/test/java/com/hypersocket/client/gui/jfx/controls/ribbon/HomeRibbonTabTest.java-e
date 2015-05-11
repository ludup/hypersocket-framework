package com.hypersocket.client.gui.jfx.controls.ribbon;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by pedro_000 on 12/5/2014.
 */
public class HomeRibbonTabTest extends Application {
        static final String RESOURCE = "RibbonFXML.fxml";

        @Override
        public void start(Stage primaryStage) throws Exception
        {
            URL resource = getClass().getResource(RESOURCE);
            Parent root = FXMLLoader.load(resource);
//        root.getStylesheets().add(getClass().getResource(STYLE_SHEET).toExternalForm());
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        public static void main(String[] args)
        {
            launch(args);
        }
}

