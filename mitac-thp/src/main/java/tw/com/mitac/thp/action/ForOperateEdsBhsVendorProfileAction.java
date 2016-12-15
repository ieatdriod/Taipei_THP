package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.ActionContext;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMarquee;
import tw.com.mitac.thp.bean.BhsOperate;
import tw.com.mitac.thp.bean.BhsOperateItem;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsVendorProfile;

/** BHS_FW_003_標竿企業介紹 */
public class ForOperateEdsBhsVendorProfileAction extends FrontBhsVendorProfileAction_BHS_FW_003 {

	protected BhsOperate bean;

	public BhsOperate getBean() {
		return bean;
	}

	public void setBean(BhsOperate bean) {
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

		return bhsOperateDemo(bean, false);
	}


}