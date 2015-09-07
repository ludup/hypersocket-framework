package com.hypersocket.client.gui.jfx;

import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.hypersocket.client.ServiceResource;

public class Status extends AbstractController {
	final static Logger LOG = Logger.getLogger(Status.class.getName());

	@FXML
	private VBox statusItems;

	@Override
	protected void onInitialize() {
		statusItems.focusTraversableProperty().set(true);
	}

	public void setResources(List<ServiceResource> group) {
		statusItems.getChildren().clear();
		for (ServiceResource item : group) {
			HBox hb = new HBox();
			hb.getStyleClass().add("item");

			// Icon
			Label status = new Label();
			status.setText(resources.getString("status.icon"));
			status.getStyleClass().add("icon");
			hb.getChildren().add(status);
			switch(item.getServiceStatus()) {
			case GOOD:
//			      Effect glow = new Glow(0.5);
//			      status.setEffect(glow);
			      status.setStyle("-fx-text-fill: green;");
			      break;
			case BAD:
//			      glow = new Glow(0.5);
//			      status.setEffect(glow);
			      status.setStyle("-fx-text-fill: red;");
			      break;      
			default:
				status.setOpacity(0.5f);
				break;
			}
			 

			// Text
			Label label = new Label();
			label.setText(item.getServiceDescription());
			hb.getChildren().add(label);
			
			//
			statusItems.getChildren().add(hb);
		}
	}

}
