
package tw.com.mitac.thp.bean;

// Generated 2016/6/9 �U�� 03:21:47 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MtsAppointmentFileLink generated by hbm2java
 */
@Entity
@Table(name = "mts_appointment_file_link", catalog = "thp")
public class MtsAppointmentFileLink implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	
	private String parentSysid;
	private String fileName;

	public MtsAppointmentFileLink() {
	}

	public MtsAppointmentFileLink(String sysid, String creator,
			String creationDate, String operator, String operationDate,
			String parentSysid, String fileName) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.parentSysid = parentSysid;
		this.fileName = fileName;
	}

	public MtsAppointmentFileLink(String sysid, String creator,
			String creationDate, String operator, String operationDate,
			String remark, String parentSysid, String fileName) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.parentSysid = parentSysid;
		this.fileName = fileName;
	}

	@Id
	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
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

	@Column(name = "REMARK", length = 65535)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "APPOINTMENT_SYSID", nullable = false, length = 45)
	public String getParentSysid() {
		return this.parentSysid;
	}

	public void setParentSysid(String parentSysid) {
		this.parentSysid = parentSysid;
	}

	@Column(name = "FILE_NAME", nullable = false)
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
