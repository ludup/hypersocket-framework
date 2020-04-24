package com.hypersocket.html;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "html_templates")
public class HtmlTemplateResource extends RealmResource {

	private static final long serialVersionUID = 4099376557971813286L;

	@Column(name = "content_selector")
	private String contentSelector;

	@Lob
	@Column(name = "html")
	private String html;

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "html_templates_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	protected Realm realm;

	@Override
	protected Realm doGetRealm() {
		return realm;
	}

	@Override
	public void setRealm(Realm realm) {
		this.realm = realm;
	}

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
