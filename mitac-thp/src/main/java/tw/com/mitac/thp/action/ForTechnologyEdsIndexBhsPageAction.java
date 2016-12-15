package tw.com.mitac.thp.action;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.ActionContext;

import tw.com.mitac.thp.bean.BhsTechnology;

public class ForTechnologyEdsIndexBhsPageAction extends FrontBhsProductsAction {

	protected BhsTechnology bean;

	public BhsTechnology getBean() {
		return bean;
	}

	public void setBean(BhsTechnology bean) {
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

		return indexBhsTechnologyPage(bean, false);
	}
}