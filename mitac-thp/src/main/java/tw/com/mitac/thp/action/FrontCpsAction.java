package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsHottopicAds;
import tw.com.mitac.thp.bean.CpsNews;

public class FrontCpsAction extends BasisFrontLoginAction {

	protected CpsNews bean;

	public CpsNews getBean() {
		return bean;
	}

	public void setBean(CpsNews bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	// 系統公告
	public String outerCpsNewsList() {

		String dateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

		List<CpsNews> cpsNewsList = cloudDao.queryTable(sf(), CpsNews.class, new QueryGroup(new QueryRule("startDate",
				LE, dateStr), new QueryRule("endDate", GE, dateStr)), new QueryOrder[] { new QueryOrder("startDate",
				DESC) }, null, null);
		addMultiLan(cpsNewsList, sf(), CpsNews.class);
		session.put("cpsNewsList", null);
		if (cpsNewsList.size() > 0) {
			session.put("cpsNewsList", cpsNewsList);
		}

		return SUCCESS;
	}

	// 系統置頂公告
	public String ajaxDoCpsNewsTop() {
		resultList = new ArrayList();
		resultList = cloudDao.queryTable(sf(), CpsNews.class, new QueryGroup(), // 沒有查詢條件，直接抓最新一筆
				new QueryOrder[] { new QueryOrder("startDate", DESC) }, null, 1); // 直接抓最新一筆

		return JSON_RESULT;
	}
}