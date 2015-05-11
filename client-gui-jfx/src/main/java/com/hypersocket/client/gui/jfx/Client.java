package com.hypersocket.client.gui.jfx;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.hypersocket.client.Option;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.gui.jfx.fontawesome.AwesomeIcons;
import com.hypersocket.client.rmi.BrowserLauncher;
import com.hypersocket.client.rmi.BrowserLauncher.BrowserLauncherFactory;
import com.hypersocket.client.rmi.ResourceLauncher;

public class Client extends Application implements Context {

	private Bridge bridge;
	private Stage primaryStage;

	public Scene openScene(Class<? extends Initializable> controller)
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
		return scene;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		bridge = new Bridge(this);

		primaryStage.initStyle(StageStyle.UNDECORATED);
		Scene scene = openScene(Dock.class);
		Screen screen = Screen.getPrimary();

		// TODO might need to monitor the bounds constantly, I can't see
		// a way to get screen geometry change events.
		Rectangle2D bounds = screen.getVisualBounds();
		primaryStage.setX(bounds.getMinX());
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setY(bounds.getMinY());
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setScene(scene);
		primaryStage.show();

		this.primaryStage = primaryStage;

		// Install JavaFX compatible browser launcher
//		if (SystemUtils.IS_OS_LINUX) {
//			// I am having problems with both AWT and this JavaFX specific
//			// browser launcher on Linux :(
//			BrowserLauncher.setFactory(new BrowserLauncherFactory() {
//
//				public ResourceLauncher create(String uri) {
//					return new ResourceLauncher() {
//						@Override
//						public int launch() {
//							try {
//								return new ProcessBuilder("x-www-browser", uri)
//										.redirectOutput(Redirect.INHERIT)
//										.redirectErrorStream(true).start()
//										.waitFor();
//							} catch (IOException | InterruptedException e) {
//								e.printStackTrace();
//								return -1;
//							}
//						}
//					};
//				}
//			});
//		} else {
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
//		}
	}

	public Bridge getBridge() {
		return bridge;
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts) {
		final Map<String, String> values = new HashMap<String, String>();

		Semaphore s = new Semaphore(1);
		try {
			s.acquire();
			Platform.runLater(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {

					VBox vbox = new VBox();
					Map<Prompt, Node> nodes = new HashMap<Prompt, Node>();
					for (Prompt p : prompts) {
						Label l = new Label(p.getLabel());
						vbox.getChildren().add(l);
						switch (p.getType()) {
						case TEXT:
							TextField txt = new TextField(p.getDefaultValue());
							vbox.getChildren().add(txt);
							nodes.put(p, txt);
							break;
						case HIDDEN:
							values.put(p.getResourceKey(), p.getDefaultValue());
							break;
						case PASSWORD:
							PasswordField pw = new PasswordField();
							vbox.getChildren().add(pw);
							nodes.put(p, pw);
							break;
						case SELECT:
							ComboBox<String> cb = new ComboBox<String>();
							for (Option o : p.getOptions()) {
								cb.itemsProperty().get().add(o.getName());
							}
							vbox.getChildren().add(cb);
							nodes.put(p, cb);
							break;
						case P:
							// TODO What's a P?
							break;
						}
					}
					Scene scene = new Scene(vbox);
					Popup popup = new Popup(primaryStage, scene, false);
					Button b = new Button("Continue");
					b.setOnAction((event) -> {
						popup.hide();
						for (Map.Entry<Prompt, Node> en : nodes.entrySet()) {
							if (en.getValue() instanceof TextField) {
								values.put(en.getKey().getResourceKey(),
										((TextField) en.getValue()).getText());
							} else if (en.getValue() instanceof PasswordField) {
								values.put(en.getKey().getResourceKey(),
										((PasswordField) en.getValue())
												.getText());
							} else if (en.getValue() instanceof ComboBox) {
								values.put(en.getKey().getResourceKey(),
										((ComboBox<String>) en.getValue())
												.getValue());
							}
						}
						s.release();
					});
					vbox.getChildren().add(b);
					popup.initModality(Modality.WINDOW_MODAL);
					popup.popup();
				}
			});
			s.acquire();
			s.release();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return values;
	}
}
