package tw.com.mitac.thp.action;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsSiteMember;

public class FrontHpsMemberAction extends BasisFrontLoginAction {
	protected CpsSiteMember bean;
	protected String countrySelect;
	protected String companyCountrySelect;

	public final CpsSiteMember getBean() {
		return bean;
	}

	public final void setBean(CpsSiteMember bean) {
		this.bean = bean;
	}

	public final String getCountrySelect() {
		return countrySelect;
	}

	public final void setCountrySelect(String countrySelect) {
		this.countrySelect = countrySelect;
	}

	public String getCompanyCountrySelect() {
		return companyCountrySelect;
	}

	public void setCompanyCountrySelect(String companyCountrySelect) {
		this.companyCountrySelect = companyCountrySelect;
	}

	public String initAccountInfo() {
		String sysid = getUserData2().getAccount().getSysid();
		bean = cloudDao.get(sf(), CpsSiteMember.class, sysid);
		if (StringUtils.isNotBlank(bean.getCountrySysid())) {
			CpsCountry a = getAllCountry().get(bean.getCountrySysid());
			if (a != null) {
				countrySelect = bean.getCountrySysid() + "#" + a.getIsForeign();
			}
		}

		if (StringUtils.isNotBlank(bean.getCompanyCountrySysid()) && StringUtils.isNotBlank(bean.getCountrySysid())) {
			companyCountrySelect = bean.getCompanyCountrySysid() + "#"
					+ getAllCountry().get(bean.getCountrySysid()).getIsForeign();
		} else {
			logger.debug("該用戶資料庫沒有國家資訊");
			List<CpsCountry> cpsCountryList = cloudDao.queryTable(sf(), CpsCountry.class, new QueryGroup(),
					new QueryOrder[0], null, 1);
			CpsCountry cct = cpsCountryList.get(0);
			companyCountrySelect = cct.getSysid() + "#" + getAllCountry().get(cct.getSysid()).getIsForeign();
		}

		return SUCCESS;
	}

	private String validpassword;

	public String getValidpassword() {
		return validpassword;
	}

	public void setValidpassword(String validpassword) {
		this.validpassword = validpassword;
	}

	public String ajaxSaveAccountInfo() {
		String sysid = getUserData2().getAccount().getSysid();
		if (!sysid.equals(bean.getSysid())) {
			addActionError("資訊異常，請重新整理");
			return ERROR;
		}
		// if (StringUtils.isBlank(validpassword)) {
		// addActionError("請輸入 確認密碼");
		// return ERROR;
		// }
		// if (!validpassword.equals(bean.getPassword())) {
		// addActionError("密碼與確認密碼 不相同");
		// return ERROR;
		// }

		Map<String, Object> setMap = getUpdatePropertyMap();

		int count = cloudDao.queryTableCount(sf(), CpsSiteMember.class, new QueryGroup(new QueryRule(PK, sysid),
				new QueryRule("firstName", bean.getFirstName()), new QueryRule("lastName", bean.getLastName())));
		if (count == 0) {
			// 沒有完全一致
			setMap.put("firstName", bean.getFirstName());
			setMap.put("lastName", bean.getLastName());
			String memberName = "";
			String firstName = StringUtils.trimToEmpty(bean.getFirstName());
			String lastName = StringUtils.trimToEmpty(bean.getLastName());
			memberName += firstName;
			if (StringUtils.isNotBlank(memberName) && StringUtils.isNotBlank(lastName))
				memberName += " ";
			memberName += lastName;
			setMap.put("memberName", memberName);
		}

		setMap.put("password", bean.getPassword());
		setMap.put("uuid", bean.getUuid());
		setMap.put("firstName", bean.getFirstName());
		setMap.put("lastName", bean.getLastName());
		setMap.put("memberCall", bean.getMemberCall());
		if (StringUtils.isNotBlank(countrySelect)) {
			String[] arr = countrySelect.split("#");
			setMap.put("countrySysid", arr[0]);
		}

		setMap.put("isMtsEpaper", bean.getIsMtsEpaper());
		setMap.put("isBhsEpaper", bean.getIsBhsEpaper());
		setMap.put("isHpsEpaper", bean.getIsHpsEpaper());
		setMap.put("memberCompany", bean.getMemberCompany());
		setMap.put("memberUnit", bean.getMemberUnit());
		setMap.put("memberTitle", bean.getMemberTitle());
		setMap.put("countryCode", bean.getCountryCode());
		setMap.put("phone", bean.getPhone());
		setMap.put("countryCode2", bean.getCountryCode2());
		setMap.put("phone2", bean.getPhone2());
		// setMap.put("mobilePhone", bean.getMobilePhone());

		if (StringUtils.isNotBlank(companyCountrySelect)) {
			String[] arr = companyCountrySelect.split("#");
			setMap.put("companyCountrySysid", arr[0]);
		}

		setMap.put("companyAreaCode", bean.getCompanyAreaCode());
		setMap.put("address", bean.getAddress());
		setMap.put("birthYear", bean.getBirthYear());
		setMap.put("birthMonth", bean.getBirthMonth());
		setMap.put("birthDate", bean.getBirthDate());
		String birthdayStr = bean.getBirthYear() + "/" + bean.getBirthMonth() + "/" + bean.getBirthDate();
		try {
			Date birthday = sdfYMD.parse(birthdayStr);
			setMap.put("birthday", birthday);
		} catch (ParseException e) {
			// e.printStackTrace();
			// addActionError("出生年月日格式輸入錯誤，請以數字方式輸入");
			// return ERROR;
		}
		setMap.put("gender", bean.getGender());
		setMap.put("memberWebsite", bean.getMemberWebsite());
		setMap.put("memberBus", bean.getMemberBus());
		logger.debug("setMap:" + setMap);

		resultString = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
				new QueryGroup(new QueryRule(PK, sysid)), setMap));

		return JSON_RESULT;
	}

	public Map<String, String> getGenderMenu() {
		Map<String, String> menu = getConstantMenu().get("gender");
		for (Iterator<String> it = menu.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			if ("MAN".equals(key))
				menu.put(key, "Male");
			else if ("WOMAN".equals(key))
				menu.put(key, "Female");
			else
				menu.put(key, "Non Specific");
		}
		return menu;
	}
}