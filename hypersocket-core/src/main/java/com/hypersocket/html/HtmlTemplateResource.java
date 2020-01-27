package com.hypersocket.html;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="html_templates")
public class HtmlTemplateResource extends RealmResource {

	private static final long serialVersionUID = 4099376557971813286L;
	
	@Column(name="content_selector")
	private String contentSelector;
	
	@Lob
	@Column(name="html")
	private String html;

	public String getContentSelector() {
		return contentSelector;
	}

	public void setContentSelector(String contentSelector) {
		this.contentSelector = contentSelector;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
	
	
}
