package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.HpsCoreHotItem;
import tw.com.mitac.thp.bean.HpsCoreTheme;
import tw.com.mitac.thp.bean.HpsCoreThemeItem;
import tw.com.mitac.thp.bean.HpsPromoteLimit;
import tw.com.mitac.thp.bean.HpsVendorItem;

public class IndexHpsPageAction extends BasisTenancyAction {
	public String indexHpsPage() {
		List<HpsVendorItem> hotItemList = (List<HpsVendorItem>) session.get("hotItemList");
		if (hotItemList == null) {
			hotItemList = new ArrayList<HpsVendorItem>();

			List<Map> l = (List<Map>) cloudDao.findProperty(sf(), HpsCoreHotItem.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true), new QueryRule("startDate", LE, systemDate),
							new QueryRule("endDate", GE, systemDate)),
					new QueryOrder[] { new QueryOrder("itemSortNo") }, false, "itemSysid", "itemName");
			for (Map<String, String> map : l) {
				String itemSysid = map.get("itemSysid");
				String itemName = map.get("itemName");
				HpsVendorItem item = cloudDao.get(sf(), HpsVendorItem.class, itemSysid);
				if (StringUtils.isNotBlank(itemName))
					item.setName(itemName);
				hotItemList.add(item);
			}

			session.put("hotItemList", hotItemList);
		}

		getCoreThemeList();

		return SUCCESS;
	}

	// 特色購物部分
	public List<HpsCoreTheme> getCoreThemeList() {
		// 取得該集合的session
		List<HpsCoreTheme> hpsCoreThemeList = (List<HpsCoreTheme>) session.get("coreThemeListaaa");
		// 判斷是否為null，如果是就產生新的值，不是就回傳
		if (hpsCoreThemeList == null) {
			hpsCoreThemeList = cloudDao.queryTable(sf(), HpsCoreTheme.class,
					new QueryGroup(
							// 布林值
							new QueryRule(IS_ENABLED, true),
							// GT:> LT:< GE:>= LE:<= EQ=
							new QueryRule("startDate", LE, systemDate), //
							new QueryRule("endDate", GE, systemDate)//
					), new QueryOrder[] {
							// ASC 順時針 DESC逆時針
							new QueryOrder("startDate", DESC) },
					// 起始點預設
					null,
					// 顯示筆數
					null);
			// logger.debug(hpsCoreSearchKeyword);
			session.put("coreThemeListaaa", hpsCoreThemeList);
		}

		// logger.debug(hpsCoreSearchKeyword);
		return hpsCoreThemeList;
	}

	public Map<String, List<HpsVendorItem>> getCoreThemeMap2() {

		Map map = (Map) session.get("QQ");
		if (map == null) {
			map = new HashMap<>();
			List<HpsCoreTheme> hpsCoreThemeList = getCoreThemeList();

			for (HpsCoreTheme hpsCoreTheme : hpsCoreThemeList) {
				List<HpsVendorItem> l = new ArrayList<>();

				List<HpsCoreThemeItem> hpsCoreThemeItemList = cloudDao.queryTable(sf(), HpsCoreThemeItem.class,
						new QueryGroup(
								// GT:> LT:< GE:>= LE:<= EQ=
								new QueryRule("themeSysid", EQ, hpsCoreTheme.getSysid()) //

						), new QueryOrder[0],

						// 起始點預設
						0,
						// 顯示筆數
						4);

				for (HpsCoreThemeItem hpsCoreThemeItem2 : hpsCoreThemeItemList) {

					List<HpsVendorItem> hpsVendorItemList = cloudDao.queryTable(sf(), HpsVendorItem.class,
							new QueryGroup(
									// GT:> LT:< GE:>= LE:<= EQ=
									new QueryRule("sysid", EQ, hpsCoreThemeItem2.getItemSysid()) //

							), new QueryOrder[0],

							// 起始點預設
							null,
							// 顯示筆數
							null);

					l.add(hpsVendorItemList.get(0));

				}

				map.put(hpsCoreTheme.getSysid(), l);
			}

			session.put("QQ", map);
		}
		return map;
	}


	
	
	

}