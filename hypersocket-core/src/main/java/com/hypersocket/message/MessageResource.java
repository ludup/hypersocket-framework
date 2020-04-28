package com.hypersocket.message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "message_resource", uniqueConstraints = @UniqueConstraint(columnNames = { "resource_key", "realm_id" }))
public class MessageResource extends RealmResource {

	private static final long serialVersionUID = -5676595715637581705L;

	@Column(name = "resource_key", nullable = true)
	private String resourceKey;

	@Column(name = "subject", length = 1024)
	private String subject;

	@Column(name = "body")
	@Lob
	private String body;

	@Column(name = "html")
	@Lob
	private String html;

	@Column(name = "enabled")
	private Boolean enabled;

	@Column(name = "track")
	private Boolean track;

	@Column(name = "system_only")
	private Boolean systemOnly;

	@Column(name = "use_template")
	private Boolean useTemplate;

	@Column(name = "attachments", length = 1024)
	private String attachments;

	@Column(name = "deliver_strategy")
	private EmailDeliveryStrategy deliveryStrategy;

	@Column(name = "additional", length = 1024)
	private String additionalTo;

	@Column(name = "reply_name", length = 1024)
	private String replyToName;

	@Column(name = "reply_email", length = 1024)
	private String replyToEmail;

	@Column(name = "variables")
	@Lob
	private String supportedVariables;

	@ManyToOne
	@JoinColumn(name = "html_template", foreignKey = @ForeignKey(name = "message_resource_cascade_2"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private HtmlTemplateResource htmlTemplate;

	@Column(name = "archive")
	private Boolean archive;

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "message_resource_cascade_1"))
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

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getTrack() {
		return track;
	}

	public void setTrack(Boolean track) {
		this.track = track;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public String getAdditionalTo() {
		return additionalTo;
	}

	public void setAdditionalTo(String additionalTo) {
		this.additionalTo = additionalTo;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public String getReplyToEmail() {
		return replyToEmail;
	}

	public void setReplyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}

	public String getSupportedVariables() {
		return supportedVariables;
	}

	public void setSupportedVariables(String supportedVariables) {
		this.supportedVariables = supportedVariables;
	}

	public EmailDeliveryStrategy getDeliveryStrategy() {
		return deliveryStrategy == null ? EmailDeliveryStrategy.PRIMARY : deliveryStrategy;
	}

	public void setDeliveryStrategy(EmailDeliveryStrategy deliveryStrategy) {
		this.deliveryStrategy = deliveryStrategy;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public HtmlTemplateResource getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(HtmlTemplateResource htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public Boolean getArchive() {
		return archive == null ? Boolean.FALSE : archive;
	}

	public void setArchive(Boolean archive) {
		this.archive = archive;
	}

}
