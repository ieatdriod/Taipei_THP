package tw.com.mitac.thp.login2;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class LoginInterceptor extends AbstractInterceptor {
	protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		Map<String, Object> session = ai.getInvocationContext().getSession();
		UserData2 userData = (UserData2) session.get("userData2");

		String method = "";
		String memberLoginTempPage = "";
		if (ServletActionContext.getRequest() != null) {
			method = ServletActionContext.getRequest().getMethod();
			memberLoginTempPage = ServletActionContext.getRequest().getRequestURL().toString();
			if (StringUtils.isNotBlank(ServletActionContext.getRequest().getQueryString()))
				memberLoginTempPage += "?" + ServletActionContext.getRequest().getQueryString();
		}

		// logger.debug("getAuthType : " +
		// ServletActionContext.getRequest().getAuthType());
		// logger.debug("getCharacterEncoding : " +
		// ServletActionContext.getRequest().getCharacterEncoding());
		// logger.debug("getContentType : " +
		// ServletActionContext.getRequest().getContentType());
		// logger.debug("getLocalAddr : " +
		// ServletActionContext.getRequest().getLocalAddr());
		// logger.debug("getLocalName : " +
		// ServletActionContext.getRequest().getLocalName());
		// logger.debug("getLocalPort : " +
		// ServletActionContext.getRequest().getLocalPort());
		// logger.debug("getMethod : " +
		// ServletActionContext.getRequest().getMethod());//
		// logger.debug("getPathInfo : " +
		// ServletActionContext.getRequest().getPathInfo());
		// logger.debug("getPathTranslated : " +
		// ServletActionContext.getRequest().getPathTranslated());
		// logger.debug("getProtocol : " +
		// ServletActionContext.getRequest().getProtocol());
		// logger.debug("getQueryString : " +
		// ServletActionContext.getRequest().getQueryString());//
		// logger.debug("getRemoteAddr : " +
		// ServletActionContext.getRequest().getRemoteAddr());
		// logger.debug("getRemoteHost : " +
		// ServletActionContext.getRequest().getRemoteHost());
		// logger.debug("getRemotePort : " +
		// ServletActionContext.getRequest().getRemotePort());
		// logger.debug("getRemoteUser : " +
		// ServletActionContext.getRequest().getRemoteUser());
		// logger.debug("getRequestedSessionId : " +
		// ServletActionContext.getRequest().getRequestedSessionId());

		// logger.debug("getRequest =>");
		// for (java.util.Enumeration enum1 =
		// ServletActionContext.getRequest().getHeaderNames();
		// enum1.hasMoreElements();) {
		// String headerName = (String) enum1.nextElement();
		// logger.debug("Header Name = " + headerName);
		// logger.debug("Header value= " +
		// ServletActionContext.getRequest().getHeader(headerName));
		// }

		// logger.debug("getResponse =>");
		// for (String headerName :
		// ServletActionContext.getResponse().getHeaderNames()) {
		// logger.debug("Header Name = " + headerName);
		// logger.debug("Header value= " +
		// ServletActionContext.getResponse().getHeader(headerName));
		// }

		// 如果 TIMEOUT則導回登入頁面
		if (userData == null) {
			if ("POST".equalsIgnoreCase(method)) {
				session.remove("memberLoginTempPage");
			} else {
				// 記錄原網址
				session.put("memberLoginTempPage", memberLoginTempPage);
			}
			logger.info("end MEMBER_LOGIN_PAGE");
			return "MEMBER_LOGIN_PAGE";
		}

		return ai.invoke();
	}
}