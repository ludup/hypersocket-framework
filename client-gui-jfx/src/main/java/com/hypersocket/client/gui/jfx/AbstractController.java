package com.hypersocket.client.gui.jfx;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

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

	@Override
	public final void initialize(URL location, ResourceBundle resources) {
		this.location = location;
		this.resources = resources;
		onInitialize();
	}

	@Override
	public final void configure(Scene scene, Client jfxhsClient) {
		this.scene = scene;
		this.context = jfxhsClient;
		onConfigure();
		
		restyleAllToolTips(scene);		
		
		context.getBridge().addListener(this);
	}
	
	protected Stage getStage() {
		return (Stage) scene.getWindow();
	}

	@Override
	public void bridgeEstablished() {
		stateChanged();
	}

	@Override
	public void bridgeLost() {
		stateChanged();
	}

	@Override
	public void ping() {
	}

	protected void onConfigure() {
	}

	protected void onInitialize() {
	}

	protected void onStateChanged() {

	}

	@Override
	public void connecting(Connection connection) {
		stateChanged();		
	}

	@Override
	public void finishedConnecting(Connection connection, Exception e) {
		stateChanged();				
	}

	@Override
	public void disconnecting(Connection connection) {
		stateChanged();						
	}

	@Override
	public void disconnected(Connection connection, Exception e) {
		stateChanged();								
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts) {
		return null;
	}

	@Override
	public void startingUpdate(String app, long totalBytesExpected) {
	}

	@Override
	public void updateProgressed(String app, long sincelastProgress, long totalSoFar) {
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
	
	

	private void restyleAllToolTips(Scene scene) {
		walkTree(scene.getRoot(), new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				if(t instanceof Control) {
					Tooltip tooltip = ((Control)t).getTooltip();
					if(tooltip != null) {
						// NOTE Bleh. Have to recreate tooltips? How crazy is that
						Tooltip newTooltip = new Tooltip();
						newTooltip.setText(tooltip.getText());
						UIHelpers.styleToolTip(newTooltip);
						((Control)t).setTooltip(newTooltip);
					}
				}				
			}
		});
	}

	private void stateChanged() {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					onStateChanged();
				}
			});
		} else {
			onStateChanged();
		}
	}

    private void walkTree(Object node, Consumer<Object> visitor) {
        visitor.accept(node);
        if(node instanceof TabPane) { 
        	((TabPane )node).getTabs().forEach(n -> walkTree(n, visitor));
        }
        else if(node instanceof Tab) {
        	walkTree(((Tab)node).getContent(), visitor);
        }
        else if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable()
                .forEach(n -> walkTree(n, visitor));
        }
    }
}
