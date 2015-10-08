package com.hypersocket.client.gui.jfx;

import com.hypersocket.client.rmi.Resource;

public class ResourceGroupKey implements Comparable<ResourceGroupKey> {
	private Resource.Type type;
	private String subType;

	public ResourceGroupKey(Resource.Type type, String subType) {
		this.type = type;
		this.subType = subType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getSubType() == null) ? 0 : getSubType().hashCode());
		result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceGroupKey other = (ResourceGroupKey) obj;
		if (getSubType() == null) {
			if (other.getSubType() != null)
				return false;
		} else if (!getSubType().equals(other.getSubType()))
			return false;
		if (getType() != other.getType())
			return false;
		return true;
	}

	public String getSubType() {
		return subType;
	}

	public Resource.Type getType() {
		return type;
	}

	@Override
	public int compareTo(ResourceGroupKey o) {
		int i = type.compareTo(o.type);
		if(i == 0) {
			String s1 = subType == null ? "" : subType;
			String s2 = o.subType == null ? "" : o.subType;
			return s1.compareTo(s2);
		}
		return i;
	}

	@Override
	public String toString() {
		return "ResourceGroupKey [type=" + type + ", subType=" + subType + "]";
	}

}