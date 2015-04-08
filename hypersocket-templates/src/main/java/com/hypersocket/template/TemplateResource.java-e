package com.hypersocket.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="templates")
public class TemplateResource extends RealmResource {

	@Column(name="logo")
	String templateLogo;
	
	@Column(name="template_variables", length=8000)
	String variables;
	
	@Column(name="template_script", length=8000)
	String script;

	@Column(name="template_type")
	TemplateType type;
	
	@Column(name="template_status")
	TemplateStatus status;
	
	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getTemplateLogo() {
		return templateLogo;
	}

	public void setTemplateLogo(String templateLogo) {
		this.templateLogo = templateLogo;
	}
	
	public void setTemplateType(String templateType) {
		this.type = TemplateType.valueOf(templateType);
	}
	
	public TemplateType getTemplateType() {
		return type;
	}
	
	public TemplateStatus getTemplateStatus() {
		return status==null ? TemplateStatus.PRIVATE : status;
	}
	
	public void setTemplateStatus(String templateStatus) {
		this.status = TemplateStatus.valueOf(templateStatus);
	}
	
	
}
