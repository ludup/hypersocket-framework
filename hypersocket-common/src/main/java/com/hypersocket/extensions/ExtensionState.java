package com.hypersocket.extensions;

import java.io.Serializable;

public enum ExtensionState implements Serializable {
	NOT_INSTALLED, INSTALLED, UPDATABLE, AWAITING_RESTART
}
