package com.hypersocket.client.gui.jfx;

import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

@SuppressWarnings("restriction")
public class Configuration {

	private BooleanProperty showSSO = new SimpleBooleanProperty();
	private BooleanProperty showFiles = new SimpleBooleanProperty();
	private BooleanProperty showNetwork = new SimpleBooleanProperty();
	private BooleanProperty showBrowser= new SimpleBooleanProperty();
	private BooleanProperty top = new SimpleBooleanProperty();
	private BooleanProperty bottom = new SimpleBooleanProperty();
	private BooleanProperty left = new SimpleBooleanProperty();
	private BooleanProperty right = new SimpleBooleanProperty();
	private BooleanProperty autoHide = new SimpleBooleanProperty();
	private BooleanProperty avoidReserved = new SimpleBooleanProperty();
	private IntegerProperty size = new SimpleIntegerProperty();
	private IntegerProperty monitor = new SimpleIntegerProperty();
	private Property<Color> color = new SimpleObjectProperty<Color>();

	//
	private final static Configuration DEFAULT_INSTANCE = new Configuration(
			Preferences.userNodeForPackage(Configuration.class));
	
	class BooleanPreferenceUpdateChangeListener implements ChangeListener<Boolean> {
		
		private Preferences node;
		private String key;

		BooleanPreferenceUpdateChangeListener(Preferences node, String key) {
			this.node = node;
			this.key = key;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable,
				Boolean oldValue, Boolean newValue) {
			node.putBoolean(key, newValue);
		}
		
	}
	class ColorPreferenceUpdateChangeListener implements ChangeListener<Color> {
		
		private Preferences node;
		private String key;

		ColorPreferenceUpdateChangeListener(Preferences node, String key) {
			this.node = node;
			this.key = key;
		}

		@Override
		public void changed(ObservableValue<? extends Color> observable,
				Color oldValue, Color newValue) {
			putColor(key, node, newValue);
		}
		
	}

	public Configuration(Preferences node) {
		
		color.setValue(getColor("color", node, Color.WHITE));
		color.addListener(new ColorPreferenceUpdateChangeListener(node, "color"));
		
		showSSO.set(node.getBoolean("showSSO", true));
		showSSO.addListener(new BooleanPreferenceUpdateChangeListener(node, "showSSO"));
		
		showBrowser.set(node.getBoolean("showBrowser", true));
		showBrowser.addListener(new BooleanPreferenceUpdateChangeListener(node, "showBrowser"));
		
		showNetwork.set(node.getBoolean("showNetwork", true));
		showNetwork.addListener(new BooleanPreferenceUpdateChangeListener(node, "showNetwork"));
		
		showFiles.set(node.getBoolean("showNetwork", true));
		showFiles.addListener(new BooleanPreferenceUpdateChangeListener(node, "showNetwork"));

		autoHide.set(node.getBoolean("autoHide", true));
		autoHide.addListener(new BooleanPreferenceUpdateChangeListener(node, "autoHide"));
		
		avoidReserved.set(node.getBoolean("avoidReserved", true));
		avoidReserved.addListener(new BooleanPreferenceUpdateChangeListener(node, "avoidReserved"));
		
		size.set(node.getInt("size", 64));
		size.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				node.putDouble("size", newValue.doubleValue());
			}
		});

		String position = node.get("position", "top");
		top.set(position.equals("top"));
		bottom.set(position.equals("bottom"));
		left.set(position.equals("left"));
		right.set(position.equals("right"));
		ChangeListener<Boolean> positionListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(
					final ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue) {
					if (observable == left) {
						node.put("position", "left");
						top.set(false);
						bottom.set(false);
						right.set(false);
					} else if (observable == right) {
						node.put("position", "right");
						top.set(false);
						bottom.set(false);
						left.set(false);
					} else if (observable == top) {
						node.put("position", "top");
						right.set(false);
						bottom.set(false);
						left.set(false);
					} else if (observable == bottom) {
						node.put("position", "bottom");
						right.set(false);
						top.set(false);
						left.set(false);
					}
				}
			}
		};
		top.addListener(positionListener);
		bottom.addListener(positionListener);
		left.addListener(positionListener);
		right.addListener(positionListener);
	}

	public static Configuration getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	public IntegerProperty sizeProperty() {
		return size;
	}
	
	public IntegerProperty monitorProperty() {
		return monitor;
	}

	public BooleanProperty autoHideProperty() {
		return autoHide;
	}

	public BooleanProperty avoidReservedProperty() {
		return avoidReserved;
	}

	public BooleanProperty topProperty() {
		return top;
	}

	public BooleanProperty bottomProperty() {
		return bottom;
	}

	public BooleanProperty leftProperty() {
		return left;
	}

	public BooleanProperty rightProperty() {
		return right;
	}
	
	public BooleanProperty showSSOProperty() {
		return showSSO;
	}
	
	public BooleanProperty showWebProperty() {
		return showBrowser;
	}
	
	public BooleanProperty showNetworkProperty() {
		return showNetwork;
	}
	
	public BooleanProperty showFilesProperty() {
		return showFiles;
	}

	public Property<Color> colorProperty() {
		return color ;
	}
	
	static void putColor(String key, Preferences p, Color color) {
		p.putDouble(key + "_r", color.getRed());
		p.putDouble(key + "_g", color.getGreen());
		p.putDouble(key + "_b", color.getBlue());
		p.putDouble(key + "_a", color.getOpacity());
	}
	
	static Color getColor(String key, Preferences p, Color defaultColour) {
		return new Color(p.getDouble(key + "_r", defaultColour == null ? 1.0 : defaultColour.getRed()), 
				p.getDouble(key + "_g", defaultColour == null ? 1.0 : defaultColour.getGreen()), 
				p.getDouble(key + "_b", defaultColour == null ? 1.0 : defaultColour.getBlue()),
				p.getDouble(key + "_a", defaultColour == null ? 1.0 : defaultColour.getOpacity()));
	}
}
