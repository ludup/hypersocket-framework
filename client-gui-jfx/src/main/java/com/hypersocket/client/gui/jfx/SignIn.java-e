package com.hypersocket.client.gui.jfx;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.Connection;

public class SignIn extends AbstractController implements Listener {
	static Logger log = LoggerFactory.getLogger(Main.class);

	@FXML
	private TextField username;
	@FXML
	private PasswordField password;
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

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		context.getBridge().addListener(this);

		serverUrls.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					setUserDetails(getSelectedConnection());
					setAvailable();
				}
			}
		});

		initUi();
	}

	@Override
	public void onStateChanged() {
		initUi();
	}

	@FXML
	private void evtServerUrlSelected(ActionEvent evt) throws Exception {
		setUserDetails(getSelectedConnection());
		setAvailable();
	}

	@FXML
	private void evtDelete(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(resources.getString("delete.confirm.title"));
			alert.setHeaderText(resources.getString("delete.confirm.header"));
			alert.setContentText(MessageFormat.format(
					resources.getString("delete.confirm.content"), getUri(sel)));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				try {
					context.getBridge().getConnectionService().delete(sel);
				} catch (Exception e) {
					log.error("Failed to delete connection.", e);
				} finally {
					initUi();
				}
			}
		}
	}

	@FXML
	private void evtDisconnect(ActionEvent evt) throws Exception {
		Connection sel = getSelectedConnection();
		if (sel != null) {
			context.getBridge().disconnect(sel);
			initUi();
		}
	}

	@FXML
	private void evtLogin(ActionEvent evt) throws Exception {
		Connection connection = getSelectedConnection();
		if (connection == null) {
			try {
				Connection newConnection = context.getBridge()
						.getConnectionService().createNew();
				URI uri = new URI(serverUrls.getEditor().getText());
				if (!uri.getScheme().equals("https")) {
					throw new IllegalArgumentException(
							"Only HTTPS is supported.");
				}
				newConnection.setHostname(uri.getHost());
				newConnection.setPort(uri.getPort() <= 0 ? 443 : uri.getPort());
				newConnection.setConnectAtStartup(saveCredentials.isSelected());
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

				connection = newConnection;
			} catch (Exception e) {
				log.error("Failed to create new connection.", e);
			}
		}

		if (connection == null) {
			log.error("No valid server selected.");
		}

		else {
			connection.setHashedPassword("");
			// TODO check with LDP how realms should be supported
			connection.setRealm("");
			connection.setStayConnected(true);
			try {
				if (saveConnection.isSelected()) {
					if (saveCredentials.isSelected()) {
						connection.setUsername(username.getText());
						connection.setHashedPassword(password.getText());
					}
					context.getBridge().getConnectionService().save(connection);
					serverUrls.itemsProperty().get().add(getUri(connection));
				} else if (saveCredentials.isSelected()) {
					connection.setUsername(username.getText());
					connection.setHashedPassword(password.getText());
					context.getBridge().getConnectionService().save(connection);
				} else {
					connection.setUsername(username.getText());
					connection.setHashedPassword(password.getText());
				}
				context.getBridge().connect(connection);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				setAvailable();
			}
		}
	}

	private void setUserDetails(Connection connection) {
		saveConnection.setSelected(connection != null);
		saveCredentials.setSelected(connection != null
				&& !StringUtils.isBlank(connection.getUsername()));
		username.setText(connection == null ? "" : connection.getUsername());
		password.setText(connection == null ? "" : connection
				.getHashedPassword());
	}

	private Connection getSelectedConnection() {
		String uri = serverUrls.getValue();
		return uri == null ? null : getConnectionForUri(uri);
	}

	private Connection getConnectionForUri(String uri) {
		try {
			for (Connection c : context.getBridge().getConnectionService()
					.getConnections()) {
				if (getUri(c).equals(uri)) {
					return c;
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
		serverUrls.itemsProperty().getValue().clear();
		Connection selectedConnection = null;
		String selectedUri = serverUrls.getValue();
		ObservableList<String> serverUrlsList = serverUrls.itemsProperty()
				.get();
		if (context.getBridge().isConnected()) {
			try {
				for (Connection c : context.getBridge().getConnectionService()
						.getConnections()) {
					String uri = getUri(c);
					if (uri.equals(selectedUri)) {
						selectedConnection = c;
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
			setUserDetails(selectedConnection);
		}
	}

	private void setAvailable() {
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
			delete.setVisible(sel != null && !selectionConnected);
			disconnect.setVisible(selectionConnected);
			serverUrls.setDisable(false);
			username.setDisable(selectionConnected);
			password.setDisable(selectionConnected);
			login.setDisable(selectionConnected);
			saveCredentials.setDisable(selectionConnected);
			saveConnection.setDisable(selectionConnected);
		} else {
			delete.setVisible(false);
			disconnect.setVisible(false);
			serverUrls.setDisable(true);
			username.setDisable(true);
			password.setDisable(true);
			login.setDisable(true);
			saveCredentials.setDisable(false);
			saveConnection.setDisable(false);
		}
	}
}
