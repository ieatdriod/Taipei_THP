package tw.com.mitac.miaa.bean;

// Generated 2015/4/17 下午 04:30:09 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Miaa01Tag generated by hbm2java
 * <p>
 * MIAA01_TAG 移除 IS_LEAF 欄位
 * </p>
 * <ol>
 * <li>在呈現權限時需重新運算(不可預期使用者會得到完整的tree)</li>
 * <li>在呈現權限時File才是真正的子節點</li>
 * </ol>
 */
@Entity
@Table(name = "miaa01_tag")
public class Miaa01Tag implements java.io.Serializable {

	private String sysid;
	private String tagName;
	private String parentTagSysid;
	// private Boolean isLeaf;
	private Long treeLevel;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private Boolean isEnable;

	@Id
	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	@Column(name = "TAG_NAME", nullable = false)
	public String getTagName() {
		return this.tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Column(name = "PARENT_TAG_SYSID", nullable = false, length = 45)
	public String getParentTagSysid() {
		return this.parentTagSysid;
	}

	public void setParentTagSysid(String parentTagSysid) {
		this.parentTagSysid = parentTagSysid;
	}

	// @Column(name = "IS_LEAF", nullable = false)
	// public Boolean getIsLeaf() {
	// return this.isLeaf;
	// }
	//
	// public void setIsLeaf(Boolean isLeaf) {
	// this.isLeaf = isLeaf;
	// }

	@Column(name = "TREE_LEVEL", nullable = false, precision = 13, scale = 0)
	public Long getTreeLevel() {
		return this.treeLevel;
	}

	public void setTreeLevel(Long treeLevel) {
		this.treeLevel = treeLevel;
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

	@Column(name = "IS_ENABLE", nullable = false)
	public Boolean getIsEnable() {
		return this.isEnable;
	}

	public void setIsEnable(Boolean isEnable) {
		this.isEnable = isEnable;
	}

}
