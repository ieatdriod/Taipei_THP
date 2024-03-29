package tw.com.mitac.thp.bean;
// Generated 2016/5/30 �U�� 02:00:57 by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CpsTopmarquee generated by hbm2java
 */
@Entity
@Table(name = "cps_topmarquee", catalog = "thp")
public class CpsTopmarquee implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String topmarqueeText;

	public CpsTopmarquee() {
	}

	public CpsTopmarquee(String sysid, String creator, String creationDate, String operator, String operationDate,
			String topmarqueeText) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.topmarqueeText = topmarqueeText;
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

	@Column(name = "TOPMARQUEE_TEXT", nullable = false)
	public String getTopmarqueeText() {
		return this.topmarqueeText;
	}

	public void setTopmarqueeText(String topmarqueeText) {
		this.topmarqueeText = topmarqueeText;
	}

}
