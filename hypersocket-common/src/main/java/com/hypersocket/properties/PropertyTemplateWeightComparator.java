package com.hypersocket.properties;

import java.util.Comparator;

public class PropertyTemplateWeightComparator implements Comparator<AbstractPropertyTemplate> {
	@Override
	public int compare(AbstractPropertyTemplate cat1,
			AbstractPropertyTemplate cat2) {
		return cat1.getWeight().compareTo(cat2.getWeight());
	}
}