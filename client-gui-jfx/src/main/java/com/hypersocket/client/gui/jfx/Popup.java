package com.hypersocket.client.gui.jfx;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Popup extends Stage {

	private boolean sizeObtained;

	public Popup(Window parent, Scene scene) {
		this(parent, scene, true);
	}

	public Popup(Window parent, Scene scene, boolean dismissOnFocusLost) {
		super(Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW) ? StageStyle.TRANSPARENT : StageStyle.UNDECORATED);
		
		initOwner(parent);
		setScene(scene);
		setMinHeight(24);

		// This is to align the window AFTER we know it's size.
		ChangeListener<? super Number> l = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (!sizeObtained) {
					positionPopup();
					sizeObtained = true;
				}
			}
		};
		widthProperty().addListener(l);

		if (dismissOnFocusLost) {
			focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {

					if (oldValue && !newValue && !isChildFocussed()) {
						hide();
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								if (!parent.focusedProperty().get() && Configuration.getDefault().autoHideProperty().get()) {
									((Stage) parent).setIconified(true);
								}
							}
						});
					}
				}
			});
		}

		// Watch for all changes and reposition this popup if appropriate
		ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				positionPopup();
			}
		};
		Configuration cfg = Configuration.getDefault();
		cfg.topProperty().addListener(cl);
		cfg.bottomProperty().addListener(cl);
		cfg.leftProperty().addListener(cl);
		cfg.rightProperty().addListener(cl);
		cfg.avoidReservedProperty().addListener(cl);
		ChangeListener<Number> sl = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				positionPopup();
			}
		};
		cfg.sizeProperty().addListener(sl);
		cfg.monitorProperty().addListener(sl);
		

		Property<Color> colorProperty = cfg.colorProperty();
		colorProperty.addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				Client.setColors(scene);
			}
		});
		Client.setColors(scene);
		
		sizeToScene();
		positionPopup();
	}

	public void popupAndWait() {
		if (!isShowing()) {
			positionPopup();
			showAndWait();
		}
	}

	public void popup() {
		if (!isShowing()) {
			positionPopup();
			sizeToScene();
			System.out.println("Popping up stage with height of " + getHeight() + " scene: " + sceneProperty().get().getHeight() + " " + sceneProperty().get().getRoot().prefHeight(getHeight()));
			show();
		}
	}

	protected boolean isChildFocussed() {
		return false;
	}

	public void positionPopup() {
		Configuration cfg = Configuration.getDefault();
		if (cfg.topProperty().get()) {
			setY(getOwner().getY() + getOwner().getHeight());
			setX(getOwner().getX() + getOwner().getWidth() - getWidth());
		} else if (cfg.bottomProperty().get()) {
			setY(getOwner().getY() - getHeight());
			setX(getOwner().getX() + getOwner().getWidth() - getWidth());
		}
	}
}