package tw.com.mitac.thp.action;

// Generated Fri Mar 11 14:27:13 CST 2016 by GenCode.java

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsRecommand;
import tw.com.mitac.thp.bean.BhsRecommandItem;
import tw.com.mitac.thp.bean.BhsRecommandLink;
import tw.com.mitac.thp.bean.BhsRecommandLinkItem;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.MtsRecommandItem;

/**
 * BhsRecommandAction generated by GenCode.java
 */
// public class BhsRecommandAction extends BhsRecommandBasisAction<BhsRecommand>
// {
public class BhsRecommandAction extends DetailController<BhsRecommand> {

	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", BhsRecommandItem.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", BhsRecommandLinkItem.class));
		return detailClassMap;
	}

	@Override
	public boolean getWithoutSaveBtn() {
		return false;
	}

	/**
	 * [jqgrid]
	 */
	public Map<String, Map> getJqgridDetailColModelInfoMap() {
		Map<String, Map> jqgridDetailColModelInfoMap = super.getJqgridDetailColModelInfoMap();
		Map<String, Map> jqgridDetailColModelMap = jqgridDetailColModelInfoMap.get("");
		Map<String, Object> colModelMap = jqgridDetailColModelMap.get("sourceSysid");
		Map<String, Object> editoptionsMap = (Map<String, Object>) colModelMap.get("editoptions");
		// Map<String, Object> searchoptionsMap = (Map<String, Object>)
		// colModelMap.get("searchoptions");

		Class<?> targetClass = null;
		try {
			String recommandType = (String) PropertyUtils.getProperty(bean, "recommandType");
			if ("B".equalsIgnoreCase(recommandType)) {
				targetClass = BhsVendorProfile.class;
			} else if ("P".equalsIgnoreCase(recommandType)) {
				targetClass = BhsRecommandLink.class;
			} else if ("T".equalsIgnoreCase(recommandType)) {
				targetClass = BhsRecommandLink.class;
			} else if ("H".equalsIgnoreCase(recommandType)) {
				targetClass = BhsHighlight.class;
			}
		} catch (Exception e) {
		}
		if (targetClass != null) {
			colModelMap.put("selectTool", targetClass.getSimpleName());
			Map<String, String> editoptionsValueMap = createDataDisplay(targetClass);
			editoptionsMap.put("value", editoptionsValueMap);
			// colModelMap.put("stype", "select");
			colModelMap.put("formatter", "select");
		}
		return jqgridDetailColModelInfoMap;
	}
	// 移至共通BhsRecommandBasisAction

	@Override
	protected boolean executeSave() {
		// 排序功能模組
		remapDataOrder(DETAIL_SET2);
		return super.executeSave();
	}

	public String aPluralityOfData() {
		String msg = SUCCESS;
		String myarr = request.getParameter("myarr");
		String type = request.getParameter("type");
		logger.debug("資料導入：" + myarr);
		String[] pluralitySysid = myarr.split(",");

		for (int i = 0; i < pluralitySysid.length; i++) {
			logger.debug("處理後值：" + pluralitySysid[i]);

			// 處理型態改變Detall
			// private java.util.Set<BhsRecommandItem> detailSet;
			// private java.util.Set<BhsRecommandLinkItem> detailSet2;僅增加第二層名稱
			logger.debug("現在項目是：" + type);
			if (type.equals("B") || type.equals("H")) {
				Set<BhsRecommandItem> dataSet = (Set<BhsRecommandItem>) findDetailSetWhenEdit(DETAIL_SET);
				BhsRecommandItem item = getDefaultDMO(BhsRecommandItem.class);

				item.setParentSysid(bean.getSysid());
				item.setSourceSysid(pluralitySysid[i]);
				item.setDataOrder(1);
				defaultValue(item);
				tw.com.mitac.thp.util.Util.defaultPK(item);
				dataSet.add(item);

			} 
		}

		resultString = msg;
		return JSON_RESULT;
	}

}