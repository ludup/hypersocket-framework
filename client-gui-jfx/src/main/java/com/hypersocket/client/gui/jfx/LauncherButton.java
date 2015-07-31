package com.hypersocket.client.gui.jfx;

import java.util.ResourceBundle;

import javafx.scene.control.OverrunStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LauncherButton extends ImageButton {
	static Logger log = LoggerFactory.getLogger(LauncherButton.class);

	public LauncherButton(ResourceBundle resources, ResourceItem resourceItem,
			Client context) {
		setTextOverrun(OverrunStyle.CLIP);
		setOnAction((event) -> {
			new Thread() {
				public void run() {
					launch(resourceItem);
				}
			}.start();
		});

	}
	
	protected void onBeforeLaunch() {
	}

	protected void launch(ResourceItem resourceItem) {
		onBeforeLaunch();
		new Thread("Launch") {
			public void run() {
				resourceItem.getResource().getResourceLauncher().launch();
			}
		}.start();
	}
}
