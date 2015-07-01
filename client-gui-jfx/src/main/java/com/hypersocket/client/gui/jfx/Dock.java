package com.hypersocket.client.gui.jfx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.MissingResourceException;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;

public class Dock extends AbstractController implements Listener {
	static Logger log = LoggerFactory.getLogger(Main.class);

	private Popup signInPopup;
	private Popup optionsPopup;

	@FXML
	private Button slideLeft;
	@FXML
	private Button slideRight;
	@FXML
	private Button signIn;
	@FXML
	private Button options;
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

	private TranslateTransition slideTransition;
	private Rectangle slideClip;

	private SignIn signInScene;

	private Popup updatePopup;
	private static Dock instance;

	public Dock() {
		if (instance != null) {
			throw new IllegalStateException("Only allowed one dock instance.");
		}
		instance = this;
	}

	public static Dock getInstance() {
		return instance;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		Configuration cfg = Configuration.getDefault();

		networkResources.setTooltip(createDockButtonToolTip(resources
				.getString("network.toolTip")));
		networkResources.selectedProperty().bindBidirectional(
				cfg.showNetworkProperty());

		ssoResources.setTooltip(createDockButtonToolTip(resources
				.getString("sso.toolTip")));
		ssoResources.selectedProperty()
				.bindBidirectional(cfg.showSSOProperty());

		browserResources.setTooltip(createDockButtonToolTip(resources
				.getString("web.toolTip")));
		browserResources.selectedProperty().bindBidirectional(
				cfg.showWebProperty());

		fileResources.setTooltip(createDockButtonToolTip(resources
				.getString("files.toolTip")));
		fileResources.selectedProperty().bindBidirectional(
				cfg.showFilesProperty());

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
				recentre();
			}
		};
		shortcuts.widthProperty().addListener(l);

		cfg.sizeProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				recentre();
				sizeButtons();
			}
		});
		cfg.colorProperty().addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				styleToolTips();
			}
		});

		rebuildIcons();
		sizeButtons();
		recentre();
		setAvailable();
	}

	public boolean arePopupsOpen() {
		return (signInPopup != null && signInPopup.isShowing())
				|| (optionsPopup != null && optionsPopup.isShowing())
				|| (updatePopup != null && updatePopup.isShowing());
	}

	@Override
	public void initUpdate(int apps) {
		
		// Starting an update, so hide the all other windows and popup the updating window
		Window parent = this.scene.getWindow();
		if (updatePopup == null) {
			try {
				final Update updateScene = (Update)context
						.openScene(Update.class);
				
				// The update popup will get future update events, but it needs this one too to initialize
				updateScene.initUpdate(apps);
				
				updatePopup = new Popup(parent, updateScene.getScene(), false);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		if(signInPopup != null && signInPopup.isShowing())
			signInPopup.hide();
		if(optionsPopup != null && optionsPopup.isShowing())
			optionsPopup.hide();
		scene.getRoot().setDisable(true);
		updatePopup.popup();

	}

	private void recentre() {
		double centre = getLaunchBarOffset();
		slideLeft.disableProperty().set(centre > 0);
		slideRight.disableProperty().set(centre > 0);

		slideTransition.setFromX(shortcuts.getTranslateX());
		slideTransition.setToX(centre);
		slideTransition.stop();
		slideTransition.play();
	}

	private double getLaunchBarOffset() {
		double centre = (shortcutContainer.getWidth() - shortcuts.getWidth()) / 2d;
		return centre;
	}

	@FXML
	private void evtSlideLeft() {
		/*
		 * We should only get this action if the button is enabled, which means
		 * at least one button is partially obscured on the left
		 */

		double scroll = 0;
		boolean first = true;
		for (Node n : shortcuts.getChildren()) {

			/*
			 * The position of the child within the container. When we find a
			 * node that crosses '0', that is how much this single scroll will
			 * adjust by, so completely revealing the hidden nide
			 */
			double p = n.getLayoutX() + shortcuts.getTranslateX();

			double amt = p + n.getLayoutBounds().getWidth();
			if (amt >= 0) {
				scroll = n.getLayoutBounds().getWidth() - amt;
				break;
			} else {
				first = false;
			}
		}
		slideLeft.disableProperty().set(first);
		if (scroll > 0) {
			slideRight.disableProperty().set(false);
			slideTransition.setFromX(shortcuts.getTranslateX());
			slideTransition.setToX(shortcuts.getTranslateX() + scroll);
			slideTransition.play();
		}
	}

	@FXML
	private void evtSlideRight() {
		/*
		 * We should only get this action if the button is enabled, which means
		 * at least one button is partially obscured on the left
		 */

		double scroll = 0;
		boolean last = true;
		ObservableList<Node> c = shortcuts.getChildren();
		for (int i = c.size() - 1; i >= 0; i--) {
			Node n = c.get(i);
			double p = n.getLayoutX() + shortcuts.getTranslateX();
			if (p <= shortcutContainer.getWidth()) {
				scroll = n.getLayoutBounds().getWidth()
						- (shortcutContainer.getWidth() - p);
				break;
			} else {
				last = false;
			}
		}
		slideRight.disableProperty().set(last);
		if (scroll > 0) {
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
	private void evtOpenSignInWindow(ActionEvent evt) throws Exception {
		openSignInWindow();
	}

	@FXML
	private void evtOpenOptionsWindow(ActionEvent evt) throws Exception {
		Window parent = this.scene.getWindow();
		if (optionsPopup == null) {
			final FramedController optionsScene = context
					.openScene(Options.class);
			optionsPopup = new Popup(parent, optionsScene.getScene()) {
				@SuppressWarnings("restriction")
				protected boolean isChildFocussed() {
					// HACK!
					//
					// When the custom colour dialog is focussed, there doesn't
					// seem to be anyway of determining what the opposite
					// component was the gained the focus. Being as that is
					// the ONLY utility dialog, it should be the one
					for (Stage s : com.sun.javafx.stage.StageHelper.getStages()) {
						if (s.getStyle() == StageStyle.UTILITY) {
							return s.isShowing();
						}
					}
					return false;
				}
			};
		}
		optionsPopup.popup();
	}

	protected void onStateChanged() {
		log.info("State changed for dock, rebuilding");
		setAvailable();
		rebuildIcons();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				recentre();
			}
		});
	}

	private void openSignInWindow() throws IOException {
		Window parent = this.scene.getWindow();
		if (signInPopup == null) {
			signInScene = (SignIn) context.openScene(SignIn.class);
			signInPopup = new Popup(parent, signInScene.getScene());
			((SignIn) signInScene).setPopup(signInPopup);
		}
		signInPopup.popup();
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
							if (!ssoResources.isSelected())
								continue;
							break;
						case BROWSER:
							if (!browserResources.isSelected())
								continue;
							break;
						case FILE:
							if (!fileResources.isSelected())
								continue;
							break;
						case NETWORK:
							if (!networkResources.isSelected())
								continue;
							break;
						default:
							break;
						}
						shortcuts.getChildren().add(
								createLauncherButtonForResource(resourceRealm,
										r));

					}
				}
			} catch (Exception e) {
				log.error("Failed to get resources.", e);
			}
		}
	}

	private Button createLauncherButtonForResource(ResourceRealm resourceRealm,
			Resource r) {
		final Button b = new Button();
		b.setTextOverrun(OverrunStyle.CLIP);
		sizeButton(b);
		b.getStyleClass().add("iconButton");
		b.setOnAction((event) -> {
			new Thread() {
				public void run() {
					System.out.println("Launch: "
							+ r.getResourceLauncher().launch());
				}
			}.start();
		});

		b.setTooltip(createDockButtonToolTip(r.getName()));

		try {
			if (r.getIcon() == null) {
				b.setText(resources.getString("resource.icon."
						+ r.getType().name()));
			} else {

				final ImageView imageView = new ImageView(getClass()
						.getResource("ajax-loader.gif").toString());
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
									.getBlob(resourceRealm.getName(),
											r.getIcon(), 10000);
							final Image img = new Image(
									new ByteArrayInputStream(arr));
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									imageView.setImage(img);
									sizeButton(b);
								}
							});
						} catch (RemoteException re) {
							log.error("Failed to load icon.", re);
						}
					}
				}.start();
			}
		} catch (MissingResourceException mre) {
			b.setText("%" + r.getType().name());
		}
		return b;
	}

	private Tooltip createDockButtonToolTip(String text) {
		Configuration cfg = Configuration.getDefault();
		Color newValue = cfg.colorProperty().getValue();
		final Tooltip tt = new Tooltip(text) {
			@Override
			public void show(Window ownerWindow, double anchorX, double anchorY) {
				Configuration cfg = Configuration.getDefault();
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
		tt.setAutoHide(true);
		tt.setStyle(String.format("-fx-background: #%02x%02x%02x",
				(int) (newValue.getRed() * 255),
				(int) (newValue.getGreen() * 255),
				(int) (newValue.getBlue() * 255)));
		tt.setStyle(String.format("-fx-text-fill: %s",
				newValue.getBrightness() < 0.5f ? "#ffffff" : "#000000"));
		tt.setStyle(String.format("-fx-background-color: #%02x%02x%02x",
				(int) (newValue.getRed() * 255),
				(int) (newValue.getGreen() * 255),
				(int) (newValue.getBlue() * 255)));
		return tt;
	}

	private void styleToolTips() {
		for (Node s : shortcuts.getChildren()) {
			recreateTooltip((ButtonBase) s);
		}
		recreateTooltip(fileResources);
		recreateTooltip(networkResources);
		recreateTooltip(ssoResources);
		recreateTooltip(browserResources);
	}

	private void recreateTooltip(ButtonBase bb) {
		bb.setTooltip(createDockButtonToolTip(bb.getTooltip().getText()));
	}

	private void sizeButtons() {
		sizeButton(networkResources);
		sizeButton(fileResources);
		sizeButton(ssoResources);
		sizeButton(browserResources);
		sizeButton(slideLeft);
		sizeButton(slideRight);
		sizeButton(signIn);
		sizeButton(options);
		for (Node n : shortcuts.getChildren()) {
			sizeButton((ButtonBase) n);
		}
	}

	private void sizeButton(ButtonBase button) {
		Configuration cfg = Configuration.getDefault();
		int sz = cfg.sizeProperty().get();
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

	private void setAvailable() {
		if (context.getBridge().isConnected()) {
			int connected = 0;
			try {
				List<ConnectionStatus> connections = context.getBridge()
						.getClientService().getStatus();
				for (ConnectionStatus c : connections) {
					if (c.getStatus() == ConnectionStatus.CONNECTED) {
						connected++;
					}
				}
				log.info(String.format("Bridge says %d are connected of %d",
						connected, connections.size()));
				if (connected > 0) {
					signIn.setStyle("-fx-text-fill: #00aa00");
				} else {
					signIn.setStyle("-fx-text-fill: #aa0000");
				}
			} catch (Exception e) {
				log.error("Failed to check connection state.", e);
				signIn.setStyle("-fx-text-fill: #777777");
			}
		} else {
			log.info("Bridge says not connected");
			signIn.setStyle("-fx-text-fill: #777777");
		}
	}

	public void notify(String msg, int type) {
		if (signInPopup == null || !signInPopup.isShowing()) {
			try {
				openSignInWindow();
			} catch (IOException e) {
				log.error("Failed to open sign in window.", e);
			}
		}
		switch (type) {
		case GUICallback.NOTIFY_CONNECT:
		case GUICallback.NOTIFY_DISCONNECT:
		case GUICallback.NOTIFY_INFO:
			signInScene.setMessage(AlertType.INFORMATION, msg);
			break;
		case GUICallback.NOTIFY_WARNING:
			signInScene.setMessage(AlertType.WARNING, msg);
			break;
		case GUICallback.NOTIFY_ERROR:
			signInScene.setMessage(AlertType.ERROR, msg);
			break;
		}
	}

}
