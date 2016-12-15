package tw.com.mitac.thp.login;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;

import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;
import tw.com.mitac.thp.action.BasisAction;
import tw.com.mitac.thp.util.Util;

public class TenancyRegisterAction extends BasisAction {
	protected String tenancyId;

	public String tenancyRegister() {
		request.setAttribute("tenancyError", session.remove("tenancyError"));
		return SUCCESS;
	}

	public String tenancyRegisterSubmit() {
		TenancyData tenancyData = (TenancyData) session.get("tenancyData");
		if (getIsMultiTenancy()) {
			if (tenancyData == null) {
				if (StringUtils.isBlank(tenancyId)) {
					session.put("tenancyError", "請輸入承租戶資訊");
					return "TENANCY_REGISTER";
				}
			} else {
				if (StringUtils.isNotBlank(tenancyId) && !tenancyId.equals(tenancyData.getTenancy().getTenancyId())) {
					logger.warn("SESSION_CLEAR");
					session.clear();
				} else {
					return SUCCESS();
				}
			}

			logger.info("TENANCY:" + tenancyId);
			MtMultiTenancy tenancy = null;
			Object[] arr1 = TenancySessionFactoryUtil.queryTenancy(tenancyId);
			if (arr1[0] != null) {
				tenancy = (MtMultiTenancy) arr1[0];
			} else {
				logger.error(arr1[1]);
				session.put("tenancyError", arr1[1]);
				return "TENANCY_REGISTER";
			}
			SessionFactory tenancySessionFactory = TenancySessionFactoryUtil.createTenancySessionFactory(tenancy);

			tenancyData = new TenancyData(tenancy, tenancySessionFactory);
			session.put("tenancyData", tenancyData);
		} else {
			if (tenancyData == null) {
				logger.info("TENANCY:DEFAULT");
				MtMultiTenancy tenancy = Util.defaultTenancy;
				SessionFactory tenancySessionFactory = sessionFactory;
				tenancyData = new TenancyData(tenancy, tenancySessionFactory);
				session.put("tenancyData", tenancyData);
			}
		}
		return SUCCESS();
	}

	protected String SUCCESS() {
		redirectPage = (String) session.remove("tenancyTempPage");
		logger.debug("redirectPage:" + redirectPage);
		if (StringUtils.isBlank(redirectPage))
			redirectPage = "/";
		return REDIRECT_PAGE;
	}

	public final String getTenancyId() {
		return tenancyId;
	}

	public final void setTenancyId(String tenancyId) {
		this.tenancyId = tenancyId;
	}
}