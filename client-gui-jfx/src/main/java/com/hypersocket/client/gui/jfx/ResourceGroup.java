package com.hypersocket.client.gui.jfx;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ResourceGroup extends AbstractController {
	final static Logger LOG = Logger.getLogger(ResourceGroup.class.getName());

	@FXML
	private VBox resourceItems;

	@Override
	protected void onInitialize() {
	}

	public void setResources(ResourceGroupList group) {
		resourceItems.getChildren().clear();
		for (ResourceItem item : group.getItems()) {
			ItemButton button = new ItemButton(resources, item, context) {
				@Override
				protected void onBeforeLaunch() {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							popup.hide();
						}
					});
				}
			};
			button.setText(item.getResource().getName());
			resourceItems.getChildren().add(button);
		}
	}

}
