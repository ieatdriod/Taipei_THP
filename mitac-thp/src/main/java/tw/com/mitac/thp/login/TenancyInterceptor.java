package tw.com.mitac.thp.login;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class TenancyInterceptor extends AbstractInterceptor {
	protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
	protected static final boolean isMultiTenancy = new Boolean(Util.globalSetting().getString("app.isMultiTenancy"));
	protected SessionFactory sessionFactory;

	protected Object tenancyData(Map<String, Object> session, String tenancyId) {
		logger.info("tenancyId:" + tenancyId);
		MtMultiTenancy tenancy = null;
		Object[] arr1 = TenancySessionFactoryUtil.queryTenancy(tenancyId);
		if (arr1[0] != null) {
			tenancy = (MtMultiTenancy) arr1[0];
		} else {
			logger.error(arr1[1]);
			return arr1[1];
		}
		SessionFactory tenancySessionFactory = TenancySessionFactoryUtil.createTenancySessionFactory(tenancy);

		TenancyData tenancyData = new TenancyData(tenancy, tenancySessionFactory);
		session.put("tenancyData", tenancyData);
		return tenancyData;
	}

	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		// logger.debug("start");
		Map<String, Object> session = ai.getInvocationContext().getSession();
		TenancyData tenancyData = (TenancyData) session.get("tenancyData");
		if (isMultiTenancy) {
			String tenancyId = "";
			String method = "";
			String tenancyTempPage = "";
			if (ServletActionContext.getRequest() != null) {
				tenancyId = ServletActionContext.getRequest().getParameter("tenancyId");
				method = ServletActionContext.getRequest().getMethod();
				tenancyTempPage = ServletActionContext.getRequest().getRequestURL().toString();
				if (StringUtils.isNotBlank(ServletActionContext.getRequest().getQueryString()))
					tenancyTempPage += "?" + ServletActionContext.getRequest().getQueryString();
			}
			logger.info("TENANCY:" + tenancyId);

			// if ((ai.getAction() instanceof
			// tw.com.mitac.thp.action.BasisTenancyAction))
			{
				// 尚未註記承租戶
				if (tenancyData == null) {
					if (StringUtils.isBlank(tenancyId)) {
						if ("POST".equalsIgnoreCase(method)) {
							session.remove("tenancyTempPage");
						} else {
							// 記錄原網址
							session.put("tenancyTempPage", tenancyTempPage);
						}
						// 導向承租戶註記頁面
						logger.info("end TENANCY_REGISTER");
						return "TENANCY_REGISTER";
					} else {
						Object result = tenancyData(session, tenancyId);
						if (result != null && result instanceof TenancyData) {
							tenancyData = (TenancyData) result;
						} else {
							session.put("tenancyError", result);
							// 導向承租戶註記頁面
							logger.info("end TENANCY_REGISTER");
							return "TENANCY_REGISTER";
						}
					}
				}
				// 已經註記承租戶
				else {
					// 收到 新 承租戶參數
					if (StringUtils.isNotBlank(tenancyId) && !tenancyId.equals(tenancyData.getTenancy().getTenancyId())) {
						logger.warn("SESSION_CLEAR");
						session.clear();
						Object result = tenancyData(session, tenancyId);
						if (result != null && result instanceof TenancyData) {
							tenancyData = (TenancyData) result;
						} else {
							session.put("tenancyError", result);
							// 導向承租戶註記頁面
							logger.info("end TENANCY_REGISTER");
							return "TENANCY_REGISTER";
						}
					}
				}
			}
		} else {
			if (tenancyData == null) {
				logger.info("TENANCY:DEFAULT");
				MtMultiTenancy tenancy = Util.defaultTenancy;
				SessionFactory tenancySessionFactory = sessionFactory;
				tenancyData = new TenancyData(tenancy, tenancySessionFactory);
				session.put("tenancyData", tenancyData);
			}
		}
		// logger.debug("end");
		
		long t1 = System.currentTimeMillis();
		String result = ai.invoke();
		long t2 = System.currentTimeMillis();
		long cost = t2 - t1;
		if (cost > 2000) {
			System.err.println("url:" + ServletActionContext.getRequest().getRequestURL().toString());
			System.err.println("cost:" + (t2 - t1));
		}
		return result;
	}

	@Autowired(required = false)
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}