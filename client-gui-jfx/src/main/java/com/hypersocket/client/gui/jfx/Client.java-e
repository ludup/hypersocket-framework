package com.hypersocket.client.gui.jfx;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
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

	static Logger log = LoggerFactory.getLogger(Client.class);
	static ResourceBundle BUNDLE = ResourceBundle.getBundle(Client.class
			.getName());

	private Bridge bridge;
	private Stage primaryStage;

	private boolean vertical;

	private static Object barrier = new Object();

	public static void initialize() throws InterruptedException {
		Thread t = new Thread("JavaFX Init Thread") {
			@Override
			public void run() {
				Application.launch(Client.class, new String[0]);
			}
		};

		synchronized (barrier) {
			t.setDaemon(true);
			t.start();
			barrier.wait();
		}
	}

	public FramedController openScene(Class<? extends Initializable> controller)
			throws IOException {
		return openScene(controller, null);
	}	
	
	public FramedController openScene(Class<? extends Initializable> controller, String fxmlSuffix)
			throws IOException {
		URL resource = controller.getResource(controller.getSimpleName()
				+ (fxmlSuffix == null ? "" : fxmlSuffix ) +".fxml");
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(ResourceBundle.getBundle(controller.getName()));
		Parent root = loader.load(resource.openStream());
		FramedController controllerInst = (FramedController) loader
				.getController();
		if (controllerInst == null) {
			throw new IOException(
					"Controller not found. Check controller in FXML");
		}
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

	private void recreateScene() {
		try {
			// Open the actual scene
			FramedController fc = openScene(Dock.class, Configuration.getDefault().isVertical() ? "Vertical" : null);
			final Scene scene = fc.getScene();

			// Background colour
			setColors(scene);

			// Finalise and show
			setStageBounds();
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			log.error("Failed to create scene.", e);
		}
	}

	private void setStageBounds() {
		Configuration cfg = Configuration.getDefault();
		Rectangle2D bounds = getConfiguredBounds();

		if (cfg.leftProperty().get()) {
			vertical = true;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get());
			primaryStage.setY(bounds.getMinY());
		} else if (cfg.rightProperty().get()) {
			vertical = true;
			primaryStage.setX(bounds.getMaxX() - cfg.sizeProperty().get());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get());
			primaryStage.setY(0);
		} else if (cfg.bottomProperty().get()) {
			vertical = false;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMaxY() - primaryStage.getHeight());
		} else {
			vertical = false;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMinY());
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Configuration cfg = Configuration.getDefault();

		synchronized (barrier) {
			barrier.notify();
		}

		// Bridges to the common client network code
		bridge = new Bridge();

		// Setup the window
		this.primaryStage = primaryStage;
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

		// Open the actual scene
		FramedController fc = openScene(Dock.class, cfg.isVertical() ? "Vertical" : null);
		final Scene scene = fc.getScene();

		// Configure the scene (window)
		BooleanProperty alwaysOnTopProperty = cfg.alwaysOnTopProperty();

		// Background colour
		setColors(scene);

		// Finalise and show
		setStageBounds();
		primaryStage.setScene(scene);
		primaryStage.show();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				primaryStage.setAlwaysOnTop(alwaysOnTopProperty.get());
			}
		});

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

		// Always on top
		alwaysOnTopProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				primaryStage.setAlwaysOnTop(newValue);
			}
		});

		Property<Color> colorProperty = cfg.colorProperty();
		colorProperty.addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				setColors(scene);
			}
		});

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
					boolean newVertical = cfg.isVertical();
					if (newVertical != vertical) {
						recreateScene();
					} else {
						setStageBounds();
					}
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
							root.hideDock(true);
						}
					}
				});

		primaryStage.onCloseRequestProperty().set(we -> {
			confirmExit();
			we.consume();
		});
	}

	public void confirmExit() {
		int active = bridge.getActiveConnections();

		if (active > 0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(BUNDLE.getString("exit.confirm.title"));
			alert.setHeaderText(BUNDLE.getString("exit.confirm.header"));
			alert.setContentText(BUNDLE.getString("exit.confirm.content"));

			ButtonType disconnect = new ButtonType(
					BUNDLE.getString("exit.confirm.disconnect"));
			ButtonType stayConnected = new ButtonType(
					BUNDLE.getString("exit.confirm.stayConnected"));
			ButtonType cancel = new ButtonType(
					BUNDLE.getString("exit.confirm.cancel"),
					ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(disconnect, stayConnected, cancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == disconnect) {
				new Thread() {
					public void run() {
						bridge.disconnectAll();
						System.exit(0);
					}
				}.start();
			} else if (result.get() == stayConnected) {
				System.exit(0);
			}
		} else {
			System.exit(0);
		}
	}

	public static void setColors(Scene scene) {
		Configuration cfg = Configuration.getDefault();
		Color newValue = cfg.colorProperty().getValue();
		scene.fillProperty().set(newValue);
		String newCol = "-fx-text-fill: "
				+ (newValue.getBrightness() < 0.5f ? "#ffffff" : "#000000")
				+ ";";
		scene.getRoot().setStyle(newCol);
		scene.setFill(newValue);
	}

	public Bridge getBridge() {
		return bridge;
	}

}
