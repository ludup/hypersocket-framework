package com.hypersocket.message;

import java.io.IOException;

import freemarker.template.Template;

public interface FreeMarkerService {

	Template createTemplate(String name, String templateSource, long lastModified) throws IOException;

}
