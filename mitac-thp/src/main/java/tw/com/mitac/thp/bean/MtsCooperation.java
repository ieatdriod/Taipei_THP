package tw.com.mitac.thp.bean;
// Generated 2016/11/9 �U�� 03:42:43 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MtsCooperation generated by hbm2java
 */
@Entity
@Table(name = "mts_cooperation", catalog = "thp")
public class MtsCooperation implements java.io.Serializable {

	private String sysid;
	private String creationDate;
	private String creator;
	private String operator;
	private String operationDate;
	private String remark;
	private String cooperationName;
	private Integer dataOrder;
	private String vendorSysid;
	private Boolean isEnabled;
	private String cooperationSummary;
	private String cooperationProfileFull;
	private String cooperationText2;
	private String cooperationText3;
	private String cooperationSummaryImg;

	private java.util.Set<MtsInfoLink> detailSet;
	private java.util.Set<MtsAdsC> detailSet2;

	public java.util.Set<MtsInfoLink> getDetailSet() {
		return detailSet;
	}

	public void setDetailSet(java.util.Set<MtsInfoLink> detailSet) {
		this.detailSet = detailSet;
	}

	public java.util.Set<MtsAdsC> getDetailSet2() {
		return detailSet2;
	}

	public void setDetailSet2(java.util.Set<MtsAdsC> detailSet2) {
		this.detailSet2 = detailSet2;
	}

	public MtsCooperation() {
	}

	public MtsCooperation(String sysid, String creationDate, String creator, String operator, String operationDate,
			String cooperationName, Integer dataOrder, String vendorSysid, Boolean isEnabled) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.cooperationName = cooperationName;
		this.dataOrder = dataOrder;
		this.vendorSysid = vendorSysid;
		this.isEnabled = isEnabled;
	}

	public MtsCooperation(String sysid, String creationDate, String creator, String operator, String operationDate,
			String remark, String cooperationName, Integer dataOrder, String vendorSysid, Boolean isEnabled,
			String cooperationSummary, String cooperationProfileFull, String cooperationText2, String cooperationText3,
			String cooperationSummaryImg) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.cooperationName = cooperationName;
		this.dataOrder = dataOrder;
		this.vendorSysid = vendorSysid;
		this.isEnabled = isEnabled;
		this.cooperationSummary = cooperationSummary;
		this.cooperationProfileFull = cooperationProfileFull;
		this.cooperationText2 = cooperationText2;
		this.cooperationText3 = cooperationText3;
		this.cooperationSummaryImg = cooperationSummaryImg;
	}

	@Id

	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	@Column(name = "CREATION_DATE", nullable = false)
	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "CREATOR", nullable = false)
	public String getCreator() {
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
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

	@Column(name = "COOPERATION_NAME", nullable = false)
	public String getCooperationName() {
		return this.cooperationName;
	}

	public void setCooperationName(String cooperationName) {
		this.cooperationName = cooperationName;
	}

	@Column(name = "DATA_ORDER", nullable = false)
	public Integer getDataOrder() {
		return this.dataOrder;
	}

	public void setDataOrder(Integer dataOrder) {
		this.dataOrder = dataOrder;
	}

	@Column(name = "VENDOR_SYSID", nullable = false, length = 45)
	public String getVendorSysid() {
		return this.vendorSysid;
	}

	public void setVendorSysid(String vendorSysid) {
		this.vendorSysid = vendorSysid;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "COOPERATION_SUMMARY")
	public String getCooperationSummary() {
		return this.cooperationSummary;
	}

	public void setCooperationSummary(String cooperationSummary) {
		this.cooperationSummary = cooperationSummary;
	}

	@Column(name = "COOPERATION_PROFILE_FULL", length = 65535)
	public String getCooperationProfileFull() {
		return this.cooperationProfileFull;
	}

	public void setCooperationProfileFull(String cooperationProfileFull) {
		this.cooperationProfileFull = cooperationProfileFull;
	}

	@Column(name = "COOPERATION_TEXT_2", length = 65535)
	public String getCooperationText2() {
		return this.cooperationText2;
	}

	public void setCooperationText2(String cooperationText2) {
		this.cooperationText2 = cooperationText2;
	}

	@Column(name = "COOPERATION_TEXT_3", length = 65535)
	public String getCooperationText3() {
		return this.cooperationText3;
	}

	public void setCooperationText3(String cooperationText3) {
		this.cooperationText3 = cooperationText3;
	}

	@Column(name = "COOPERATION_SUMMARY_IMG")
	public String getCooperationSummaryImg() {
		return this.cooperationSummaryImg;
	}

	public void setCooperationSummaryImg(String cooperationSummaryImg) {
		this.cooperationSummaryImg = cooperationSummaryImg;
	}

}