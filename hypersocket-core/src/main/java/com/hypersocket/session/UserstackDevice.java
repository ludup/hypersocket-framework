package com.hypersocket.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserstackDevice implements Serializable {

	private static final long serialVersionUID = 4472768001423833621L;

	boolean is_mobile_device;
	String type;
	String brand;
	String brand_code;
	String brand_url;
	String name;
	public boolean isIs_mobile_device() {
		return is_mobile_device;
	}
	public void setIs_mobile_device(boolean is_mobile_device) {
		this.is_mobile_device = is_mobile_device;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getBrand_code() {
		return brand_code;
	}
	public void setBrand_code(String brand_code) {
		this.brand_code = brand_code;
	}
	public String getBrand_url() {
		return brand_url;
	}
	public void setBrand_url(String brand_url) {
		this.brand_url = brand_url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
