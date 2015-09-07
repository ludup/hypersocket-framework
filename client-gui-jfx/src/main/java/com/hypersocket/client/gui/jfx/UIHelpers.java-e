package com.hypersocket.client.gui.jfx;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.Duration;

public class UIHelpers {
	static Logger log = LoggerFactory.getLogger(UIHelpers.class);
	private static boolean ttHackWarning;

	public static void hackTooltipStartTiming(Tooltip tooltip) {
		// http://stackoverflow.com/questions/26854301/control-javafx-tooltip-delay
		try {
			Field fieldBehavior = Tooltip.class.getDeclaredField("BEHAVIOR");
			fieldBehavior.setAccessible(true);
			Object objBehavior = fieldBehavior.get(tooltip);

			Field fieldTimer = objBehavior.getClass().getDeclaredField(
					"activationTimer");
			fieldTimer.setAccessible(true);
			Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

			objTimer.getKeyFrames().clear();
			objTimer.getKeyFrames().add(new KeyFrame(new Duration(50)));
		} catch (Exception e) {
			if (!ttHackWarning) {
				log.warn(
						"Tooltip work-around failed. Tooltip delay will not be adjusted.",
						e);
				ttHackWarning = true;
			}
		}
	}

	public static Tooltip createDockButtonToolTip(String text) {
		Configuration cfg = Configuration.getDefault();
		final Tooltip tt = new StyledTooltip(text) {
			@Override
			public void show(Window ownerWindow, double anchorX, double anchorY) {

				if (Dock.getInstance().arePopupsOpen()) {
					return;
				}

				Rectangle2D bnds = Client.getConfiguredBounds();

				if (cfg.leftProperty().get()) {
					anchorX = bnds.getMinX() + cfg.sizeProperty().doubleValue()
							+ 8.0 ;
				} else if (cfg.rightProperty().get()) {
					anchorX = bnds.getMaxX() - cfg.sizeProperty().doubleValue()
							- 8.0 - prefWidth(USE_COMPUTED_SIZE);
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
		
		// Hack the tooltip time
		hackTooltipStartTiming(tt);
		
		// Without this, there is weird double-clicking required behaviour for launcher and other buttons after an autohide
		tt.setConsumeAutoHidingEvents(false);
		tt.setAutoHide(true);
		
		return tt;
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
		return toHex(color, opacity ? color.getOpacity() : -1);
	}

	public static String toHex(Color color, double opacity) {
		if (opacity > -1)
			return String.format("#%02x%02x%02x%02x",
					(int) (color.getRed() * 255),
					(int) (color.getGreen() * 255),
					(int) (color.getBlue() * 255), (int) (opacity * 255));
		else
			return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
					(int) (color.getGreen() * 255),
					(int) (color.getBlue() * 255));
	}

}
