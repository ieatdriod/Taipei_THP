package tw.com.mitac.miaa.bean;

// Generated 2015/5/15 下午 02:10:12 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Miaa03FileProperty generated by hbm2java
 */
@Entity
@Table(name = "miaa03_file_property")
public class Miaa03FileProperty implements java.io.Serializable {

	private String sysid;
	private String filePropertyId;
	private String filePropertyName;
	private String fileSysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private Boolean isEnable;

	public Miaa03FileProperty() {
	}

	public Miaa03FileProperty(String sysid, String filePropertyId, String filePropertyName, String fileSysid,
			String creator, String creationDate, String operator, String operationDate, Boolean isEnable) {
		this.sysid = sysid;
		this.filePropertyId = filePropertyId;
		this.filePropertyName = filePropertyName;
		this.fileSysid = fileSysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.isEnable = isEnable;
	}

	@Id
	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	@Column(name = "FILE_PROPERTY_ID", nullable = false)
	public String getFilePropertyId() {
		return this.filePropertyId;
	}

	public void setFilePropertyId(String filePropertyId) {
		this.filePropertyId = filePropertyId;
	}

	@Column(name = "FILE_PROPERTY_NAME", nullable = false)
	public String getFilePropertyName() {
		return this.filePropertyName;
	}

	public void setFilePropertyName(String filePropertyName) {
		this.filePropertyName = filePropertyName;
	}

	@Column(name = "FILE_SYSID", nullable = false, length = 45)
	public String getFileSysid() {
		return this.fileSysid;
	}

	public void setFileSysid(String fileSysid) {
		this.fileSysid = fileSysid;
	}

	@Column(name = "CREATOR", nullable = false)
	public String getCreator() {
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Column(name = "CREATION_DATE", nullable = false)
	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "OPERATOR", nullable = false)
	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Column(name = "OPERATION_DATE", nullable = false)
	public String getOperationDate() {
		return this.operationDate;
	}

	public void setOperationDate(String operationDate) {
		this.operationDate = operationDate;
	}

	@Column(name = "IS_ENABLE", nullable = false)
	public Boolean getIsEnable() {
		return this.isEnable;
	}

	public void setIsEnable(Boolean isEnable) {
		this.isEnable = isEnable;
	}

}