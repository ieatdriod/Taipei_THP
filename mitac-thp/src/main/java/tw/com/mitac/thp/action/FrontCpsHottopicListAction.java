package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsHottopic;
import tw.com.mitac.thp.bean.CpsHottopicAds;
import tw.com.mitac.thp.bean.CpsHottopicInfo;

public class FrontCpsHottopicListAction extends BasisTenancyAction {

	/**
	 * 畫面載入設定 -一定會從HotTopic入口近來.沒有就導去HOTTOP
	 */
	public String presetLoadingScreen() {

		// 取得KEY
		String loadKey = request.getParameter("cpsHottopicInFoSysid");

		if (StringUtils.isNotBlank(loadKey)) {

			// 取得詳細內文資源
			List<CpsHottopicInfo> cpsHottopicInfoList = cloudDao.queryTable(sf(), CpsHottopicInfo.class,
					new QueryGroup(new QueryRule(PK, loadKey)), new QueryOrder[0], null, null);
			addMultiLan(cpsHottopicInfoList, sf(), CpsHottopicInfo.class);

			if (cpsHottopicInfoList.size() > 0) {

				request.setAttribute("cpsHottopicInfoList", cpsHottopicInfoList.get(0));

				// 取得其他詳細內文資源
				List<CpsHottopicInfo> cpsHottopicInfoMoreList = cloudDao.queryTable(sf(), CpsHottopicInfo.class,
						new QueryGroup(new QueryRule("hottopicSysid", cpsHottopicInfoList.get(0).getHottopicSysid()),
								new QueryRule(IS_ENABLED, true), new QueryRule(PK, NI, loadKey)),
						new QueryOrder[0], null, null);
				addMultiLan(cpsHottopicInfoMoreList, sf(), CpsHottopicInfo.class);

				if (cpsHottopicInfoMoreList.size() > 0) {
					request.setAttribute("cpsHottopicInfoMoreList", cpsHottopicInfoMoreList);
				} else {
					request.setAttribute("cpsHottopicInfoMoreList", null);
				}

				// 取得現有主題名稱
				List<CpsHottopic> cpsHottopicName = cloudDao.queryTable(sf(), CpsHottopic.class,
						new QueryGroup(new QueryRule(PK, cpsHottopicInfoList.get(0).getHottopicSysid()),
								new QueryRule(IS_ENABLED, true)),
						new QueryOrder[0], null, null);
				addMultiLan(cpsHottopicName, sf(), CpsHottopic.class);
				if (cpsHottopicName.size() > 0) {
					request.setAttribute("cpsHottopicName", cpsHottopicName.get(0).getHottopicName());
				}

				// 顯示更多主題-排除現有主題
				List<CpsHottopic> cpsHottopicMoreList = cloudDao.queryTable(sf(), CpsHottopic.class,
						new QueryGroup(new QueryRule(PK, NI, cpsHottopicInfoList.get(0).getHottopicSysid()),
								new QueryRule(IS_ENABLED, true)),
						new QueryOrder[0], null, null);
				addMultiLan(cpsHottopicMoreList, sf(), CpsHottopic.class);

				if (cpsHottopicMoreList.size() > 0) {
					request.setAttribute("cpsHottopicMoreList", cpsHottopicMoreList);
					// 顯示更多主題--圖片
					for (CpsHottopic cpsHottopic : cpsHottopicMoreList) {
						if (getIsMobile()) {
							List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
									new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
											new QueryRule("adsDisplaySource", "PHONE"),
											new QueryRule(IS_ENABLED, true)),
									new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
							addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
							if (cpsHottopicAdsList.size() > 0) {
								request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
										cpsHottopicAdsList.get(0));
							}
						} else {
							List<CpsHottopicAds> cpsHottopicAdsList = cloudDao.queryTable(sf(), CpsHottopicAds.class,
									new QueryGroup(new QueryRule(FK, cpsHottopic.getSysid()),
											new QueryRule("adsDisplaySource", "WEB"), new QueryRule(IS_ENABLED, true)),
									new QueryOrder[] { new QueryOrder(DATA_ORDER, ASC) }, 0, 1);
							addMultiLan(cpsHottopicAdsList, sf(), CpsHottopicAds.class);
							if (cpsHottopicAdsList.size() > 0) {
								request.setAttribute("cpsHottopicAdsMoreList" + cpsHottopic.getSysid(),
										cpsHottopicAdsList.get(0));
							}
						}
					}

				} else {
					request.setAttribute("cpsHottopicMoreList", null);
				}

			}
		} else {
			redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
					+ getHotTopicPath();
			return REDIRECT_PAGE;
		}

		return SUCCESS;
	}
}