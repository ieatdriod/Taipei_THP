package tw.com.mitac.thp.bean;
// Generated 2016/6/4 �W�� 09:33:24 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MtsAdsB generated by hbm2java
 */
@Entity
@Table(name = "mts_ads_b", catalog = "thp")
public class MtsAdsB implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String adsType;
	private Boolean isEnabled;
	private Integer dataOrder;
	private String adsUrl;
	private String forumSysid;
	private String adsImage;
	private Boolean adsTw;
	private Boolean adsCn;
	private Boolean adsUs;

	public MtsAdsB() {
	}

	public MtsAdsB(String sysid, String creator, String creationDate, String operator, String operationDate,
			String adsType, Integer dataOrder, String adsImage) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.adsType = adsType;
		this.dataOrder = dataOrder;
		this.adsImage = adsImage;
	}

	public MtsAdsB(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String adsType, Boolean isEnabled, Integer dataOrder, String adsUrl, String forumSysid,
			String adsImage, Boolean adsTw, Boolean adsCn, Boolean adsUs) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.adsType = adsType;
		this.isEnabled = isEnabled;
		this.dataOrder = dataOrder;
		this.adsUrl = adsUrl;
		this.forumSysid = forumSysid;
		this.adsImage = adsImage;
		this.adsTw = adsTw;
		this.adsCn = adsCn;
		this.adsUs = adsUs;
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

	@Column(name = "ADS_TYPE", nullable = false, length = 2)
	public String getAdsType() {
		return this.adsType;
	}

	public void setAdsType(String adsType) {
		this.adsType = adsType;
	}

	@Column(name = "IS_ENABLED")
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "DATA_ORDER", nullable = false)
	public Integer getDataOrder() {
		return this.dataOrder;
	}

	public void setDataOrder(Integer dataOrder) {
		this.dataOrder = dataOrder;
	}

	@Column(name = "ADS_URL")
	public String getAdsUrl() {
		return this.adsUrl;
	}

	public void setAdsUrl(String adsUrl) {
		this.adsUrl = adsUrl;
	}

	@Column(name = "FORUM_SYSID", length = 45)
	public String getForumSysid() {
		return this.forumSysid;
	}

	public void setForumSysid(String forumSysid) {
		this.forumSysid = forumSysid;
	}

	@Column(name = "ADS_IMAGE", nullable = false)
	public String getAdsImage() {
		return this.adsImage;
	}

	public void setAdsImage(String adsImage) {
		this.adsImage = adsImage;
	}

	@Column(name = "ADS_TW")
	public Boolean getAdsTw() {
		return this.adsTw;
	}

	public void setAdsTw(Boolean adsTw) {
		this.adsTw = adsTw;
	}

	@Column(name = "ADS_CN")
	public Boolean getAdsCn() {
		return this.adsCn;
	}

	public void setAdsCn(Boolean adsCn) {
		this.adsCn = adsCn;
	}

	@Column(name = "ADS_US")
	public Boolean getAdsUs() {
		return this.adsUs;
	}

	public void setAdsUs(Boolean adsUs) {
		this.adsUs = adsUs;
	}

}