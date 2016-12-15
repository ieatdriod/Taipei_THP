package tw.com.mitac.thp.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.HqlStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.util.Util;

public class ModifyPasswordAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "initModifyPwd";
	}

	private static final Class<?> coreClass = CpsMember.class;
	private static final String MODIFY_PASSWORD = "modifyPassword";

	private String uid;
	private String accountPassword;
	private String accountPasswordNew;
	private String accountPasswordRe;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public final String getAccountPassword() {
		return accountPassword;
	}

	public final void setAccountPassword(String accountPassword) {
		this.accountPassword = accountPassword;
	}

	public final String getAccountPasswordNew() {
		return accountPasswordNew;
	}

	public final void setAccountPasswordNew(String accountPasswordNew) {
		this.accountPasswordNew = accountPasswordNew;
	}

	public final String getAccountPasswordRe() {
		return accountPasswordRe;
	}

	public final void setAccountPasswordRe(String accountPasswordRe) {
		this.accountPasswordRe = accountPasswordRe;
	}

	public String initModifyPwd() {
		UserData userData = (UserData) session.get("userData");
		uid = userData.getUid();
		return MODIFY_PASSWORD;
	}

	public String execute() throws Exception {
		logger.info("uid:" + uid);
		UserData userData = (UserData) session.get("userData");
		if (!userData.getUid().equals(uid)) {
			uid = userData.getUid();
			addActionError("請再次嘗試");
		}

		if (StringUtils.isBlank(accountPassword)) {
			addActionError("未輸入舊密碼");
		}
		if (StringUtils.isBlank(accountPasswordNew) || StringUtils.isBlank(accountPasswordRe)) {
			addActionError("未輸入新密碼");
		} else if (!StringUtils.equals(accountPasswordNew, accountPasswordRe)) {
			addActionError("新密碼與確認密碼不同");
		}

		if (hasActionErrors()) {
			return MODIFY_PASSWORD;
		}

		List l = cloudDao.queryTable(sf(), coreClass, new QueryGroup(new QueryRule("uuid", userData.getUid())),
				new QueryOrder[0], null, null);
		if (l.size() == 0) {
			addActionError(getText("loginError.accountNotFound"));
		} else {
			Object account = l.get(0);
			String pwd = (String) PropertyUtils.getProperty(account, "password");

			if (pwd == null)
				addActionError(getText("loginError.pwdNotFound"));
			else if (!pwd.equals(Util.encode(accountPassword)))
				addActionError(getText("loginError.pwdError"));
			// else if
			// (!ThcConstants.ACCOUNT_STATUS_APPROVE.equals(account.getStatus()))
			// addActionError(getText("loginError.accountDisabled"));
		}

		Map<String, Object> setMap = new HashMap<String, Object>();
		setMap.put("password", Util.encode(accountPasswordNew));
		HqlStatement s = new UpdateStatement(coreClass.getSimpleName(), new QueryGroup(new QueryRule("uuid",
				userData.getUid())), setMap);
		String daoMsg = cloudDao.save(sf(), s);
		if (!SUCCESS.equals(daoMsg)) {
			logger.error(daoMsg);
			addActionError(daoMsg);
			return MODIFY_PASSWORD;
		}

		addActionMessage("更新密碼成功，建議重新登入使用");
		return MODIFY_PASSWORD;
	}
}