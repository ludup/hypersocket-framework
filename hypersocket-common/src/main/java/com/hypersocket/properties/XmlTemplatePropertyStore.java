package com.hypersocket.properties;

import java.io.IOException;

import org.w3c.dom.Element;

public interface XmlTemplatePropertyStore extends PropertyStore {

	void init(Element element) throws IOException;
}
