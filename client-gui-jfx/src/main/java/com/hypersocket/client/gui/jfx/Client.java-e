package com.hypersocket.client.gui.jfx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.gui.jfx.Configuration.BrowserType;
import com.hypersocket.client.gui.jfx.fontawesome.AwesomeIcons;
import com.hypersocket.client.rmi.BrowserLauncher;
import com.hypersocket.client.rmi.BrowserLauncher.BrowserLauncherFactory;
import com.hypersocket.client.rmi.ResourceLauncher;
import com.sshtools.forker.client.ForkerBuilder;
import com.sshtools.forker.client.ForkerBuilder.IO;

public class Client extends Application {
	public static int DROP_SHADOW_SIZE = 11;

	static Logger log = LoggerFactory.getLogger(Client.class);
	static ResourceBundle BUNDLE = ResourceBundle.getBundle(Client.class
			.getName());

	private Bridge bridge;
	private Stage primaryStage;

	private boolean vertical;

	private static Object barrier = new Object();
	private ExecutorService loadQueue = Executors.newSingleThreadExecutor();
	private boolean waitingForExitChoice;

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

	public FramedController openScene(
			Class<? extends Initializable> controller, String fxmlSuffix)
			throws IOException {
		URL resource = controller.getResource(controller.getSimpleName()
				+ (fxmlSuffix == null ? "" : fxmlSuffix) + ".fxml");
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

	public static String getCustomCSSResource() {
		StringBuilder bui = new StringBuilder();

		// Get the base colour. All other colours are derived from this
		Configuration cfg = Configuration.getDefault();
		Color backgroundColour = cfg.colorProperty().getValue();

		if (backgroundColour.getOpacity() == 0) {
			// Prevent total opacity, as mouse events won't be received
			backgroundColour = new Color(backgroundColour.getRed(),
					backgroundColour.getGreen(), backgroundColour.getBlue(),
					1f / 255f);
		}

		bui.append("* {\n");

		bui.append("-fx-background: ");
		bui.append(UIHelpers.toHex(backgroundColour, true));
		bui.append(";\n");

		// Others
		Color baseColour;
		Color highlightColour;
		Color foregroundColour1;
		Color foregroundColour2;
		Color foregroundColour3;

		// Base foreground colours around the brightness of the background
		// colour
		if (backgroundColour.getBrightness() < 0.5f) {
			// Darker background
			baseColour = backgroundColour.deriveColor(1, 1, 1.25, 1);

			// Foregrounds
			foregroundColour1 = Color.LIGHTGRAY;
			foregroundColour2 = Color.WHITE;
			foregroundColour3 = Color.DARKGRAY;

		} else {
			// Light background
			baseColour = backgroundColour.deriveColor(1, 1, 0.75, 1);

			// Foregrounds
			foregroundColour1 = Color.DARKGRAY;
			foregroundColour2 = Color.BLACK;
			foregroundColour3 = Color.LIGHTGRAY;
		}

		// Highlight (TODO derive from hue)
		if (backgroundColour.getSaturation() == 0) {
			// Greyscale, so just use HS blue
			highlightColour = Color.web("246aff");
		} else {
			// A colour, so choose the next adjacent colour in the HSB colour
			// wheel (45 degrees)
			// highlightColour = backgroundColour.deriveColor(1f - ( ( 1f / 360f
			// ) * 45f), 1f, 1f, 1f);
			highlightColour = backgroundColour.deriveColor(45f, 1f, 1f, 1f);
		}

		// Accent
		bui.append("-fx-accent: ");
		bui.append(UIHelpers.toHex(highlightColour, false));
		bui.append(";\n");

		// Base
		bui.append("-fx-base: ");
		bui.append(UIHelpers.toHex(baseColour, false));
		bui.append(";\n");

		// Inner background
		bui.append("-fx-inner-background: ");
		bui.append(UIHelpers.toHex(baseColour, false));
		bui.append(";\n");

		// Accent
		bui.append("-fx-focus-color: ");
		bui.append(UIHelpers.toHex(highlightColour, false));
		bui.append(";\n");

		// FG1
		bui.append("-fx-dark-text-color: ");
		bui.append(UIHelpers.toHex(foregroundColour1, false));
		bui.append(";\n");

		// FG2
		bui.append("-fx-mid-text-color: ");
		bui.append(UIHelpers.toHex(foregroundColour2, false));
		bui.append(";\n");

		// FG3
		bui.append("-fx-light-text-color: ");
		bui.append(UIHelpers.toHex(foregroundColour3, false));
		bui.append(";\n");

		// End
		bui.append("}\n");

		// Tooltips
		bui.append(".tooltip {\n");
		bui.append("-fx-text-fill: ");
		bui.append(UIHelpers.toHex(foregroundColour2, false));
		bui.append(";\n");
		bui.append("-fx-background-color: ");
		bui.append(UIHelpers.toHex(backgroundColour, false));
		bui.append(";\n");
//		bui.append("-fx-effect: dropshadow( three-pass-box , ");
//
//		bui.append(UIHelpers.toHex(foregroundColour1, false));
//
//		bui.append(" , 10, 0.0 , 0 , 3 );");
		
		bui.append("-fx-effect: dropshadow(gaussian, rgba(0,0,0,.2), 10.0, 0.5, 2.0, 2.0);\n");
		
		bui.append(";\n");

		bui.append("}\n");
		
		// Root pane
		bui.append(".shadowed {\n");
		int insets = DROP_SHADOW_SIZE;
		if(cfg.topProperty().get()) {
			bui.append("-fx-background-insets: 0 0 " + insets + " 0;\n");
			bui.append("-fx-padding: 0 0 " + insets + " 0;\n");
		}else if(cfg.bottomProperty().get()) {
			bui.append("-fx-background-insets: " + insets + " 0 0 0;\n");
			bui.append("-fx-padding: " + insets + " 0 0 0;\n");
		}else if(cfg.leftProperty().get()) {
			bui.append("-fx-background-insets: 0 " + insets + " 0 0;\n");
			bui.append("-fx-padding: 0 " + insets + " 0 0;\n");
		}else {
			bui.append("-fx-background-insets: 0 0 0 " + insets + ";\n");
			bui.append("-fx-padding: 0 0 0 " + insets + ";\n");
		}
//		bui.append("-fx-effect: dropshadow( gaussian, ");

//		bui.append(UIHelpers.toHex(foregroundColour1, backgroundColour.getOpacity() / 2f));

		bui.append("-fx-effect: dropshadow(gaussian, " + UIHelpers.toHex(foregroundColour1, backgroundColour.getOpacity() / 2f) + ", " + DROP_SHADOW_SIZE + ", 0.5, 2.0, 2.0);\n");
//		bui.append(" , 22, 0.0 , 0 , " + insets + " );");
		
		bui.append(";\n");
		bui.append("}\n");
		
		// Popovers
		bui.append(".popover > .border {\n");
		bui.append("-fx-fill: ");
		bui.append(UIHelpers.toHex(backgroundColour, false));
		bui.append(";\n");
		bui.append("}\n");
		bui.append(".popover > .content {\n");
		bui.append("-fx-background-color: ");
		bui.append(UIHelpers.toHex(backgroundColour, false));
		bui.append(";\n");
		bui.append("}\n");
		 
		return bui.toString();

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
			FramedController fc = openScene(Dock.class, Configuration
					.getDefault().isVertical() ? "Vertical" : null);
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
		log.info(String.format("Setting stage bounds to %s", bounds));
		
		int dropShadowSize = DROP_SHADOW_SIZE;

		if (cfg.leftProperty().get()) {
			vertical = true;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get() + dropShadowSize);
			primaryStage.setY(bounds.getMinY());
		} else if (cfg.rightProperty().get()) {
			vertical = true;
			primaryStage.setX(bounds.getMaxX() - cfg.sizeProperty().get());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setWidth(cfg.sizeProperty().get() + dropShadowSize);
			primaryStage.setY(0);
		} else if (cfg.bottomProperty().get()) {
			vertical = false;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get() + dropShadowSize);
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMaxY() - primaryStage.getHeight());
		} else {
			vertical = false;
			primaryStage.setX(bounds.getMinX());
			primaryStage.setHeight(cfg.sizeProperty().get() + dropShadowSize);
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setY(bounds.getMinY());
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);
		writeCSS();

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
		FramedController fc = openScene(Dock.class,
				cfg.isVertical() ? "Vertical" : null);
		fc.getScene().getRoot().getStyleClass().add("rootPane");

		// Configure the scene (window)
		BooleanProperty alwaysOnTopProperty = cfg.alwaysOnTopProperty();

		// Background colour
		setColors(fc.getScene());

		// Finalise and show
		setStageBounds();
		primaryStage.setScene(fc.getScene());
		primaryStage.show();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				primaryStage.setAlwaysOnTop(alwaysOnTopProperty.get());
			}
		});

		cfg.browserTypeProperty().addListener(
				new ChangeListener<BrowserType>() {
					@Override
					public void changed(
							ObservableValue<? extends BrowserType> observable,
							BrowserType oldValue, BrowserType newValue) {
						configureBrowserLauncher();

					}
				});

		configureBrowserLauncher();

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
				writeCSS();
				setColors(primaryStage.getScene());
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
					writeCSS();
					setColors(primaryStage.getScene());
					boolean newVertical = cfg.isVertical();
					if (newVertical != vertical) {
						Dock.getInstance().cleanUp();
						recreateScene();
					} else {
						setStageBounds();
					}
				}
			}
		};
		cfg.topProperty().addListener(dockPositionListener);
		cfg.bottomProperty().addListener(dockPositionListener);
		cfg.leftProperty().addListener(dockPositionListener);
		cfg.rightProperty().addListener(dockPositionListener);

		ChangeListener<Number> geometryChangeListener = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				writeCSS();
				setColors(primaryStage.getScene());
				setStageBounds();
			}
		};
		cfg.monitorProperty().addListener(geometryChangeListener);
		cfg.sizeProperty().addListener(geometryChangeListener);
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
							if(root.isAwaitingLaunch())
								root.onLaunched(() -> root.hideDock(true));
							else
								root.hideDock(true);
						}
						else if(newValue) {
							// If now focussed, stop the onLaunched from firing and hiding again
							root.onLaunched(null);
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
			waitingForExitChoice = true;
			try {
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
			} finally {
				waitingForExitChoice = false;
			}
		} else {
			System.exit(0);
		}
	}

	public static File getCustomCSSFile() {
		File tmpFile;
		if (System.getProperty("hypersocket.bootstrap.distDir") == null)
			tmpFile = new File(new File(System.getProperty("java.io.tmpdir")),
					System.getProperty("user.name") + "-hs-jfx.css");
		else
			tmpFile = new File(new File(
					System.getProperty("hypersocket.bootstrap.distDir")),
					"hs-jfx.css");
		return tmpFile;
	}

	public static void writeCSS() {
		try {
			File tmpFile = getCustomCSSFile();
			String url = toUri(tmpFile).toExternalForm();
			log.info(String.format("Writing user style sheet to %s", url));
			PrintWriter pw = new PrintWriter(new FileOutputStream(tmpFile));
			try {
				pw.println(getCustomCSSResource());
			} finally {
				pw.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not create custom CSS resource.");
		}
	}

	public static void setColors(Scene scene) {
		scene.setFill(new Color(0, 0, 0, 0));
		applyStyles(scene.getRoot());
	}

	public static void applyStyles(Parent root) {
		ObservableList<String> ss = root.getStylesheets();
		File tmpFile = getCustomCSSFile();
		String url = toUri(tmpFile).toExternalForm();
		ss.remove(url);
		ss.add(url);
	}

	public ExecutorService getLoadQueue() {
		return loadQueue;
	}

	public Bridge getBridge() {
		return bridge;
	}

	public void clearLoadQueue() {
		loadQueue.shutdownNow();
		loadQueue = Executors.newSingleThreadExecutor();
	}

	public boolean isWaitingForExitChoice() {
		return waitingForExitChoice;
	}

	private void configureBrowserLauncher() {
		Configuration cfg = Configuration.getDefault();
		switch (cfg.browserTypeProperty().getValue()) {
		case RUN_COMMAND:
			BrowserLauncher.setFactory(new BrowserLauncherFactory() {
				@Override
				public ResourceLauncher create(String uri) {
					return new ResourceLauncher() {
						@Override
						public int launch() {
							String[] args = parseArgs(cfg
									.browserCommandProperty().get().trim());

							if (args.length == 0) {
								getHostServices().showDocument(uri);
								return 0;
							}
							List<String> l = new ArrayList<>();
							for (String a : args) {
								l.add(a.replace("%u", uri));
							}
							if (l.size() == 1) {
								l.add(uri);
							}
							try {
								Process p = new ForkerBuilder(l).io(IO.SINK)
										.background(true).start();
								return p.waitFor();
							} catch (Exception e) {
								log.error(String.format(
										"Failed to open URI %s", uri), e);
								return -1;
							}
						}
					};
				}
			});
			break;
		default:
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
			break;
		}
	}

	private String[] parseArgs(String argString) {
		boolean inQuotes = false;
		boolean escaped = false;
		List<String> l = new ArrayList<>();
		String word = null;
		for (char c : argString.toCharArray()) {
			if (c == '\\' && !escaped) {
				escaped = true;
			} else if (!escaped && c == '"') {
				inQuotes = !inQuotes;
			} else if (!escaped && !inQuotes && c == ' ') {
				if (word != null) {
					l.add(word);
					word = null;
				}
			} else {
				if (word == null)
					word = "";
				word += c;
				escaped = false;
			}
		}
		if (word != null) {
			l.add(word);
		}
		return l.toArray(new String[0]);
	}

	private static URL toUri(File tmpFile) {
		try {
			return tmpFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
