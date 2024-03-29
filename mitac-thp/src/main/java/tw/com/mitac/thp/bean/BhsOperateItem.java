package tw.com.mitac.thp.bean;
// Generated 2016/6/14 �W�� 11:04:02 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BhsOperateItem generated by hbm2java
 */
@Entity
@Table(name = "bhs_operate_item", catalog = "thp")
public class BhsOperateItem implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String parentSysid;
	private String operateIntroduction;

	public BhsOperateItem() {
	}

	public BhsOperateItem(String sysid, String creator, String creationDate, String operator, String operationDate) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
	}

	public BhsOperateItem(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String parentSysid, String operateIntroduction) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.parentSysid = parentSysid;
		this.operateIntroduction = operateIntroduction;
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

	@Column(name = "OPERATE_SYSID", length = 45)
	public String getParentSysid() {
		return this.parentSysid;
	}

	public void setParentSysid(String parentSysid) {
		this.parentSysid = parentSysid;
	}

	@Column(name = "OPERATE_INTRODUCTION", length = 25)
	public String getOperateIntroduction() {
		return this.operateIntroduction;
	}

	public void setOperateIntroduction(String operateIntroduction) {
		this.operateIntroduction = operateIntroduction;
	}

}
