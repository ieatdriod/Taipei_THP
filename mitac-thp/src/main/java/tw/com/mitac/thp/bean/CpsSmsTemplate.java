package tw.com.mitac.thp.bean;

// Generated 2016/4/25 下午 01:55:57 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CpsSmsTemplate generated by hbm2java
 */
@Entity
@Table(name = "cps_sms_template", catalog = "thp")
public class CpsSmsTemplate implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String entitySysid;
	private String vendorSysid;
	private String dataId;
	private String name;
	private String smsMessage;
	private Boolean isSystem;

	public CpsSmsTemplate() {
	}

	public CpsSmsTemplate(String sysid, String creator, String creationDate, String operator, String operationDate,
			String entitySysid, String dataId, String name, String smsMessage, Boolean isSystem) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.entitySysid = entitySysid;
		this.dataId = dataId;
		this.name = name;
		this.smsMessage = smsMessage;
		this.isSystem = isSystem;
	}

	public CpsSmsTemplate(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String entitySysid, String vendorSysid, String dataId, String name, String smsMessage,
			Boolean isSystem) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.entitySysid = entitySysid;
		this.vendorSysid = vendorSysid;
		this.dataId = dataId;
		this.name = name;
		this.smsMessage = smsMessage;
		this.isSystem = isSystem;
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

	@Column(name = "ENTITY_SYSID", nullable = false, length = 45)
	public String getEntitySysid() {
		return this.entitySysid;
	}

	public void setEntitySysid(String entitySysid) {
		this.entitySysid = entitySysid;
	}

	@Column(name = "VENDOR_SYSID", length = 45)
	public String getVendorSysid() {
		return this.vendorSysid;
	}

	public void setVendorSysid(String vendorSysid) {
		this.vendorSysid = vendorSysid;
	}

	@Column(name = "SMS_ID", nullable = false, length = 30)
	public String getDataId() {
		return this.dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	@Column(name = "SMS_NAME", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "SMS_MESSAGE", nullable = false, length = 65535)
	public String getSmsMessage() {
		return this.smsMessage;
	}

	public void setSmsMessage(String smsMessage) {
		this.smsMessage = smsMessage;
	}

	@Column(name = "IS_SYSTEM", nullable = false)
	public Boolean getIsSystem() {
		return this.isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

}