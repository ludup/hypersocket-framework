package com.hypersocket.client.gui.jfx;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.control.OverrunStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LauncherButton extends ImageButton {
	static Logger log = LoggerFactory.getLogger(LauncherButton.class);

	public LauncherButton(ResourceBundle resources, ResourceItem resourceItem,
			Client context) {
		setTextOverrun(OverrunStyle.CLIP);
		setOnAction((event) -> {
			onInitiateLaunch();
			new Thread() {
				public void run() {
					launch(resourceItem);
				}
			}.start();
		});

	}
	
	protected void onInitiateLaunch() {
		// Called before the launch on the JFX thread
	}
	
	protected void onBeforeLaunch() {
		// Called before the launch on the launch thread
	}
	
	protected void onAfterLaunch() {
		// Called when launch is complete on the launch thread
	}
	
	protected void onFinishLaunch() {
		// Called when launch is complete on the JFX thread
	}

	protected void launch(ResourceItem resourceItem) {
		onBeforeLaunch();
		Thread t = new Thread("Launch") {
			public void run() {
				try {
					resourceItem.getResource().getResourceLauncher().launch();
				}
				finally {
					onAfterLaunch();
					Platform.runLater(() -> onFinishLaunch());
				}
				
			}
		};
		t.setDaemon(true);
		t.start();
	}
}
