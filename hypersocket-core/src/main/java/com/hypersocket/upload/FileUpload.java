package com.hypersocket.upload;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "uploaded_files")
public class FileUpload extends RealmResource {

	@Column(name = "file_name")
	String fileName;

	@Column(name = "md5_sum")
	String md5Sum;

	@Column(name = "file_size")
	Long fileSize;

	@Column(name = "type")
	String type;
	
	public FileUpload() {

	}

//	public FileUpload(String fileName, String md5Sum, Long fileSize, String type) {
//		this.fileName = fileName;
//		this.md5Sum = md5Sum;
//		this.fileSize = fileSize;
//		this.type = type;
//	}

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
	
	

}
