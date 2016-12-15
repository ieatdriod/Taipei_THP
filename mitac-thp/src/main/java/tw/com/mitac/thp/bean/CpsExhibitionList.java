package tw.com.mitac.thp.bean;
// Generated 2016/6/4 �U�� 02:19:09 by Hibernate Tools 4.3.1.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CpsExhibitionList generated by hbm2java
 */
@Entity
@Table(name = "cps_exhibition_list", catalog = "thp")
public class CpsExhibitionList implements java.io.Serializable {

	private String sysid;
	private String creationDate;
	private String creator;
	private String operator;
	private String operationDate;
	private String remark;
	private Boolean isEnabled;
	private Integer dataOrder;
	private String exhibitionName;
	private Integer exhibitionCost;
	private String exhibitionSummary;
	private Date exhibitionStartDate;
	private Date exhibitionEndDate;
	private String exhibitionInfo;
	private String exhibitionBanner;
	private String exhibitionSummaryImg;
	private String exhibitionExplanation;

	public CpsExhibitionList() {
	}

	public CpsExhibitionList(String sysid, String creationDate, String creator, String operator, String operationDate,
			Integer dataOrder, String exhibitionName) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.dataOrder = dataOrder;
		this.exhibitionName = exhibitionName;
	}

	public CpsExhibitionList(String sysid, String creationDate, String creator, String operator, String operationDate,
			String remark, Boolean isEnabled, Integer dataOrder, String exhibitionName, Integer exhibitionCost,
			String exhibitionSummary, Date exhibitionStartDate, Date exhibitionEndDate, String exhibitionInfo,
			String exhibitionBanner, String exhibitionSummaryImg, String exhibitionExplanation) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.isEnabled = isEnabled;
		this.dataOrder = dataOrder;
		this.exhibitionName = exhibitionName;
		this.exhibitionCost = exhibitionCost;
		this.exhibitionSummary = exhibitionSummary;
		this.exhibitionStartDate = exhibitionStartDate;
		this.exhibitionEndDate = exhibitionEndDate;
		this.exhibitionInfo = exhibitionInfo;
		this.exhibitionBanner = exhibitionBanner;
		this.exhibitionSummaryImg = exhibitionSummaryImg;
		this.exhibitionExplanation = exhibitionExplanation;
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

	@Column(name = "EXHIBITION_NAME", nullable = false)
	public String getExhibitionName() {
		return this.exhibitionName;
	}

	public void setExhibitionName(String exhibitionName) {
		this.exhibitionName = exhibitionName;
	}

	@Column(name = "EXHIBITION_COST")
	public Integer getExhibitionCost() {
		return this.exhibitionCost;
	}

	public void setExhibitionCost(Integer exhibitionCost) {
		this.exhibitionCost = exhibitionCost;
	}

	@Column(name = "EXHIBITION_SUMMARY")
	public String getExhibitionSummary() {
		return this.exhibitionSummary;
	}

	public void setExhibitionSummary(String exhibitionSummary) {
		this.exhibitionSummary = exhibitionSummary;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "EXHIBITION_START_DATE", length = 10)
	public Date getExhibitionStartDate() {
		return this.exhibitionStartDate;
	}

	public void setExhibitionStartDate(Date exhibitionStartDate) {
		this.exhibitionStartDate = exhibitionStartDate;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "EXHIBITION_END_DATE", length = 10)
	public Date getExhibitionEndDate() {
		return this.exhibitionEndDate;
	}

	public void setExhibitionEndDate(Date exhibitionEndDate) {
		this.exhibitionEndDate = exhibitionEndDate;
	}

	@Column(name = "EXHIBITION_INFO")
	public String getExhibitionInfo() {
		return this.exhibitionInfo;
	}

	public void setExhibitionInfo(String exhibitionInfo) {
		this.exhibitionInfo = exhibitionInfo;
	}

	@Column(name = "EXHIBITION_BANNER")
	public String getExhibitionBanner() {
		return this.exhibitionBanner;
	}

	public void setExhibitionBanner(String exhibitionBanner) {
		this.exhibitionBanner = exhibitionBanner;
	}

	@Column(name = "EXHIBITION_SUMMARY_IMG")
	public String getExhibitionSummaryImg() {
		return this.exhibitionSummaryImg;
	}

	public void setExhibitionSummaryImg(String exhibitionSummaryImg) {
		this.exhibitionSummaryImg = exhibitionSummaryImg;
	}

	@Column(name = "EXHIBITION_EXPLANATION", length = 65535)
	public String getExhibitionExplanation() {
		return this.exhibitionExplanation;
	}

	public void setExhibitionExplanation(String exhibitionExplanation) {
		this.exhibitionExplanation = exhibitionExplanation;
	}

}
