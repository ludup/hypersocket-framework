package com.hypersocket.client.gui.jfx;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.hypersocket.client.gui.jfx.fontawesome.AwesomeIcons;
import com.hypersocket.client.rmi.BrowserLauncher;
import com.hypersocket.client.rmi.BrowserLauncher.BrowserLauncherFactory;
import com.hypersocket.client.rmi.ResourceLauncher;

public class Client extends Application {
	static ResourceBundle BUNDLE = ResourceBundle.getBundle(Client.class
			.getName());

	private Bridge bridge;
	private Stage primaryStage;

	public FramedController openScene(Class<? extends Initializable> controller)
			throws IOException {
		URL resource = controller.getResource(controller.getSimpleName()
				+ ".fxml");
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(ResourceBundle.getBundle(controller.getName()));
		Parent root = loader.load(resource.openStream());
		FramedController controllerInst = (FramedController) loader
				.getController();
		root.getStylesheets().add(
				controller.getResource(Client.class.getSimpleName() + ".css")
						.toExternalForm());
		root.getStylesheets().add(
				controller.getResource(controller.getSimpleName() + ".css")
						.toExternalForm());
		AwesomeIcons.install(root);
		Scene scene = new Scene(root);
		controllerInst.configure(scene, this);
		return controllerInst;
	}

	public static Screen getConfiguredScreen() {
		Configuration cfg = Configuration.getDefault();
		ObservableList<Screen> screens = Screen.getScreens();
		return screens.get(Math.min(screens.size() - 1, cfg.monitorProperty()
				.get()));
	}

	public static Rectangle2D getConfiguredBounds() {

		Configuration cfg = Configuration.getDefault();
		Screen screen = getConfiguredScreen();

		// TODO might need to monitor the bounds constantly, I can't see
		// a way to get screen geometry change events.
		Rectangle2D visualBounds = screen.getVisualBounds();
		Rectangle2D screenBounds = screen.getBounds();
		return cfg.avoidReservedProperty().get() ? visualBounds : screenBounds;
	}

	private void setStageBounds() {
		Configuration cfg = Configuration.getDefault();
		Rectangle2D bounds = getConfiguredBounds();

		if (cfg.leftProperty().get()) {
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get());
			primaryStage.setY(bounds.getMinY());
		} else if (cfg.rightProperty().get()) {
			primaryStage.setX(bounds.getMaxX() - cfg.sizeProperty().get());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get());
			primaryStage.setY(0);
		} else if (cfg.bottomProperty().get()) {
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMaxY() - primaryStage.getHeight());
		} else {
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMinY());
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;

		bridge = new Bridge();
		if (Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)) {
			primaryStage.initStyle(StageStyle.TRANSPARENT);
		} else {
			primaryStage.initStyle(StageStyle.UNDECORATED);
		}
		primaryStage.setTitle(BUNDLE.getString("title"));
		primaryStage.getIcons().add(
				new Image(getClass().getResourceAsStream(
						"hypersocket-icon256x256.png")));
		primaryStage.getIcons().add(
				new Image(getClass().getResourceAsStream(
						"hypersocket-icon128x128.png")));
		primaryStage.getIcons().add(
				new Image(getClass().getResourceAsStream(
						"hypersocket-icon64x64.png")));
		primaryStage.getIcons().add(
				new Image(getClass().getResourceAsStream(
						"hypersocket-icon48x48.png")));
		primaryStage.getIcons().add(
				new Image(getClass().getResourceAsStream(
						"hypersocket-icon32x32.png")));
		FramedController fc = openScene(Dock.class);
		final Scene scene = fc.getScene();
		Configuration cfg = Configuration.getDefault();
		Property<Color> colorProperty = cfg.colorProperty();
		colorProperty.addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				setColors(scene);
			}
		});
		setColors(scene);

		setStageBounds();
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setScene(scene);
		primaryStage.show();

		// Install JavaFX compatible browser launcher
		// if (SystemUtils.IS_OS_LINUX) {
		// // I am having problems with both AWT and this JavaFX specific
		// // browser launcher on Linux :(
		// BrowserLauncher.setFactory(new BrowserLauncherFactory() {
		//
		// public ResourceLauncher create(String uri) {
		// return new ResourceLauncher() {
		// @Override
		// public int launch() {
		// try {
		// return new ProcessBuilder("x-www-browser", uri)
		// .redirectOutput(Redirect.INHERIT)
		// .redirectErrorStream(true).start()
		// .waitFor();
		// } catch (IOException | InterruptedException e) {
		// e.printStackTrace();
		// return -1;
		// }
		// }
		// };
		// }
		// });
		// } else {
		BrowserLauncher.setFactory(new BrowserLauncherFactory() {

			public ResourceLauncher create(String uri) {
				return new ResourceLauncher() {
					@Override
					public int launch() {
						getHostServices().showDocument(uri);
						return 0;
					}
				};
			}
		});
		// }

		// Listen for configuration changes
		cfg.avoidReservedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				setStageBounds();
			}
		});
		ChangeListener<Boolean> dockPositionListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue) {
					setStageBounds();
				}
			}
		};
		ChangeListener<Number> geometryChangeListener = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				setStageBounds();
			}
		};
		cfg.monitorProperty().addListener(geometryChangeListener);
		cfg.sizeProperty().addListener(geometryChangeListener);
		cfg.topProperty().addListener(dockPositionListener);
		cfg.bottomProperty().addListener(dockPositionListener);
		cfg.leftProperty().addListener(dockPositionListener);
		cfg.rightProperty().addListener(dockPositionListener);
		// exitAlert.setTitle(resources.getString("exit.confirm.title"));
		// exitAlert.setHeaderText(resources.getString("exit.confirm.header"));
		// exitAlert.setContentText(resources.getString("exit.confirm.content"));
		// Optional<ButtonType> result = alert.showAndWait();
		// if (result.get() == ButtonType.OK) {
		// System.exit(0);
		// }

		//
		primaryStage.focusedProperty().addListener(
				new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue, Boolean newValue) {
						Dock root = (Dock) fc;
						if (!newValue && cfg.autoHideProperty().get()
								&& !root.arePopupsOpen()) {
							primaryStage.setIconified(true);
						}
					}
				});

		primaryStage.onCloseRequestProperty().set(
				we -> {

					int active = bridge.getActiveConnections();

					if (active > 0) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle(BUNDLE.getString("exit.confirm.title"));
						alert.setHeaderText(BUNDLE
								.getString("exit.confirm.header"));
						alert.setContentText(BUNDLE
								.getString("exit.confirm.content"));

						ButtonType disconnect = new ButtonType(BUNDLE
								.getString("exit.confirm.disconnect"));
						ButtonType stayConnected = new ButtonType(BUNDLE
								.getString("exit.confirm.stayConnected"));
						ButtonType cancel = new ButtonType(BUNDLE
								.getString("exit.confirm.cancel"),
								ButtonData.CANCEL_CLOSE);

						alert.getButtonTypes().setAll(disconnect,
								stayConnected, cancel);

						Optional<ButtonType> result = alert.showAndWait();
						if (result.get() == disconnect) {
							bridge.disconnectAll();
							System.exit(0);
						}
						else if (result.get() == stayConnected) {
							System.exit(0);
						}
						we.consume();
					}
					else {
						System.exit(0);
					}
				});
	}

	public static void setColors(Scene scene) {
		Configuration cfg = Configuration.getDefault();
		Color newValue = cfg.colorProperty().getValue();
		scene.fillProperty().set(newValue);
		String newCol = "-fx-text-fill: "
				+ (newValue.getBrightness() < 0.5f ? "#ffffff" : "#000000")
				+ ";";
		System.out.println("New col: " + newCol);
		scene.getRoot().setStyle(newCol);
		scene.setFill(newValue);
	}

	public Bridge getBridge() {
		return bridge;
	}

}
