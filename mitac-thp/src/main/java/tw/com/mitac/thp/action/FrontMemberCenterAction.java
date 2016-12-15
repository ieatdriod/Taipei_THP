package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsFaq;
import tw.com.mitac.thp.bean.CpsNews;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsTopmarquee;
import tw.com.mitac.thp.bean.HpsBillSalesOrder;
import tw.com.mitac.thp.bean.HpsCoreFavoriteItem;
import tw.com.mitac.thp.bean.HpsPromoteBonus;
import tw.com.mitac.thp.bean.HpsPromoteBonusMember;
import tw.com.mitac.thp.bean.HpsVendorItem;

public class FrontMemberCenterAction extends BasisFrontLoginAction {
	
	protected CpsSiteMember bean;
	protected String countrySelect;
	
	public CpsSiteMember getBean() {
		return bean;
	}

	public String getCountrySelect() {
		return countrySelect;
	}

	public String memberCenterPage() {
		session.remove("cpsNewsList");
		String dateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		List<CpsNews> cpsNewsList = cloudDao.queryTable(sf(), CpsNews.class, new QueryGroup(new QueryRule("startDate",
				LE, dateStr), new QueryRule("endDate", GE, dateStr)), new QueryOrder[] { new QueryOrder("startDate",
				DESC) }, null, null);
		addMultiLan(cpsNewsList, sf(), CpsNews.class);
		if (cpsNewsList.size() > 0) {
			session.put("cpsNewsList", cpsNewsList);
		}
		
		return SUCCESS;
	}
	
	public String initAccountInfo() {
		String sysid = getUserData2().getAccount().getSysid();
		bean = cloudDao.get(sf(), CpsSiteMember.class, sysid);
		countrySelect = bean.getCountrySysid() + "#" + getAllCountry().get(bean.getCountrySysid()).getIsForeign();
		return SUCCESS;
	}
	
	// 系統置頂公告
	public String ajaxDoCpsNewsTop() {
		resultList = new ArrayList();
		resultList = cloudDao.queryTable(sf(), CpsTopmarquee.class, new QueryGroup(), // 沒有查詢條件，直接抓最新一筆
				new QueryOrder[] { new QueryOrder("creationDate", DESC) }, null, 1); // 直接抓最新一筆

		return JSON_RESULT;
	}

	public String memberCenterOrder() {
		List<HpsBillSalesOrder> hpsBillSalesOrderList = cloudDao.queryTable(sf(), HpsBillSalesOrder.class,
				new QueryGroup(new QueryRule("buyerMemberSysid", getUserData2().getAccount().getSysid())),
				new QueryOrder[0], null, null);
		request.setAttribute("hpsBillSalesOrderList", hpsBillSalesOrderList);
		return SUCCESS;
	}

	public String memberCenterOrderDetail() {
		String orderPk = request.getParameter("q");
		HpsBillSalesOrder hpsBillSalesOrder = cloudDao.get(sf(), HpsBillSalesOrder.class, orderPk);
		request.setAttribute("hpsBillSalesOrder", hpsBillSalesOrder);
		return SUCCESS;
	}

	public String memberCenterBonus() {
		List<HpsPromoteBonusMember> hpsPromoteBonusMemberList = cloudDao.queryTable(sf(), HpsPromoteBonusMember.class,
				new QueryGroup(new QueryRule("memberSysid", getUserData2().getAccount().getSysid())),
				new QueryOrder[] { new QueryOrder("bonusDeadlineDate") }, null, null);
		List<Map> list = formatListToMap(hpsPromoteBonusMemberList);
		if (list.size() > 0) {
			List<String> bonusSysidList = new ArrayList<String>();
			for (HpsPromoteBonusMember hpsPromoteBonusMember : hpsPromoteBonusMemberList) {
				bonusSysidList.add(hpsPromoteBonusMember.getBonusSysid());
			}
			List<Map> bonusList = (List<Map>) cloudDao.findProperty(sf(), HpsPromoteBonus.class, QueryGroup.DEFAULT,
					new QueryOrder[0], false, PK, "bonusTitle");
			Map<String, String> bonusMap = new HashMap<String, String>();
			for (Map<String, String> map : bonusList) {
				bonusMap.put(map.get(PK), map.get("bonusTitle"));
			}
			for (Map<String, Object> map : list) {
				String bonusSysid = (String) map.get("bonusSysid");
				map.put("bonusTitle", bonusMap.get(bonusSysid));
			}
		}
		request.setAttribute("hpsPromoteBonusMemberList", list);
		return SUCCESS;
	}

	public String favoriteProduct() {
		List<String> itemSysidList = (List<String>) cloudDao.findProperty(sf(), HpsCoreFavoriteItem.class,
				new QueryGroup(new QueryRule("memberSysid", getUserData2().getAccount().getSysid())),
				new QueryOrder[0], true, "itemSysid");
		if (itemSysidList.size() > 0) {
			List<HpsVendorItem> itemList = cloudDao.queryTable(sf(), HpsVendorItem.class, new QueryGroup(new QueryRule(
					PK, IN, itemSysidList)), new QueryOrder[0], null, null);
			request.setAttribute("itemList", itemList);
		}
		return SUCCESS;
	}
}