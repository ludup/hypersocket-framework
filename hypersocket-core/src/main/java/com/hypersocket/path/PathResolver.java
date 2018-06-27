package com.hypersocket.path;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface PathResolver {

	boolean isSystem();

	InputStream resolvePath(String path) throws FileNotFoundException;

}
