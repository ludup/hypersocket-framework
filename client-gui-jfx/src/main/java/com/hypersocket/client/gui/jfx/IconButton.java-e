package com.hypersocket.client.gui.jfx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconButton extends LauncherButton {
	static Logger log = LoggerFactory.getLogger(IconButton.class);

	private final static Map<String, Image> iconCache = new WeakHashMap<>();

	private Timeline bouncer = new Timeline();
	private Timeline shrinker = new Timeline();
	private Timeline grower = new Timeline();

	private boolean launching;

	public IconButton(ResourceBundle resources, ResourceItem resourceItem,
			Client context, ResourceGroupList group) {
		super(resources, resourceItem, context);
		getStyleClass().add("iconButton");
		setTooltipText(resourceItem.getResource().getName());
		String typeName = resourceItem.getResource().getType().name();
		try {
			System.out.println(resourceItem.getResource().getName() + " type: " + typeName + " icon: " + resourceItem.getResource().getIcon());
			if (resourceItem.getResource().getIcon() == null) {
				String imgPath = String.format("types/type-%s.png",
						typeName.toLowerCase());
				URL resource = getClass().getResource(imgPath);
				if (resource == null) {
					setText(resources.getString("resource.icon." + typeName));
					log.warn(String.format(
							"Falling back to text based icon for type %s",
							typeName));
				} else {
					final ImageView imageView = new ImageView(
							resource.toString());
					configureButton(imageView);
					setGraphic(imageView);
				}
			} else {

				final ImageView imageView = new ImageView(getClass()
						.getResource("ajax-loader.gif").toString());
				configureButton(imageView);
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

								String subType = Dock.getSubType(group);
								String imgPath = String.format("types/%s.png",
										subType);
								URL resource = getClass().getResource(imgPath);
								if (resource == null) {
									log.error("Failed to load icon.", re);
								} else {
									try {
										setImageFromResource(imageView,
												resource);
									} catch (IOException ioe) {
										log.error("Failed to load icon.", ioe);
									}
								}
							}
						}

						private void setImageFromResource(
								final ImageView imageView, URL resource)
								throws IOException {
							InputStream openStream = resource.openStream();
							try {
								Image img = new Image(openStream);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										imageView.setImage(img);
										sizeToImage();
									}
								});
							} finally {
								openStream.close();
							}
						}
					});
				}
			}
		} catch (MissingResourceException mre) {
			setText("%" + typeName);
		}
		sizeToImage();

		// Bouncer for click events
		double bounceSpeed = 50;
		bouncer.setCycleCount(3);
		bouncer.getKeyFrames().addAll(
				makeKeyFrame(0, 0, 1.0, 1.0),
				makeKeyFrame(bounceSpeed * 2f, 4, 1.0, 1.0),
				
				makeKeyFrame(bounceSpeed * 2.5f, 4, 1.1, 0.9),
				makeKeyFrame(bounceSpeed * 4.5f, 0, 1.0, 1.0),
				makeKeyFrame(bounceSpeed * 6.5f, -4, 1.0, 1.0),
				makeKeyFrame(bounceSpeed * 8.5f, 0, 1.0, 1.0)
				);
		bouncer.setOnFinished(value -> {
			if(launching) {
				bouncer.play();
			}
		});
		
		// Grower for hover in
		grower.getKeyFrames().addAll(makeKeyFrame(0, 0, 1.0, 1.0), makeKeyFrame(100, 0, 1.2, 1.2));

		// Shrinker for hover out
		shrinker.getKeyFrames().addAll(makeKeyFrame(0, 0, 1.2, 1.2), makeKeyFrame(100, 0, 1.0, 1.0));

		// Monitor mouse activity
		setOnMouseEntered(value -> {
			grower.play();
		});
		setOnMouseExited(value -> {
			shrinker.play();
		});
	}
	protected void onInitiateLaunch() {
		launching = true;
		bouncer.play();
	}
	
	protected void onFinishLaunch() {
		launching = false;
	}
	
	private KeyFrame makeKeyFrame(double d, double y, double sx, double sy) {
		return new KeyFrame(Duration.millis(d), 
				new KeyValue(translateYProperty(),y), 
				new KeyValue(scaleXProperty(), sx),
				new KeyValue(scaleYProperty(), sy));
	}

	private void configureButton(final ImageView imageView) {
		imageView.setFitHeight(32);
		imageView.setFitWidth(32);
		imageView.setPreserveRatio(true);
		imageView.getStyleClass().add("launcherIcon");
	}
}
