package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsFaq;
import tw.com.mitac.thp.bean.CpsHottopic;
import tw.com.mitac.thp.bean.CpsQa;

public class FrontQandAAction extends BasisTenancyAction {

	/** 預設載入畫面一次全部2016-11-17 */
	public String qaDefault() {
		List<CpsFaq> cpsFaqList = cloudDao.queryTable(sf(), CpsFaq.class, new QueryGroup(), new QueryOrder[]{ new QueryOrder(DATA_ORDER, ASC) }, 0, 8);
		addMultiLan(cpsFaqList, sf(), CpsFaq.class);
		request.setAttribute("cpsFaqList", cpsFaqList);

		return SUCCESS;
	}
}