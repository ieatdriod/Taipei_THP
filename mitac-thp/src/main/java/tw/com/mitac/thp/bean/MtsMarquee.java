
package tw.com.mitac.thp.bean;
// Generated 2016/5/20 �U�� 03:45:54 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MtsMarquee generated by hbm2java
 */
@Entity
@Table(name = "mts_marquee", catalog = "thp")
public class MtsMarquee implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private Integer dataOrder;
	private String marqueeText;
	private Boolean isEnabled;
	private String marqueeUrl;
	private String sourceId;

	public MtsMarquee() {
	}

	public MtsMarquee(String sysid, Integer dataOrder, String marqueeText, Boolean isEnabled, String sourceId) {
		this.sysid = sysid;
		this.dataOrder = dataOrder;
		this.marqueeText = marqueeText;
		this.isEnabled = isEnabled;
		this.sourceId = sourceId;
	}

	public MtsMarquee(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, Integer dataOrder, String marqueeText, Boolean isEnabled, String marqueeUrl, String sourceId) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.dataOrder = dataOrder;
		this.marqueeText = marqueeText;
		this.isEnabled = isEnabled;
		this.marqueeUrl = marqueeUrl;
		this.sourceId = sourceId;
	}

	@Id
	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	@Column(name = "CREATOR")
	public String getCreator() {
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Column(name = "CREATION_DATE")
	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "OPERATOR")
	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Column(name = "OPERATION_DATE")
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

	@Column(name = "DATA_ORDER", nullable = false)
	public Integer getDataOrder() {
		return this.dataOrder;
	}

	public void setDataOrder(Integer dataOrder) {
		this.dataOrder = dataOrder;
	}

	@Column(name = "MARQUEE_TEXT", nullable = false)
	public String getMarqueeText() {
		return this.marqueeText;
	}

	public void setMarqueeText(String marqueeText) {
		this.marqueeText = marqueeText;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "MARQUEE_URL")
	public String getMarqueeUrl() {
		return this.marqueeUrl;
	}

	public void setMarqueeUrl(String marqueeUrl) {
		this.marqueeUrl = marqueeUrl;
	}

	@Column(name = "SOURCE_ID", nullable = false)
	public String getSourceId() {
		return this.sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

}

