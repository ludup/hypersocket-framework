package com.hypersocket.profile.export;

import com.hypersocket.io.FileContentGenerator;

public interface ProfileExporter {
	FileContentGenerator generator();
	String fileName();
}
