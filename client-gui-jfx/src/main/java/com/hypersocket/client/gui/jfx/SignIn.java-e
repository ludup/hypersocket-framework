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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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

	private Connection foregroundConnection;
	private Semaphore promptSemaphore = new Semaphore(1);
	private boolean abortPrompt;
	private boolean promptsAvailable;
	private String promptedUsername;
	private char[] promptedPassword;
	private Map<Prompt, Control> promptNodes = new LinkedHashMap<Prompt, Control>();
	private final Map<String, String> promptValues = new LinkedHashMap<String, String>();

	private boolean adjusting;

	@Override
	public void disconnecting(Connection connection) {
		abortPrompts();
		super.disconnecting(connection);
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
		super.disconnected(connection, e);
		log.info("Disconnected " + connection);
		if (Objects.equals(connection, foregroundConnection)) {
			log.info("Clearing foreground connection");
			foregroundConnection = null;
		}
	}

	@Override
	public void onStateChanged() {
		if (!adjusting) {
			initUi();
		}
	}

	@Override
	public void finishedConnecting(final Connection connection, Exception e) {
		if (e == null && Objects.equals(connection, foregroundConnection)) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
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
							} catch (Exception e) {
								log.error("Failed to save credentials.", e);
							}
						} else {
							log.warn("No username or password save as credentials. Does you scheme have both?");
						}
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
					getStage().sizeToScene();
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (abortPrompt) {
			log.info("Returning nothing from prompt, was aborted.");
			return null;
		} else {
			if(promptValues.containsKey("username"))
				promptedUsername = promptValues.get("username");
			if(promptValues.containsKey("password"))
				promptedPassword = promptValues.get("password").toCharArray();
			
			return promptValues;
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		// saveConnection.selectedProperty().set(false);
		context.getBridge().addListener(this);
		serverUrls.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					urlSelected();
				}
			}
		});

		initUi();
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
		Connection selectedConnection = getSelectedConnection();
		String uriString = serverUrls.getEditor().getText();
		log.info(String.format("Selected URI is %s", uriString));

		if (selectedConnection == null) {
			// If no connection was found, this is new, so add it
			// saveConnection.selectedProperty().set(false);
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
				// TODO Maybe show some error
			} catch (Exception e) {
				log.error("Failed to create new connection.", e);
				return;
			}
		} else {
			log.info(String.format("Selected connection exists for %s",
					uriString));
		}

		// If there is an existing connection and it differs from the current
		// one's URI, disconnect first,
		if (foregroundConnection != null) {

			if (getUri(foregroundConnection).equals(getUri(selectedConnection))) {
				log.info(String.format("Already connected to %s", uriString));
				setAvailable();
				return;
			}

			try {
				abortPrompts();

				log.info(String.format("Disconnecting temporary connection %s",
						getUri(foregroundConnection)));
				context.getBridge().disconnect(foregroundConnection);
				foregroundConnection = null;
			} catch (Exception e) {
				log.error(String.format(
						"Failed to disconnect temporary connection %d.",
						foregroundConnection.getId()), e);
			}
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

		// Clear up the prompt UI as well
		clearCredentials();
		getStage().sizeToScene();
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
		if (sel != null) {
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
				}
				context.getBridge().getConnectionService().delete(sel);
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
			context.getBridge().disconnect(sel);
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
			setAvailable();
			getStage().sizeToScene();
			promptSemaphore.release();
		} catch (Exception e) {
			log.error("Failed to login.", e);
		}
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

		saveConnection.setSelected(connection != null && connection.getId() != null);
		saveCredentials.setSelected(connection != null
				&& !StringUtils.isBlank(connection.getUsername()));

		// These will be collected during prompts and maybe saved
		promptedUsername = null;
		promptedPassword = null;

		if (connection != null && foregroundConnection == null) {
			// Try to connect
			try {
				context.getBridge().connect(connection);
				log.info(String.format("Connected to %s", uri));
				foregroundConnection = connection;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				setAvailable();
			}
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
			serverUrls.itemsProperty().getValue().clear();
			Connection selectedConnection = null;
			String selectedUri = serverUrls.getValue();
			ObservableList<String> serverUrlsList = serverUrls.itemsProperty()
					.get();
			if (context.getBridge().isConnected()) {
				try {

					List<ConnectionStatus> connections = context.getBridge()
							.getClientService().getStatus();
					for (ConnectionStatus c : connections) {
						String uri = getUri(c.getConnection());
						if (uri.equals(selectedUri)) {
							selectedConnection = c.getConnection();
						}
						serverUrlsList.add(uri);
					}
				} catch (Exception e) {
					log.error("Failed to load connections.", e);
				}
			}
			if (selectedConnection == null && !serverUrlsList.isEmpty()) {
				String first = serverUrlsList.get(0);
				selectedConnection = getConnectionForUri(first);
				serverUrls.setValue(first);
			}
			setAvailable();
			if (selectedConnection != null) {
				setUserDetails(serverUrls.getEditor().getText(),
						selectedConnection);
			} else {
				log.info("No initial connections (fg is "
						+ foregroundConnection + ")");
			}
		} finally {
			adjusting = false;
		}
	}

	private void setAvailable() {
		log.info("setAvailable()");
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
			log.info("connected: " + selectionConnected + " pa: "
					+ promptsAvailable);
			optionsUI.setVisible(promptsAvailable || selectionConnected);
			promptUI.setVisible(promptsAvailable);
			saveConnection.setVisible(selectionConnected);
			delete.setVisible(sel != null && !selectionConnected);
			disconnect.setVisible(selectionConnected && ( sel.getId() == null || !saveCredentials.selectedProperty().get() ) );
			serverUrls.setDisable(false);
			login.setDisable(selectionConnected);
			saveCredentials.setDisable(selectionConnected);
			saveConnection.setDisable(!selectionConnected);
		} else {
			promptUI.setVisible(false);
			optionsUI.setVisible(false);
			delete.setVisible(false);
			disconnect.setVisible(false);
			serverUrls.setDisable(true);
			login.setDisable(true);
			saveCredentials.setDisable(false);
		}
	}
}
