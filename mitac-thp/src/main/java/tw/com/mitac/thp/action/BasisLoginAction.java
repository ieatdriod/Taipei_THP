package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.miaa.bean.Miaa02File;
import tw.com.mitac.miaa.dao.MiaaDAO;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.login.UserData;
import tw.com.mitac.thp.util.Util;

public abstract class BasisLoginAction extends BasisTenancyAction {
	private static final long serialVersionUID = 1L;

	public String getCookieLanKey() {
		return "language";
	}

	protected final UserData getUserData() {
		return (UserData) session.get("userData");
	}

	protected final String getUserID() {
		return getUserData().getUid();
	}

	public final CpsMember getUserAccount() {
		CpsMember userAccount = (CpsMember) getUserData().getData().get("userAccount");
		if (userAccount == null) {
			List<CpsMember> userAccountList = cloudDao.queryTable(sf(), CpsMember.class, new QueryGroup(new QueryRule(
					"uuid", getUserData().getUid())), new QueryOrder[0], 0, 1);
			userAccount = userAccountList.get(0);

			getUserData().getData().put("userAccount", userAccount);
		}
		return userAccount;
	}

	public String getPageBigTitle() {
		String pageBigTitle = getText("web.title");
		if (StringUtils.equals(CpsVendor.class.getSimpleName(), getUserAccount().getSourceType()))
			pageBigTitle = getDataCpsVendorTable().get(getUserAccount().getSourceSysid()).getName();
		else if (StringUtils.equals(CpsEntity.class.getSimpleName(), getUserAccount().getSourceType()))
			pageBigTitle = getDataCpsEntityETable().get(getUserAccount().getSourceSysid()).getName();
		return pageBigTitle;
	}

	@Override
	public boolean getIsMenuExpandAll() {
		if (StringUtils.equals(CpsVendor.class.getSimpleName(), getUserAccount().getSourceType()))
			return true;
		return false;
	}

	protected MiaaDAO miaaDao;

	@Autowired
	public final void setMiaaDao(MiaaDAO miaaDao) {
		this.miaaDao = miaaDao;
	}

	// ---------- ---------- ---------- ---------- ----------
	@Override
	protected final String createOperatorValue() {
		UserData userData = getUserData();
		return userData.getUid() // + "-" + userData.getAccount().getName()
		;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected abstract String getMiaaInitUrl();

	public final String getMiaaFileSysid() {
		String miaaFileSysid = (String) sessionGet("miaaFileSysid");
		if (StringUtils.isBlank(miaaFileSysid)) {
			miaaFileSysid = "x";
			String initUrl = getMiaaInitUrl();
			List<String> sysidList = (List<String>) cloudDao.findProperty(sf(), Miaa02File.class, new QueryGroup(
					new QueryRule("initUrl", initUrl)), new QueryOrder[0], false, "sysid");
			if (sysidList.size() > 0)
				miaaFileSysid = sysidList.get(0);
			sessionSet("miaaFileSysid", miaaFileSysid);
		}
		return miaaFileSysid;
	}

	@Override
	public void validate() {
		// 優先執行 避免到jsp才執行，取到錯值
		getActionType();

		if (getUserData() == null) {
			logger.info("SKIP - TOMEOUT");
		} else {
			if (Util.isLogin) {
				String fileSysid = getMiaaFileSysid();
				logger.debug("fileSysid:[" + fileSysid + "]");

				boolean enterEnable = false;
				if (StringUtils.isNotBlank(fileSysid)) {
					if (StringUtils.equals(fileSysid, "x")) {
						enterEnable = true;
						logger.debug("always go");
					} else {
						if (miaaDao.fileSysidList(sf(), getUserData().getUid()).contains(fileSysid)) {
							enterEnable = true;
							List<String> filePropertyIdList = miaaDao.filePropertyIdList(sf(), getUserData().getUid(),
									fileSysid);
							for (String filePropertyId : filePropertyIdList)
								request.setAttribute(filePropertyId, true);
						}
					}
				}
				if (!enterEnable)
					addActionError(getText("errMsg.unabledEnter"));
			}
			// else {
			// // TODO ??
			// }
		}
		super.validate();
	}
}