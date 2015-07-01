package com.hypersocket.client.gui.jfx;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import com.sun.javafx.scene.control.skin.ColorPalette;
import com.sun.javafx.scene.control.skin.ColorPickerSkin;


/*
 * The class uses a nasty hack using restricted API to try and determine if the custom colour picker is 
 * open. If it is, we don't want it's receipt of focus to autohide the dock.
 */
@SuppressWarnings("restriction")
public class Options extends AbstractController {
	
	@FXML
	private CheckBox autoHide;
	
	@FXML
	private CheckBox avoidReserved;
	
	@FXML
	private ToggleButton top;
	
	@FXML
	private ToggleButton bottom;
	
	@FXML
	private ToggleButton left;
	
	@FXML
	private ToggleButton right;
	
	@FXML
	private Slider size;
	
	@FXML
	private ColorPicker color;
	
	@FXML
	private HBox monitorContainer;

	@Override
	protected void onInitialize() {
	}

	@Override
	protected void onConfigure() {
		Configuration cfg = Configuration.getDefault();
		autoHide.selectedProperty().bindBidirectional(cfg.autoHideProperty());
		avoidReserved.selectedProperty().bindBidirectional(cfg.avoidReservedProperty());
		top.selectedProperty().bindBidirectional(cfg.topProperty());
		bottom.selectedProperty().bindBidirectional(cfg.bottomProperty());
		left.selectedProperty().bindBidirectional(cfg.leftProperty());
		right.selectedProperty().bindBidirectional(cfg.rightProperty());
		size.valueProperty().bindBidirectional(cfg.sizeProperty());
		color.valueProperty().bindBidirectional(cfg.colorProperty());
		color.setSkin(new CustomColorPickerSkin(color));
		
		int idx = 1;
		for(Screen s : Screen.getScreens()) {
			Button b = new Button();
			b.setOnAction((event) -> {
				moveToScreen(s);
			});
			b.setText(String.valueOf(idx));
			monitorContainer.getChildren().add(b);
			idx++;
		}
	}

	public boolean isCustomColorPopupShowing() {
		return ((ColorPalette)((CustomColorPickerSkin)color.getSkin()).getPopupContent()).isCustomColorDialogShowing();
	}
	
	private void moveToScreen(Screen s) {
		Configuration.getDefault().monitorProperty().set(Screen.getScreens().indexOf(s));
	}

	class CustomColorPickerSkin extends ColorPickerSkin {

		public CustomColorPickerSkin(ColorPicker colorPicker) {
			super(colorPicker);
		}
		

	    @Override public Node getPopupContent() {
	    	return super.getPopupContent();
	    }
	}
}
