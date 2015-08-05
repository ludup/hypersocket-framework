package com.hypersocket.client.gui.jfx;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.hypersocket.client.rmi.GUICallback;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class Update extends AbstractController {
	final static Logger LOG = Logger.getLogger(Update.class.getName());

	@FXML
	private ProgressBar progress;
	@FXML
	private Label message;

	private double totalBytesExpected;
	private Timeline awaitingBridgeLoss;
	private Timeline awaitingBridgeEstablish;
	private int appsToUpdate;
	private int appsUpdated;

	private Popup popup;

	@Override
	protected void onInitialize() {
	}

	@Override
	public void initUpdate(int apps) {
		super.initUpdate(apps);
		LOG.info(String.format("Initialising update. Expecting %d apps", apps));
		this.message.textProperty().set(resources.getString("init"));
		appsToUpdate = apps;
		appsUpdated = 0;
	}

	@Override
	public void startingUpdate(String app, long totalBytesExpected) {
		LOG.info(String.format("Starting up of %s, expect %d bytes", app,
				totalBytesExpected));
		this.totalBytesExpected = totalBytesExpected;
		this.message.textProperty().set(resources.getString("updating"));
		progress.progressProperty().setValue(0);
	}

	@Override
	public void updateProgressed(String app, long sincelastProgress,
			long totalSoFar) {
		progress.progressProperty().setValue(
				(double) totalSoFar / totalBytesExpected);
	}

	@Override
	public void bridgeEstablished() {
		super.bridgeEstablished();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (awaitingBridgeEstablish != null) {
					// Bridge established as result of update, now restart the
					// client itself
					resetAwaingBridgeEstablish();
					message.textProperty().set(
							resources.getString("guiRestart"));
					new Timeline(new KeyFrame(Duration.seconds(5), ae -> Main
							.getInstance().restart())).play();
				}
			}
		});
	}

	@Override
	public void bridgeLost() {
		super.bridgeLost();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (awaitingBridgeLoss != null) {
					// Bridge lost as result of update, wait for it to come back
					resetAwaingBridgeLoss();
					message.textProperty().set(
							resources.getString("waitingStart"));
					awaitingBridgeEstablish = new Timeline(new KeyFrame(
							Duration.seconds(30),
							ae -> giveUpWaitingForBridgeEstablish()));
					awaitingBridgeEstablish.play();
				}
			}
		});
	}

	@Override
	public void updateComplete(String app, long totalBytesTransfered) {
		progress.progressProperty().setValue(1);
		this.message.textProperty().set(resources.getString("updated"));
		appsUpdated++;
		LOG.info(String.format(
				"Update of %s complete, have now updated %d of %d apps", app,
				appsUpdated, appsToUpdate));
	}

	@Override
	public void updateFailure(String app, String message) {
		LOG.info(String.format("Failed to update app %s. %s", app, message));
		resetState();
		popup.hide();
		try {
			context.getBridge().notify(message, GUICallback.NOTIFY_ERROR);
		} catch (RemoteException e) {
			// Not actually remote
		}
	}

	@Override
	public void initDone(String errorMessage) {
		if (errorMessage == null) {
			LOG.info(String
					.format("All apps updated, starting restart process"));
			awaitingBridgeLoss = new Timeline(new KeyFrame(
					Duration.seconds(30), ae -> giveUpWaitingForBridgeStop()));
			awaitingBridgeLoss.play();
		} else {
			this.message.textProperty().set(errorMessage);
			progress.progressProperty().setValue(1);
			resetState();
		}
	}

	public void setPopup(Popup popup) {
		this.popup = popup;
	}

	private void resetState() {
		resetAwaingBridgeEstablish();
		resetAwaingBridgeLoss();
		totalBytesExpected = 0;
		appsToUpdate = 0;
		appsUpdated = 0;
	}

	private void giveUpWaitingForBridgeEstablish() {
		LOG.info("Given up waiting for bridge to start");
		resetAwaingBridgeEstablish();
		popup.hide();
		try {
			context.getBridge().notify(
					resources.getString("givenUpWaitingForBridgeEstablish"),
					GUICallback.NOTIFY_ERROR);
		} catch (RemoteException e) {
			// Not actually remote
		}
	}

	private void giveUpWaitingForBridgeStop() {
		LOG.info("Given up waiting for bridge to stop");
		resetAwaingBridgeLoss();
		popup.hide();
		try {
			context.getBridge().notify(
					resources.getString("givenUpWaitingForBridgeStop"),
					GUICallback.NOTIFY_ERROR);
		} catch (RemoteException e) {
			// Not actually remote
		}
	}

	private void resetAwaingBridgeLoss() {
		if (awaitingBridgeLoss != null) {
			awaitingBridgeLoss.stop();
			awaitingBridgeLoss = null;
		}
	}

	private void resetAwaingBridgeEstablish() {
		if (awaitingBridgeEstablish != null) {
			awaitingBridgeEstablish.stop();
			awaitingBridgeEstablish = null;
		}
	}
}
