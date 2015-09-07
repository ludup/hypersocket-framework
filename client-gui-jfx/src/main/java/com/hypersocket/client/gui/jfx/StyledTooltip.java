package com.hypersocket.client.gui.jfx;

import java.net.MalformedURLException;

import javafx.scene.control.Tooltip;
import javafx.stage.Window;

public class StyledTooltip extends Tooltip {

	private boolean ssAdded;

	public StyledTooltip() {
		super();
	}

	public StyledTooltip(String text) {
		super(text);
	}

	@Override
	public void show(Window ownerWindow, double anchorX, double anchorY) {
		if (!ssAdded) {
			try {
				String externalForm = Client.getCustomCSSFile().toURI().toURL().toExternalForm();
				getScene().getRoot().getStyleClass().add(externalForm);
			} catch (MalformedURLException e) {
			}
			ssAdded = true;
		}
		
		super.show(ownerWindow, anchorX, anchorY);
	}
}
