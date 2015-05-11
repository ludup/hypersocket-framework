package com.hypersocket.client.gui.jfx;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;

public class Dock extends AbstractController implements Listener {
	static Logger log = LoggerFactory.getLogger(Main.class);

	private Preferences preferences = Preferences
			.userNodeForPackage(Dock.class);

	private Popup signInPopup;
	private Popup optionsPopup;

	@FXML
	private Button slideLeft;
	@FXML
	private Button slideRight;
	@FXML
	private Button signIn;
	@FXML
	private HBox shortcuts;
	@FXML
	private ToggleButton fileResources;
	@FXML
	private ToggleButton browserResources;
	@FXML
	private ToggleButton networkResources;
	@FXML
	private ToggleButton ssoResources;
	@FXML
	private HBox shortcutContainer;
	// @FXML
	// private StackPane stack;

	private TranslateTransition slideTransition;
	private double offset;

	private Rectangle slideClip;

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setAvailable();
		fileResources
				.setSelected(preferences.getBoolean("fileResources", true));
		browserResources.setSelected(preferences.getBoolean("browserResources",
				true));
		networkResources.setSelected(preferences.getBoolean("networkResources",
				true));
		ssoResources.setSelected(preferences.getBoolean("ssoResources", true));
		context.getBridge().addListener(this);

		slideTransition = new TranslateTransition(Duration.seconds(0.5),
				shortcuts);
		slideTransition.setAutoReverse(false);
		slideTransition.setCycleCount(1);

		slideClip = new Rectangle();
		slideClip.widthProperty().bind(shortcutContainer.widthProperty());
		slideClip.heightProperty().bind(shortcutContainer.heightProperty());
		shortcutContainer.setClip(slideClip);

		/*
		 * Watch for the width of the inner launchers pane. This will get called
		 * when launchers are added and removed.
		 */
		ChangeListener<? super Number> l = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {

				double centre = getLaunchBarOffset();
				slideLeft.disableProperty().set(centre > 0);
				slideRight.disableProperty().set(centre > 0);
				
				
				// offset = 100;
				System.err.println("rebuild: offset: " + centre + " clip: "
						+ slideClip.toString() + " " + offset + " barwidth: "
						+ shortcuts.getBoundsInParent().getWidth() + "/"
						+ shortcuts.getWidth() + "/"
						+ shortcutContainer.getBoundsInLocal().getWidth()
						+ " available: " + shortcutContainer.getWidth());
				slideTransition.setFromX(shortcuts.getTranslateX());
				slideTransition.setToX(centre);
				slideTransition.play();
			}
		};
		shortcuts.widthProperty().addListener(l);

		// shortcuts.layoutXProperty().bind(shortcutContainer.widthProperty().subtract(shortcuts.prefWidth(-1)).divide(2));

		// Region clipRegion = new Region();
		// clipRegion.w
		//
		// shortcuts.setClip(clipRegion);
		rebuildIcons();
	}

	private double getLaunchBarOffset() {
		double centre = (shortcutContainer.getWidth() - shortcuts.getWidth()) / 2d;
		return centre;
	}

	@FXML
	private void evtSlideLeft() {
		/* We should only get this action if the button is enabled, which means
		 * at least one button is partially obscured on the left 
		 */
		
		double scroll = 0;
		boolean first = true;
		for(Node n : shortcuts.getChildren()) {
			
			/* The position of the child within the container. When we find
			 * a node that crosses '0', that is how much this single scroll
			 * will adjust by, so completely revealing the hidden nide 
			 */
			double p = n.getLayoutX() + shortcuts.getTranslateX();
			
			double amt = p + n.getLayoutBounds().getWidth(); 
			if(amt >=0) {
				scroll =  n.getLayoutBounds().getWidth() - amt;
				System.out.println("Reveal " + scroll);
				break;
			}
			else {
				first = false;
			}
		}
		slideLeft.disableProperty().set(first);
		if(scroll >0) {
			slideRight.disableProperty().set(false);
			slideTransition.setFromX(shortcuts.getTranslateX());
			slideTransition.setToX(shortcuts.getTranslateX() + scroll);
			slideTransition.play();
		}
	}

	@FXML
	private void evtSlideRight() {
		/* We should only get this action if the button is enabled, which means
		 * at least one button is partially obscured on the left 
		 */
		
		double scroll = 0;
		boolean last = true;
		ObservableList<Node> c = shortcuts.getChildren();
		for(int i = c.size() - 1 ; i >= 0 ; i--) {
			Node n = c.get(i);
			double p = n.getLayoutX() + shortcuts.getTranslateX();
			if(p <= shortcutContainer.getWidth()) {
				scroll = n.getLayoutBounds().getWidth() - ( shortcutContainer.getWidth() - p );
				break;
			}
			else {
				last = false;
			}
		}
		slideRight.disableProperty().set(last);
		if(scroll >0) {
			slideLeft.disableProperty().set(false);
			slideTransition.setFromX(shortcuts.getTranslateX());
			slideTransition.setToX(shortcuts.getTranslateX() - scroll);
			slideTransition.play();
		}
	}

	@FXML
	private void evtRefilter() {
		rebuildIcons();
	}

	@FXML
	private void evtExit(ActionEvent evt) throws Exception {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(resources.getString("exit.confirm.title"));
		alert.setHeaderText(resources.getString("exit.confirm.header"));
		alert.setContentText(resources.getString("exit.confirm.content"));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			System.exit(0);
		}
	}

	@FXML
	private void evtOpenSignInWindow(ActionEvent evt) throws Exception {
		Window parent = this.scene.getWindow();
		if (signInPopup == null) {
			Scene signInScene = context.openScene(SignIn.class);
			signInPopup = new Popup(parent, signInScene);
		}
		signInPopup.popup();
	}

	@FXML
	private void evtOpenOptionsWindow(ActionEvent evt) throws Exception {
		Window parent = this.scene.getWindow();
		if (optionsPopup == null) {
			Scene optionsScene = context.openScene(Options.class);
			optionsPopup = new Popup(parent, optionsScene);
		}
		optionsPopup.popup();
	}

	protected void onStateChanged() {
		setAvailable();
		rebuildIcons();
	}

	private void rebuildIcons() {
		shortcuts.getChildren().clear();
		if (context.getBridge().isConnected()) {
			ResourceService resourceService = context.getBridge()
					.getResourceService();
			try {
				for (ResourceRealm resourceRealm : resourceService
						.getResourceRealms()) {
					for (Resource r : resourceRealm.getResources()) {
						switch (r.getType()) {
						case SSO:
							if (!ssoResources.isSelected()) {
								continue;
							}
							break;
						case BROWSER:
							if (!browserResources.isSelected()) {
								continue;
							}
							break;
						case FILE:
							if (!fileResources.isSelected()) {
								continue;
							}
							break;
						case NETWORK:
							if (!networkResources.isSelected()) {
								continue;
							}
							break;
						default:
							break;
						}
						Button b = new Button();
						b.setTextOverrun(OverrunStyle.CLIP);
						b.setPrefHeight(56);
						b.setPrefWidth(56);
						b.getStyleClass().add("iconButton");
						b.setOnAction((event) -> {
							new Thread() {
								public void run() {
									System.out.println("Launch: "
											+ r.getResourceLauncher().launch());
								}
							}.start();
						});
						Tooltip tt = new Tooltip(r.getName());
						b.setTooltip(tt);
						try {
							if (r.getIcon() == null) {
								b.setText(resources.getString("resource.icon."
										+ r.getType().name()));
							} else {

								final ImageView imageView = new ImageView(
										getClass().getResource(
												"ajax-loader.gif").toString());
								imageView.setFitHeight(32);
								imageView.setFitWidth(32);
								imageView.setPreserveRatio(true);
								imageView.getStyleClass().add("launcherIcon");
								b.setGraphic(imageView);

								// Load the actual logo in a thread, it may take
								// a short while
								new Thread() {
									public void run() {
										try {
											byte[] arr = context
													.getBridge()
													.getClientService()
													.getBlob(
															resourceRealm
																	.getName(),
															r.getIcon(), 10000);
											final Image img = new Image(
													new ByteArrayInputStream(
															arr));
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													imageView.setImage(img);
												}
											});
										} catch (RemoteException re) {
											log.error("Failed to load icon.",
													re);
										}
									}
								}.start();
							}
						} catch (MissingResourceException mre) {
							b.setText("%" + r.getType().name());
						}
						shortcuts.getChildren().add(b);
						System.out.println("b: " + b.getPrefWidth() + " / "
								+ b.getWidth() + " / "
								+ b.getBoundsInLocal().getWidth() + " / "
								+ b.getBoundsInParent().getWidth());

					}
				}
			} catch (Exception e) {
				log.error("Failed to get resources.", e);
			}
		}

		// Now need to centre the bar based on the new icons

		// shortcuts.layout();
		// shortcutContainer.layout();
		//
		// double centre = (shortcutContainer.getWidth() - shortcuts.getWidth())
		// / 2d;
		// // offset = 100;
		// System.err.println("rebuild: offset: " + centre + " clip: "
		// + slideClip.toString() + " " + offset + " barwidth: "
		// + shortcuts.getBoundsInParent().getWidth() + "/"
		// + shortcuts.getWidth() + "/"
		// + shortcutContainer.getBoundsInLocal().getWidth()
		// + " available: " + shortcutContainer.getWidth());
		// slideTransition.setToX(centre);
		// slideTransition.play();

		// left off: Rectangle[x=0.0, y=0.0, width=1315.0, height=64.0,
		// fill=0x000000ff] 0 barwidth: 310.0 available: 1315.0
	}

	private void setAvailable() {
		if (context.getBridge().isConnected()) {
			int connected = 0;
			try {
				for (Connection c : context.getBridge().getConnectionService()
						.getConnections()) {
					if (context.getBridge().getClientService().isConnected(c)) {
						connected++;
					}
				}
			} catch (Exception e) {
				log.error("Failed to check connection state.", e);
			}
			if (connected > 0) {
				signIn.setStyle("-fx-text-fill: #00aa00");
			} else {
				signIn.setStyle("-fx-text-fill: #aa0000");
			}
		} else {
			signIn.setStyle("-fx-text-fill: #777777");
		}
	}

}
