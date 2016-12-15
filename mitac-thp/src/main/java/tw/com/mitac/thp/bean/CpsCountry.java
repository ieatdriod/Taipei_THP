package tw.com.mitac.thp.bean;
// Generated 2016/4/14 �W�� 09:42:17 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CpsCountry generated by hbm2java
 */
@Entity
@Table(name = "cps_country", catalog = "thp")
public class CpsCountry implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String dataId;
	private String name;
	private Boolean isForeign;
	private Boolean isEnabled;
	private String area;
	private String countryCallingCode;

	public CpsCountry() {
	}

	public CpsCountry(String sysid, String creator, String creationDate, String operator, String operationDate,
			String dataId, String name, Boolean isForeign, Boolean isEnabled, String area) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.dataId = dataId;
		this.name = name;
		this.isForeign = isForeign;
		this.isEnabled = isEnabled;
		this.area = area;
	}

	public CpsCountry(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String dataId, String name, Boolean isForeign, Boolean isEnabled, String area,
			String countryCallingCode) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.dataId = dataId;
		this.name = name;
		this.isForeign = isForeign;
		this.isEnabled = isEnabled;
		this.area = area;
		this.countryCallingCode = countryCallingCode;
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

	@Column(name = "COUNTRY_ID", nullable = false, length = 30)
	public String getDataId() {
		return this.dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	@Column(name = "COUNTRY_NAME", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "IS_FOREIGN", nullable = false)
	public Boolean getIsForeign() {
		return this.isForeign;
	}

	public void setIsForeign(Boolean isForeign) {
		this.isForeign = isForeign;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "AREA", nullable = false, length = 50)
	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Column(name = "COUNTRY_CALLING_CODE", length = 10)
	public String getCountryCallingCode() {
		return this.countryCallingCode;
	}

	public void setCountryCallingCode(String countryCallingCode) {
		this.countryCallingCode = countryCallingCode;
	}

}