package tw.com.mitac.thp.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.HpsEntityProfile;
import tw.com.mitac.thp.bean.HpsPromoteBonus;
import tw.com.mitac.thp.bean.HpsPromoteBonusHistory;
import tw.com.mitac.thp.bean.HpsPromoteBonusMember;
import tw.com.mitac.thp.login2.LoginAction;
import tw.com.mitac.thp.util.ConstantUtil;
import tw.com.mitac.thp.util.Util;

public class FrontMemberRegisterAction extends LoginAction {
	protected CpsSiteMember bean;
	protected String countrySelect;

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

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\w+\\.*\\w+@(\\w+\\.){1,5}[a-zA-Z]{2,3}$");

	/**
	 * Email 格式檢查程式
	 * 
	 * @since 2006/07/19
	 **/
	private static boolean isValidEmail(String email) {
		boolean result = false;
		if (EMAIL_PATTERN.matcher(email).matches()) {
			result = true;
		}
		return result;
	}

	@Override
	public void validate() {
		super.validate();
		if (bean != null) {
			if (StringUtils.isNotBlank(bean.getEmail())) {
				if (!isValidEmail(bean.getEmail()))
					addActionError("email格式不符");
			}
		}
	}

	/**
	 * <pre>
	 * 會員註冊頁面
	 * </pre>
	 */
	public String frontRegisterMemberEdit() {
		request.setAttribute("memberLoginError", session.remove("memberLoginError"));

		String referer = request.getHeader("referer");
		logger.debug("referer:" + referer);
		if (StringUtils.isNotBlank(referer) && !referer.contains("CPS_FW_002"))
			session.put("registerTempPage", referer);

		return SUCCESS;
	}

	@Override
	public String execute() {
		Object[] arr = fnGenerateNewMember();
		resultObject = arr;
		boolean isSuccess = (boolean) arr[0];
		if (isSuccess) {
			this.uid = bean.getUuid();
			login(true, 0);
		}
		return JSON_RESULT;
	}

	/**
	 * 會員註冊
	 * 
	 * @return
	 */
	public String newMember() {
		Object[] arr = fnGenerateNewMember();
		boolean isSuccess = (boolean) arr[0];
		if (!isSuccess) {
			for (Object object : arr)
				if (object instanceof String)
					addActionError((String) object);
			return ERROR;
		}
		StringBuilder msg = new StringBuilder();
		for (Object object : arr)
			if (object instanceof String)
				msg.append(object).append("\n");

		session.put("memberLoginError", msg.toString());// 若回傳至cps2/register

		redirectPage = (String) session.remove("memberLoginTempPage");
		if (StringUtils.isBlank(redirectPage)) {
			redirectPage = (String) session.remove("registerTempPage");
			logger.debug("redirectPage:" + redirectPage);
			if (StringUtils.isBlank(redirectPage))
				redirectPage = "/";
		}

		return REDIRECT_PAGE;
	}

	/**
	 * 會員註冊
	 * 
	 * @return Object[]{Boolean isSuccess,String msg...}
	 */
	public Object[] fnGenerateNewMember() {
		List<String> msgList = recaptcha(request.getParameter("g-recaptcha-response"));
		if (msgList.size() > 0) {
			StringBuilder msg = new StringBuilder();
			for (String error_code : msgList) {
				msg.append(error_code);
			}
			return new Object[] { false, msg.toString() };
		}

		// if(bean==null)bean = new CpsSiteMember();
		Util.defaultPK(bean);
		bean.setPassword(Util.encode(bean.getPassword()));
		bean.setIsEnabled(true);
		bean.setRegisterDate(systemDate);

		if (StringUtils.isBlank(bean.getGender()))
			bean.setGender("NONE");

		String birthdayStr = bean.getBirthYear() + "/" + bean.getBirthMonth() + "/" + bean.getBirthDate();
		try {
			Date birthday = sdfYMD.parse(birthdayStr);
			bean.setBirthday(birthday);
		} catch (ParseException e) {
			// e.printStackTrace();
			// addActionError("出生年月日格式輸入錯誤，請以數字方式輸入");
			// return ERROR;
		}

		if (StringUtils.isBlank(bean.getEmail()))
			bean.setEmail(bean.getUuid());

		String memberName = bean.getMemberName();
		if (StringUtils.isBlank(memberName)) {
			memberName = "";
			String firstName = StringUtils.trimToEmpty(bean.getFirstName());
			String lastName = StringUtils.trimToEmpty(bean.getLastName());
			memberName += firstName;
			if (StringUtils.isNotBlank(memberName) && StringUtils.isNotBlank(lastName))
				memberName += " ";
			memberName += lastName;
			bean.setMemberName(memberName);
		}

		if (StringUtils.isNotBlank(countrySelect)) {
			String[] arr = countrySelect.split("#");
			bean.setCountrySysid(arr[0]);
		}

		String mtsEpaper = request.getParameter("mtsEpaper");
		String bhsEpaper = request.getParameter("bhsEpaper");
		String hpsEpaper = request.getParameter("hpsEpaper");
		if (StringUtils.isNotBlank(mtsEpaper))
			bean.setIsMtsEpaper(true);
		else
			bean.setIsMtsEpaper(false);
		if (StringUtils.isNotBlank(bhsEpaper))
			bean.setIsBhsEpaper(true);
		else
			bean.setIsBhsEpaper(false);
		if (StringUtils.isNotBlank(hpsEpaper))
			bean.setIsHpsEpaper(true);
		else
			bean.setIsHpsEpaper(false);

		if (StringUtils.isNotBlank(bean.getOauthType()) && StringUtils.isNotBlank(bean.getOauthId()))
			bean.setIsActivate(true);
		else
			bean.setIsActivate(false);

		String saveMsg = saveRegister();
		if (!SUCCESS.equals(saveMsg))
			return new Object[] { false, saveMsg };

		if (bean.getIsActivate()) {
			return new Object[] { true, getText("msg.registerSuccess") };
		} else {
			// 寄認證信
			String activateLink = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
					+ "/pages2/siteMemberActivate?q=" + bean.getSysid();

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add("歡迎加入生策會平台<br>");
			contentStringList.add("請點選以下連結進行開通<br>");
			contentStringList.add("<a href='" + activateLink + "'>開通連結</a>");

			new MailThread(new MailBean(bean.getEmail(), "帳號開通認證信", contentStringList), getSendMailSetting()).start();

			return new Object[] { true, "Your registration has been sent ! Please check mail to complete member certification." };//認證信已寄出，請至信箱收信
		}
	}

	protected String saveRegister() {
		try {
			List saveList = new ArrayList();
			defaultValue(bean);
			saveList.add(bean);

			// 紅利贈送
			List<HpsEntityProfile> hpsEntityProfileList = cloudDao.queryTable(sf(), HpsEntityProfile.class,
					QueryGroup.DEFAULT, new QueryOrder[0], null, null);
			for (HpsEntityProfile hpsEntityProfile : hpsEntityProfileList) {
				HpsPromoteBonus hpsPromoteBonus = cloudDao.get(sf(), HpsPromoteBonus.class,
						hpsEntityProfile.getBonusSysid());
				if (hpsPromoteBonus.getIsEnabled()) {
					HpsPromoteBonusMember hpsPromoteBonusMember = new HpsPromoteBonusMember();
					Util.defaultPK(hpsPromoteBonusMember);
					defaultValue(hpsPromoteBonusMember);
					hpsPromoteBonusMember.setBonusSysid(hpsEntityProfile.getBonusSysid());
					hpsPromoteBonusMember.setBonusTitle(hpsPromoteBonus.getBonusTitle());
					if (hpsPromoteBonus.getIsTimeLimit()) {
						hpsPromoteBonusMember.setBonusDeadlineDate(hpsPromoteBonus.getBonusDeadlineDate());
					} else {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, 9999);
						cal.set(Calendar.MONTH, Calendar.DECEMBER);
						cal.set(Calendar.DAY_OF_MONTH, 31);
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						hpsPromoteBonusMember.setBonusDeadlineDate(cal.getTime());
					}
					hpsPromoteBonusMember.setBonus(hpsEntityProfile.getBonusPoint());
					hpsPromoteBonusMember.setPointToAmount(hpsPromoteBonus.getPointToAmount());
					hpsPromoteBonusMember.setMemberSysid(bean.getSysid());
					saveList.add(hpsPromoteBonusMember);

					HpsPromoteBonusHistory history = new HpsPromoteBonusHistory();
					Util.defaultPK(history);
					defaultValue(history);
					history.setBonusSysid(hpsEntityProfile.getBonusSysid());
					history.setBonusTitle(hpsPromoteBonus.getBonusTitle());
					history.setVendorSysid(hpsPromoteBonus.getVendorSysid());
					history.setMemberSysid(bean.getSysid());
					history.setIssueDatetime(systemDatetime);
					history.setBonusPoint(hpsEntityProfile.getBonusPoint());
					history.setBonusStatus(ConstantUtil.BONUS_STATUS_EFFECT);
					history.setSourceNo("");
					saveList.add(history);

					// TODO 產生折價卷
				}
			}

			String daoMsg = "";
			if (StringUtils.isBlank(daoMsg)) {
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			}

			if (!daoMsg.equals(SUCCESS)) {
				return daoMsg;
			}
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
		return SUCCESS;
	}

	/**
	 * 測試識別代碼是否重複
	 * 
	 * @return
	 */
	public String tryid() {
		// String msg = "";
		boolean b = false;
		try {
			List<QueryRule> ruleList = new ArrayList<QueryRule>();
			Object id = request.getParameter("bean.uuid");
			ruleList.add(new QueryRule("uuid", id));
			logger.debug("id:" + id);

			int count = cloudDao.queryTableCount(sf(), CpsSiteMember.class,
					new QueryGroup(ruleList.toArray(new QueryRule[0])));
			if (count > 0) {
				b = true;
			} else {
				b = false;
			}
			// JSONObject jsonObject = new JSONObject(msg);
			// resultString = jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return renderText(String.valueOf(!b));
	}
}