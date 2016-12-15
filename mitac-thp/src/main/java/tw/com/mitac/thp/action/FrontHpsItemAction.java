package tw.com.mitac.thp.action;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreFavoriteItem;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsCoreTheme;
import tw.com.mitac.thp.bean.HpsCoreThemeItem;
import tw.com.mitac.thp.bean.HpsVendorHotItem;
import tw.com.mitac.thp.bean.HpsVendorItem;
import tw.com.mitac.thp.bean.HpsVendorRecommendItem;
import tw.com.mitac.thp.login2.UserData2;
import tw.com.mitac.thp.util.FileUtil;

public class FrontHpsItemAction extends BasisTenancyAction {
	protected static final String ITEM_OUTER_MAIN_SHOW_NUMBER = "8"; // ResourceBundle.getBundle("OuterRuleSetting").getString("itemOuterMainShowNumber");
	protected HpsVendorItem bean;
	protected String vendorSysid;

	public final HpsVendorItem getBean() {
		return bean;
	}

	public final void setBean(HpsVendorItem bean) {
		this.bean = bean;
	}

	public final String getVendorSysid() {
		return vendorSysid;
	}

	public final void setVendorSysid(String vendorSysid) {
		this.vendorSysid = vendorSysid;
	}

	/**
	 * 產品頁
	 * 
	 * @return
	 */
	public String outerItemSingle() {
		String itemSysid = request.getParameter("itemSysid");
		if (StringUtils.isBlank(itemSysid)) {
			addActionError(getText("msg.itemLost"));
			return ERROR;
		} else {
			bean = cloudDao.get(sf(), HpsVendorItem.class, itemSysid);

			List<String> itemTypeList = executeTreeItemType(bean.getItemTypeSysid());
			if (itemTypeList == null)
				return ERROR;
			buildPictureBillMap();

			UserData2 userData2 = (UserData2) session.get("userData2");
			if (userData2 != null) {
				int fCount = cloudDao.queryTableCount(sf(), HpsCoreFavoriteItem.class, new QueryGroup(new QueryRule(
						"memberSysid", userData2.getAccount().getSysid()), new QueryRule("itemSysid", itemSysid)));
				if (fCount > 0)
					request.setAttribute("isFavorite", true);
			}

			vendorSysid = bean.getVendorSysid();
			int vendorItemCount = cloudDao.queryTableCount(sf(), HpsVendorItem.class, new QueryGroup(new QueryRule(
					"vendorSysid", vendorSysid)));
			request.setAttribute("vendorItemCount", vendorItemCount);

			// 取出cookie
			String oldcookieurl = cookiesMap.get("url");
			String oldcookieimg = cookiesMap.get("img");
			// String oldcookieurl2 = cookiesMap.get("url2");
			// String oldcookieimg2 = cookiesMap.get("img2");

			// 寫入cookie
			Cookie cookieurl2 = new Cookie("url2", oldcookieurl);
			Cookie cookieimg2 = new Cookie("img2", oldcookieimg);

			Cookie cookieurl = new Cookie("url", itemSysid);
			Cookie cookieimg = new Cookie("img", bean.getMainPictureFilePath());
			// 設置Cookie的生命周期
			cookieurl.setMaxAge(60 * 60 * 24 * 365);
			cookieimg.setMaxAge(60 * 60 * 24 * 365);
			cookieurl2.setMaxAge(60 * 60 * 24 * 365);
			cookieimg2.setMaxAge(60 * 60 * 24 * 365);
			response.addCookie(cookieurl);
			response.addCookie(cookieimg);
			response.addCookie(cookieurl2);
			response.addCookie(cookieimg2);

		}
		return SUCCESS;
	}

	protected final String MAIN_FILE_PATH() {
		return getSettingResource().get("file.hpsVendorItemPic");
	}

	/** 找所有圖片路徑 */
	protected void buildPictureBillMap() {
		if (StringUtils.isNotBlank(bean.getSysid())) {
			String subMainFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator;
			File targetDir = new File(subMainFilePath);
			Map<String, List<String>> fileMap = new LinkedHashMap<String, List<String>>();
			if (targetDir.isDirectory()) {
				String[] fileNames = targetDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return FileUtil.validateExtention(pictureExtention, name);
					}
				});
				List<String> fileList = null;
				for (int i = 0, key = 0; i < fileNames.length; i++) {
					if (i % 4 == 0) {
						fileList = new ArrayList<String>();
						fileMap.put("" + key, fileList);
						key++;
					}
					fileList.add(fileNames[i]);
				}
			}
			sessionSet("pictureFileMap", fileMap);
		} else {
			sessionSet("pictureFileMap", null);
		}
	}

	protected String showItemPage = "1";
	protected String itemType;

	public String getShowItemPage() {
		return showItemPage;
	}

	public void setShowItemPage(String showItemPage) {
		this.showItemPage = showItemPage;
	}

	public final String getItemType() {
		return itemType;
	}

	public final void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public List<String> executeTreeItemType(String itemType) {
		HpsCoreItemType hpsCoreItemType = getDataHpsCoreItemTypeTable().get(itemType);
		if (hpsCoreItemType == null)
			return null;

		List<String> itemTypeList = new LinkedList<String>();
		itemTypeList.add(hpsCoreItemType.getSysid());
		// String itemTypeDisplay = hpsCoreItemType.getName();
		HpsCoreItemType targetItemType = hpsCoreItemType;
		for (int i = 1; i <= 100; i++) {
			String parentItemTypeSysid = targetItemType.getParentItemTypeSysid();
			if (StringUtils.isBlank(parentItemTypeSysid))
				break;

			targetItemType = getDataHpsCoreItemTypeTable().get(parentItemTypeSysid);
			itemTypeList.add(0, targetItemType.getSysid());
			// itemTypeDisplay = targetItemType.getName() + "&nbsp;&gt;&nbsp;" +
			// itemTypeDisplay;
		}
		// session.put("itemTypeDisplay", itemTypeDisplay);
		session.put("itemTypeList", itemTypeList);
		return itemTypeList;
	}

	public String outerMain() {
		logger.info("itemType:" + itemType);

		if (StringUtils.isNotBlank(itemType)) {
			List<String> itemTypeList = executeTreeItemType(itemType);
			if (itemTypeList == null)
				return ERROR;
		}

		List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
		// 商品
		queryRuleList.add(new QueryRule("itemTypeSysid", itemType));
		queryRuleList.add(new QueryRule("itemTypeSysid2", itemType));
		queryRuleList.add(new QueryRule("itemTypeSysid3", itemType));
		QueryGroup group1 = new QueryGroup(OR, queryRuleList.toArray(new QueryRule[0]), null);
		List<QueryGroup> groups = new ArrayList<QueryGroup>();
		groups.add(group1);
		QueryGroup queryGroup = new QueryGroup(AND, new QueryRule[] { new QueryRule("vendorSysid", CN, "") },
		// new QueryRule("vendorSysid" , "*")
				groups.toArray(new QueryGroup[0]));

		int dataCounter = cloudDao.queryCount(sf(), HpsVendorItem.class, queryGroup);

		int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		int nowShowItemPage = Integer.parseInt(getShowItemPage());
		int validatePagesNum = (int) Math.ceil((double) dataCounter / (double) length);
		if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
			nowShowItemPage = validatePagesNum;
		else if (nowShowItemPage <= 0)// 檢核超過總頁數的情況
			nowShowItemPage = 1;
		setShowItemPage(String.valueOf(nowShowItemPage));

		int from = length * (nowShowItemPage - 1);
		List<HpsVendorItem> itemList = cloudDao.queryTable(sf(), HpsVendorItem.class, queryGroup,
				new QueryOrder[] { new QueryOrder(PK, DESC) }, from, length);

		QueryResults queryResults = new QueryResults();
		queryResults.setRows(length);
		queryResults.setPage(nowShowItemPage);
		queryResults.setTotal(validatePagesNum);
		queryResults.setRecord(dataCounter);
		queryResults.setGridModel(itemList);
		sessionSet("queryResults", queryResults);

		return "outerMain";
	}

	private String outerSearchKeepCondition;
	private String outerSearchKeepData;

	public final String getOuterSearchKeepCondition() {
		return outerSearchKeepCondition;
	}

	public final void setOuterSearchKeepCondition(String outerSearchKeepCondition) {
		this.outerSearchKeepCondition = outerSearchKeepCondition;
	}

	public final String getOuterSearchKeepData() {
		return outerSearchKeepData;
	}

	public final void setOuterSearchKeepData(String outerSearchKeepData) {
		this.outerSearchKeepData = outerSearchKeepData;
	}

	/**
	 * 舊的搜尋，只能搜尋ITEM
	 * 
	 * @return
	 */
	public String outerSearch() {
		String rule = request.getParameter("searchRule");
		String text = request.getParameter("outerSearchBarInput");
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(text))
			setOuterSearchKeepData(text);
		List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
		if (NAME.equals(rule)) {
			queryRuleList.add(new QueryRule(NAME, CN, text));
		} else if ("type".equals(rule)) {
			List<HpsCoreItemType> itemTypeList = cloudDao.queryTable(sf(), HpsCoreItemType.class, new QueryGroup(
					new QueryRule(NAME, CN, text), new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(
					PK, DESC) }, null, null);
			Set<String> allTypeSet = new LinkedHashSet<String>();
			for (HpsCoreItemType type : itemTypeList) {
				allTypeSet.add(type.getSysid());
				Map<String, HpsCoreItemType> allTypeMap = traceAllTypeBelowTheseType(type.getSysid());
				for (Entry<String, HpsCoreItemType> entry : allTypeMap.entrySet())
					allTypeSet.add(entry.getKey());
			}
			String itemTypeSysidStr = "x";
			for (String key : allTypeSet)
				itemTypeSysidStr += "," + key;
			queryRuleList.add(new QueryRule("itemTypeSysid", IN, itemTypeSysidStr));
		}
		// queryRuleList.add(new QueryRule("enterpriseSysid",
		// enterprise.getSysid()));
		queryRuleList.add(new QueryRule(IS_ENABLED, true));
		if (StringUtils.isNotBlank(vendorSysid))
			queryRuleList.add(new QueryRule("vendorSysid", vendorSysid));
		QueryGroup queryGroup = new QueryGroup(queryRuleList.toArray(new QueryRule[0]));

		int dataCounter = cloudDao.queryCount(sf(), HpsVendorItem.class, queryGroup);

		int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		int nowShowItemPage = Integer.parseInt(getShowItemPage());
		int validatePagesNum = (int) Math.ceil((double) dataCounter / (double) length);
		if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
			nowShowItemPage = validatePagesNum;
		else if (nowShowItemPage <= 0)// 檢核超過總頁數的情況
			nowShowItemPage = 1;
		setShowItemPage(String.valueOf(nowShowItemPage));

		int from = length * (nowShowItemPage - 1);
		List<HpsVendorItem> itemList = cloudDao.queryTable(sf(), HpsVendorItem.class, queryGroup,
				new QueryOrder[] { new QueryOrder(OD, DESC) }, from, length);

		QueryResults queryResults = new QueryResults();
		queryResults.setRows(length);
		queryResults.setPage(nowShowItemPage);
		queryResults.setTotal(validatePagesNum);
		queryResults.setRecord(dataCounter);
		queryResults.setGridModel(itemList);
		sessionSet("queryResults", queryResults);

		return SUCCESS;
	}

	/**
	 * // XXX 處理樹狀結構 不確定這樣效能好不好<br>
	 * 查出所有在parentVendorItemTypeSysid之下的產品類別，
	 * 此parentVendorItemTypeSysid可以是多個sysid用逗號隔開
	 * 
	 * @param parentVendorItemTypeSysid
	 * @return
	 */
	protected Map<String, HpsCoreItemType> traceAllTypeBelowTheseType(String parentVendorItemTypeSysid) {
		String key = "data" + HpsCoreItemType.class.getSimpleName() + "traceAllTypeBelowTheseType"
				+ parentVendorItemTypeSysid;
		Map<String, HpsCoreItemType> targetMap = (Map<String, HpsCoreItemType>) appMap().get(key);
		// new LinkedHashMap<String, CoreItemType>();
		if (targetMap == null) {
			targetMap = new LinkedHashMap<String, HpsCoreItemType>();
			if (StringUtils.isNotBlank(parentVendorItemTypeSysid)) {
				List<HpsCoreItemType> nextLevelList = cloudDao.queryTable(sf(), HpsCoreItemType.class, new QueryGroup(
						new QueryRule("parentVendorItemTypeSysid", IN, parentVendorItemTypeSysid), new QueryRule(
								IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(PK, DESC) }, null, null);
				// logger.debug("測試 此接查詢到幾筆:" + nextLevelList.size());
				if (nextLevelList.size() > 0) {
					String nextLevelTypeSysidStr = "";
					for (HpsCoreItemType type : nextLevelList) {
						targetMap.put(type.getSysid(), type);
						nextLevelTypeSysidStr += "," + type.getSysid();
					}
					// logger.debug("測試 組出來的下一接查詢條件:" + nextLevelTypeSysidStr);
					targetMap.putAll(traceAllTypeBelowTheseType(nextLevelTypeSysidStr.substring(1)));
				}

			}
			appMap().put(key, targetMap);
		}
		return targetMap;
	}

	/**
	 * @param vendorSysid
	 */
	public String store() {
		List<String> hotItemSysidList = (List<String>) cloudDao.findProperty(sf(), HpsVendorHotItem.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid)), new QueryOrder[0], true, "itemSysid");
		List<HpsVendorItem> hotItemList = cloudDao.queryTable(sf(), HpsVendorItem.class, new QueryGroup(new QueryRule(
				PK, IN, hotItemSysidList)), new QueryOrder[0], null, null);
		request.setAttribute("hotItemList", hotItemList);

		List<String> recommendItemSysidList = (List<String>) cloudDao.findProperty(sf(), HpsVendorRecommendItem.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid)), new QueryOrder[0], true, "itemSysid");
		List<HpsVendorItem> recommendItemList = cloudDao.queryTable(sf(), HpsVendorItem.class, new QueryGroup(
				new QueryRule(PK, IN, recommendItemSysidList)), new QueryOrder[0], null, null);
		request.setAttribute("recommendItemList", recommendItemList);
		return vendorIndexPage();
	}

	/**
	 * @param vendorSysid
	 */
	public String vendorIndexPage() {
		CpsVendor cpsVendor = getDataCpsVendorTable().get(vendorSysid);
		request.setAttribute("cpsVendor", cpsVendor);

		int vendorItemCount = cloudDao.queryTableCount(sf(), HpsVendorItem.class, new QueryGroup(new QueryRule(
				"vendorSysid", vendorSysid)));
		request.setAttribute("vendorItemCount", vendorItemCount);
		return SUCCESS;
	}

	public String vendorSearch() {
		outerSearch();
		return vendorIndexPage();
	}

	/** 店家名稱 */
	public String frontVendorName() {
		String pk = request.getParameter("pk");
		CpsVendor cpsVendorBean = cloudDao.get(sf(), CpsVendor.class, pk);
		if (cpsVendorBean != null) {
			JSONObject jsonObject = new JSONObject(cpsVendorBean);
			resultString = jsonObject.toString();
			logger.debug("resultString=" + resultString);
		}
		return JSON_RESULT;
	}

	// ---------- ---------- ---------- ---------- ----------
	public String brand() {
		String brandSysid = request.getParameter("brandSysid");
		request.setAttribute("brand", getDataHpsCoreBrandETable().get(brandSysid));

		// 查詢條件
		QueryGroup queryGroup = new QueryGroup(new QueryRule("brandSysid", EQ, brandSysid));

		int dataCounter = cloudDao.queryCount(sf(), HpsVendorItem.class, queryGroup);

		int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		int nowShowItemPage = Integer.parseInt(getShowItemPage());// 現在的頁碼
		int validatePagesNum = (int) Math.ceil((double) dataCounter / (double) length);
		if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
			nowShowItemPage = validatePagesNum;
		else if (nowShowItemPage <= 0)// 檢核超過總頁數的情況
			nowShowItemPage = 1;
		setShowItemPage(String.valueOf(nowShowItemPage));

		int from = length * (nowShowItemPage - 1);
		List<HpsVendorItem> itemList = cloudDao.queryTable(sf(), HpsVendorItem.class, queryGroup,
				new QueryOrder[] { new QueryOrder(OD, DESC) }, from, length);

		QueryResults queryResults = new QueryResults();
		queryResults.setRows(length);
		queryResults.setPage(nowShowItemPage);
		queryResults.setTotal(validatePagesNum);
		queryResults.setRecord(dataCounter);
		queryResults.setGridModel(itemList);
		sessionSet("queryResults", queryResults);

		return SUCCESS;

	}

	public String topicPL() {
		// 接收Sysid
		String themeSysid = request.getParameter("themeSysid");
		HpsCoreTheme hpsCoreTheme = cloudDao.get(sf(), HpsCoreTheme.class, themeSysid);

		request.setAttribute("hpsCoreTheme", hpsCoreTheme);
		List<String> itemSysidList = (List<String>) cloudDao.findProperty(sf(), HpsCoreThemeItem.class,
		// 查詢條件
				new QueryGroup(new QueryRule("themeSysid", EQ, themeSysid)),
				// 排序
				new QueryOrder[0],
				// 是否要重複
				false,
				// 查詢哪些欄位
				"itemSysid");
		// 如果List搜尋出來SIZE是0 他會忽略掉此語法,直接搜尋所有表格
		QueryGroup queryGroup = new QueryGroup(new QueryRule(PK, IN, itemSysidList));
		// 判斷是否是0，如果是就產生新的讓他查詢失敗
		if (itemSysidList.size() == 0) {
			queryGroup = new QueryGroup(new QueryRule(PK, EQ, "x"));

		}

		int dataCounter = cloudDao.queryCount(sf(), HpsVendorItem.class, queryGroup);

		int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		int nowShowItemPage = Integer.parseInt(getShowItemPage());// 現在的頁碼
		int validatePagesNum = (int) Math.ceil((double) dataCounter / (double) length);
		if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
			nowShowItemPage = validatePagesNum;
		else if (nowShowItemPage <= 0)// 檢核超過總頁數的情況
			nowShowItemPage = 1;
		setShowItemPage(String.valueOf(nowShowItemPage));

		int from = length * (nowShowItemPage - 1);
		List<HpsVendorItem> itemList = cloudDao.queryTable(sf(), HpsVendorItem.class, queryGroup,
				new QueryOrder[] { new QueryOrder(OD, DESC) }, from, length);

		QueryResults queryResults = new QueryResults();
		queryResults.setRows(length);
		queryResults.setPage(nowShowItemPage);
		queryResults.setTotal(validatePagesNum);
		queryResults.setRecord(dataCounter);
		queryResults.setGridModel(itemList);
		sessionSet("queryResults", queryResults);

		return SUCCESS;
	}
}