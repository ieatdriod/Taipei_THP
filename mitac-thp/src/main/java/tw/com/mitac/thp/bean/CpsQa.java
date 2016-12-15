package tw.com.mitac.thp.bean;
// Generated 2016/7/6 �U�� 05:14:20 by Hibernate Tools 4.3.1.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CpsQa generated by hbm2java
 */
@Entity
@Table(name = "cps_qa", catalog = "thp")
public class CpsQa implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String qaDepartment;
	private String qaType;
	private String qaTitle;
	private String qaText;
	private String createName;
	private String email;
	private String memberSysid;
	private Date qaRedate;
	private String backMemberSysid;
	private String qaRetext;
	private String remark;

	public CpsQa() {
	}

	public CpsQa(String sysid, String creator, String creationDate, String operator, String operationDate,
			String qaDepartment, String qaTitle, String qaText, String createName, String email, Date qaRedate) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.qaDepartment = qaDepartment;
		this.qaTitle = qaTitle;
		this.qaText = qaText;
		this.createName = createName;
		this.email = email;
		this.qaRedate = qaRedate;
	}

	public CpsQa(String sysid, String creator, String creationDate, String operator, String operationDate,
			String qaDepartment, String qaType, String qaTitle, String qaText, String createName, String email,
			String memberSysid, Date qaRedate, String backMemberSysid, String qaRetext, String remark) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.qaDepartment = qaDepartment;
		this.qaType = qaType;
		this.qaTitle = qaTitle;
		this.qaText = qaText;
		this.createName = createName;
		this.email = email;
		this.memberSysid = memberSysid;
		this.qaRedate = qaRedate;
		this.backMemberSysid = backMemberSysid;
		this.qaRetext = qaRetext;
		this.remark = remark;
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

	@Column(name = "QA_DEPARTMENT", nullable = false, length = 45)
	public String getQaDepartment() {
		return this.qaDepartment;
	}

	public void setQaDepartment(String qaDepartment) {
		this.qaDepartment = qaDepartment;
	}

	@Column(name = "QA_TYPE", length = 1)
	public String getQaType() {
		return this.qaType;
	}

	public void setQaType(String qaType) {
		this.qaType = qaType;
	}

	@Column(name = "QA_TITLE", nullable = false)
	public String getQaTitle() {
		return this.qaTitle;
	}

	public void setQaTitle(String qaTitle) {
		this.qaTitle = qaTitle;
	}

	@Column(name = "QA_TEXT", nullable = false, length = 65535)
	public String getQaText() {
		return this.qaText;
	}

	public void setQaText(String qaText) {
		this.qaText = qaText;
	}

	@Column(name = "CREATE_NAME", nullable = false)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "EMAIL", nullable = false)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "MEMBER_SYSID", length = 45)
	public String getMemberSysid() {
		return this.memberSysid;
	}

	public void setMemberSysid(String memberSysid) {
		this.memberSysid = memberSysid;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "QA_REDATE", length = 19)
	public Date getQaRedate() {
		return this.qaRedate;
	}

	public void setQaRedate(Date qaRedate) {
		this.qaRedate = qaRedate;
	}

	@Column(name = "BACK_MEMBER_SYSID", length = 45)
	public String getBackMemberSysid() {
		return this.backMemberSysid;
	}

	public void setBackMemberSysid(String backMemberSysid) {
		this.backMemberSysid = backMemberSysid;
	}

	@Column(name = "QA_RETEXT", length = 65535)
	public String getQaRetext() {
		return this.qaRetext;
	}

	public void setQaRetext(String qaRetext) {
		this.qaRetext = qaRetext;
	}

	@Column(name = "REMARK", length = 65535)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
