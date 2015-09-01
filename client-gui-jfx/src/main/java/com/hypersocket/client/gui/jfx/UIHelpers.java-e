package com.hypersocket.client.gui.jfx;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public class UIHelpers {

	public static Tooltip createDockButtonToolTip(String text) {
		Configuration cfg = Configuration.getDefault();
		final Tooltip tt = new StyledTooltip(text) {
			@Override
			public void show(Window ownerWindow, double anchorX, double anchorY) {
				Rectangle2D bnds = Client.getConfiguredBounds();
	
				if (cfg.leftProperty().get()) {
				} else if (cfg.rightProperty().get()) {
				} else if (cfg.bottomProperty().get()) {
					anchorY = bnds.getMaxY() - cfg.sizeProperty().doubleValue()
							- 8.0 - prefHeight(USE_COMPUTED_SIZE);
				} else {
					anchorY = cfg.sizeProperty().doubleValue() + bnds.getMinY()
							+ 8.0;
				}
	
				super.show(ownerWindow, anchorX, anchorY);
			}
		};
		styleToolTip(tt);
		return tt;
	}

	public static void styleToolTip(final Tooltip tt) {
//		Configuration cfg = Configuration.getDefault();
//		Color newValue = cfg.colorProperty().getValue();
		tt.setAutoHide(true);
//		tt.setStyle(String.format("-fx-background: #%02x%02x%02x",
//				(int) (newValue.getRed() * 255),
//				(int) (newValue.getGreen() * 255),
//				(int) (newValue.getBlue() * 255)));
//		tt.setStyle(String.format("-fx-text-fill: %s",
//				newValue.getBrightness() < 0.5f ? "#ffffff" : "#000000"));
//		tt.setStyle(String.format("-fx-background-color: #%02x%02x%02x",
//				(int) (newValue.getRed() * 255),
//				(int) (newValue.getGreen() * 255),
//				(int) (newValue.getBlue() * 255)));
	}

	public static void sizeToImage(ButtonBase button) {
		int sz = Configuration.getDefault().sizeProperty().get();
		int df = sz / 8;
		sz -= df;
		if (button.getGraphic() != null) {
			ImageView iv = ((ImageView) button.getGraphic());
			iv.setFitWidth(sz - df);
			iv.setFitHeight(sz - df);
		} else {
			int fs = (int) ((float) sz * 0.6f);
			button.setStyle("-fx-font-size: " + fs + "px;");
		}
		button.setMinSize(sz, sz);
		button.setMaxSize(sz, sz);
		button.setPrefSize(sz, sz);
		button.layout();
	}

	public static String toHex(Color color, boolean opacity) {
		if (opacity)
			return String.format("#%02x%02x%02x%02x",
					(int) (color.getRed() * 255),
					(int) (color.getGreen() * 255),
					(int) (color.getBlue() * 255),
					(int) (color.getOpacity() * 255));
		else
			return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
					(int) (color.getGreen() * 255),
					(int) (color.getBlue() * 255));
	}

}
