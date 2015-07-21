package com.hypersocket.client.gui.jfx;

import java.util.ResourceBundle;

import javafx.geometry.Pos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemButton extends LauncherButton {
	static Logger log = LoggerFactory.getLogger(ItemButton.class);

	public ItemButton(ResourceBundle resources, ResourceItem resourceItem,
			Client context) {
		super(resources, resourceItem, context);
		getStyleClass().add("itemButton");
		setMaxWidth(Double.MAX_VALUE);
		setAlignment(Pos.CENTER_LEFT);
	}
}
