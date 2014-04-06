/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.websocket;

import java.io.IOException;

public class WebSocketException extends IOException {

	private static final long serialVersionUID = -4042264309539592620L;

	public WebSocketException(String s) {
        super(s);
    }

    public WebSocketException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
