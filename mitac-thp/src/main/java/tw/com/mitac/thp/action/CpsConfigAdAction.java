package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;

// Generated Mon Mar 21 11:37:12 CST 2016 by GenCode.java

import tw.com.mitac.thp.bean.CpsConfigAd;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;

/**
 * CpsConfigAdAction generated by GenCode.java
 */
public class CpsConfigAdAction extends BasisCrudAction<CpsConfigAd> {
	
	protected String adsDisplaySourceFilter;
	
	public String getAdsDisplaySourceFilter() {
		return adsDisplaySourceFilter;
	}

	public void setAdsDisplaySourceFilter(String adsDisplaySourceFilter) {
		this.adsDisplaySourceFilter = adsDisplaySourceFilter;
	}

	@Override
	public String[] getImgCols() {
		return new String[] { "bannerImg" };
	}
	
	@Override
	public String main() {
		String result = super.main();
		if (getQueryCondition() == null) {
			beaninfo = new HashMap<String, String>();
			beaninfo.put("adsDisplaySource", "WEB");
			find();
		}
		return result;
	}
	
	
	
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get("adsDisplaySource")))
			rules.add(new QueryRule("adsDisplaySource", EQ, beaninfo.get("adsDisplaySource")));

		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}
	
	
	@Override
	protected boolean executeSave() {
		// 檢核
		String bannerType = bean.getBannerType();

		if ("V".equals(bannerType)) {
			if (StringUtils.isBlank(bean.getBannerUrl())) {
				addActionError("請輸入連結網址");
				return false;
			}
		}

		boolean result = super.executeSave();

		// 此檢核不能寫在存檔前，否則上傳圖片時會出錯，僅顯示訊息給USER看
		if ("P".equals(bannerType)) {
			if (StringUtils.isBlank(bean.getBannerImg())) {
				addActionError("貼心小提醒:您未上傳圖片");
			}
		}
		return result;
	}
	
}