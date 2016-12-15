package tw.com.mitac.thp.bean;

// Generated 2016/5/31 下午 01:32:13 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BhsRecommandItem generated by hbm2java
 */
@Entity
@Table(name = "bhs_recommand_item", catalog = "thp")
public class BhsRecommandItem implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String parentSysid;
	private Integer dataOrder;
	private String sourceSysid;

	public BhsRecommandItem() {
	}

	public BhsRecommandItem(String sysid, String creator, String creationDate, String operator, String operationDate,
			Integer dataOrder) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.dataOrder = dataOrder;
	}

	public BhsRecommandItem(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String parentSysid, Integer dataOrder, String sourceSysid) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.parentSysid = parentSysid;
		this.dataOrder = dataOrder;
		this.sourceSysid = sourceSysid;
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

	@Column(name = "RECOMMAND_SYSID", length = 45)
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

	@Column(name = "SOURCE_SYSID", length = 45)
	public String getSourceSysid() {
		return this.sourceSysid;
	}

	public void setSourceSysid(String sourceSysid) {
		this.sourceSysid = sourceSysid;
	}

}
