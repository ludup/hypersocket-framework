package com.hypersocket.client.gui.jfx.controls.ribbon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by pedro_000 on 1/21/14.
 */
public class RibbonWithGroupsTest extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {

        Image iconImage = new Image(getClass().getResourceAsStream("play.png"));
        Button iconButton = new Button("Play", new ImageView(iconImage));
        iconButton.setContentDisplay(ContentDisplay.LEFT);

        iconImage = new Image(getClass().getResourceAsStream("stop.png"));
        iconButton = new Button("Stop", new ImageView(iconImage));
        iconButton.setContentDisplay(ContentDisplay.LEFT);

        iconImage = new Image(getClass().getResourceAsStream("pause.png"));
        iconButton = new Button("Pause", new ImageView(iconImage));
        iconButton.setContentDisplay(ContentDisplay.LEFT);

        iconImage = new Image(getClass().getResourceAsStream("fastForward.png"));
        iconButton = new Button("Next", new ImageView(iconImage));
        iconButton.setContentDisplay(ContentDisplay.LEFT);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.getNodes().add(iconButton);
        ribbonGroup.getNodes().add(iconButton);
        ribbonGroup.getNodes().add(iconButton);
        ribbonGroup.getNodes().add(iconButton);

        ribbonGroup = new RibbonGroup();
        iconImage = new Image(getClass().getResourceAsStream("save.png"));
        iconButton = new Button("Save Results", new ImageView(iconImage));
        iconButton.setContentDisplay(ContentDisplay.LEFT);
        ribbonGroup.getNodes().add(iconButton);
        
        RibbonTab ribbonTab = new RibbonTab("Test");
        ribbonTab.getRibbonGroups().add(ribbonGroup);

        Ribbon ribbon = new Ribbon();
        ribbon.getTabs().add(ribbonTab);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(ribbon);
        Scene scene = new Scene(rootNode);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
