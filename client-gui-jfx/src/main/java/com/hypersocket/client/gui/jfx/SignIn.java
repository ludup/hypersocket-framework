package com.hypersocket.client.gui.jfx;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.Option;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionStatus;

public class SignIn extends AbstractController implements Listener {
	static Logger log = LoggerFactory.getLogger(Main.class);

	@FXML
	private BorderPane credentialsUI;
	@FXML
	private BorderPane promptUI;
	@FXML
	private BorderPane optionsUI;
	@FXML
	private BorderPane messageContainer;
	@FXML
	private ComboBox<String> serverUrls;
	@FXML
	private CheckBox saveConnection;
	@FXML
	private CheckBox saveCredentials;
	@FXML
	private Hyperlink disconnect;
	@FXML
	private Hyperlink delete;
	@FXML
	private Button login;
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

	private boolean adjusting;
	private boolean busy;
	private boolean disconnecting;
	private Popup popup;

	private boolean deleteOnDisconnect;

	private Timeline messageHider;

	private FadeTransition fadeTransition;

	@Override
	public void disconnecting(Connection connection) {
		abortPrompts();
		super.disconnecting(connection);
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
		super.disconnected(connection, e);
		log.info("Disconnected " + connection);
		if (disconnecting) {
			busy = false;
			disconnecting = false;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setAvailable();
					sizeAndPosition();
				}
			});
		}
		if (Objects.equals(connection, foregroundConnection)) {
			log.info("Clearing foreground connection");
			foregroundConnection = null;
		}
		if (deleteOnDisconnect) {
			try {
				context.getBridge().getConnectionService().delete(connection);
			} catch (RemoteException e1) {
				log.error("Failed to delete.", e);
			}
		}
	}

	public void setPopup(Popup popup) {
		this.popup = popup;
	}

	@Override
	public void onStateChanged() {
		if (!adjusting) {
			initUi();
		}
	}

	public void notify(String msg, int type) {
	}

	@Override
	public void finishedConnecting(final Connection connection, Exception e) {
		if (Objects.equals(connection, foregroundConnection)) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					busy = false;
					// setMessage(null, null);
					setAvailable();
					sizeAndPosition();
					if (e == null) {
						if (saveCredentials.selectedProperty().get()) {
							/*
							 * If we get here this implies save connection as
							 * well, but we need to have collected the username
							 * and password
							 */
							if (promptedUsername != null
									&& promptedPassword != null) {
								try {
									connection.setUsername(promptedUsername);
									connection.setHashedPassword(new String(
											promptedPassword));

									saveConnection(connection);
								} catch (Exception e) {
									log.error("Failed to save credentials.", e);
								}
							} else {
								log.warn("No username or password save as credentials. Does you scheme have both?");
							}
						}
					} else {
						abortPrompts();
						log.error("Failed to connect.", e);
						setMessage(AlertType.ERROR, e.getMessage());
					}
				}
			});
		}
		super.finishedConnecting(connection, e);
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts) {

		try {
			abortPrompt = false;
			promptSemaphore.acquire();
			busy = false;
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
							TextField txt = new TextField(p.getDefaultValue());
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
							PasswordField pw = new PasswordField();
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
						case P:
							// TODO What's a P?
							break;
						}
						idx++;
					}
					credentialsUI.setCenter(vbox);
					promptsAvailable = true;
					setAvailable();
					sizeAndPosition();

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
	protected void onConfigure() {
		super.onConfigure();
		context.getBridge().addListener(this);
		setMessage(null, null);

		initUi();
	}

	public void setMessage(Alert.AlertType type, String message) {
		log.info(String.format("Setting message %s (%s)", message,
				type == null ? "clear" : type.name()));
		messageContainer.getStyleClass().remove("errorMessage");
		messageContainer.getStyleClass().remove("warningMessage");
		messageContainer.getStyleClass().remove("informationMessage");
		if (type != null) {
			messageText.setText(message);
			switch (type) {
			case ERROR:
				messageContainer.getStyleClass().add("errorMessage");
				messageIcon.setText(resources.getString("message.error.icon"));
				break;
			case INFORMATION:
				messageContainer.getStyleClass().add("informationMessage");
				messageIcon.setText(resources
						.getString("message.information.icon"));
				break;
			case WARNING:
				messageContainer.getStyleClass().add("warningMessage");
				messageIcon
						.setText(resources.getString("message.warning.icon"));
				break;
			default:
				break;
			}
		}

		if (type == null && root.getChildren().contains(messageContainer)) {
			root.getChildren().remove(messageContainer);
			sizeAndPosition();
		} else if (type != null
				&& !root.getChildren().contains(messageContainer)) {
			root.getChildren().add(0, messageContainer);
			sizeAndPosition();
		}

		messageContainer.setOpacity(1);

		// Stop previous fade timer and animation
		if (messageHider != null && messageHider.getStatus() == Status.RUNNING) {
			messageHider.stop();
		}
		if (fadeTransition != null
				&& fadeTransition.getStatus() == Status.RUNNING) {
			fadeTransition.stop();
		}

		// Start a new one
		messageHider = new Timeline(new KeyFrame(Duration.millis(10000),
				ae -> hideMessage()));
		messageHider.play();

	}

	private void hideMessage() {
		if (root.getChildren().contains(messageContainer)) {
			fadeTransition = new FadeTransition(Duration.millis(1000),
					messageContainer);
			fadeTransition.setFromValue(1.0f);
			fadeTransition.setToValue(0.0f);
			fadeTransition.setCycleCount(1);
			fadeTransition.setAutoReverse(true);
			fadeTransition.onFinishedProperty().set(ae -> {
				root.getChildren().remove(messageContainer);
				sizeAndPosition();
			});
			fadeTransition.play();
		}
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

	@FXML
	private void evtSaveCredentials(ActionEvent evt) throws Exception {
		saveConnection.selectedProperty().set(true);
		setAvailable();
	}

	@FXML
	private void evtServerUrlSelected(ActionEvent evt) throws Exception {
		if (!adjusting)
			urlSelected();
	}

	private void urlSelected() {
		abortPrompts();

		Connection selectedConnection = getSelectedConnection();
		String uriString = serverUrls.getEditor().getText();
		log.info(String.format("Selected URI is %s", uriString));

		if (selectedConnection == null) {
			// If no connection was found, this is new, so add it
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

				selectedConnection = newConnection;
			} catch (URISyntaxException urise) {
				setMessage(AlertType.ERROR,
						resources.getString("error.invalidUri"));
			} catch (Exception e) {
				log.error("Failed to create new connection.", e);
				return;
			}
		} else {
			log.info(String.format("Selected connection exists for %s",
					uriString));

		}

		// Clear any messages
		setMessage(null, null);

		// If there is an existing connection and it differs from the current
		// one's URI, disconnect first,
		if (foregroundConnection != null) {

			String foregroundUri = getUri(foregroundConnection);
			String selectedUri = getUri(selectedConnection);

			if (foregroundUri.equals(selectedUri)) {
				log.info(String.format("Already connected to %s", uriString));
				setAvailable();
				return;
			} else {
				foregroundConnection = null;
			}
			//
			//
			// try {
			// busy = false;
			// log.info(String.format("Disconnecting temporary connection %s",
			// foregroundUri));
			// context.getBridge().disconnect(foregroundConnection);
			// foregroundConnection = null;
			// } catch (Exception e) {
			// log.error(String.format(
			// "Failed to disconnect temporary connection %d.",
			// foregroundConnection.getId()), e);
			// }
		}

		setUserDetails(uriString, selectedConnection);
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
				getStage().sizeToScene();
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
				confirmDelete(sel);
			}
		}
	}

	private void saveConnection(Connection sel) throws RemoteException {
		foregroundConnection = context.getBridge().getClientService().save(sel);
		log.info("Connection saved");
		setAvailable();
	}

	@FXML
	private void evtDelete(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null && sel.getId() != null) {
			confirmDelete(sel);
		}
	}

	private void confirmDelete(Connection sel) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(resources.getString("delete.confirm.title"));
		alert.setHeaderText(resources.getString("delete.confirm.header"));
		alert.setContentText(MessageFormat.format(
				resources.getString("delete.confirm.content"), getUri(sel)));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (context.getBridge().getClientService().isConnected(sel)) {
					log.info("Disconnecting deleted connection.");
					context.getBridge().disconnect(sel);
					deleteOnDisconnect = true;
				} else {
					context.getBridge().getConnectionService().delete(sel);
				}
				log.info("Connection deleted");
			} catch (Exception e) {
				log.error("Failed to delete connection.", e);
			} finally {
				initUi();
			}
		}
	}

	@FXML
	private void evtDisconnect(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null) {
			disconnecting = true;
			busy = true;
			foregroundConnection = null;
			adjusting = true;
			try {
				log.info("Clearing selection");
				serverUrls.getSelectionModel().select("");
			} finally {
				adjusting = false;
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
			log.info("Sending prompt values ..");
			for (Map.Entry<String, String> en : promptValues.entrySet()) {
				log.info(en.getKey() + " = " + en.getValue());
			}
			credentialsUI.getChildren().clear();
			promptsAvailable = false;
			busy = true;
			setAvailable();
			sizeAndPosition();
			promptSemaphore.release();
		} catch (Exception e) {
			log.error("Failed to login.", e);
		}
	}

	void sizeAndPosition() {
		Stage stage = getStage();
		if (stage != null) {
			stage.sizeToScene();
			popup.positionPopup();
		}
	}

	private void populateUserDetails(String uri, Connection connection) {
		saveConnection.setSelected(connection != null
				&& connection.getId() != null);
		saveCredentials.setSelected(connection != null
				&& !StringUtils.isBlank(connection.getUsername()));
	}

	private void setUserDetails(String uri, Connection connection) {
		try {
			if (context.getBridge().getClientService().isConnected(connection)) {
				// Already connected
				foregroundConnection = connection;
				log.info("Already connected, won't try to connect.");
			}
		} catch (Exception e) {
			log.warn("Failed to test if already connected.", e);
		}
		populateUserDetails(uri, connection);

		// These will be collected during prompts and maybe saved
		promptedUsername = null;
		promptedPassword = null;

		if (connection != null && foregroundConnection == null) {
			// Try to connect
			busy = true;
			setAvailable();
			foregroundConnection = connection;
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
								setMessage(AlertType.ERROR, e.getMessage());
							}
						});
					} finally {
						setAvailable();
					}
				}
			}.start();
			log.info(String.format("Connected to %s", uri));
		}
	}

	private Connection getSelectedConnection() {
		String uri = serverUrls.getValue();
		return uri == null ? null : getConnectionForUri(uri);
	}

	private Connection getConnectionForUri(String uri) {
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

	private void initUi() {
		adjusting = true;
		try {
			log.info("Rebuilding URIs");
			String previousUri = serverUrls.getValue();

			serverUrls.itemsProperty().getValue().clear();
			Connection selectedConnection = null;
			String selectedUri = "";
			ObservableList<String> serverUrlsList = FXCollections
					.observableArrayList();

			// An empty URL
			serverUrlsList.add("");

			/*
			 * If there is a current foreground connection, make sure that is in
			 * the list and use it as the actual connection object
			 */
			if (foregroundConnection != null) {
				selectedUri = getUri(foregroundConnection);
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

			log.info("Selecting " + selectedUri);
			serverUrls.itemsProperty().setValue(serverUrlsList);
			serverUrls.setValue(selectedUri);

			// Adjust available actions etc
			log.info("Rebuilt URIs");
			setAvailable();
			sizeAndPosition();
			if (selectedConnection != null) {
				setUserDetails(serverUrls.getEditor().getText(),
						selectedConnection);
			}
		} finally {
			adjusting = false;
		}
	}

	void setAvailable() {
		if (context.getBridge().isConnected()) {
			Connection sel = getSelectedConnection();
			boolean selectionConnected = false;
			try {
				selectionConnected = sel != null
						&& context.getBridge().getClientService()
								.isConnected(sel);
			} catch (Exception e) {
				log.warn("Failed to test if connected. Assuming not.", e);
			}
			optionsUI.setVisible(!disconnecting
					&& (promptsAvailable || selectionConnected));
			promptUI.setVisible(!disconnecting && promptsAvailable);
			progressUI.setVisible(busy);
			serverUrls.editorProperty().get().setDisable(busy);
			serverUrls.setDisable(busy);
			saveConnection.setVisible(selectionConnected);
			delete.setVisible(sel != null && !selectionConnected
					&& sel.getId() != null);
			delete.setDisable(disconnecting);
			disconnect.setVisible(selectionConnected
					&& (sel.getId() == null || !saveCredentials
							.selectedProperty().get()));
			disconnect.setVisible(selectionConnected);
			serverUrls.setDisable(false);
			login.setDisable(selectionConnected);
			saveCredentials.setDisable(selectionConnected);
			saveConnection.setDisable(!selectionConnected);

		} else {
			serverUrls.editorProperty().get().setDisable(false);
			serverUrls.setDisable(false);
			progressUI.setVisible(false);
			promptUI.setVisible(false);
			optionsUI.setVisible(false);
			delete.setVisible(false);
			disconnect.setVisible(false);
			serverUrls.setDisable(true);
			login.setDisable(true);
			saveCredentials.setDisable(false);
		}
		rebuildContainer();
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
}
