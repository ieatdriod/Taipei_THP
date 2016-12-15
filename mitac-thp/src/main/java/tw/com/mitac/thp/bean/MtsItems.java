package tw.com.mitac.thp.bean;

// Generated 2016/1/29 下午 02:17:32 by Hibernate Tools 4.3.1

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MtsItems generated by hbm2java
 */
@Entity
@Table(name = "mts_items", catalog = "thp")
public class MtsItems implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String dataId;
	private String name;
	private String itemsType;
	private String vendorSysid;
	private BigDecimal itemsPrice;

	public MtsItems() {
	}

	public MtsItems(String sysid, String creator, String creationDate, String operator, String operationDate,
			String dataId, String name, String itemsType, String vendorSysid, BigDecimal itemsPrice) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.dataId = dataId;
		this.name = name;
		this.itemsType = itemsType;
		this.vendorSysid = vendorSysid;
		this.itemsPrice = itemsPrice;
	}

	public MtsItems(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String dataId, String name, String itemsType, String vendorSysid, BigDecimal itemsPrice) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.dataId = dataId;
		this.name = name;
		this.itemsType = itemsType;
		this.vendorSysid = vendorSysid;
		this.itemsPrice = itemsPrice;
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

	@Column(name = "ITEMS_ID", nullable = false, length = 30)
	public String getDataId() {
		return this.dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	@Column(name = "ITEMS_NAME", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "ITEMS_TYPE", nullable = false, length = 45)
	public String getItemsType() {
		return this.itemsType;
	}

	public void setItemsType(String itemsType) {
		this.itemsType = itemsType;
	}

	@Column(name = "VENDOR_SYSID", nullable = false, length = 45)
	public String getVendorSysid() {
		return this.vendorSysid;
	}

	public void setVendorSysid(String vendorSysid) {
		this.vendorSysid = vendorSysid;
	}

	@Column(name = "ITEMS_PRICE", nullable = false, precision = 21, scale = 8)
	public BigDecimal getItemsPrice() {
		return this.itemsPrice;
	}

	public void setItemsPrice(BigDecimal itemsPrice) {
		this.itemsPrice = itemsPrice;
	}

}
