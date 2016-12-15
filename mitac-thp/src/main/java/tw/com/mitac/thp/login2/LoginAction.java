package tw.com.mitac.thp.login2;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.action.BasisTenancyAction;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.Util;

/**
 * 登入系統
 */
public class LoginAction extends BasisTenancyAction {
	protected static final int VERIFYCODE_MODE_NONE = 0;
	protected static final int VERIFYCODE_MODE_DYNA = 1;
	protected static final int VERIFYCODE_MODE_RECAPTCHA = 2;

	protected static final String NOTLOGIN = "MEMBER_LOGIN_PAGE";

	protected String uid;
	protected String password;
	protected String verifycode;

	public String memberLoginPage() {
		request.setAttribute("memberLoginError", session.remove("memberLoginError"));
		return SUCCESS;
	}

	/**
	 * 
	 * @param isSkipPassword
	 * @param verifycodeMode
	 *            1:DYNA_NUMBER 2:recaptcha
	 * @return
	 */
	protected String login(boolean isSkipPassword, int verifycodeMode) {
		session.remove("memberLoginError");

		switch (verifycodeMode) {
		case VERIFYCODE_MODE_DYNA:
			String dyna = (String) session.get(DYNA_NUMBER); // 圖形驗證碼
			if (StringUtils.isNotBlank(dyna)) {
				if (StringUtils.isBlank(verifycode)) {
					session.put("memberLoginError", getText("loginError.withoutVerifycode"));
					return NOTLOGIN;
				}
				if (!verifycode.equals("abcd1234"))// 壓測用
					if (!verifycode.equals(dyna)) {
						session.put("memberLoginError", getText("loginError.verifycodeError"));
						return NOTLOGIN;
					}
			}
			break;
		case VERIFYCODE_MODE_RECAPTCHA:
			List<String> msgList = recaptcha(verifycode);
			if (msgList.size() > 0) {
				StringBuilder msg = new StringBuilder();
				for (String error_code : msgList) {
					msg.append(error_code);
				}
				session.put("memberLoginError", msg.toString());
				return NOTLOGIN;
			}
			break;
		}

		UserData2 userData = null;

		CpsSiteMember account = null;
		String groupType = null;

		logger.info("uid=" + uid);
		// 取得帳號
		if (StringUtils.isBlank(uid) || (!isSkipPassword && StringUtils.isBlank(password))) {
			addActionError("Invalid userid or password.");
		} else {
			List<CpsSiteMember> l = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(new QueryRule("uuid",
					uid)), new QueryOrder[0], null, null);
			if (l.size() == 0) {
				addFieldError("uid", getText("loginError.accountNotFound"));
			} else {
				account = l.get(0);

				if (!isSkipPassword && account.getPassword() == null)
					addFieldError("uid", getText("loginError.pwdNotFound"));
				else if (!isSkipPassword && !account.getPassword().equals(Util.encode(password)))
					addFieldError("uid", getText("loginError.pwdError"));
				else if (!account.getIsActivate())
					addActionError("");//尚未認證通過，請至信箱查詢，或使用補發認證信
			}
		}
		if (hasErrors()) {
			String loginError = "";
			for (String string : getActionErrors()) {
				logger.info("login error:" + string);
				loginError += string + "\n";
			}
			for (String field : getFieldErrors().keySet()) {
				logger.info("login error field:" + field);
				for (String string : getFieldErrors().get(field)) {
					logger.info("ERR:" + string);
					loginError +=
					// "[" + field + "]" +
					string + "\n";
				}
			}

			session.put("memberLoginError", loginError);
			return NOTLOGIN;
		}

		userData = new UserData2(uid, account);
		session.put("userData2", userData);
		return SUCCESS();
	}

	/**
	 * set redirectPage
	 * 
	 * @return SUCCESS
	 */
	protected String SUCCESS() {
		redirectPage = (String) session.remove("memberLoginTempPage");
		// logger.debug("try to login from cps2/memberLoginPage:" +
		// redirectPage);

		// for (java.util.Enumeration enum1 = request.getHeaderNames();
		// enum1.hasMoreElements();) {
		// String headerName = (String) enum1.nextElement();
		// logger.debug("Header Name = " + headerName);
		// logger.debug("Header value= " + request.getHeader(headerName));
		// }
		// logger.debug("getLocalAddr = " + request.getLocalAddr());
		// logger.debug("getLocalName = " + request.getLocalName());
		// logger.debug("getLocalPort = " + request.getLocalPort());
		// logger.debug("getRemoteAddr = " + request.getRemoteAddr());
		// logger.debug("getRemoteHost = " + request.getRemoteHost());
		// logger.debug("getRemotePort = " + request.getRemotePort());
		// logger.debug("getRemoteUser = " + request.getRemoteUser());
		if (StringUtils.isBlank(redirectPage)) {
			redirectPage = request.getHeader("referer");
			// logger.debug("try to login from the same page:" + redirectPage);

			if (StringUtils.isBlank(redirectPage))
				redirectPage = "/";
		}

		return REDIRECT_PAGE;
	}

	public String login() {
		return login(false, VERIFYCODE_MODE_DYNA);
	}

	public String ajaxLogin() {
		login(false, VERIFYCODE_MODE_RECAPTCHA);
		resultString = SUCCESS;
		String memberLoginError = (String) session.remove("memberLoginError");
		if (StringUtils.isNotBlank(memberLoginError))
			resultString = memberLoginError;
		return JSON_RESULT;
	}

	public String logout() {
		Object tenancyData = session.remove("tenancyData");
		Object userData = session.remove("userData");
		logger.warn("SESSION_CLEAR");
		session.clear();
		if (tenancyData != null) {
			logger.info("logout keep tenancyData");
			session.put("tenancyData", tenancyData);
		}
		if (userData != null) {
			logger.info("logout keep userData");
			session.put("userData", userData);//
		}

		redirectPage = request.getHeader("referer");
		return REDIRECT_PAGE;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public final String getVerifycode() {
		return verifycode;
	}

	public final void setVerifycode(String verifycode) {
		this.verifycode = verifycode;
	}

	public String siteMemberActivate() {
		String sysid = request.getParameter("q");
		List<Map> l = (List<Map>) cloudDao.findProperty(sf(), CpsSiteMember.class, new QueryGroup(new QueryRule(PK,
				sysid)), null, false, "uuid", "isActivate");
		if (l.size() > 0) {
			String uuid = (String) l.get(0).get("uuid");
			boolean isActivate = (boolean) l.get(0).get("isActivate");
			if (isActivate) {
				addActionError("帳號不需重複開通");
			} else {
				Map<String, Object> setMap = getUpdatePropertyMap();
				setMap.put("isActivate", true);
				String daoMsg = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
						new QueryGroup(new QueryRule(PK, sysid)), setMap));
				if (!SUCCESS.equals(daoMsg)) {
					addActionError(daoMsg);
				} else {
					this.uid = uuid;
					this.verifycode = (String) session.get(DYNA_NUMBER);
					return login(true, VERIFYCODE_MODE_DYNA);
				}
			}
		} else {
			addActionError("開通失敗");
		}
		return SUCCESS;
	}
}