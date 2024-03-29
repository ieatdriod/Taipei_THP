package tw.com.mitac.thp.bean;
// Generated 2016/10/17 �U�� 01:32:59 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CpsVendor generated by hbm2java
 */
@Entity
@Table(name = "cps_vendor", catalog = "thp")
public class CpsVendor implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String name;
	private String address;
	private String vendorPhone;
	private String execute;
	private Boolean isEnabled;
	private String vendorShortName;
	private String entitySysid;
	private String vendorContact;
	private String vendorContactTel;
	private Boolean isEnabledConversations;
	private Boolean isEnabledVideoconferencing;
	private java.util.Set<CpsMemberForVendor> detailSet;

	public java.util.Set<CpsMemberForVendor> getDetailSet() {
		return detailSet;
	}

	public void setDetailSet(java.util.Set<CpsMemberForVendor> detailSet) {
		this.detailSet = detailSet;
	}

	public CpsVendor() {
	}

	public CpsVendor(String sysid, String creator, String creationDate, String operator, String operationDate,
			String name, Boolean isEnabled, Boolean isEnabledConversations, Boolean isEnabledVideoconferencing) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.name = name;
		this.isEnabled = isEnabled;
		this.isEnabledConversations = isEnabledConversations;
		this.isEnabledVideoconferencing = isEnabledVideoconferencing;
	}

	public CpsVendor(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String name, String address, String vendorPhone, String execute, Boolean isEnabled,
			String vendorShortName, String entitySysid, String vendorContact, String vendorContactTel,
			Boolean isEnabledConversations, Boolean isEnabledVideoconferencing) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.name = name;
		this.address = address;
		this.vendorPhone = vendorPhone;
		this.execute = execute;
		this.isEnabled = isEnabled;
		this.vendorShortName = vendorShortName;
		this.entitySysid = entitySysid;
		this.vendorContact = vendorContact;
		this.vendorContactTel = vendorContactTel;
		this.isEnabledConversations = isEnabledConversations;
		this.isEnabledVideoconferencing = isEnabledVideoconferencing;
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

	@Column(name = "VENDOR_NAME", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "ADDRESS")
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "VENDOR_PHONE")
	public String getVendorPhone() {
		return this.vendorPhone;
	}

	public void setVendorPhone(String vendorPhone) {
		this.vendorPhone = vendorPhone;
	}

	@Column(name = "EXECUTE")
	public String getExecute() {
		return this.execute;
	}

	public void setExecute(String execute) {
		this.execute = execute;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "VENDOR_SHORT_NAME")
	public String getVendorShortName() {
		return this.vendorShortName;
	}

	public void setVendorShortName(String vendorShortName) {
		this.vendorShortName = vendorShortName;
	}

	@Column(name = "ENTITY_SYSID", length = 45)
	public String getEntitySysid() {
		return this.entitySysid;
	}

	public void setEntitySysid(String entitySysid) {
		this.entitySysid = entitySysid;
	}

	@Column(name = "VENDOR_CONTACT", length = 45)
	public String getVendorContact() {
		return this.vendorContact;
	}

	public void setVendorContact(String vendorContact) {
		this.vendorContact = vendorContact;
	}

	@Column(name = "VENDOR_CONTACT_TEL", length = 45)
	public String getVendorContactTel() {
		return this.vendorContactTel;
	}

	public void setVendorContactTel(String vendorContactTel) {
		this.vendorContactTel = vendorContactTel;
	}

	@Column(name = "IS_ENABLED_CONVERSATIONS", nullable = false)
	public Boolean getIsEnabledConversations() {
		return this.isEnabledConversations;
	}

	public void setIsEnabledConversations(Boolean isEnabledConversations) {
		this.isEnabledConversations = isEnabledConversations;
	}

	@Column(name = "IS_ENABLED_VIDEOCONFERENCING", nullable = false)
	public Boolean getIsEnabledVideoconferencing() {
		return this.isEnabledVideoconferencing;
	}

	public void setIsEnabledVideoconferencing(Boolean isEnabledVideoconferencing) {
		this.isEnabledVideoconferencing = isEnabledVideoconferencing;
	}

}
