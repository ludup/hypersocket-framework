package com.hypersocket.client.gui.jfx;

import javafx.scene.control.Button;

public class ImageButton extends Button {

	public void sizeToImage() {
		UIHelpers.sizeToImage(this);
	}

	public void setTooltipText(String text) {
		setTooltip(UIHelpers.createDockButtonToolTip(text));
	}
}
