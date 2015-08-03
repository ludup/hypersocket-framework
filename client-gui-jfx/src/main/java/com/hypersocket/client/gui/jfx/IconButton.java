package com.hypersocket.client.gui.jfx;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconButton extends LauncherButton {
	static Logger log = LoggerFactory.getLogger(IconButton.class);

	private final static Map<String, Image> iconCache = new WeakHashMap<>();

	public IconButton(ResourceBundle resources, ResourceItem resourceItem,
			Client context) {
		super(resources, resourceItem, context);
		getStyleClass().add("iconButton");
		setTooltipText(resourceItem.getResource().getName());
		try {
			if (resourceItem.getResource().getIcon() == null) {
				setText(resources.getString("resource.icon."
						+ resourceItem.getResource().getType().name()));
			} else {

				final ImageView imageView = new ImageView(getClass()
						.getResource("ajax-loader.gif").toString());
				imageView.setFitHeight(32);
				imageView.setFitWidth(32);
				imageView.setPreserveRatio(true);
				imageView.getStyleClass().add("launcherIcon");
				setGraphic(imageView);

				String cacheKey = resourceItem.getResourceRealm().getName()
						+ "-" + resourceItem.getResource().getIcon();
				if (iconCache.containsKey(cacheKey)) {
					imageView.setImage(iconCache.get(cacheKey));
				} else {
					context.getLoadQueue().execute(new Runnable() {
						@Override
						public void run() {
							try {
								byte[] arr = context
										.getBridge()
										.getClientService()
										.getBlob(
												resourceItem.getResourceRealm()
														.getName(),
												resourceItem.getResource()
														.getIcon(), 10000);
								Image img = new Image(new ByteArrayInputStream(
										arr));
								iconCache.put(cacheKey, img);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										imageView.setImage(img);
										sizeToImage();
									}
								});
							} catch (RemoteException re) {
								log.error("Failed to load icon.", re);
							}
						}
					});
				}
			}
		} catch (MissingResourceException mre) {
			setText("%" + resourceItem.getResource().getType().name());
		}
		sizeToImage();
	}
}
