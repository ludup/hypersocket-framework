package com.hypersocket.upload;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "uploaded_files")
public class FileUpload extends RealmResource {

	private static final long serialVersionUID = 8896410295739619520L;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "md5_sum")
	private String md5Sum;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "type")
	private String type;

	@Column(name = "is_public")
	private Boolean isPublic;

	@Column(name = "short_code", nullable = true)
	private String shortCode;

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "uploaded_files_cascade_1"))
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

	@Transient
	private String url;

	@Transient
	private String content;

	public FileUpload() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMd5Sum() {
		return md5Sum;
	}

	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getShortCode() {
		return shortCode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getPublicFile() {
		return isPublic == null ? Boolean.FALSE : isPublic;
	}

	public void setPublicFile(Boolean isPublic) {
		this.isPublic = isPublic;
	}
}
