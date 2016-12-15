package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.bean.MtsArticleType;
import tw.com.mitac.thp.bean.MtsRecommand;
import tw.com.mitac.thp.bean.MtsVendorCategoryLink;
import tw.com.mitac.thp.bean.MtsVendorProfile;

public class IndexMtsPage2Action extends BasisTenancyAction {
	public String indexMtsPage() {
		// --------------醫療新知分類區------------------------
		List<MtsArticleType> mtsArticleTypeList = findMtsArticleTypeList();
		request.setAttribute("mtsArticleTypeList", mtsArticleTypeList);

		String articleTypeSysid = request.getParameter("articleTypeSysid");
		if (StringUtils.isBlank(articleTypeSysid) && mtsArticleTypeList.size() > 0) {
			MtsArticleType firstArticleType = mtsArticleTypeList.get(0);
			articleTypeSysid = firstArticleType.getSysid();
		}

		if (StringUtils.isNotBlank(articleTypeSysid)) {
			request.setAttribute("articleTypeShowIndex", articleTypeSysid);
		}

		String key1 = "data" + MtsRecommand.class.getSimpleName() + "_" + "mtsRecommandList" + "_" + getCookieLan();
		List<MtsRecommand> mtsRecommandList = (List<MtsRecommand>) appMap().get(key1);
		if (mtsRecommandList == null) {
			mtsRecommandList = cloudDao.query(sf(), MtsRecommand.class, QueryGroup.DEFAULT,
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsRecommandList, sf(), MtsRecommand.class);
			appMap().put(key1, mtsRecommandList);
		}
		request.setAttribute("mtsRecommandList", mtsRecommandList);

		String key2 = "data" + MtsVendorProfile.class.getSimpleName() + "_" + "mtsVendorCategoryMap";
		Map<String, List<String>> mtsVendorCategoryMap = (Map<String, List<String>>) appMap().get(key2);
		if (mtsVendorCategoryMap == null) {
			mtsVendorCategoryMap = new HashMap<String, List<String>>();
			appMap().put(key2, mtsVendorCategoryMap);

			List<Map> categorySysidAllList = (List<Map>) cloudDao.findProperty(sf(), MtsVendorCategoryLink.class,
					QueryGroup.DEFAULT, null, false, "sourceSysid", "categorySysid");
			for (Map<String, String> map : categorySysidAllList) {
				String key = map.get("sourceSysid");
				String categorySysid = map.get("categorySysid");
				List<String> categorySysidList = mtsVendorCategoryMap.get(key);
				if (categorySysidList == null) {
					categorySysidList = new ArrayList<String>();
					mtsVendorCategoryMap.put(key, categorySysidList);
				}
				categorySysidList.add(categorySysid);
			}
		}
		request.setAttribute("mtsVendorCategoryMap", mtsVendorCategoryMap);

		return SUCCESS;
	}

	/**
	 * 醫療新知分類
	 */
	public List<MtsArticleType> findMtsArticleTypeList() {
		List<MtsArticleType> mtsArticleTypeList = new ArrayList<MtsArticleType>(getDataMtsArticleTypeTable().values());
		return mtsArticleTypeList;
	}
}