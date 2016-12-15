package tw.com.mitac.thp.bean;
// Generated 2016/12/6 �W�� 10:53:23 by Hibernate Tools 4.3.1.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * CpsSiteMember generated by hbm2java
 */
@Entity
@Table(name = "cps_site_member", catalog = "thp", uniqueConstraints = @UniqueConstraint(columnNames = "UUID"))
public class CpsSiteMember implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String uuid;
	private String memberCall;
	private String memberName;
	private String firstName;
	private String lastName;
	private String password;
	private String email;
	private Boolean isEnabled;
	private String gender;
	private Date birthday;
	private String countryCode;
	private String phone;
	private String countryCode2;
	private String phone2;
	private String mobilePhone;
	private String countrySysid;
	private String companyCountrySysid;
	private String companyAreaCode;
	private String address;
	private String birthYear;
	private String birthMonth;
	private String birthDate;
	private Date registerDate;
	private Boolean isMtsEpaper;
	private Boolean isBhsEpaper;
	private Boolean isHpsEpaper;
	private String oauthType;
	private String oauthId;
	private String memberTitle;
	private String memberCompany;
	private String memberWebsite;
	private String memberBus;
	private String memberUnit;
	private Boolean isActivate;
	private String emailVerifyCode;
	private Date emailVerifyCodeTime;

	public CpsSiteMember() {
	}

	public CpsSiteMember(String sysid, String creator, String creationDate, String operator, String operationDate,
			String uuid, String memberName, String password, String email, Boolean isEnabled, String gender,
			Boolean isMtsEpaper, Boolean isBhsEpaper, Boolean isHpsEpaper, Boolean isActivate) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.uuid = uuid;
		this.memberName = memberName;
		this.password = password;
		this.email = email;
		this.isEnabled = isEnabled;
		this.gender = gender;
		this.isMtsEpaper = isMtsEpaper;
		this.isBhsEpaper = isBhsEpaper;
		this.isHpsEpaper = isHpsEpaper;
		this.isActivate = isActivate;
	}

	public CpsSiteMember(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String uuid, String memberCall, String memberName, String firstName, String lastName,
			String password, String email, Boolean isEnabled, String gender, Date birthday, String countryCode,
			String phone, String countryCode2, String phone2, String mobilePhone, String countrySysid,
			String companyCountrySysid, String companyAreaCode, String address, String birthYear, String birthMonth,
			String birthDate, Date registerDate, Boolean isMtsEpaper, Boolean isBhsEpaper, Boolean isHpsEpaper,
			String oauthType, String oauthId, String memberTitle, String memberCompany, String memberWebsite,
			String memberBus, String memberUnit, Boolean isActivate, String emailVerifyCode, Date emailVerifyCodeTime) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.uuid = uuid;
		this.memberCall = memberCall;
		this.memberName = memberName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
		this.isEnabled = isEnabled;
		this.gender = gender;
		this.birthday = birthday;
		this.countryCode = countryCode;
		this.phone = phone;
		this.countryCode2 = countryCode2;
		this.phone2 = phone2;
		this.mobilePhone = mobilePhone;
		this.countrySysid = countrySysid;
		this.companyCountrySysid = companyCountrySysid;
		this.companyAreaCode = companyAreaCode;
		this.address = address;
		this.birthYear = birthYear;
		this.birthMonth = birthMonth;
		this.birthDate = birthDate;
		this.registerDate = registerDate;
		this.isMtsEpaper = isMtsEpaper;
		this.isBhsEpaper = isBhsEpaper;
		this.isHpsEpaper = isHpsEpaper;
		this.oauthType = oauthType;
		this.oauthId = oauthId;
		this.memberTitle = memberTitle;
		this.memberCompany = memberCompany;
		this.memberWebsite = memberWebsite;
		this.memberBus = memberBus;
		this.memberUnit = memberUnit;
		this.isActivate = isActivate;
		this.emailVerifyCode = emailVerifyCode;
		this.emailVerifyCodeTime = emailVerifyCodeTime;
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

	@Column(name = "UUID", unique = true, nullable = false)
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(name = "MEMBER_CALL")
	public String getMemberCall() {
		return this.memberCall;
	}

	public void setMemberCall(String memberCall) {
		this.memberCall = memberCall;
	}

	@Column(name = "MEMBER_NAME", nullable = false)
	public String getMemberName() {
		return this.memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	@Column(name = "FIRST_NAME", length = 100)
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "LAST_NAME", length = 100)
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "PASSWORD", nullable = false)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "EMAIL", nullable = false)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "GENDER", nullable = false, length = 30)
	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BIRTHDAY", length = 10)
	public Date getBirthday() {
		return this.birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@Column(name = "COUNTRY_CODE", length = 10)
	public String getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Column(name = "PHONE")
	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "COUNTRY_CODE2", length = 10)
	public String getCountryCode2() {
		return this.countryCode2;
	}

	public void setCountryCode2(String countryCode2) {
		this.countryCode2 = countryCode2;
	}

	@Column(name = "PHONE2")
	public String getPhone2() {
		return this.phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	@Column(name = "MOBILE_PHONE")
	public String getMobilePhone() {
		return this.mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	@Column(name = "COUNTRY_SYSID", length = 45)
	public String getCountrySysid() {
		return this.countrySysid;
	}

	public void setCountrySysid(String countrySysid) {
		this.countrySysid = countrySysid;
	}

	@Column(name = "COMPANY_COUNTRY_SYSID", length = 45)
	public String getCompanyCountrySysid() {
		return this.companyCountrySysid;
	}

	public void setCompanyCountrySysid(String companyCountrySysid) {
		this.companyCountrySysid = companyCountrySysid;
	}

	@Column(name = "COMPANY_AREA_CODE", length = 45)
	public String getCompanyAreaCode() {
		return this.companyAreaCode;
	}

	public void setCompanyAreaCode(String companyAreaCode) {
		this.companyAreaCode = companyAreaCode;
	}

	@Column(name = "ADDRESS", length = 45)
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "BIRTH_YEAR", length = 10)
	public String getBirthYear() {
		return this.birthYear;
	}

	public void setBirthYear(String birthYear) {
		this.birthYear = birthYear;
	}

	@Column(name = "BIRTH_MONTH", length = 10)
	public String getBirthMonth() {
		return this.birthMonth;
	}

	public void setBirthMonth(String birthMonth) {
		this.birthMonth = birthMonth;
	}

	@Column(name = "BIRTH_DATE", length = 10)
	public String getBirthDate() {
		return this.birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "REGISTER_DATE", length = 10)
	public Date getRegisterDate() {
		return this.registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	@Column(name = "IS_MTS_EPAPER", nullable = false)
	public Boolean getIsMtsEpaper() {
		return this.isMtsEpaper;
	}

	public void setIsMtsEpaper(Boolean isMtsEpaper) {
		this.isMtsEpaper = isMtsEpaper;
	}

	@Column(name = "IS_BHS_EPAPER", nullable = false)
	public Boolean getIsBhsEpaper() {
		return this.isBhsEpaper;
	}

	public void setIsBhsEpaper(Boolean isBhsEpaper) {
		this.isBhsEpaper = isBhsEpaper;
	}

	@Column(name = "IS_HPS_EPAPER", nullable = false)
	public Boolean getIsHpsEpaper() {
		return this.isHpsEpaper;
	}

	public void setIsHpsEpaper(Boolean isHpsEpaper) {
		this.isHpsEpaper = isHpsEpaper;
	}

	@Column(name = "OAUTH_TYPE", length = 30)
	public String getOauthType() {
		return this.oauthType;
	}

	public void setOauthType(String oauthType) {
		this.oauthType = oauthType;
	}

	@Column(name = "OAUTH_ID")
	public String getOauthId() {
		return this.oauthId;
	}

	public void setOauthId(String oauthId) {
		this.oauthId = oauthId;
	}

	@Column(name = "MEMBER_TITLE")
	public String getMemberTitle() {
		return this.memberTitle;
	}

	public void setMemberTitle(String memberTitle) {
		this.memberTitle = memberTitle;
	}

	@Column(name = "MEMBER_COMPANY")
	public String getMemberCompany() {
		return this.memberCompany;
	}

	public void setMemberCompany(String memberCompany) {
		this.memberCompany = memberCompany;
	}

	@Column(name = "MEMBER_WEBSITE")
	public String getMemberWebsite() {
		return this.memberWebsite;
	}

	public void setMemberWebsite(String memberWebsite) {
		this.memberWebsite = memberWebsite;
	}

	@Column(name = "MEMBER_BUS")
	public String getMemberBus() {
		return this.memberBus;
	}

	public void setMemberBus(String memberBus) {
		this.memberBus = memberBus;
	}

	@Column(name = "MEMBER_UNIT")
	public String getMemberUnit() {
		return this.memberUnit;
	}

	public void setMemberUnit(String memberUnit) {
		this.memberUnit = memberUnit;
	}

	@Column(name = "IS_ACTIVATE", nullable = false)
	public Boolean getIsActivate() {
		return this.isActivate;
	}

	public void setIsActivate(Boolean isActivate) {
		this.isActivate = isActivate;
	}

	@Column(name = "EMAIL_VERIFY_CODE", length = 50)
	public String getEmailVerifyCode() {
		return this.emailVerifyCode;
	}

	public void setEmailVerifyCode(String emailVerifyCode) {
		this.emailVerifyCode = emailVerifyCode;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EMAIL_VERIFY_CODE_TIME", length = 19)
	public Date getEmailVerifyCodeTime() {
		return this.emailVerifyCodeTime;
	}

	public void setEmailVerifyCodeTime(Date emailVerifyCodeTime) {
		this.emailVerifyCodeTime = emailVerifyCodeTime;
	}

}