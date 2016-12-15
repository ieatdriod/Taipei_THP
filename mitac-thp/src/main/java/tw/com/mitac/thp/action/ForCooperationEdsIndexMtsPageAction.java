package tw.com.mitac.thp.action;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.ActionContext;

import tw.com.mitac.thp.bean.MtsCooperation;

public class ForCooperationEdsIndexMtsPageAction extends IndexMtsPageSpecialAction {

	protected MtsCooperation bean;

	public MtsCooperation getBean() {
		return bean;
	}

	public void setBean(MtsCooperation bean) {
		this.bean = bean;
	}

	public String demo() {

		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}

		return mtsCooperationPage(bean, "I", false);
	}
}