package com.hypersocket.client.gui.jfx;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class Flinger extends StackPane {
	
	public enum Direction {
		VERTICAL, HORIZONTAL
	}

	private Property<Direction> direction = new SimpleObjectProperty<Direction>(Direction.HORIZONTAL);
	private Pane container;

	public Flinger() {
		setup();
		direction.addListener(new ChangeListener<Direction>() {

			@Override
			public void changed(
					ObservableValue<? extends Direction> observable,
					Direction oldValue, Direction newValue) {
				setup();				
			}
		});
	}
	
	public Property<Direction> directionProperty() {
		return direction;
	}
	
	public Pane getContent() {
		return container;
	}
	
	private void setup() {
		List<Node> children = null;
		if(container != null) {
			children = new ArrayList<Node>(container.getChildrenUnmodifiable());
			container.getChildren().clear();
		}
		getChildren().clear();		
		container = direction.getValue().equals(Direction.VERTICAL) ? new VBox() : new HBox();
		if(children != null) {
			container.getChildren().addAll(children);
		}
		getChildren().add(container);
	}
}
