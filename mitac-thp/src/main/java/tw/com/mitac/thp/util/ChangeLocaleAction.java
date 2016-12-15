package tw.com.mitac.thp.util;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.action.BasisTenancyAction;

public class ChangeLocaleAction extends BasisTenancyAction {
	protected String lan;

	public final String getLan() {
		return lan;
	}

	public final void setLan(String lan) {
		this.lan = lan;
	}

	public String execute(String cookieLanKey) {
		if (languageTypeMap.containsKey(lan)) {
			// logger.debug("request.getLocale():" + request.getLocale());
			// logger.debug("response.getLocale():" + response.getLocale());

			// cookiesMap.put("tw.com.mitac.petInspect.language", lan);
			Cookie cookie = new Cookie(cookieLanKey, StringUtils.defaultString(lan));
			// 設置Cookie的生命周期
			cookie.setMaxAge(60 * 60 * 24 * 365);
			// cookie.setDomain(".petInspect.mitac.com.tw");
			response.addCookie(cookie);

			// String[] arr = lan.split("_");
			// response.setLocale(new Locale(arr[0], arr[1]));
		} else {
			logger.warn("lan:" + lan);
		}

		redirectPage = request.getHeader("referer");
		if (StringUtils.isBlank(redirectPage))
			redirectPage = "/";

		return REDIRECT_PAGE;
	}

	public String execute() {
		return execute("language");
	}

	public String changeSiteLocale() {
		return execute("siteLanguage");
	}
}