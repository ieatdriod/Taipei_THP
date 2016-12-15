package tw.com.mitac.thp.bean;

// Generated 2016/5/31 下午 01:32:13 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BhsRecommand generated by hbm2java
 */
@Entity
@Table(name = "bhs_recommand", catalog = "thp")
public class BhsRecommand implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String recommandTitle;
	private String recommandType;
	private Integer dataOrder;
	private Boolean isEnabled;
	private java.util.Set<BhsRecommandItem> detailSet;
	private java.util.Set<BhsRecommandLinkItem> detailSet2;

	public java.util.Set<BhsRecommandItem> getDetailSet() {
		return detailSet;
	}

	public void setDetailSet(java.util.Set<BhsRecommandItem> detailSet) {
		this.detailSet = detailSet;
	}
	
	public java.util.Set<BhsRecommandLinkItem> getDetailSet2() {
		return detailSet2;
	}

	public void setDetailSet2(java.util.Set<BhsRecommandLinkItem> detailSet2) {
		this.detailSet2 = detailSet2;
	}

	public BhsRecommand() {
	}

	public BhsRecommand(String sysid, String creator, String creationDate, String operator, String operationDate,
			String recommandTitle, String recommandType, Integer dataOrder, Boolean isEnabled) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.recommandTitle = recommandTitle;
		this.recommandType = recommandType;
		this.dataOrder = dataOrder;
		this.isEnabled = isEnabled;
	}

	public BhsRecommand(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String recommandTitle, String recommandType, Integer dataOrder, Boolean isEnabled) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.recommandTitle = recommandTitle;
		this.recommandType = recommandType;
		this.dataOrder = dataOrder;
		this.isEnabled = isEnabled;
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

	@Column(name = "RECOMMAND_TITLE", nullable = false)
	public String getRecommandTitle() {
		return this.recommandTitle;
	}

	public void setRecommandTitle(String recommandTitle) {
		this.recommandTitle = recommandTitle;
	}

	@Column(name = "RECOMMAND_TYPE", nullable = false, length = 1)
	public String getRecommandType() {
		return this.recommandType;
	}

	public void setRecommandType(String recommandType) {
		this.recommandType = recommandType;
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

}