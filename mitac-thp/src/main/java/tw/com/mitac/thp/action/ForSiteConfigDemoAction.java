package tw.com.mitac.thp.action;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.opensymphony.xwork2.ActionContext;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.bean.CpsConfig;
import tw.com.mitac.thp.bean.CpsVendor;

public class ForSiteConfigDemoAction extends BasisTenancyAction {

	 protected CpsConfig bean;
	
	 public CpsConfig getBean() {
	 return bean;
	 }
	
	 public void setBean(CpsConfig bean) {
	 this.bean = bean;
	 }
	
	public String demo() {
//		String privacyPolicy = request.getParameter("privacyPolicy");
//		String termsService = request.getParameter("termsService");

		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}

		return showValue(bean.getPrivacyPolicy(), bean.getTermsService(),bean.getAboutUs());
	}

	public String outerItemSingle() {
		CpsConfig cpsConfig = null;

		List<CpsConfig> config = cloudDao.queryTable(sf(), CpsConfig.class, new QueryGroup(), new QueryOrder[0], null,
				null);
		
		addMultiLan(config, sf(), CpsConfig.class);
		if (config.size() > 0) {
			cpsConfig = config.get(0);
		}
		showValue(cpsConfig.getPrivacyPolicy(), cpsConfig.getTermsService(),cpsConfig.getAboutUs());

		return SUCCESS;

	}

	protected String showValue(String privacy, String terms,String aboutUs) {

		request.setAttribute("privacy", privacy);
		request.setAttribute("terms", terms);
		request.setAttribute("aboutUs", aboutUs);

		return SUCCESS;
	}

}