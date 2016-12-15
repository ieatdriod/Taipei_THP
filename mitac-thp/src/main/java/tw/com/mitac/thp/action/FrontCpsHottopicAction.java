package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsHottopic;
import tw.com.mitac.thp.bean.CpsHottopicAds;
import tw.com.mitac.thp.bean.CpsHottopicInfo;
import tw.com.mitac.thp.bean.MtsRecommand;

public class FrontCpsHottopicAction extends IndexMtsPageSpecialAction {

	/**
	 * 畫面載入設定
	 */
	public String presetLoadingScreen() {

		String loadKey = request.getParameter("cpsHottopicSysid");
		if (StringUtils.isBlank(loadKey)) {
			notLoadedWithKey();
		} else {
			loadWithKey(loadKey);
		}

		return SUCCESS;
	}

	/**
	 * 預設沒有帶入KEY的話
	 */
	public String notLoadedWithKey() {

		List<CpsHottopic> cpsHottopicList = cloudDao.queryTable(sf(), CpsHottopic.class,
				new QueryGroup(new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) },
				0, 1);
		addMultiLan(cpsHottopicList, sf(), CpsHottopic.class);
		if (cpsHottopicList.size() > 0) {
			// 大標題圖判斷
			if (getIsMobile()) {
				List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
						new QueryGroup(new QueryRule(FK, cpsHottopicList.get(0).getSysid()),
								new QueryRule("adsDisplaySource", "PHONE"), new QueryRule(IS_ENABLED, true)),
						new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
				addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
				if (cpsHottopicAdsList.size() > 0) {
					request.setAttribute("cpsHottopicAdsList", cpsHottopicAdsList.get(0));
				}
			} else {
				List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
						new QueryGroup(new QueryRule(FK, cpsHottopicList.get(0).getSysid()),
								new QueryRule("adsDisplaySource", "WEB"), new QueryRule(IS_ENABLED, true)),
						new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
				addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
				if (cpsHottopicAdsList.size() > 0) {
					request.setAttribute("cpsHottopicAdsList", cpsHottopicAdsList.get(0));
				}
			}

			// 細項內文判斷
			List<CpsHottopicInfo> cpsHottopicInfoList = cloudDao.queryTable(sf(), CpsHottopicInfo.class,
					new QueryGroup(new QueryRule("hottopicSysid", cpsHottopicList.get(0).getSysid()),
							new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, null, null);
			addMultiLan(cpsHottopicInfoList, sf(), CpsHottopicInfo.class);
			if (cpsHottopicInfoList.size() > 0) {
				request.setAttribute("cpsHottopicInfoList", cpsHottopicInfoList);
			}

			request.setAttribute("cpsHottopicList", cpsHottopicList.get(0));
		}

		// 排序第2筆以後
		List<CpsHottopic> cpsHottopicMoreList = cloudDao.queryTable(sf(), CpsHottopic.class,
				new QueryGroup(new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) },
				1, null);
		addMultiLan(cpsHottopicMoreList, sf(), CpsHottopic.class);
		if (cpsHottopicMoreList.size() > 0) {
			request.setAttribute("cpsHottopicMoreList", cpsHottopicMoreList);
			for (CpsHottopic cpsHottopic : cpsHottopicMoreList) {
				if (getIsMobile()) {
					List<CpsHottopicAds> cpsHottopicAdsMoreList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
							new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
									new QueryRule("adsDisplaySource", "PHONE"), new QueryRule(IS_ENABLED, true)),
							new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
					addMultiLan(cpsHottopicAdsMoreList, sf(), CpsHottopicAds.class);
					if (cpsHottopicAdsMoreList.size() > 0) {
						request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
								cpsHottopicAdsMoreList.get(0));
					}
				} else {
					List<CpsHottopicAds> cpsHottopicAdsMoreList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
							new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
									new QueryRule("adsDisplaySource", "WEB"), new QueryRule(IS_ENABLED, true)),
							new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
					addMultiLan(cpsHottopicAdsMoreList, sf(), CpsHottopicAds.class);
					if (cpsHottopicAdsMoreList.size() > 0) {
						request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
								cpsHottopicAdsMoreList.get(0));
					}
				}
			}
		} else {
			request.setAttribute("cpsHottopicAdsMoreList", null);

		}

		return SUCCESS;
	}

	/**
	 * 有帶入KEY的話
	 */
	public String loadWithKey(String loadKey) {

		List<CpsHottopic> cpsHottopicList = cloudDao.queryTable(sf(), CpsHottopic.class,
				new QueryGroup(new QueryRule(PK, loadKey), new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
		addMultiLan(cpsHottopicList, sf(), CpsHottopic.class);
		if (cpsHottopicList.size() > 0) {
			// 大標題圖判斷
			if (getIsMobile()) {
				List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
						new QueryGroup(new QueryRule(FK, loadKey), new QueryRule("adsDisplaySource", "PHONE"),
								new QueryRule(IS_ENABLED, true)),
						new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
				addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
				if (cpsHottopicAdsList.size() > 0) {
					request.setAttribute("cpsHottopicAdsList", cpsHottopicAdsList.get(0));
				}
			} else {
				List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
						new QueryGroup(new QueryRule(FK, loadKey), new QueryRule("adsDisplaySource", "WEB"),
								new QueryRule(IS_ENABLED, true)),
						new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
				addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
				if (cpsHottopicAdsList.size() > 0) {
					request.setAttribute("cpsHottopicAdsList", cpsHottopicAdsList.get(0));
				}
			}

			// 細項內文判斷
			List<CpsHottopicInfo> cpsHottopicInfoList = cloudDao.queryTable(sf(), CpsHottopicInfo.class,
					new QueryGroup(new QueryRule("hottopicSysid", loadKey), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, null, null);
			addMultiLan(cpsHottopicInfoList, sf(), CpsHottopicInfo.class);
			logger.debug("細項內文判斷比數:" + cpsHottopicInfoList.size());
			if (cpsHottopicInfoList.size() > 0) {
				request.setAttribute("cpsHottopicInfoList", cpsHottopicInfoList);
			}

			request.setAttribute("cpsHottopicList", cpsHottopicList.get(0));
		}

		// PK以外
		List<CpsHottopic> cpsHottopicMoreList = cloudDao.queryTable(sf(), CpsHottopic.class,
				new QueryGroup(new QueryRule(PK, NE, loadKey), new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, null, null);
		addMultiLan(cpsHottopicMoreList, sf(), CpsHottopic.class);
		if (cpsHottopicMoreList.size() > 0) {
			request.setAttribute("cpsHottopicMoreList", cpsHottopicMoreList);
			for (CpsHottopic cpsHottopic : cpsHottopicMoreList) {
				if (getIsMobile()) {
					List<CpsHottopicAds> cpsHottopicAdsMoreList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
							new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
									new QueryRule("adsDisplaySource", "PHONE"), new QueryRule(IS_ENABLED, true)),
							new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
					addMultiLan(cpsHottopicAdsMoreList, sf(), CpsHottopicAds.class);
					if (cpsHottopicAdsMoreList.size() > 0) {
						request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
								cpsHottopicAdsMoreList.get(0));
					}
				} else {
					List<CpsHottopicAds> cpsHottopicAdsMoreList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
							new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
									new QueryRule("adsDisplaySource", "WEB"), new QueryRule(IS_ENABLED, true)),
							new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
					addMultiLan(cpsHottopicAdsMoreList, sf(), CpsHottopicAds.class);
					if (cpsHottopicAdsMoreList.size() > 0) {
						request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
								cpsHottopicAdsMoreList.get(0));
					}
				}
			}
		} else {
			request.setAttribute("cpsHottopicAdsMoreList", null);
		}

		return SUCCESS;
	}

}