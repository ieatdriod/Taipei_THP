package tw.com.mitac.thp.bean;
// Generated 2016/11/9 �U�� 03:16:41 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * MtsVendorProfile generated by hbm2java
 */
@Entity
@Table(name = "mts_vendor_profile", catalog = "thp", uniqueConstraints = @UniqueConstraint(columnNames = "VENDOR_SYSID"))
public class MtsVendorProfile implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String vendorSysid;
	private String vendorName;
	private String vendorVideoUrl1;
	private String vendorVideoUrl2;

	private String vendorProfileFull;
	private String vendorProfileSummary;
	private String vendorLiveShow;
	private String vendorImageSummary;
	private java.util.Set<MtsAdsC> detailSet;

	public java.util.Set<MtsAdsC> getDetailSet() {
		return detailSet;
	}

	public void setDetailSet(java.util.Set<MtsAdsC> detailSet) {
		this.detailSet = detailSet;
	}
	public MtsVendorProfile() {
	}

	public MtsVendorProfile(String sysid, String creator, String creationDate, String operator, String operationDate,
			String vendorSysid) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.vendorSysid = vendorSysid;

	}

	public MtsVendorProfile(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String vendorSysid, String vendorName, String vendorVideoUrl1, String vendorVideoUrl2,
			 String vendorProfileFull, String vendorProfileSummary, String vendorLiveShow,
			String vendorImageSummary) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.vendorSysid = vendorSysid;
		this.vendorName = vendorName;
		this.vendorVideoUrl1 = vendorVideoUrl1;
		this.vendorVideoUrl2 = vendorVideoUrl2;

		this.vendorProfileFull = vendorProfileFull;
		this.vendorProfileSummary = vendorProfileSummary;
		this.vendorLiveShow = vendorLiveShow;
		this.vendorImageSummary = vendorImageSummary;
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

	@Column(name = "VENDOR_SYSID", unique = true, nullable = false, length = 45)
	public String getVendorSysid() {
		return this.vendorSysid;
	}

	public void setVendorSysid(String vendorSysid) {
		this.vendorSysid = vendorSysid;
	}

	@Column(name = "VENDOR_NAME")
	public String getVendorName() {
		return this.vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	@Column(name = "VENDOR_VIDEO_URL_1")
	public String getVendorVideoUrl1() {
		return this.vendorVideoUrl1;
	}

	public void setVendorVideoUrl1(String vendorVideoUrl1) {
		this.vendorVideoUrl1 = vendorVideoUrl1;
	}

	@Column(name = "VENDOR_VIDEO_URL_2")
	public String getVendorVideoUrl2() {
		return this.vendorVideoUrl2;
	}

	public void setVendorVideoUrl2(String vendorVideoUrl2) {
		this.vendorVideoUrl2 = vendorVideoUrl2;
	}



	@Column(name = "VENDOR_PROFILE_FULL", length = 65535)
	public String getVendorProfileFull() {
		return this.vendorProfileFull;
	}

	public void setVendorProfileFull(String vendorProfileFull) {
		this.vendorProfileFull = vendorProfileFull;
	}

	@Column(name = "VENDOR_PROFILE_SUMMARY")
	public String getVendorProfileSummary() {
		return this.vendorProfileSummary;
	}

	public void setVendorProfileSummary(String vendorProfileSummary) {
		this.vendorProfileSummary = vendorProfileSummary;
	}

	@Column(name = "VENDOR_LIVE_SHOW", length = 60)
	public String getVendorLiveShow() {
		return this.vendorLiveShow;
	}

	public void setVendorLiveShow(String vendorLiveShow) {
		this.vendorLiveShow = vendorLiveShow;
	}

	@Column(name = "VENDOR_IMAGE_SUMMARY")
	public String getVendorImageSummary() {
		return this.vendorImageSummary;
	}

	public void setVendorImageSummary(String vendorImageSummary) {
		this.vendorImageSummary = vendorImageSummary;
	}

}
