package com.hypersocket.menus;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="menu")
public class Menu {

	String resourceKey;
	String bundle;
	Integer weight;
	String resourceName;
	String icon;
	boolean canCreate;
	boolean canUpdate;
	boolean canDelete;
	List<Menu> menus = new ArrayList<Menu>();
	
	public Menu(MenuRegistration m, boolean canCreate, boolean canUpdate, boolean canDelete, String icon) {
		this.bundle = m.bundle;
		this.resourceKey = m.getResourceKey();
		this.weight = m.getWeight();
		this.resourceName = m.getResourceName();
		this.canCreate = canCreate;
		this.canUpdate = canUpdate;
		this.canDelete = canDelete;
		this.icon = icon;
	}
	
	public String getId() {
		return resourceKey;
	}
	
	public List<Menu> getMenus() {
		return menus;
	}
	
	public void add(Menu m) {
		menus.add(m);
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public boolean isCanCreate() {
		return canCreate;
	}

	public void setCanCreate(boolean canCreate) {
		this.canCreate = canCreate;
	}

	public boolean isCanUpdate() {
		return canUpdate;
	}

	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
}
