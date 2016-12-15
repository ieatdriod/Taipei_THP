package tw.com.mitac.thp.login;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.SessionFactory;

import tw.com.mitac.miaa.login.MiaaUserData;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class UserLoginInterceptor extends AbstractInterceptor {
	protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		Map<String, Object> session = ai.getInvocationContext().getSession();
		// Map<String, Object> application =
		// ai.getInvocationContext().getApplication();
		UserData userData = (UserData) session.get("userData");

		String method = "";
		String userLoginTempPage = "";
		if (ServletActionContext.getRequest() != null) {
			method = ServletActionContext.getRequest().getMethod();
			userLoginTempPage = ServletActionContext.getRequest().getRequestURL().toString();
			if (StringUtils.isNotBlank(ServletActionContext.getRequest().getQueryString()))
				userLoginTempPage += "?" + ServletActionContext.getRequest().getQueryString();
		}
		// logger.debug("userLoginTempPage:" + userLoginTempPage);

		// logger.debug("getMethod : " +
		// ServletActionContext.getRequest().getMethod());//

		if (Util.isLogin) {
			// if ((ai.getAction() instanceof
			// tw.com.mitac.thp.action.BasisLoginAction))
			{
				if (userData == null) {
					if ("POST".equalsIgnoreCase(method)) {
						session.remove("loginTempPage");
					} else {
						// 記錄原網址
						session.put("loginTempPage", userLoginTempPage);
					}
					// 如果 time out 則導回登入頁面
					logger.info("end USER_LOGIN_PAGE");
					return "USER_LOGIN_PAGE";
				}
			}
		} else {
			if (userData == null) {
				logger.info("USER:DEFAULT");
				userData = new UserData("default");
				session.put("userData", userData);

				MiaaUserData miaaUserData = new MiaaUserData("default", sf(ai));
				session.put("miaaUserData", miaaUserData);
			}
		}

		return ai.invoke();
	}

	protected SessionFactory sf(ActionInvocation ai) {
		return getTenancyData(ai).getTenancySessionFactory();
	}

	protected final TenancyData getTenancyData(ActionInvocation ai) {
		Map<String, Object> session = ai.getInvocationContext().getSession();
		return (TenancyData) session.get("tenancyData");
	}
}