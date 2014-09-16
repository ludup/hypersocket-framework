package com.hypersocket.client.util;

import java.io.IOException;

public interface CommandInputGenerator {

	void commandStarted(Process p) throws IOException;

	void commandOutput(Process p, char r) throws IOException;

}
