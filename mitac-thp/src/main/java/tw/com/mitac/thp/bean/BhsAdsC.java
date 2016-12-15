package tw.com.mitac.thp.bean;
// Generated 2016/6/23 �W�� 11:00:25 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BhsAdsC generated by hbm2java
 */
@Entity
@Table(name = "bhs_ads_c", catalog = "thp")
public class BhsAdsC implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String parentSysid;
	private Integer dataOrder;
	private Boolean isEnabled;
	private String bannerType;
	private String adsImage;
	private String adsUrl;
	private Boolean adsTw;
	private Boolean adsCn;
	private Boolean adsUs;

	public BhsAdsC() {
	}

	public BhsAdsC(String sysid, String creator, String creationDate, String operator, String operationDate,
			Integer dataOrder, Boolean isEnabled, String bannerType, Boolean adsTw, Boolean adsCn, Boolean adsUs) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.dataOrder = dataOrder;
		this.isEnabled = isEnabled;
		this.bannerType = bannerType;
		this.adsTw = adsTw;
		this.adsCn = adsCn;
		this.adsUs = adsUs;
	}

	public BhsAdsC(String sysid, String creator, String creationDate, String operator, String operationDate,
			String parentSysid, Integer dataOrder, Boolean isEnabled, String bannerType, String adsImage, String adsUrl,
			Boolean adsTw, Boolean adsCn, Boolean adsUs) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.parentSysid = parentSysid;
		this.dataOrder = dataOrder;
		this.isEnabled = isEnabled;
		this.bannerType = bannerType;
		this.adsImage = adsImage;
		this.adsUrl = adsUrl;
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

	@Column(name = "SOURCE_SYSID", length = 45)
	public String getParentSysid() {
		return this.parentSysid;
	}

	public void setParentSysid(String parentSysid) {
		this.parentSysid = parentSysid;
	}

	@Column(name = "DATA_ORDER", nullable = false)
	public Integer getDataOrder() {
		return this.dataOrder;
	}

	public void setDataOrder(Integer dataOrder) {
		this.dataOrder = dataOrder;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "BANNER_TYPE", nullable = false)
	public String getBannerType() {
		return this.bannerType;
	}

	public void setBannerType(String bannerType) {
		this.bannerType = bannerType;
	}

	@Column(name = "ADS_IMAGE")
	public String getAdsImage() {
		return this.adsImage;
	}

	public void setAdsImage(String adsImage) {
		this.adsImage = adsImage;
	}

	@Column(name = "ADS_URL")
	public String getAdsUrl() {
		return this.adsUrl;
	}

	public void setAdsUrl(String adsUrl) {
		this.adsUrl = adsUrl;
	}

	@Column(name = "ADS_TW", nullable = false)
	public Boolean getAdsTw() {
		return this.adsTw;
	}

	public void setAdsTw(Boolean adsTw) {
		this.adsTw = adsTw;
	}

	@Column(name = "ADS_CN", nullable = false)
	public Boolean getAdsCn() {
		return this.adsCn;
	}

	public void setAdsCn(Boolean adsCn) {
		this.adsCn = adsCn;
	}

	@Column(name = "ADS_US", nullable = false)
	public Boolean getAdsUs() {
		return this.adsUs;
	}

	public void setAdsUs(Boolean adsUs) {
		this.adsUs = adsUs;
	}

}
