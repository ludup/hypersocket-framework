package com.hypersocket.client.gui.jfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.ServiceResource;
import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.gui.jfx.Flinger.Direction;
import com.hypersocket.client.gui.jfx.Popup.PositionType;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.Resource.Type;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;

public class Dock extends AbstractController implements Listener {

	public enum Mode {
		LAUNCHERS, UPDATE, IDLE
	}

	/**
	 * How wide (or high when vertical mode is supported) will the 'tab' be,
	 * i.e. the area where the user hovers over to dock to reveal it
	 */
	static final int AUTOHIDE_TAB_SIZE = 80;

	/*
	 * How height (or wide when vertical mode is supported) will the 'tab' be,
	 * i.e. the area where the user hovers over to dock to reveal it
	 */
	static final int AUTOHIDE_TAB_OPPOSITE_SIZE = 18;

	/* How long the autohide should take to complete (in MS) */

	static final int AUTOHIDE_DURATION = 125;

	/*
	 * How long after the mouse leaves the dock area, will the dock be hidden
	 * (in MS)
	 */
	static final int AUTOHIDE_HIDE_TIME = 2000;

	/*
	 * How long (in MS) to keep the dock open after a launch. This prevents
	 * autohide when focus is lost for a number of milliseconds. If focus is
	 * regained, the timer is cleared.
	 */
	static final int LAUNCH_WAIT = 2000;

	/*
	 * How long to keep popup messages visible (in MS)
	 */
	static final double MESSAGE_FADE_TIME = 10000;

	static Logger log = LoggerFactory.getLogger(Dock.class);

	private Popup signInPopup;
	private Popup optionsPopup;
	private Popup resourceGroupPopup;

	@FXML
	private Button slideLeft;
	@FXML
	private Button slideRight;
	@FXML
	private Button signIn;
	@FXML
	private Button options;
	@FXML
	private Button status;
	@FXML
	private Pane shortcuts;
	@FXML
	private Flinger flinger;
	@FXML
	private ToggleButton fileResources;
	@FXML
	private ToggleButton browserResources;
	@FXML
	private ToggleButton networkResources;
	@FXML
	private ToggleButton ssoResources;
	@FXML
	private BorderPane dockContent;
	@FXML
	private StackPane dockStack;
	@FXML
	private Label pull;
	@FXML
	private Button exit;

	private SignIn signInScene;
	private Timeline dockHider;
	private boolean hidden;
	private Timeline dockHiderTrigger;
	private Timeline launchWait;
	private long yEnd;
	private boolean hiding;
	private ContextMenu contextMenu;
	private Configuration cfg;
	private static Dock instance;
	private Map<ResourceGroupKey, ResourceGroupList> icons = new TreeMap<>();
	private List<ServiceResource> serviceResources = new ArrayList<>();
	// private ResourceGroup resourceGroup;
	private Popup statusPopup;
	private Status statusContent;

	private ChangeListener<Number> sizeChangeListener;
	private ChangeListener<Color> colorChangeListener;
	private ChangeListener<Boolean> borderChangeListener;
	private AbstractController optionsScene;
	private Mode mode = Mode.LAUNCHERS;
	private int appsToUpdate;

	private Update updateScene;

	public Dock() {
		instance = this;
	}

	public static Dock getInstance() {
		return instance;
	}

	/*
	 * Class methods
	 */

	public void notify(String msg, int type) {
		Pos pos = Pos.CENTER;
		if (cfg.topProperty().get()) {
			pos = Pos.TOP_LEFT;
		} else if (cfg.bottomProperty().get()) {
			pos = Pos.BOTTOM_LEFT;
		} else if (cfg.leftProperty().get()) {
			pos = Pos.TOP_RIGHT;
		} else if (cfg.rightProperty().get()) {
			pos = Pos.TOP_LEFT;
		}

		Notifications notificationBuilder = Notifications.create().text(msg)
				.hideAfter(Duration.seconds(10)).position(pos)
				.onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
					}
				});
		
		notificationBuilder.hideCloseButton();

		Configuration cfg = Configuration.getDefault();
		Color backgroundColour = cfg.colorProperty().getValue();
		if (backgroundColour.getBrightness() < 0.5) {
			notificationBuilder.darkStyle();
		}

		// if (darkStyleChkBox.isSelected()) {
		// notificationBuilder.darkStyle();
		// }

		switch (type) {
		case GUICallback.NOTIFY_WARNING:
			notificationBuilder.showWarning();
			break;
		case GUICallback.NOTIFY_INFO:
			notificationBuilder.showInformation();
			break;
		case GUICallback.NOTIFY_CONNECT:
		case GUICallback.NOTIFY_DISCONNECT:
			notificationBuilder.showConfirm();
			break;
		case GUICallback.NOTIFY_ERROR:
			notificationBuilder.showError();
			break;
		default:
			notificationBuilder.show();
		}

	}

	public void setMode(Mode mode) {
		if (mode != this.mode) {
			this.mode = mode;
			log.info(String.format("Dock mode is now %s", mode));
			switch (mode) {
			case LAUNCHERS:
				rebuildIcons();
				setAvailable();
				flinger.recentre();
				break;
			case UPDATE:
				try {
					// Starting an update, so hide the all other windows
					hideIfShowing(signInPopup);
					hideIfShowing(optionsPopup);
					hideIfShowing(resourceGroupPopup);
					setAvailable();
					updateScene = (Update) context
							.openScene(Update.class, Configuration
									.getDefault().isVertical() ? "Vertical" : null);
					Scene scn = updateScene.getScene();
					scn.setFill(new Color(0, 0, 0, 0));

					/*
					 * The update popup will get future update events, but it
					 * needs this one to initialize
					 */
					updateScene.initUpdate(appsToUpdate);

					Parent sceneRoot = scn.rootProperty().get();
					scn.setRoot(new Group());
					
					if(cfg.isVertical())
						((VBox)sceneRoot).minWidthProperty().bind(shortcuts.heightProperty());
					else
						((HBox)sceneRoot).minWidthProperty().bind(shortcuts.widthProperty());
					
					flinger.getContent().getChildren().clear();
					flinger.getContent().getChildren().add(sceneRoot);
				} catch (IOException ioe) {
					log.error("Failed to load update scene.", ioe);
				}
				break;
			case IDLE:
				flinger.getContent().getChildren().clear();
				setAvailable();
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	public boolean arePopupsOpen() {
		return context.isWaitingForExitChoice()
				|| (signInPopup != null && signInPopup.isShowing())
				|| (optionsPopup != null && optionsPopup.isShowing())
				|| (statusPopup != null && statusPopup.isShowing())
				|| (resourceGroupPopup != null && resourceGroupPopup
						.isShowing());
	}

	public void onLaunched(Runnable runnable) {
		if(launchWait != null) {
			launchWait.setOnFinished(runnable == null ? null : eh -> runnable.run());
		}
	}

	/*
	 * The following are all events from the {@link Bridge}, and will come in on
	 * the RMI thread.
	 */

	@Override
	public void initUpdate(int apps) {
		this.appsToUpdate = apps;
		Platform.runLater(() -> setMode(Mode.UPDATE));
	}

	@Override
	public void loadResources(Connection connection) {
		rebuildAllLaunchers();
	}

	@Override
	public void finishedConnecting(Connection connection, Exception e) {
		log.info(String.format("New connection finished connected (%s)",
				connection.toString()));
	}

	@Override
	public void bridgeEstablished() {
		log.info(String.format("Bridge established, rebuilding all launchers"));
		rebuildAllLaunchers();
		if(context.getBridge().isServiceUpdating()) {
			setMode(Mode.UPDATE);
		}
	}

	@Override
	public void bridgeLost() {
		log.info(String.format("Bridge lost, rebuilding all launchers"));
		rebuildAllLaunchers();

		// TODO
		// Stop connecting process if bridge lost during an upgrade
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
		log.info(String.format("Connection disconnected (%s)",
				connection.toString()));
		rebuildAllLaunchers();
	}

	public boolean isAwaitingLaunch() {
		return launchWait != null && launchWait.getStatus() == javafx.animation.Animation.Status.RUNNING;
	}


	// Overrides

	@Override
	protected void onCleanUp() {
		if(updateScene != null) {
			updateScene.cleanUp();
		}
		if(signInScene != null) {
			signInScene.cleanUp();
		}
		if(optionsScene != null) {
			optionsScene.cleanUp();
		}
		if(statusContent != null) {
			statusContent.cleanUp();
		}
		cfg.sizeProperty().removeListener(sizeChangeListener);
		cfg.colorProperty().removeListener(colorChangeListener);
		cfg.topProperty().removeListener(borderChangeListener);
		cfg.bottomProperty().removeListener(borderChangeListener);
		cfg.leftProperty().removeListener(borderChangeListener);
		cfg.rightProperty().removeListener(borderChangeListener);
	}

	@Override
	protected void onConfigure() {
		cfg = Configuration.getDefault();

		flinger = new Flinger();
		flinger.gapProperty().set(4);
		flinger.directionProperty().setValue(
				cfg.isVertical() ? Direction.VERTICAL : Direction.HORIZONTAL);
		slideLeft.disableProperty().bind(flinger.leftOrUpDisableProperty());
		slideRight.disableProperty().bind(flinger.rightOrDownDisableProperty());
		shortcuts.getChildren().add(flinger);

		AnchorPane.setTopAnchor(flinger, 0d);
		AnchorPane.setBottomAnchor(flinger, 0d);
		AnchorPane.setLeftAnchor(flinger, 0d);
		AnchorPane.setRightAnchor(flinger, 0d);

		networkResources.setTooltip(UIHelpers.createDockButtonToolTip(resources
				.getString("network.toolTip")));
		networkResources.selectedProperty().bindBidirectional(
				cfg.showNetworkProperty());

		ssoResources.setTooltip(UIHelpers.createDockButtonToolTip(resources
				.getString("sso.toolTip")));
		ssoResources.selectedProperty()
				.bindBidirectional(cfg.showSSOProperty());

		browserResources.setTooltip(UIHelpers.createDockButtonToolTip(resources
				.getString("web.toolTip")));
		browserResources.selectedProperty().bindBidirectional(
				cfg.showWebProperty());

		status.setTooltip(UIHelpers.createDockButtonToolTip(status.getTooltip()
				.getText()));
		exit.setTooltip(UIHelpers.createDockButtonToolTip(exit.getTooltip()
				.getText()));
		signIn.setTooltip(UIHelpers.createDockButtonToolTip(signIn.getTooltip()
				.getText()));
		options.setTooltip(UIHelpers.createDockButtonToolTip(options
				.getTooltip().getText()));

		fileResources.setTooltip(UIHelpers.createDockButtonToolTip(resources
				.getString("files.toolTip")));
		fileResources.selectedProperty().bindBidirectional(
				cfg.showFilesProperty());

		// Button size changes
		sizeChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				flinger.recentre();
				sizeButtons();
			}
		};
		cfg.sizeProperty().addListener(sizeChangeListener);

		// Colour changes
		colorChangeListener = new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				// styleToolTips();
			}
		};
		cfg.colorProperty().addListener(colorChangeListener);

		// Border changes
		borderChangeListener = new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue) {
					flinger.directionProperty().setValue(
							cfg.isVertical() ? Direction.VERTICAL
									: Direction.HORIZONTAL);
					configurePull();
				}
			}
		};
		cfg.topProperty().addListener(borderChangeListener);
		cfg.bottomProperty().addListener(borderChangeListener);
		cfg.leftProperty().addListener(borderChangeListener);
		cfg.rightProperty().addListener(borderChangeListener);

		dockContent.prefWidthProperty().bind(dockStack.widthProperty());
		
		if(context.getBridge().isServiceUpdating()) {
			setMode(Mode.UPDATE);
		}

		rebuildResources();
		rebuildIcons();
		// styleToolTips();
		sizeButtons();
		setAvailable();
		configurePull();
		if (cfg.autoHideProperty().get())
			maybeHideDock();
	}

	private void configurePull() {
		if (cfg.topProperty().get())
			pull.setText(resources.getString("pullTop"));
		else if (cfg.bottomProperty().get())
			pull.setText(resources.getString("pullBottom"));
		else if (cfg.leftProperty().get()) {
			pull.setText(resources.getString("pullBottom"));
			pull.setAlignment(Pos.CENTER_RIGHT);
		} else if (cfg.rightProperty().get()) {
			pull.setText(resources.getString("pullTop"));
			pull.setAlignment(Pos.CENTER_LEFT);
		}
	}

	private void hideIfShowing(Popup popup) {
		if (popup != null && popup.isShowing())
			popup.hide();
	}

	private static String textFill(Color color) {
		return String.format("-fx-text-fill: %s ;",
				UIHelpers.toHex(color, false));
	}

	private static String background(Color color, boolean opacity) {
		return String.format("-fx-background-color: %s ;",
				UIHelpers.toHex(color, opacity));
	}

	private void showContextMenu(double x, double y) {
		if (contextMenu != null && contextMenu.isShowing())
			contextMenu.hide();
		contextMenu = new ContextMenu();
		// contextMenu.getStyleClass().add("background");

		Color bg = cfg.colorProperty().getValue();
		Color fg = bg.getBrightness() < 0.5f ? Color.WHITE : Color.BLACK;

		contextMenu.setStyle(background(bg, true));

		contextMenu.setOnHidden(value -> {
			if (cfg.autoHideProperty().get() && !arePopupsOpen())
				maybeHideDock();
		});
		if (!cfg.autoHideProperty().get()) {
			MenuItem hide = new MenuItem(resources.getString("menu.hide"));
			hide.setOnAction(value -> getStage().setIconified(true));
			hide.setStyle(textFill(fg));
			contextMenu.getItems().add(hide);
		}
		MenuItem close = new MenuItem(resources.getString("menu.exit"));
		close.setOnAction(value -> {
			context.confirmExit();
			maybeHideDock();
		});
		close.setStyle(textFill(fg));
		contextMenu.getItems().add(close);
		Point2D loc = new Point2D(x + getStage().getX(), y + getStage().getY());
		contextMenu.show(dockContent, loc.getX(), loc.getY());
	}

	private void rebuildAllLaunchers() {
		log.info("Rebuilding all launchers");
		rebuildResources();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (icons.size() > 0 && mode == Mode.IDLE) {
					setMode(Mode.LAUNCHERS);
				} else if (icons.size() == 0 && mode == Mode.LAUNCHERS) {
					setMode(Mode.IDLE);
				} else {
					if (mode == Mode.LAUNCHERS)
						rebuildIcons();
					setAvailable();
				}
			}
		});
	}

	private void showStatus(Button source) throws IOException {
		Window parent = this.scene.getWindow();
		if (statusPopup == null) {
			statusContent = (Status) context.openScene(Status.class);
			statusPopup = new Popup(parent, statusContent.getScene(), true,
					PositionType.DOCKED) {
				@Override
				protected void hideParent(Window parent) {
					hideDock(true);
				}
			};
			((Status) statusContent).setPopup(statusPopup);
		}
		statusContent.setResources(serviceResources);
		statusPopup.popup();
	}

	/*
	 * private void showResourceGroup(Button source, ResourceGroupList group)
	 * throws IOException { Window parent = this.scene.getWindow(); if
	 * (resourceGroupPopup == null) { resourceGroup = (ResourceGroup) context
	 * .openScene(ResourceGroup.class); resourceGroupPopup = new Popup(parent,
	 * resourceGroup.getScene(), true, PositionType.POSITIONED) {
	 * 
	 * @Override protected void hideParent(Window parent) { hideDock(true); } };
	 * ((ResourceGroup) resourceGroup).setPopup(resourceGroupPopup); }
	 * positionResourceGroupPopup(source); resourceGroup.setResources(group);
	 * resourceGroupPopup.popup(); }
	 */

	/*
	 * private void positionResourceGroupPopup(Button source) { Point2D
	 * sceneCoord = source.localToScreen(0, 0); if (cfg.topProperty().get() ||
	 * cfg.bottomProperty().get())
	 * resourceGroupPopup.setPosition(sceneCoord.getX()); else
	 * resourceGroupPopup.setPosition(sceneCoord.getY()); }
	 */

	private void openSignInWindow() throws IOException {
		Window parent = this.scene.getWindow();
		if (signInPopup == null) {
			signInScene = (SignIn) context.openScene(SignIn.class);
			signInPopup = new Popup(parent, signInScene.getScene()) {
				@Override
				protected void hideParent(Window parent) {
					hideDock(true);
				}
			};
			((SignIn) signInScene).setPopup(signInPopup);
		}
		signInPopup.popup();
	}

	private void rebuildResources() {

		serviceResources.clear();
		icons.clear();
		log.info("Rebuilding resources");
		if (context.getBridge().isConnected()
				&& !context.getBridge().isServiceUpdating()) {
			ResourceService resourceService = context.getBridge()
					.getResourceService();

			try {
				serviceResources.addAll(resourceService.getServiceResources());
			} catch (Exception e) {
				log.error("Failed to get service resources.", e);
			}

			try {
				for (ResourceRealm resourceRealm : resourceService
						.getResourceRealms()) {
					for (Resource r : resourceRealm.getResources()) {
						rebuildResourceIcon(resourceRealm, r, r.getIcon());
					}
				}
			} catch (Exception e) {
				log.error("Failed to get resources.", e);
			}
			log.info(String.format("Found %d top level launchers", icons.size()));
		}
	}

	private void rebuildResourceIcon(ResourceRealm resourceRealm, Resource r,
			String groupName) {
		ResourceGroupKey igk = new ResourceGroupKey(r.getType(), groupName);
		ResourceGroupList ig = icons.get(igk);
		if (ig == null) {
			ig = new ResourceGroupList(igk);
			icons.put(igk, ig);
		}
		ig.getItems().add(new ResourceItem(r, resourceRealm));
	}

	private void rebuildIcons() {

		if (mode != Mode.LAUNCHERS) {
			return;
		}

		context.clearLoadQueue();
		flinger.getContent().getChildren().clear();

		// Type lastType = null;
		for (Map.Entry<ResourceGroupKey, ResourceGroupList> ig : icons
				.entrySet()) {
			Type type = ig.getKey().getType();
			// if (lastType != null && type != lastType) {
			// shortcuts
			// .getChildren()
			// .add(new Separator(
			// cfg.topProperty().get()
			// || cfg.bottomProperty().get() ? Orientation.VERTICAL
			// : Orientation.HORIZONTAL));
			// }
			switch (type) {
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

			// SSO launchers are not grouped, all others are
			// if (type == Resource.Type.SSO) {
			for (ResourceItem item : ig.getValue().getItems()) {
				flinger.getContent()
						.getChildren()
						.add(new IconButton(resources, item, context, ig
								.getValue()) {

							@Override
							protected void onFinishLaunch() {
								super.onFinishLaunch();
								
								if (launchWait != null
										&& launchWait.getStatus() == javafx.animation.Animation.Status.RUNNING)
									launchWait.stop();

								launchWait = new Timeline(new KeyFrame(Duration
										.millis(Dock.LAUNCH_WAIT)));
								launchWait.play();
							}

						});
			}
			// } else {
			// shortcuts.getChildren().add(createGroupButton(ig.getValue()));
			// }

			// lastType = type;
		}

	}

	/*
	 * private Button createGroupButton(final ResourceGroupList group) {
	 * 
	 * final Button groupButton = new Button();
	 * groupButton.setTextOverrun(OverrunStyle.CLIP);
	 * groupButton.getStyleClass().add("iconButton");
	 * groupButton.setOnMouseEntered((event) -> { if (resourceGroupPopup != null
	 * && resourceGroupPopup.isShowing()) { resourceGroup.setResources(group);
	 * positionResourceGroupPopup(groupButton);
	 * resourceGroupPopup.sizeToScene(); } }); groupButton.setOnAction((event)
	 * -> { try { showResourceGroup((Button) event.getSource(), group); } catch
	 * (IOException e) { log.error("Failed to show resource group.", e); } });
	 * 
	 * String subType = getSubType(group);
	 * 
	 * String tipText = getTipText(group, subType);
	 * 
	 * String imgPath = String.format("types/%s.png", subType); URL resource =
	 * getClass().getResource(imgPath); if (resource == null) { // Fallback
	 * tipText += " (" + imgPath + " not found)"; imgPath =
	 * String.format("types/unknown.png"); resource =
	 * getClass().getResource(imgPath); }
	 * 
	 * // Create image and set on button final ImageView imageView = new
	 * ImageView(resource.toString()); imageView.setFitHeight(32);
	 * imageView.setFitWidth(32); imageView.setPreserveRatio(true);
	 * imageView.getStyleClass().add("launcherIcon");
	 * groupButton.setGraphic(imageView);
	 * 
	 * // Tooltip for the sub-type
	 * groupButton.setTooltip(UIHelpers.createDockButtonToolTip(tipText));
	 * UIHelpers.sizeToImage(groupButton); return groupButton; }
	 */

	/*
	 * private String getTipText(final ResourceGroupList group, String subType)
	 * { String tipTextKey = String.format("subType.%s", subType); String
	 * tipText = resources.containsKey(tipTextKey) ? resources
	 * .getString(tipTextKey) : MessageFormat.format(
	 * resources.getString("subType.unknown"), group.getKey() .getType().name(),
	 * tipTextKey); return tipText; }
	 */

	static String getSubType(final ResourceGroupList group) {
		// Determine image path from 'logo'
		String subType = group.getKey().getSubType();
		if (subType == null) {
			subType = group.getKey().getType().name();
		}

		// Hack for VNC subtypes that end in the display number
		int idx = subType.lastIndexOf(':');
		if (idx != -1) {
			subType = subType.substring(0, idx);
		}

		// Hack for FTP subtypes that end in the display number
		if (subType.startsWith("ftp")) {
			subType = "ftp";
		}

		// Only use the first word
		subType = subType.split("\\s+")[0];
		return subType;
	}

	private void maybeHideDock() {
		if (hiding) {
			return;
		}
		stopDockHiderTrigger();
		dockHiderTrigger = new Timeline(new KeyFrame(
				Duration.millis(AUTOHIDE_HIDE_TIME), ae -> hideDock(true)));
		dockHiderTrigger.play();
	}

	private void stopDockHiderTrigger() {
		if (dockHiderTrigger != null
				&& dockHiderTrigger.getStatus() == Animation.Status.RUNNING)
			dockHiderTrigger.stop();
	}

	void hideDock(boolean hide) {
		stopDockHiderTrigger();

		if (hide != hidden) {
			/*
			 * If already hiding, we don't want the mouse event that MIGHT
			 * happen when the resizing dock passes under the mouse (the user
			 * wont have moved mouse yet)
			 */
			if (hiding) {
				// TODO check this ...
				return;
			}

			hidden = hide;
			hiding = true;

			dockHider = new Timeline(new KeyFrame(Duration.millis(5),
					ae -> shiftDock()));
			yEnd = System.currentTimeMillis() + AUTOHIDE_DURATION;
			dockHider.play();
		}
	}

	private void shiftDock() {
		long now = System.currentTimeMillis();
		Rectangle2D cfgBounds = Client.getConfiguredBounds();

		// The bounds to work in
		int boundsSize = cfg.isVertical() ? (int) cfgBounds.getHeight()
				: (int) cfgBounds.getWidth();

		// Total amount to slide
		int value = cfg.sizeProperty().get() - AUTOHIDE_TAB_OPPOSITE_SIZE;

		// How far along the timeline?
		float fac = Math.min(1f,
				1f - ((float) (yEnd - now) / (float) AUTOHIDE_DURATION));

		// The amount of movement so far
		float amt = fac * (float) value;

		// The amount to shrink the width (or height when vertical) of the
		// visible 'bar'
		float barSize = (float) boundsSize * fac;

		// If showing, reverse
		final boolean fhidden = hidden;

		if (!hidden) {
			amt = value - amt;
			barSize = (float) boundsSize - barSize;
		}

		// Reveal or hide the pull tab
		dockContent.setOpacity(hidden ? 1f - fac : fac);
		pull.setOpacity((hidden ? fac : 1f - fac) * 0.5f);

		Stage stage = getStage();
		if (stage != null) {
			if (cfg.topProperty().get()) {
				getScene().getRoot().translateYProperty().set(-amt);
				stage.setHeight(cfg.sizeProperty().get() - amt
						+ Client.DROP_SHADOW_SIZE);
				stage.setWidth(Math.max(AUTOHIDE_TAB_SIZE, cfgBounds.getWidth()
						- barSize));
				stage.setX(cfgBounds.getMinX()
						+ ((cfgBounds.getWidth() - stage.getWidth()) / 2f));
			} else if (cfg.bottomProperty().get()) {
				stage.setY(cfgBounds.getMaxY() + amt);
				stage.setHeight(cfg.sizeProperty().get() - amt
						+ Client.DROP_SHADOW_SIZE);
				stage.setWidth(Math.max(AUTOHIDE_TAB_SIZE, cfgBounds.getWidth()
						- barSize));
				stage.setX(cfgBounds.getMinX()
						+ ((cfgBounds.getWidth() - stage.getWidth()) / 2f));
			} else if (cfg.leftProperty().get()) {
				getScene().getRoot().translateXProperty().set(-amt);
				stage.setWidth(cfg.sizeProperty().get() - amt);
				stage.setHeight(Math.max(AUTOHIDE_TAB_SIZE,
						cfgBounds.getHeight() - barSize));
				stage.setY(cfgBounds.getMinY()
						+ ((cfgBounds.getHeight() - stage.getHeight()) / 2f));
			} else if (cfg.rightProperty().get()) {
				stage.setX(cfgBounds.getMaxX() + amt - cfg.sizeProperty().get());
				stage.setWidth(cfg.sizeProperty().get() - amt);
				stage.setHeight(Math.max(AUTOHIDE_TAB_SIZE,
						cfgBounds.getHeight() - barSize));
				stage.setY(cfgBounds.getMinY()
						+ ((cfgBounds.getHeight() - stage.getHeight()) / 2f));
			} else {
				throw new UnsupportedOperationException();
			}
		}

		// The update or the sign in dialog may have been popped, so make sure
		// it is position correctly
		if (signInPopup != null && signInPopup.isShowing()) {
			signInPopup.sizeToScene();
		}

		// If not fully hidden / revealed, play again
		if (now < yEnd) {
			dockHider.playFromStart();
		} else {
			// Defer this as events may still be coming in
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (!fhidden && stage != null) {
						stage.requestFocus();
					}
					hiding = false;
				}
			});
		}
	}

	// private void styleToolTips() {
	// for (Node s : shortcuts.getChildren()) {
	// if (s instanceof ButtonBase) {
	// recreateTooltip((ButtonBase) s);
	// }
	// }
	// recreateTooltip(options);
	// recreateTooltip(signIn);
	// recreateTooltip(exit);
	// recreateTooltip(status);
	// recreateTooltip(fileResources);
	// recreateTooltip(networkResources);
	// recreateTooltip(ssoResources);
	// recreateTooltip(browserResources);
	//
	// }

	// private void recreateTooltip(ButtonBase bb) {
	// if (bb != null && bb.getTooltip() != null)
	// bb.setTooltip(UIHelpers.createDockButtonToolTip(bb.getTooltip()
	// .getText()));
	// }

	private void sizeButtons() {
		UIHelpers.sizeToImage(networkResources);
		UIHelpers.sizeToImage(fileResources);
		UIHelpers.sizeToImage(ssoResources);
		UIHelpers.sizeToImage(browserResources);
		UIHelpers.sizeToImage(slideLeft);
		UIHelpers.sizeToImage(slideRight);
		UIHelpers.sizeToImage(signIn);
		UIHelpers.sizeToImage(exit);
		UIHelpers.sizeToImage(options);
		UIHelpers.sizeToImage(status);
		for (Node n : flinger.getContent().getChildren()) {
			if (n instanceof ButtonBase) {
				UIHelpers.sizeToImage((ButtonBase) n);
			}
		}
		setAvailable();
	}

	private void setAvailable() {
		for (String s : Arrays.asList("statusNotConnected", "statusConnected",
				"statusError")) {
			signIn.getStyleClass().remove(s);
		}
		if (context.getBridge().isConnected()) {
			int connected = 0;
			try {
				List<ConnectionStatus> connections = context.getBridge()
						.getClientService().getStatus();
				for (ConnectionStatus c : connections) {
					log.info(String.format("Connection %s = %s", c
							.getConnection().getHostname(), c.getStatus()));
					if (c.getStatus() == ConnectionStatus.CONNECTED) {
						connected++;
					}
				}
				log.info(String.format("Bridge says %d are connected of %d",
						connected, connections.size()));
				if (connected > 0) {
					signIn.getStyleClass().add("statusNotConnected");
				} else {
					signIn.getStyleClass().add("statusConnected");
				}
			} catch (Exception e) {
				log.error("Failed to check connection state.", e);
				signIn.getStyleClass().add("statusError");
			}
		} else {
			log.info("Bridge says not connected");
			signIn.getStyleClass().add("statusError");
		}
	}
	
	@FXML
	private void evtMouseEnter(MouseEvent evt) throws Exception {
		if (cfg.autoHideProperty().get()) {
			hideDock(false);
			evt.consume();
		}
	}

	@FXML
	private void evtMouseExit(MouseEvent evt) throws Exception {
		if (cfg.autoHideProperty().get() && !arePopupsOpen()
				&& (contextMenu == null || !contextMenu.isShowing())) {
			maybeHideDock();
			evt.consume();
		}
	}

	@FXML
	private void evtMouseClick(MouseEvent evt) throws Exception {
		if (evt.getButton() == MouseButton.SECONDARY) {
			showContextMenu(evt.getX(), evt.getY());
			evt.consume();
		} else if (contextMenu != null)
			contextMenu.hide();
	}

	@FXML
	private void evtExit(ActionEvent evt) throws Exception {
		context.confirmExit();
		maybeHideDock();
	}

	@FXML
	private void evtSlideLeft() {
		flinger.slideLeftOrUp();
	}

	@FXML
	private void evtSlideRight() {
		flinger.slideRightOrDown();
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
	private void evtStatus(ActionEvent evt) throws Exception {
		showStatus((Button) evt.getSource());
	}

	@FXML
	private void evtOpenOptionsWindow(ActionEvent evt) throws Exception {
		Window parent = this.scene.getWindow();
		if (optionsPopup == null) {
			optionsScene = (AbstractController) context
					.openScene(Options.class);
			optionsPopup = new Popup(parent, optionsScene.getScene()) {

				@Override
				protected void hideParent(Window parent) {
					hideDock(true);
				}

				@SuppressWarnings("restriction")
				protected boolean isChildFocussed() {
					// HACK!
					//
					// When the custom colour dialog is focused, there doesn't
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

			((Options) optionsScene).setPopup(optionsPopup);
		}
		optionsPopup.popup();
	}
	
}
