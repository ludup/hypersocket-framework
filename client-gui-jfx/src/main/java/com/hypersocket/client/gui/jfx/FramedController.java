package com.hypersocket.client.gui.jfx;

import javafx.fxml.Initializable;
import javafx.scene.Scene;

public interface FramedController extends Initializable {

	void configure(Scene scene, Client jfxhsClient);
}
