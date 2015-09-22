package com.hypersocket.client.gui.jfx;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.commons.lang.StringUtils;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.Option;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.GUICallback;

/**
 * Controller for the "Sign In" window, where connections are managed and
 * credentials prompted for.
 */
public class SignIn extends AbstractController implements Listener {
	static Logger log = LoggerFactory.getLogger(Main.class);

	@FXML
	private BorderPane credentialsUI;
	@FXML
	private BorderPane promptUI;
	@FXML
	private VBox optionsUI;
	@FXML
	private ComboBox<String> serverUrls;
	@FXML
	private CheckBox saveConnection;
	@FXML
	private CheckBox saveCredentials;
	@FXML
	private CheckBox stayConnected;
	@FXML
	private CheckBox connectOnStartup;
	@FXML
	private Button login;
	@FXML
	private Button connect;
	@FXML
	private Button disconnect;
	@FXML
	private Button delete;
	@FXML
	private ProgressIndicator spinner;
	@FXML
	private HBox progressUI;
	@FXML
	private VBox container;
	@FXML
	private VBox root;
	@FXML
	private Label messageText;
	@FXML
	private Label messageIcon;

	private Connection foregroundConnection;
	private Semaphore promptSemaphore = new Semaphore(1);
	private boolean abortPrompt;
	private boolean promptsAvailable;
	private String promptedUsername;
	private char[] promptedPassword;
	private Map<Prompt, Control> promptNodes = new LinkedHashMap<Prompt, Control>();
	private final Map<String, String> promptValues = new LinkedHashMap<String, String>();
	private List<Connection> disconnecting = new ArrayList<>();
	private List<Connection> connecting = new ArrayList<>();
	private List<Connection> waitingForUpdatesOrResources = new ArrayList<>();
	private boolean adjusting;
	private boolean deleteOnDisconnect;

	/*
	 * Class methods
	 */

	/*
	 * The following are all events from the {@link Bridge}, and will come in on
	 * the RMI thread.
	 */
	@Override
	public void disconnecting(Connection connection) {
		abortPrompts();
		super.disconnecting(connection);
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
		super.disconnected(connection, e);
		Platform.runLater(() -> {
			log.info("Disconnected " + connection + " (delete "
					+ deleteOnDisconnect + ")");
			if (disconnecting.contains(connection)) {
				disconnecting.remove(connection);
				setAvailable();
				sizeAndPosition();
			}
			if (Objects.equals(connection, foregroundConnection)) {
				log.info("Clearing foreground connection");
				foregroundConnection = null;
			}
			if (deleteOnDisconnect) {
				try {
					doDelete(connection);
					initUi();
				} catch (RemoteException e1) {
					log.error("Failed to delete.", e);
				}
			} else {
				setAvailable();
			}
		});
	}

	@Override
	public void started(final Connection connection) {
		Platform.runLater(() -> {
			log.info("Started " + connection);
			waitingForUpdatesOrResources.remove(connection);
			initUi();
			// setAvailable();
		});
	}

	@Override
	public void finishedConnecting(final Connection connection, Exception e) {
		Platform.runLater(() -> {
			log.info("Finished connecting "
					+ connection
					+ ". "
					+ (e == null ? "No error" : "Error occured."
							+ e.getMessage()) + " Foreground is "
					+ foregroundConnection);

			if (e != null) {
				waitingForUpdatesOrResources.remove(connection);
			}

			if (connecting.remove(connection)) {

				if (Objects.equals(connection, foregroundConnection)) {
					if (e != null)

						/*
						 * If we are connecting the foreground connection and it
						 * fails, add a decoration to the server URL to indicate
						 * an error
						 */
						Decorator.addDecoration(serverUrls.getEditor(),
								new GraphicDecoration(createErrorImageNode()));
				}
			}
			;

			if (Objects.equals(connection, foregroundConnection)) {
				foregroundConnection = null;

				// setMessage(null, null);
				setAvailable();
				if (e == null) {
					if (saveCredentials.selectedProperty().get()) {
						/*
						 * If we get here this implies save connection as well,
						 * but we need to have collected the username and
						 * password
						 */
						if (promptedUsername != null
								&& promptedPassword != null) {
							try {
								connection.setUsername(promptedUsername);
								connection.setHashedPassword(new String(
										promptedPassword));

								saveConnection(connection);
							} catch (Exception ex) {
								log.error("Failed to save credentials.", ex);
							}
						} else {
							log.warn("No username or password save as credentials. Does you scheme have both?");
						}
					}
				} else {
					abortPrompts();
					log.error("Failed to connect.", e);
					Dock.getInstance().notify(e.getMessage(),
							GUICallback.NOTIFY_ERROR);
				}
			}
		});
		super.finishedConnecting(connection, e);
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts, int attempts,
			boolean success) {

		try {
			abortPrompt = false;
			promptSemaphore.acquire();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					clearCredentials();
					VBox vbox = new VBox();
					int idx = 0;
					for (Prompt p : prompts) {
						Label l = new Label(p.getLabel());
						vbox.getChildren().add(l);
						switch (p.getType()) {
						case TEXT:
							CustomTextField txt = new CustomTextField();
							txt.setText(p.getDefaultValue());
							if (!success) {
								txt.setLeft(createErrorImageNode());
							}
							vbox.getChildren().add(txt);
							txt.getStyleClass().add("input");
							txt.setOnAction((event) -> {
								focusNextPrompt(txt);
							});
							promptNodes.put(p, txt);
							break;
						case HIDDEN:
							promptValues.put(p.getResourceKey(),
									p.getDefaultValue());
							break;
						case PASSWORD:
							CustomPasswordField pw = new CustomPasswordField();
							if (!success) {
								pw.setLeft(createErrorImageNode());
							}
							vbox.getChildren().add(pw);
							pw.getStyleClass().add("input");
							promptNodes.put(p, pw);
							pw.setOnAction((event) -> {
								focusNextPrompt(pw);
							});
							break;
						case SELECT:
							ComboBox<String> cb = new ComboBox<String>();
							for (Option o : p.getOptions()) {
								cb.itemsProperty().get().add(o.getName());
							}
							cb.getStyleClass().add("input");
							vbox.getChildren().add(cb);
							promptNodes.put(p, cb);
							if (idx == 0) {
								cb.requestFocus();
							}
							break;
						case A:
							Hyperlink h = new Hyperlink(p.getLabel());
							h.getStyleClass().add("input");
							vbox.getChildren().add(h);
							promptNodes.put(p, h);
							break;
						case P:
							// TODO What's a P?
							break;
						}
						idx++;
					}
					credentialsUI.setCenter(vbox);
					promptsAvailable = true;
					setAvailable();

					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							focusNextPrompt(null);
						}
					});
				}
			});
			promptSemaphore.acquire();
			promptSemaphore.release();
			promptsAvailable = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (abortPrompt) {
			log.info("Returning nothing from prompt, was aborted.");
			return null;
		} else {
			if (promptValues.containsKey("username"))
				promptedUsername = promptValues.get("username");
			if (promptValues.containsKey("password"))
				promptedPassword = promptValues.get("password").toCharArray();

			return promptValues;
		}
	}

	@Override
	public void bridgeEstablished() {
		Platform.runLater(() -> {
			abortPrompts();
			initUi();
		});
	}

	@Override
	public void bridgeLost() {
		Platform.runLater(() -> {
			waitingForUpdatesOrResources.clear();
			connecting.clear();
			abortPrompts();
			initUi();
		});
	}

	// Overrides

	@Override
	protected void onConfigure() {
		super.onConfigure();
		initUi();

		/*
		 * This is DUMB, but i can't see another way. It stops invisible
		 * components being considered for layout (and so taking up space. You'd
		 * think this might be part of JavaFX, but no ...
		 * 
		 * http://stackoverflow.com/questions/12200195/javafx-hbox-hide-item
		 */
		disconnect.managedProperty().bind(disconnect.visibleProperty());
		connect.managedProperty().bind(connect.visibleProperty());
		delete.managedProperty().bind(delete.visibleProperty());

		optionsUI.managedProperty().bind(optionsUI.visibleProperty());
		promptUI.managedProperty().bind(promptUI.visibleProperty());
		progressUI.managedProperty().bind(progressUI.visibleProperty());
		saveConnection.managedProperty().bind(saveConnection.visibleProperty());
		stayConnected.managedProperty().bind(stayConnected.visibleProperty());
		connectOnStartup.managedProperty().bind(
				connectOnStartup.visibleProperty());

		serverUrls.getEditor().textProperty()
				.addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						if (newValue.equals("")
								&& serverUrls.getEditor().isFocused()) {
							showUrlPopOver();
						}
					}
				});

		serverUrls.getEditor().focusedProperty()
				.addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue, Boolean newValue) {
						if (newValue
								&& (popOver == null || !popOver.isShowing())) {
							showUrlPopOver();
						}
					}
				});

	}

	protected void onSetPopup(Popup popup) {

		popup.showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				/*
				 * TODO HACK - Wait 500ms before showing the popover, I can't
				 * find a better way. Using showingProperty() of the popup()
				 * just makes the popover disappear
				 */
				if (newValue) {
					Timeline timeline = new Timeline(new KeyFrame(Duration
							.millis(500), ae -> showUrlPopOver()));
					timeline.play();
				}
			}
		});
	}

	// Private methods

	private void showUrlPopOver() {
		/**
		 * Crashing Still!!!!
		 */
//		if (!serverUrls.isDisabled()
//				&& serverUrls.getEditor().getText().trim().equals(""))
//			showPopOver(resources.getString("serverURL.tooltip"), serverUrls);
	}

	private void focusNextPrompt(Control c) {
		
		boolean found = c == null;
		for (Map.Entry<Prompt, Control> en : promptNodes.entrySet()) {
			if (!found && Objects.equals(en.getValue(), c)) {
				found = true;
			} else if (found) {
				log.info("Will now focus " + en.getValue());
				en.getValue().requestFocus();
				return;
			}
		}

		// Get to the end? treat this as submit of form
		log.info("Action on last prompt, submitting");
		evtLogin(null);
	}

	private void urlSelected() {
		hidePopOver();
		abortPrompts();
		Decorator.removeAllDecorations(serverUrls.getEditor());
		Connection selectedConnection = getChosenConnection();
		String uriString = serverUrls.getEditor().getText();
		log.info(String.format("Selected URI is %s", uriString));

		if (selectedConnection == null) {
			log.info("No connection for the selection, creating one");

			// If no connection for this URI was found, it is new, so add it
			try {
				Connection newConnection = context.getBridge()
						.getConnectionService().createNew();

				String realUri = uriString;
				if (!realUri.startsWith("https://")) {
					if (realUri.indexOf("://") != -1) {
						throw new IllegalArgumentException(
								"Only HTTPS is supported.");
					}
					realUri = "https://" + realUri;
				}
				URI uri = new URI(realUri);
				if (!uri.getScheme().equals("https")) {
					throw new IllegalArgumentException(
							"Only HTTPS is supported.");
				}

				log.info(String.format("Created new connection for %s",
						uri.toString()));

				newConnection.setHostname(uri.getHost());
				newConnection.setPort(uri.getPort() <= 0 ? 443 : uri.getPort());
				newConnection.setConnectAtStartup(false);
				String path = uri.getPath();
				if (path.equals("") || path.equals("/")) {
					path = "/hypersocket";
				} else if (path.indexOf('/', 1) > -1) {
					path = path.substring(0, path.indexOf('/', 1));
				}
				newConnection.setPath(path);

				// Prompt for authentication
				newConnection.setUsername("");
				newConnection.setHashedPassword("");
				newConnection.setRealm("");

				selectedConnection = foregroundConnection = newConnection;

			} catch (URISyntaxException urise) {
				Dock.getInstance().notify(
						resources.getString("error.invalidUri"),
						GUICallback.NOTIFY_ERROR);
			} catch (Exception e) {
				log.error("Failed to create new connection.", e);
				return;
			}
		} else {
			// A connection with the URI already exists, is it our foreground
			// connection?

			log.info(String.format("Selected connection exists for %s (%s:%d)",
					uriString, selectedConnection.getHostname(),
					selectedConnection.getPort()));

			if (selectedConnection.equals(foregroundConnection)) {
				try {
					if (context.getBridge().getClientService()
							.isConnected(foregroundConnection)) {

						// Already connected, don't do anything

						foregroundConnection = selectedConnection;
						log.info("Already connected, won't try to connect.");
						return;
					}
				} catch (Exception e) {
					log.warn("Failed to test if already connected.", e);
				}
			} else {

				// The foreground connection is this new connection

				foregroundConnection = selectedConnection;
			}
		}

		setUserDetails(selectedConnection);
		setAvailable();
	}

	private void abortPrompts() {
		// If prompts are waiting, cancel them
		if (!promptSemaphore.tryAcquire()) {
			// Prompts are waiting
			abortPrompt = true;
		}
		// Will release this acquire if successful, or the waiting one
		// if not
		promptSemaphore.release();

		// Update UI
		Runnable r = new Runnable() {
			@Override
			public void run() {
				clearCredentials();
				setAvailable();
			}
		};
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(r);
	}

	private void clearCredentials() {
		credentialsUI.getChildren().clear();
		promptNodes.clear();
		promptValues.clear();
	}

	@FXML
	private void evtSaveConnection(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null
				&& context.getBridge().getClientService().isConnected(sel)) {
			if (saveConnection.isSelected() && sel.getId() == null) {
				saveConnection(sel);
			} else if (!saveConnection.isSelected() && sel.getId() != null) {
				if (!confirmDelete(sel)) {
					saveConnection.setSelected(true);
				}
				setAvailable();
			}
		}
	}

	private void saveConnection(Connection sel) throws RemoteException {
		foregroundConnection = context.getBridge().getClientService().save(sel);
		log.info("Connection saved");
		setAvailable();
	}

	private void initUi() {
		adjusting = true;
		try {
			Decorator.removeAllDecorations(serverUrls.getEditor());
			log.info("Rebuilding URIs");
			String previousUri = serverUrls.getValue();

			serverUrls.itemsProperty().getValue().clear();
			Connection selectedConnection = null;
			String selectedUri = "";
			ObservableList<String> serverUrlsList = FXCollections
					.observableArrayList();

			/*
			 * If there is a current foreground connection, make sure that is in
			 * the list and use it as the actual connection object
			 */
			if (foregroundConnection != null) {
				selectedUri = getUri(foregroundConnection);
				log.info(String.format("Using foreground connection %s",
						selectedUri));
				serverUrlsList.add(selectedUri);
				selectedConnection = foregroundConnection;
			}

			if (context.getBridge().isConnected()) {
				try {
					List<ConnectionStatus> connections = context.getBridge()
							.getClientService().getStatus();

					// Look for new connections
					for (ConnectionStatus c : connections) {

						Connection conx = c.getConnection();
						String uri = getUri(conx);

						// We might end up using the first connected Uri
						if (selectedUri.equals("")
								&& c.getStatus() == ConnectionStatus.CONNECTED) {
							log.info(String.format(
									"Using first connected connection %s", uri));
							selectedUri = uri;
							selectedConnection = conx;
						}

						if (!serverUrlsList.contains(uri)) {
							serverUrlsList.add(uri);
						}
					}
				} catch (Exception e) {
					log.error("Failed to load connections.", e);
				}
			}

			if (selectedUri != null && selectedConnection == null
					&& !serverUrlsList.isEmpty()) {
				// Finally fall back to the first Uri in the list
				if (previousUri != null && previousUri.length() == 0
						&& serverUrlsList.contains(previousUri)) {
					selectedUri = previousUri;
					selectedConnection = getConnectionForUri(selectedUri);
				}
				if (selectedConnection == null || selectedUri == null) {
					selectedUri = serverUrlsList.get(0);
					selectedConnection = getConnectionForUri(selectedUri);
				}
			}

			// Select initial URI
			log.info("Selecting " + selectedUri);
			serverUrls.itemsProperty().setValue(serverUrlsList);
			serverUrls.setValue(selectedUri);

			serverUrls.getEditor().getStyleClass().add("uiText");

			// Adjust available actions etc
			log.info("Rebuilt URIs");
			populateUserDetails(getSelectedConnection());
			setAvailable();
		} finally {
			adjusting = false;
		}
	}

	private boolean confirmDelete(Connection sel) {
		popup.setDismiss(false);
		try {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(resources.getString("delete.confirm.title"));
			alert.setHeaderText(resources.getString("delete.confirm.header"));
			alert.setContentText(MessageFormat.format(
					resources.getString("delete.confirm.content"), getUri(sel)));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				try {
					if (sel.equals(foregroundConnection)) {
						abortPrompts();
					}

					// Remove from list now
					adjusting = true;
					if (sel.equals(foregroundConnection)) {
						foregroundConnection = null;
					}
					String uri = getUri(sel);
					serverUrls.itemsProperty().get().remove(uri);
					adjusting = false;

					if (context.getBridge().getClientService().isConnected(sel)) {
						log.info("Disconnecting deleted connection.");
						deleteOnDisconnect = true;
						doDisconnect(sel);
					} else {
						doDelete(sel);
						initUi();
					}
				} catch (Exception e) {
					log.error("Failed to delete connection.", e);
				}
				return true;
			} else {
				return false;
			}
		} finally {
			popup.setDismiss(true);
		}
	}

	private void doDelete(Connection sel) throws RemoteException {
		log.info(String.format("Deleting connection %s", sel));
		context.getBridge().getConnectionService().delete(sel);
		String uri = getUri(sel);
		adjusting = true;
		try {
			serverUrls.itemsProperty().get().remove(uri);
			setAvailable();
		} finally {
			adjusting = false;
			log.info("Connection deleted");
		}
	}

	private void doDisconnect(Connection sel) {

		if (disconnecting.contains(sel)) {
			throw new IllegalStateException("Already disconnecting " + sel);
		}
		disconnecting.add(sel);
		if (sel.getId() == null) {
			adjusting = true;

			try {
				log.info("Disconnected temporary connection, clearing");
				/*
				 * If this is a temporary connection being deleted, clear it
				 * from the URL list too and maybe the URL editor
				 */
				if (sel.equals(getChosenConnection())) {
					serverUrls.getEditor().setText("");
				}
				serverUrls.itemsProperty().get().remove(getUri(sel));
			} finally {
				adjusting = false;
			}
		}

		setAvailable();
		new Thread() {
			public void run() {
				try {
					context.getBridge().disconnect(sel);
				} catch (Exception e) {
					log.error("Failed to disconnect.", e);
				}
			}
		}.start();
	}

	private void sizeAndPosition() {
		Stage stage = getStage();
		if (stage != null) {
			stage.sizeToScene();
			popup.sizeToScene();
		}
	}

	private void populateUserDetails(Connection connection) {
		
		saveConnection.setSelected(connection != null
				&& connection.getId() != null);
		saveCredentials.setSelected(connection != null
				&& !StringUtils.isBlank(connection.getUsername()));
		connectOnStartup.setSelected(connection != null
				&& connection.isConnectAtStartup());
		stayConnected.setSelected(connection != null
				&& connection.isStayConnected());
	}

	private void setUserDetails(Connection connection) {

		populateUserDetails(connection);

		// These will be collected during prompts and maybe saved
		promptedUsername = null;
		promptedPassword = null;

		int status;
		try {
			status = context.getBridge().getClientService()
					.getStatus(connection);
		} catch (RemoteException e1) {
			status = ConnectionStatus.DISCONNECTED;
		}
		if (status == ConnectionStatus.DISCONNECTED) {
			connecting.add(connection);
			waitingForUpdatesOrResources.add(connection);
			setAvailable();
			new Thread() {
				public void run() {
					try {
						context.getBridge().connect(connection);
					} catch (Exception e) {
						foregroundConnection = null;
						log.error("Failed to connect.", e);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								Dock.getInstance().notify(e.getMessage(),
										GUICallback.NOTIFY_ERROR);
							}
						});
					} finally {
						log.info(String.format("Connected to %s",
								getUri(connection)));
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								setAvailable();
							}
						});
					}
				}
			}.start();
		} else {
			log.warn("Request to connect an already connected or connecting connection "
					+ connection);
		}
	}

	private Connection getChosenConnection() {
		String uri = serverUrls.getEditor().getText();
		return uri == null ? null : getConnectionForUri(uri);
	}

	private Connection getSelectedConnection() {
		String uri = serverUrls.getValue();
		return uri == null ? null : getConnectionForUri(uri);
	}

	private Connection getConnectionForUri(String uri) {
		if (context.getBridge().isConnected()) {
			try {
				List<ConnectionStatus> connections = context.getBridge()
						.getClientService().getStatus();
				for (ConnectionStatus c : connections) {
					if (getUri(c.getConnection()).equals(uri)) {
						return c.getConnection();
					}
				}
			} catch (Exception e) {
				log.error("Could not find connection for URI.", e);
			}
		}
		return null;
	}

	private String getUri(Connection connection) {
		if (connection == null) {
			return "";
		}
		String uri = "https://" + connection.getHostname();
		if (connection.getPort() != 443) {
			uri += ":" + connection.getPort();
		}
		uri += connection.getPath();
		return uri;
	}

	private void setAvailable() {

		Runnable runnable = new Runnable() {
			public void run() {
				if (context.getBridge().isConnected()) {
					Connection sel = getSelectedConnection();
					boolean busy = (!waitingForUpdatesOrResources.isEmpty()
							|| !connecting.isEmpty() || !disconnecting
								.isEmpty()) && !promptsAvailable;
					boolean selectionConnected = false;
					try {
						selectionConnected = sel != null
								&& context.getBridge().getClientService()
										.isConnected(sel);
					} catch (Exception e) {
						log.warn("Failed to test if connected. Assuming not.",
								e);
					}
					optionsUI.setVisible(disconnecting.isEmpty()
							&& (promptsAvailable || selectionConnected));
					promptUI.setVisible(disconnecting.isEmpty()
							&& promptsAvailable);
					progressUI.setVisible(busy);
					serverUrls.editorProperty().get().setDisable(busy);
					serverUrls.setDisable(busy);
					saveConnection.setVisible(selectionConnected);
					stayConnected.setVisible(selectionConnected);
					connectOnStartup.setVisible(selectionConnected);
					delete.setVisible(sel != null && !selectionConnected
							&& sel.getId() != null);
					delete.setDisable(!disconnecting.isEmpty() || busy);
					connect.setVisible(!selectionConnected);
					connect.setDisable(busy);
					disconnect.setVisible(selectionConnected
							&& (sel.getId() == null || !saveCredentials
									.selectedProperty().get()));
					disconnect.setVisible(selectionConnected);
					serverUrls.setDisable(false);
					login.setDisable(selectionConnected);
					saveCredentials.setDisable(selectionConnected);
					saveConnection.setDisable(!selectionConnected);
					stayConnected.setDisable(!saveCredentials
							.selectedProperty().get()
							|| !saveCredentials.selectedProperty().get());
					connectOnStartup.setDisable(!saveCredentials
							.selectedProperty().get()
							|| !saveCredentials.selectedProperty().get());

				} else {
					serverUrls.editorProperty().get().setDisable(false);
					serverUrls.setDisable(false);
					progressUI.setVisible(false);
					promptUI.setVisible(false);
					optionsUI.setVisible(false);
					stayConnected.setVisible(false);
					connectOnStartup.setVisible(false);
					delete.setVisible(false);
					disconnect.setVisible(false);
					serverUrls.setDisable(true);
					login.setDisable(true);
					saveCredentials.setDisable(false);
					stayConnected.setDisable(false);
					connectOnStartup.setDisable(false);
					connect.setVisible(true);
					connect.setDisable(true);
				}
				rebuildContainer();
				sizeAndPosition();
			}
		};
		if (Platform.isFxApplicationThread())
			runnable.run();
		else
			Platform.runLater(runnable);

	}

	private void rebuildContainer() {
		container.getChildren().clear();
		if (optionsUI.isVisible()) {
			container.getChildren().add(optionsUI);
		}
		if (credentialsUI.isVisible()) {
			container.getChildren().add(credentialsUI);
		}
		if (progressUI.isVisible()) {
			container.getChildren().add(progressUI);
		}
		if (promptUI.isVisible()) {
			container.getChildren().add(promptUI);
		}
	}

	private Node createErrorImageNode() {
		Image image = new Image(getClass().getResource("error.png")
				.toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.scaleXProperty().set(0.5);
		imageView.scaleYProperty().set(0.5);
		return imageView;
	}

	/*
	 * The following are all events from UI
	 */

	@FXML
	private void evtSaveCredentials(ActionEvent evt) throws Exception {
		saveConnection.selectedProperty().set(true);
		setAvailable();
	}

	@FXML
	private void evtServerUrlSelected(ActionEvent evt) throws Exception {
		System.out.println(evt);
		if (!adjusting)
			urlSelected();
	}

	@FXML
	private void evtConnect(ActionEvent evt) throws Exception {
		urlSelected();
	}

	@FXML
	private void evtDelete(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null && sel.getId() != null) {
			confirmDelete(sel);
		}
	}

	@FXML
	private void evtDisconnect(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null) {
			doDisconnect(sel);
		}
	}

	@FXML
	private void evtStayConnected(ActionEvent evt) throws Exception {
		Connection c = getSelectedConnection();
		if (c != null) {
			c.setStayConnected(stayConnected.selectedProperty().get());
			saveConnection(c);
		}

	}

	@FXML
	private void evtConnectOnStartup(ActionEvent evt) throws Exception {
		Connection c = getSelectedConnection();
		if (c != null) {
			c.setConnectAtStartup(connectOnStartup.selectedProperty().get());
			saveConnection(c);
		}
	}

	@SuppressWarnings("unchecked")
	@FXML
	private void evtLogin(ActionEvent evt) {
		try {
			for (Map.Entry<Prompt, Control> en : promptNodes.entrySet()) {
				if (en.getValue() instanceof TextField) {
					promptValues.put(en.getKey().getResourceKey(),
							((TextField) en.getValue()).getText());
				} else if (en.getValue() instanceof PasswordField) {
					promptValues.put(en.getKey().getResourceKey(),
							((PasswordField) en.getValue()).getText());
				} else if (en.getValue() instanceof ComboBox) {
					promptValues.put(en.getKey().getResourceKey(),
							((ComboBox<String>) en.getValue()).getValue());
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("Sending prompt values ..");
				for (Map.Entry<String, String> en : promptValues.entrySet()) {
					log.debug(en.getKey() + " = " + en.getValue());
				}
			}
			credentialsUI.getChildren().clear();
			promptsAvailable = false;
			setAvailable();
			promptSemaphore.release();
		} catch (Exception e) {
			log.error("Failed to login.", e);
		}
	}

	@FXML
	private void evtShowTooltipPopover(MouseEvent evt) {
		if (evt.getSource() == connect) {
			showPopOver(resources.getString("connect.tooltip"), connect);
		} else if (evt.getSource() == disconnect) {
			showPopOver(resources.getString("disconnect.tooltip"), disconnect);
		} else if (evt.getSource() == delete) {
			showPopOver(resources.getString("delete.tooltip"), delete);
		}
	}

	@FXML
	private void evtHideTooltipPopover(MouseEvent evt) {
		hidePopOver();
	}
}
