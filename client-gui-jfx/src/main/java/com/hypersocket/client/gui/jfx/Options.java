package com.hypersocket.client.gui.jfx;

import java.io.File;
import java.text.MessageFormat;
import java.util.Calendar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import com.hypersocket.HypersocketVersion;
import com.hypersocket.client.gui.jfx.Configuration.BrowserType;
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
	private CheckBox alwaysOnTop;

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
	@FXML
	private Button browse;
	@FXML
	private RadioButton systemBrowser;
	@FXML
	private RadioButton runCommand;
	@FXML
	private TextField browserCommand;
	@FXML
	private Label copyright;
	@FXML
	private Label version;
	@FXML
	private ImageView logo;

	private Configuration cfg;
	private FileChooser fileChooser;

	@Override
	protected void onInitialize() {
	}

	@Override
	protected void onConfigure() {

		cfg = Configuration.getDefault();
		alwaysOnTop.selectedProperty().bindBidirectional(
				cfg.alwaysOnTopProperty());
		autoHide.selectedProperty().bindBidirectional(cfg.autoHideProperty());
		avoidReserved.selectedProperty().bindBidirectional(
				cfg.avoidReservedProperty());
		top.selectedProperty().bindBidirectional(cfg.topProperty());
		bottom.selectedProperty().bindBidirectional(cfg.bottomProperty());
		left.selectedProperty().bindBidirectional(cfg.leftProperty());
		right.selectedProperty().bindBidirectional(cfg.rightProperty());
		size.valueProperty().bindBidirectional(cfg.sizeProperty());
		color.valueProperty().bindBidirectional(cfg.colorProperty());
		color.setSkin(new CustomColorPickerSkin(color));

		cfg.colorProperty().addListener(new ChangeListener<Color>() {

			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				setLogo();
			}
		});

		int idx = 1;
		for (Screen s : Screen.getScreens()) {
			Button b = new Button();
			b.getStyleClass().add("uiButton");
			b.setOnAction((event) -> {
				moveToScreen(s);
			});
			b.setText(String.valueOf(idx));
			monitorContainer.getChildren().add(b);
			idx++;
		}

		// Browser type
		ToggleGroup browserToggleGroup = new ToggleGroup();
		systemBrowser.setToggleGroup(browserToggleGroup);
		runCommand.setToggleGroup(browserToggleGroup);
		switch (cfg.browserTypeProperty().getValue()) {
		case RUN_COMMAND:
			runCommand.selectedProperty().set(true);
			break;
		default:
			systemBrowser.selectedProperty().set(true);
			break;
		}
		browserCommand.textProperty().bindBidirectional(
				cfg.browserCommandProperty());

		// Show a popover when run command is focussed
		browserCommand.focusedProperty().addListener(
				new ChangeListener<Boolean>() {
					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue, Boolean newValue) {
						if (newValue)
							showPopOver(
									resources.getString("runCommand.tooltip"),
									browserCommand);
					}
				});

		// Setup about text
		Calendar c = Calendar.getInstance();
		copyright.setText(MessageFormat.format(
				resources.getString("copyright"),
				String.valueOf(c.get(Calendar.YEAR))));
		version.setText(MessageFormat.format(
				resources.getString("version"),
				HypersocketVersion.getVersion(context.getBridge()
						.getExtensionPlace().getApp())));
		setLogo();

		setAvailable();
	}

	public boolean isCustomColorPopupShowing() {
		return ((ColorPalette) ((CustomColorPickerSkin) color.getSkin())
				.getPopupContent()).isCustomColorDialogShowing();
	}

	private void moveToScreen(Screen s) {
		Configuration.getDefault().monitorProperty()
				.set(Screen.getScreens().indexOf(s));
	}

	public void evtBrowse(ActionEvent ae) {
		fileChooser = new FileChooser();
		try {
			popup.setDismissOnFocusLost(false);
			fileChooser.setTitle("Choose Command");
			File selectedFile = fileChooser.showOpenDialog(scene.getWindow());
			if (selectedFile != null) {
				browserCommand.setText("\"" + selectedFile.getAbsolutePath()
						+ "\"");
			}
		} finally {
			popup.setDismissOnFocusLost(true);
			fileChooser = null;
		}
	}

	public void evtBrowserChange(ActionEvent ae) {
		cfg.browserTypeProperty().setValue(
				runCommand.isSelected() ? BrowserType.RUN_COMMAND
						: BrowserType.SYSTEM_BROWSER);
		setAvailable();
	}

	void setAvailable() {
		browserCommand.setDisable(systemBrowser.isSelected());
		browse.setDisable(systemBrowser.isSelected());
	}

	void setLogo() {
		Color c = cfg.colorProperty().getValue();
		if (c.getBrightness() < 0.5)
			logo.setImage(new Image(getClass().getResource("logo.png")
					.toExternalForm()));
		else
			logo.setImage(new Image(getClass().getResource("logo-black.png")
					.toExternalForm()));
	}
	
	@FXML
	private void evtShowTooltipPopover(MouseEvent evt) {
		System.out.println(evt);;
		if(evt.getSource() == avoidReserved) {
			showPopOver(
					resources.getString("avoidReserved.toolTip"),
					avoidReserved);
		}
	}
	
	@FXML
	private void evtHideTooltipPopover(MouseEvent evt) {
		if(evt.getSource() == avoidReserved) {
			hidePopOver();
		}
	}

	class CustomColorPickerSkin extends ColorPickerSkin {

		public CustomColorPickerSkin(ColorPicker colorPicker) {
			super(colorPicker);
		}

		@Override
		public Node getPopupContent() {
			return super.getPopupContent();
		}
	}
}
