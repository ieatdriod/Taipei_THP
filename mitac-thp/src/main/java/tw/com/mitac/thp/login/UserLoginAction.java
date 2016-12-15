package tw.com.mitac.thp.login;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.miaa.login.MiaaUserData;
import tw.com.mitac.thp.action.BasisTenancyAction;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.util.Util;

/**
 * 登入系統
 */
public class UserLoginAction extends BasisTenancyAction {
	protected static final int VERIFYCODE_MODE_NONE = 0;
	protected static final int VERIFYCODE_MODE_DYNA = 1;
	protected static final int VERIFYCODE_MODE_RECAPTCHA = 2;

	protected String uid;
	protected String password;

	public String userLoginPage() {
		request.setAttribute("loginError", session.remove("loginError"));
		return SUCCESS;
	}

	/**
	 * 
	 * @param isSkipPassword
	 * @param verifycodeMode
	 *            1:DYNA_NUMBER 2:recaptcha
	 * @return
	 */
	public String userLogin(boolean isSkipPassword, int verifycodeMode) {
		session.put("userLoginFrom", request.getHeader("referer"));

		UserData userData = (UserData) session.get("userData");
		if (Util.isLogin) {
			if (userData == null) {
				if (StringUtils.isBlank(uid)) {
					session.put("loginError", getText("loginError.withoutAccount"));
					return NOTLOGIN();
				}
			} else {
				if (StringUtils.isNotBlank(uid) && !uid.equals(userData.getUid())) {
					userLogout();
				} else {
					return SUCCESS();
				}
			}

			switch (verifycodeMode) {
			case VERIFYCODE_MODE_DYNA:
				String verifycode = request.getParameter("verifycode");
				String dyna = (String) session.get(DYNA_NUMBER); // 圖形驗證碼

				if (StringUtils.isBlank(verifycode)) {
					session.put("loginError", getText("loginError.withoutVerifycode"));
					return NOTLOGIN();
				}
				if (!verifycode.equals(dyna)) {
					session.put("loginError", getText("loginError.verifycodeError"));
					return NOTLOGIN();
				}
				break;
			case VERIFYCODE_MODE_RECAPTCHA:
				String recaptcha = request.getParameter("g-recaptcha-response");
				List<String> msgList = recaptcha(recaptcha);
				if (msgList.size() > 0) {
					StringBuilder msg = new StringBuilder();
					for (String error_code : msgList) {
						msg.append(error_code);
					}
					session.put("loginError", msg.toString());
					return NOTLOGIN();
				}
				break;
			}

			logger.info("USER:" + uid);
			// 取得帳號
			if (StringUtils.isBlank(uid) || (!isSkipPassword && StringUtils.isBlank(password))) {
				addActionError("Invalid userid or password.");
			} else {
				List<CpsMember> l = cloudDao.queryTable(sf(), CpsMember.class, new QueryGroup(
						new QueryRule("uuid", uid)), new QueryOrder[0], null, null);
				if (l.size() == 0) {
					addActionError(getText("loginError.accountNotFound"));
				} else {
					CpsMember account = l.get(0);

					if (!isSkipPassword && account.getPassword() == null)
						addActionError(getText("loginError.pwdNotFound"));
					else if (!isSkipPassword && !account.getPassword().equals(Util.encode(password)))
						addActionError(getText("loginError.pwdError"));
					// else if
					// (!ThcConstants.ACCOUNT_STATUS_APPROVE.equals(account.getStatus()))
					// addActionError(getText("loginError.accountDisabled"));
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
						loginError += "[" + field + "]" + string + "\n";
					}
				}

				session.put("loginError", loginError);
				return NOTLOGIN();
			}

			userData = new UserData(uid);
			session.put("userData", userData);

			MiaaUserData miaaUserData = new MiaaUserData(uid, sf());
			session.put("miaaUserData", miaaUserData);
		} else {
			if (userData == null) {
				logger.info("USER:DEFAULT");
				userData = new UserData("default");
				session.put("userData", userData);

				MiaaUserData miaaUserData = new MiaaUserData("default", sf());
				session.put("miaaUserData", miaaUserData);
			}
		}
		return SUCCESS();
	}

	protected String SUCCESS() {
		redirectPage = (String) session.remove("loginTempPage");
		logger.debug("redirectPage:" + redirectPage);
		if (StringUtils.isBlank(redirectPage))
			redirectPage = "/userWelcome";
		return REDIRECT_PAGE;
	}

	protected String NOTLOGIN() {
		redirectPage = (String) session.get("userLoginFrom");//
		// redirectPage = request.getHeader("referer");
		logger.debug("referer:" + redirectPage);
		if (StringUtils.isBlank(redirectPage))
			return "USER_LOGIN_PAGE";
		return REDIRECT_PAGE;
	}

	public String userLogin() {
		return userLogin(false, VERIFYCODE_MODE_NONE);
	}

	public String userLogout() {
		String result = NOTLOGIN();

		Object tenancyData = session.remove("tenancyData");
		Object userData2 = session.remove("userData2");
		logger.warn("SESSION_CLEAR");
		session.clear();
		if (tenancyData != null) {
			logger.info("logout keep tenancyData");
			session.put("tenancyData", tenancyData);
		}
		if (userData2 != null) {
			logger.info("logout keep userData2");
			session.put("userData2", userData2);//
		}

		return result;
	}

	public String userLoginWithVerifycode() {
		return userLogin(false, VERIFYCODE_MODE_RECAPTCHA);
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
}