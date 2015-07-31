package com.hypersocket.client.gui.jfx;

import java.util.ArrayList;
import java.util.List;

public class ResourceGroupList {
	private ResourceGroupKey key;
	private List<ResourceItem> items = new ArrayList<>();

	public ResourceGroupList(ResourceGroupKey key) {
		this.key = key;
	}

	public List<ResourceItem> getItems() {
		return items;
	}

	public ResourceGroupKey getKey() {
		return key;
	}
}