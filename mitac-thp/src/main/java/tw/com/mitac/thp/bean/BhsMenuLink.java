package tw.com.mitac.thp.bean;

// Generated 2016/6/2 上午 10:53:00 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BhsMenuLink generated by hbm2java
 */
@Entity
@Table(name = "bhs_menu_link", catalog = "thp")
public class BhsMenuLink implements java.io.Serializable {

	private String sysid;
	private String creationDate;
	private String creator;
	private String operator;
	private String operationDate;
	private String remark;
	private String menuSysid;
	private String parentSysid;

	public BhsMenuLink() {
	}

	public BhsMenuLink(String sysid, String creationDate, String creator, String operator, String operationDate,
			String parentSysid) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.parentSysid = parentSysid;
	}

	public BhsMenuLink(String sysid, String creationDate, String creator, String operator, String operationDate,
			String remark, String menuSysid, String parentSysid) {
		this.sysid = sysid;
		this.creationDate = creationDate;
		this.creator = creator;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.menuSysid = menuSysid;
		this.parentSysid = parentSysid;
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

	@Column(name = "MENU_SYSID", length = 45)
	public String getMenuSysid() {
		return this.menuSysid;
	}

	public void setMenuSysid(String menuSysid) {
		this.menuSysid = menuSysid;
	}

	@Column(name = "SOURCE_SYSID", nullable = false, length = 45)
	public String getParentSysid() {
		return this.parentSysid;
	}

	public void setParentSysid(String parentSysid) {
		this.parentSysid = parentSysid;
	}

}
