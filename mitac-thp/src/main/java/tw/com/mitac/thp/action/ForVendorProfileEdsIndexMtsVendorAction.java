package tw.com.mitac.thp.action;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Query;
import org.hibernate.Session;

import com.opensymphony.xwork2.ActionContext;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsDoctor;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsMarquee;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

@SuppressWarnings({ "unchecked", "rawtypes" })
/** MTS_FW_002_醫療團隊介紹 */
public class ForVendorProfileEdsIndexMtsVendorAction extends IndexMtsVendorAction {
	protected MtsVendorProfile bean;

	public MtsVendorProfile getBean() {
		return bean;
	}

	public void setBean(MtsVendorProfile bean) {
		this.bean = bean;
	}

	public String demo() {
		logger.debug("bean:" + ReflectionToStringBuilder.toString(bean, ToStringStyle.MULTI_LINE_STYLE));
		
		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}

		return execute(bean.getVendorSysid(), bean, false);
	}


}