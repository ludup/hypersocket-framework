package com.hypersocket.client.gui.jfx;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.Prompt;
import com.hypersocket.client.gui.jfx.Bridge.Listener;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.extensions.ExtensionDefinition;

public class AbstractController implements FramedController, Listener {
	static Logger log = LoggerFactory.getLogger(AbstractController.class);

	protected Client context;
	protected ResourceBundle resources;
	protected URL location;
	protected Scene scene;
	protected PopOver popOver;
	protected Popup popup;

	private Node popOverNode;

	@Override
	public final void initialize(URL location, ResourceBundle resources) {
		this.location = location;
		this.resources = resources;
		onInitialize();
	}

	@Override
	public final void cleanUp() {
		context.getBridge().removeListener(this);
		onCleanUp();
	}

	@Override
	public final void configure(Scene scene, Client jfxhsClient) {
		this.scene = scene;
		this.context = jfxhsClient;
		onConfigure();
		context.getBridge().addListener(this);
	}

	protected Stage getStage() {
		return (Stage) scene.getWindow();
	}

	@Override
	public void bridgeEstablished() {
	}

	@Override
	public void bridgeLost() {
	}

	@Override
	public void ping() {
	}

	protected void onConfigure() {
	}

	protected void onCleanUp() {
	}

	protected void onInitialize() {
	}

	@Override
	public void connecting(Connection connection) {
	}

	@Override
	public void started(Connection connection) {
	}

	@Override
	public void finishedConnecting(Connection connection, Exception e) {
	}

	@Override
	public void loadResources(Connection connection) {
	}

	@Override
	public void disconnecting(Connection connection) {
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts, int attempts,
			boolean success) {
		return null;
	}

	@Override
	public void startingUpdate(String app, long totalBytesExpected) {
	}

	@Override
	public void updateProgressed(String app, long sincelastProgress,
			long totalSoFar, long totalBytesExpected) {
	}

	@Override
	public void updateComplete(String app, long totalBytesTransfered) {
	}

	@Override
	public void updateFailure(String app, String message) {
	}

	@Override
	public void extensionUpdateComplete(String app, ExtensionDefinition def) {
	}

	@Override
	public void initUpdate(int apps) {
	}

	@Override
	public void initDone(String errorMessage) {
	}

	public final void setPopup(Popup popup) {
		this.popup = popup;
		onSetPopup(popup);
	}
	
	protected void onSetPopup(Popup popup) {
		
	}
	
	protected void showPopOver(String text, Node node) {

		
		if(!Objects.equals(popOverNode, node)) {
			hidePopOver();
			popOverNode = node;
			
		}
		
		if(popOver != null && popOver.isShowing()) {
			return;
		}

		if(popOver == null) {
			popOver = new PopOver();

			popOver.setConsumeAutoHidingEvents(false);
			popup.setPopOver(popOver);
			popOver.getRoot().focusTraversableProperty().set(false);
			popOver.getRoot().getStylesheets().add(
					Client.class.getResource(Client.class.getSimpleName() + ".css")
							.toExternalForm());
			popOver.getRoot().maxWidthProperty().set(380);
	    	Client.applyStyles(popOver.getRoot());
			
			Label l = new Label();
			l.wrapTextProperty().set(true);
			l.setText(text);
			
			popOver.setContentNode(l);
		}
		else {
			((Label)popOver.getContentNode()).setText(text);
		}
		Configuration cfg = Configuration.getDefault();
		
		Bounds bounds = node.localToScene(node.getBoundsInLocal());

		
		if(cfg.topProperty().get() || cfg.bottomProperty().get() || cfg.rightProperty().get()) {
			popOver.arrowLocationProperty().set(ArrowLocation.RIGHT_TOP);
			popOver.show(popup, popup.getX() - popOver.getRoot().layoutBoundsProperty().get().getMaxX() - 20, popup.getY() + bounds.getMinY() - (bounds.getHeight() ));
			
			/* NOTE: Ugh. Without this manual layout and re-show, the popover will initially be in the wrong place.
			 * This is because only a max width is set and a layout must occur before we know the true width.
			 */
			popOver.getRoot().applyCss();
			popOver.getRoot().layout();
			popOver.show(popup, popup.getX() - popOver.getRoot().layoutBoundsProperty().get().getMaxX() - 20, popup.getY() + bounds.getMinY() - (bounds.getHeight() ));
		}
		else {
			popOver.arrowLocationProperty().set(ArrowLocation.LEFT_TOP);
			popOver.show(popup, popup.getX() + popup.getWidth() + 5, popup.getY() + bounds.getMinY() - (bounds.getHeight() ));
			
			/* NOTE: Ugh. Without this manual layout and re-show, the popover will initially be in the wrong place.
			 * This is because only a max width is set and a layout must occur before we know the true width.
			 */
			popOver.getRoot().applyCss();
			popOver.getRoot().layout();
			popOver.show(popup, popup.getX() + popup.getWidth() + 5, popup.getY() + bounds.getMinY() - (bounds.getHeight() ));
		}
	}
	
	protected void hidePopOver() {
		if(popOver != null) {
			popOver.hide(Duration.millis(0));
		}
	}

	protected void walkTree(Object node, Consumer<Object> visitor) {
		if (node == null) {
			return;
		}
		visitor.accept(node);
		if (node instanceof TabPane) {
			((TabPane) node).getTabs().forEach(n -> walkTree(n, visitor));
		} else if (node instanceof Tab) {
			walkTree(((Tab) node).getContent(), visitor);
			walkTree(((Tab) node).getGraphic(), visitor);
		} else if (node instanceof Parent) {
			((Parent) node).getChildrenUnmodifiable().forEach(
					n -> walkTree(n, visitor));
		}
	}
}
