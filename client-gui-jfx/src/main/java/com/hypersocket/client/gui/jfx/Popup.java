package com.hypersocket.client.gui.jfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Popup extends Stage {

	public Popup(Window parent, Scene scene) {
		this(parent, scene, true);
	}

	public Popup(Window parent, Scene scene, boolean dismissOnFocusLost) {
		super(StageStyle.UNDECORATED);
		initOwner(parent);
		setScene(scene);

		// This is to align the window AFTER we know it's size. 
		ChangeListener<? super Number> l = new ChangeListener<Number>() {

			boolean widthObtained;

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (!widthObtained) {
					setY(getOwner().getY() + getOwner().getHeight());
					setX(getOwner().getX() + getOwner().getWidth() - getWidth());
					widthObtained = true;
				}
			}
		};
		widthProperty().addListener(l);

		sizeToScene();
		if (dismissOnFocusLost) {
			focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {
					if (oldValue && !newValue) {
						hide();
					}
				}
			});
		}
	}

	public void popupAndWait() {
		if (!isShowing()) {
			setY(getOwner().getY() + getOwner().getHeight());
			setX(getOwner().getX() + getOwner().getWidth() - getWidth());
			showAndWait();
		}
	}

	public void popup() {

		if (!isShowing()) {
			setY(getOwner().getY() + getOwner().getHeight());
			setX(getOwner().getX() + getOwner().getWidth() - getWidth());
			show();
		}
	}
}