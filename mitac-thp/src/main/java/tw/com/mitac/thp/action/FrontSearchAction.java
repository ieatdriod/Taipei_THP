package tw.com.mitac.thp.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.bean.BhsMenuLink;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsHottopic;
import tw.com.mitac.thp.bean.CpsHottopicInfo;
import tw.com.mitac.thp.bean.CpsKeywords;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsArticle;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsMenu;
import tw.com.mitac.thp.bean.MtsMenuLink;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.login2.UserData2;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FrontSearchAction extends BasisTenancyAction {
	private String outerSearchKeepCondition;
	private String outerSearchKeepData;// keep搜尋關鍵字

	// 拮取關鍵字前後總字數,以螢幕寬度1366為準，顯示三行內容
	// private int showDataNums = 89;
	private int showDataNums = 300;
	protected boolean isNbsp = false;
	protected char[] wordSplitHead = {
			// ' ',
			// ',',
			'.', ';',
			// '，',
			'．', '。', '；' };
	protected char[] wordSplitTail = { ' ', ',', '.', ';', '，', '．', '。', '；' };

	protected String mtsContentSearchTop = ""; // mts筆數
	protected String bhsContentSearchTop = ""; // bhs筆數
	protected String hotTopicSearchTop = ""; // hotTopic筆數

	// 大首頁計算總比數
	int allCount = 0;
	int mtsCount = 0;
	int bhsCount = 0;
	int hotTopicCount = 0;
	// 各細項比數
	int mtsVendorProfileCount = 0;
	int mtsProductsProfileCount = 0;
	int mtsHighlightCount = 0;
	int mtscooperationCount = 0;
	int mtsArticleCount = 0;
	int mtsArticleForBreakingNews = 0;
	int mtsArticleForTaiwanInsight = 0;

	int bhsVendorProfileCount = 0;
	int bhsProductsProfileCount = 0;
	int bhsTechnologyCount = 0;
	int bhsHighlightCount = 0;
	int bhsArticleCount = 0;
	int bhsArticleForBreakingNews = 0;
	int bhsArticleForTaiwanInsight = 0;

	int cpsHottopicInfoCount = 0;

	public String getOuterSearchKeepCondition() {
		return outerSearchKeepCondition;
	}

	public void setOuterSearchKeepCondition(String outerSearchKeepCondition) {
		this.outerSearchKeepCondition = outerSearchKeepCondition;
	}

	public String getOuterSearchKeepData() {
		return outerSearchKeepData;
	}

	public void setOuterSearchKeepData(String outerSearchKeepData) {
		this.outerSearchKeepData = outerSearchKeepData;
	}

	public final String getMtsContentSearchTop() {
		return mtsContentSearchTop;
	}

	public final void setMtsContentSearchTop(String mtsContentSearchTop) {
		this.mtsContentSearchTop = mtsContentSearchTop;
	}

	public final String getBhsContentSearchTop() {
		return bhsContentSearchTop;
	}

	public final void setBhsContentSearchTop(String bhsContentSearchTop) {
		this.bhsContentSearchTop = bhsContentSearchTop;
	}

	public final String getHotTopicSearchTop() {
		return hotTopicSearchTop;
	}

	public final void setHotTopicSearchTop(String hotTopicSearchTop) {
		this.hotTopicSearchTop = hotTopicSearchTop;
	}

	/**
	 * <pre>
	 * 取得關鍵字
	 * </pre>
	 * 
	 * 10次以上加入常用關鍵字
	 */
	public String ajaxKeyWord() {
		resultList = cloudDao.findProperty(sf(), CpsKeywords.class, new QueryGroup(OR, new QueryRule[] { new QueryRule(
				"initSearchCount", GT, 0) }, new QueryGroup[] { new QueryGroup(new QueryRule("initSearchCount", EQ, 0),
				new QueryRule("searchCount", GT, 10)) }), new QueryOrder[] { new QueryOrder("searchCount", DESC) },
				true, "keywords");
		return JSON_RESULT;
	}

	protected void genKeywordCount() {
		// 處理關鍵字次數 start.
		session.remove("cpsKeywords");
		if (StringUtils.isNotBlank(outerSearchKeepData)) {
			List<CpsKeywords> cpsKeywords = cloudDao.queryTable(sf(), CpsKeywords.class, new QueryGroup(new QueryRule(
					"keywords", outerSearchKeepData), new QueryRule("entitySysid", getCpsEntitySysid())),
					new QueryOrder[0], 0, null);
			addMultiLan(cpsKeywords, sf(), CpsKeywords.class);
			session.put("searchCpsKeywords", getText("web.searchCpsKeywords") + "：" + outerSearchKeepData);
			if (cpsKeywords.size() > 0) {
				// 更新searchCount + 1
				CpsKeywords ckw = cpsKeywords.get(0);
				ckw.setSearchCount(ckw.getSearchCount() + 1);
				List<Object> saveList = new ArrayList<Object>();
				saveList.add(ckw);
				cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
			} else {
				CpsKeywords ckw = new CpsKeywords();
				Util.defaultPK(ckw);
				defaultValue(ckw);
				ckw.setKeywords(outerSearchKeepData);
				ckw.setInitSearchCount(0);
				ckw.setSearchCount(1);
				ckw.setRemark("");
				UserData2 userData2 = (UserData2) session.get("userData2");
				if (userData2 != null) {
					ckw.setOperator(userData2.getAccount().getUuid());
					ckw.setCreator(userData2.getAccount().getUuid());
				} else {
					ckw.setOperator("guest");
					ckw.setCreator("guest");
				}
				ckw.setEntitySysid(getCpsEntitySysid());
				cloudDao.save(sf(), new Object[] { ckw }, false, "INSERT");
			}
		}
	}

	public String medMenuSearch() throws Exception {
		showDataNums = 200;
		isNbsp = true;
		logger.debug("showDataNums:" + showDataNums);
		String rule = request.getParameter("searchRule");
		String keyWord = request.getParameter("outerSearchBarInput");
		String menuSysid = request.getParameter("menuSysid");
		String rootSysid = request.getParameter("rootSysid");

		logger.debug("==keyWord=" + keyWord);
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(keyWord)) {
			keyWord = keyWord.trim();
			setOuterSearchKeepData(keyWord);
		}

		MtsMenu rootMenu = getDataMtsMenuTable().get(rootSysid);
		String menuType = rootMenu.getMenuType();
		if (StringUtils.isNotBlank(menuType)) {
			String realSearchBy = menuSysid;
			if (StringUtils.isNotBlank(menuSysid)) {
				String[] menuSysidArr = menuSysid.split(",");
				// 同時有第三與第四層資料時，移除第三層
				List<String> serchMenuList = new ArrayList<>();
				for (String string : menuSysidArr)
					serchMenuList.add(string);

				List<String> serchParentList = new ArrayList<String>();
				for (String serchNodeSysid : serchMenuList) {
					MtsMenu node = getDataMtsMenuTable().get(serchNodeSysid);
					if (StringUtils.isNotBlank(node.getParentMtsMenuSysid()))
						if (serchMenuList.contains(node.getParentMtsMenuSysid())) {
							serchParentList.add(node.getParentMtsMenuSysid());
							logger.debug("remove:" + node.getParentMtsMenuSysid());
						}
				}
				serchMenuList.removeAll(serchParentList);

				realSearchBy = StringUtils.join(serchMenuList, ",");

				// 中文化
				String menuName = "";
				for (String serchMenuSysid : serchMenuList) {
					MtsMenu node = getDataMtsMenuTable().get(serchMenuSysid);
					if (StringUtils.isNotBlank(node.getName()))
						menuName += (node.getName() + "; ");
				}
				request.setAttribute("serchMenuList", getText("web.searchKeymenu") + "："
				// + "</br>"
						+ menuName);
			}

			Session session = sf().openSession();

			String methodName = "mts_" + menuType + "_Search";
			Method m = this.getClass().getMethod(methodName, Session.class, String.class, String.class);
			List<SearchResult> searchResultList = (List<SearchResult>) m.invoke(this, session, keyWord, realSearchBy);

			session.close();

			Map<String, Collection> menuListMap = new HashMap<String, Collection>();
			request.setAttribute("menuListMap", menuListMap);
			if (StringUtils.isNotBlank(realSearchBy) && allCount > 0) {
				String[] menuSysidArr = realSearchBy.split(",");
				if (menuSysidArr.length == 1) {
					String _menuSysid = menuSysidArr[0];
					for (SearchResult searchResult : searchResultList) {
						String fk = searchResult.getSysid();
						List<String> menuList = new ArrayList<String>();
						menuListMap.put(fk, menuList);
						menuList.add(_menuSysid);
					}
				} else {
					List<Map> l = (List<Map>) cloudDao.findProperty(sf(), MtsMenuLink.class, new QueryGroup(
							new QueryRule("menuSysid", IN, menuSysidArr)), new QueryOrder[] { new QueryOrder(FK),
							new QueryOrder("menuSysid") }, true, "menuSysid", FK);
					for (Map<String, String> map : l) {
						String fk = map.get(FK);
						String _menuSysid = map.get("menuSysid");
						Collection<String> menuList = menuListMap.get(fk);
						if (menuList == null) {
							menuList = new ArrayList<String>();
							menuListMap.put(fk, menuList);
						}
						menuList.add(_menuSysid);
					}

					Set<MenuSearchSort> reSortSet = new TreeSet<MenuSearchSort>();
					for (String key : menuListMap.keySet()) {
						Collection<String> menuList = menuListMap.get(key);
						reSortSet.add(new MenuSearchSort(key, menuList.size()));
					}
					Map<String, SearchResult> searchResultMap = new HashMap<String, SearchResult>();
					for (SearchResult searchResult : searchResultList)
						searchResultMap.put(searchResult.getSysid(), searchResult);
					searchResultList.clear();
					for (MenuSearchSort menuSearchSort : reSortSet) {
						SearchResult searchResult = searchResultMap.get(menuSearchSort.getKey());
						if (searchResult != null)
							searchResultList.add(0, searchResult);
					}
				}
			}
		}

		return SUCCESS;
	}

	public String bioMenuSearch() throws Exception {
		showDataNums = 200;
		isNbsp = true;
		logger.debug("showDataNums:" + showDataNums);
		String rule = request.getParameter("searchRule");
		String keyWord = request.getParameter("outerSearchBarInput");
		String menuSysid = request.getParameter("menuSysid");
		String rootSysid = request.getParameter("rootSysid");

		logger.debug("==keyWord=" + keyWord);
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(keyWord)) {
			keyWord = keyWord.trim();
			setOuterSearchKeepData(keyWord);
		}

		BhsMenu rootMenu = getDataBhsMenuTable().get(rootSysid);
		String menuType = rootMenu.getMenuType();
		if (StringUtils.isNotBlank(menuType)) {
			String realSearchBy = menuSysid;
			if (StringUtils.isNotBlank(menuSysid)) {
				String[] menuSysidArr = menuSysid.split(",");
				// 同時有第三與第四層資料時，移除第三層
				List<String> serchMenuList = new ArrayList<>();
				for (String string : menuSysidArr)
					serchMenuList.add(string);

				List<String> serchParentList = new ArrayList<String>();
				for (String serchNodeSysid : serchMenuList) {
					BhsMenu node = getDataBhsMenuTable().get(serchNodeSysid);
					if (StringUtils.isNotBlank(node.getParentBhsMenuSysid()))
						if (serchMenuList.contains(node.getParentBhsMenuSysid())) {
							serchParentList.add(node.getParentBhsMenuSysid());
							logger.debug("remove:" + node.getParentBhsMenuSysid());
						}
				}
				serchMenuList.removeAll(serchParentList);

				realSearchBy = StringUtils.join(serchMenuList, ",");

				// 中文化
				String menuName = "";
				for (String serchMenuSysid : serchMenuList) {
					BhsMenu node = getDataBhsMenuTable().get(serchMenuSysid);
					if (StringUtils.isNotBlank(node.getName()))
						menuName += (node.getName() + ";");
				}
				request.setAttribute("serchMenuList", getText("web.searchKeymenu") + "："
				// + "</br>"
						+ menuName);
			}

			Session session = sf().openSession();

			String methodName = "bhs_" + menuType + "_Search";
			Method m = this.getClass().getMethod(methodName, Session.class, String.class, String.class);
			List<SearchResult> searchResultList = (List<SearchResult>) m.invoke(this, session, keyWord, realSearchBy);

			session.close();

			Map<String, Collection> menuListMap = new HashMap<String, Collection>();
			request.setAttribute("menuListMap", menuListMap);
			if (StringUtils.isNotBlank(realSearchBy) && allCount > 0) {
				String[] menuSysidArr = realSearchBy.split(",");
				if (menuSysidArr.length == 1) {
					String _menuSysid = menuSysidArr[0];
					for (SearchResult searchResult : searchResultList) {
						String fk = searchResult.getSysid();
						List<String> menuList = new ArrayList<String>();
						menuListMap.put(fk, menuList);
						menuList.add(_menuSysid);
					}
				} else {
					List<Map> l = (List<Map>) cloudDao.findProperty(sf(), BhsMenuLink.class, new QueryGroup(
							new QueryRule("menuSysid", IN, menuSysidArr)), new QueryOrder[] { new QueryOrder(FK),
							new QueryOrder("menuSysid") }, true, "menuSysid", FK);
					for (Map<String, String> map : l) {
						String fk = map.get(FK);
						String _menuSysid = map.get("menuSysid");
						Collection<String> menuList = menuListMap.get(fk);
						if (menuList == null) {
							menuList = new ArrayList<String>();
							menuListMap.put(fk, menuList);
						}
						menuList.add(_menuSysid);
					}

					Set<MenuSearchSort> reSortSet = new TreeSet<MenuSearchSort>();
					for (String key : menuListMap.keySet()) {
						Collection<String> menuList = menuListMap.get(key);
						reSortSet.add(new MenuSearchSort(key, menuList.size()));
					}
					Map<String, SearchResult> searchResultMap = new HashMap<String, SearchResult>();
					for (SearchResult searchResult : searchResultList)
						searchResultMap.put(searchResult.getSysid(), searchResult);
					searchResultList.clear();
					for (MenuSearchSort menuSearchSort : reSortSet) {
						SearchResult searchResult = searchResultMap.get(menuSearchSort.getKey());
						if (searchResult != null)
							searchResultList.add(0, searchResult);
					}
				}
			}
		}

		return SUCCESS;
	}

	class MenuSearchSort implements Comparable<MenuSearchSort> {
		protected String key;
		protected Integer count;

		public MenuSearchSort(String key, Integer count) {
			this.key = key;
			this.count = count;
		}

		public final String getKey() {
			return key;
		}

		public final Integer getCount() {
			return count;
		}

		@Override
		public int compareTo(MenuSearchSort o) {
			int a = this.count.compareTo(o.count);
			if (a == 0)
				a = this.key.compareTo(o.key);
			return a;
		}
	}

	protected String pkInHql() {
		String pkInHql = " select distinct sourceSysid from " + multiLanClassName(getCookieLan());
		pkInHql += " where sourceTable=:sourceTable ";
		pkInHql += " and (columnValueString like :keyWord or columnValueText like :keyWord) ";
		return pkInHql;
	}

	protected String pkInBhsArticle() {
		String pkInBhsArticle = " select distinct sysid from " + BhsArticle.class.getSimpleName();
		pkInBhsArticle += " where bhsArticleType in ('A')";
		// pkInBhsArticle += " and (bhsArticleType=:A or bhsArticleType=:B) ";
		return pkInBhsArticle;
	}

	protected String vendorSysidInHql() {
		String vendorSysidInHql = " select distinct sysid from " + CpsVendor.class.getSimpleName();
		vendorSysidInHql += " where " + IS_ENABLED + "=true ";
		return vendorSysidInHql;
	}

	protected String pkInMtsMenu() {
		String hql = " select distinct " + FK + " from " + MtsMenuLink.class.getSimpleName();
		hql += " where menuSysid in (:menuSysid) ";
		return hql;
	}

	protected String pkInBhsMenu() {
		String hql = " select distinct " + FK + " from " + BhsMenuLink.class.getSimpleName();
		hql += " where menuSysid in (:menuSysid) ";
		return hql;
	}

	protected String hotTopicSysidInHql() {
		String vendorSysidInHql = " select distinct sysid from " + CpsHottopic.class.getSimpleName();
		vendorSysidInHql += " where " + IS_ENABLED + "=true ";
		return vendorSysidInHql;
	}

	protected String pkInCpsHottopicInfoMenu() {
		String hql = " select distinct " + FK + " from " + CpsHottopicInfo.class.getSimpleName();
		hql += " where hottopicSysid in (:hottopicSysid) ";
		return hql;
	}

	public List<SearchResult> mts_T_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = MtsVendorProfile.class;
		String subUrl = "pages2/MTS_FW_002?k=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " vendorName as vendorName, ";
		hql += " vendorProfileFull as vendorProfileFull, ";
		hql += " vendorImageSummary as vendorImageSummary ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		// hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInMtsMenu();
			hql += " ) ";
		}

		logger.debug("hql:" + hql);

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
				getText("web.mts.vendorProfile"), "vendorName", "vendorProfileFull", "vendorImageSummary");
		request.setAttribute("mtsSearchResultsVendorProfile", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> mts_S_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = MtsProducts.class;
		String subUrl = "pages2/MTS_FW_003?Type=S&mtsProductsSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " name as name, ";
		hql += " productsProfileFull as productsProfileFull, ";
		hql += " productsImageSummary1 as productsImageSummary1 ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInMtsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
				getText("web.mts.tm.medicalServices"), NAME, "productsProfileFull", "productsImageSummary1");
		request.setAttribute("mtsSearchResultsProducts", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> mts_I_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = MtsCooperation.class;
		String subUrl = "pages2/MTS_FW_003?Type=I&mtsProductsSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " cooperationName as cooperationName, ";
		hql += " cooperationProfileFull as cooperationProfileFull, ";
		hql += " cooperationSummaryImg as cooperationSummaryImg ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInMtsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
				getText("web.mts.interCollaboration"), "cooperationName", "cooperationProfileFull",
				"cooperationSummaryImg");
		request.setAttribute("mtsSearchResultsCooperation", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> mts_H_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = MtsHighlight.class;
		String subUrl = "pages2/MTS_FW_008?mtsHightlightSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " name as name, ";
		hql += " highlightProfileFull as highlightProfileFull, ";
		hql += " highlightSummaryImg as highlightSummaryImg ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInMtsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
				getText("web.mts.highlight"), NAME, "highlightProfileFull", "highlightSummaryImg");
		request.setAttribute("mtsSearchResultsHighlight", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> bhs_B_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = BhsVendorProfile.class;
		String subUrl = "pages2/BHS_FW_003?vendorSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " vendorName as vendorName, ";
		hql += " vendorProfileFull as vendorProfileFull, ";
		hql += " vendorImageSummary as vendorImageSummary ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		// hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInBhsMenu();
			hql += " ) ";
		}

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
				getText("web.bhs.vendorProfile"), "vendorName", "vendorProfileFull", "vendorImageSummary");
		request.setAttribute("bhsSearchResultsVendorProfile", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> bhs_P_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = BhsProducts.class;
		String subUrl = "pages2/BHS_FW_004?vendorSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " name as name, ";
		hql += " productsProfileFull as productsProfileFull, ";
		hql += " productsImageSummary1 as productsImageSummary1 ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInBhsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
				getText("web.bhs.bs.Products"), NAME, "productsProfileFull", "productsImageSummary1");
		request.setAttribute("bhsSearchResultsProducts", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> bhs_T_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = BhsTechnology.class;
		String subUrl = "pages2/BHS_FW_004?vendorSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " name as name, ";
		hql += " technologyProfileFull as technologyProfileFull, ";
		hql += " technologySummaryImg as technologySummaryImg ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInBhsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
				getText("web.bhs.bs.technology"), NAME, "technologyProfileFull", "technologySummaryImg");
		request.setAttribute("bhsSearchResultsTechnology", searchResultList);
		return searchResultList;
	}

	public List<SearchResult> bhs_H_Search(Session session, String keyWord, String menuSysid) {
		Class<?> clazz = BhsHighlight.class;
		String subUrl = "pages2/BHS_FW_012?hlSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " name as name, ";
		hql += " highlightProfileFull as highlightProfileFull, ";
		hql += " highlightSummaryImg as highlightSummaryImg ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and vendorSysid in ( ";
		hql += vendorSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(menuSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInBhsMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(menuSysid)) {
			query.setParameterList("menuSysid", menuSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
				getText("web.bhs.highlight"), NAME, "highlightProfileFull", "highlightSummaryImg");
		request.setAttribute("bhsSearchResultsHighlight", searchResultList);
		return searchResultList;
	}

	// HottopicInfo
	public List<SearchResult> hotTopic_I_Search(Session session, String keyWord, String hottopicSysid) {
		Class<?> clazz = CpsHottopicInfo.class;
		String subUrl = "pages2/HotTopicList?cpsHottopicSysid=";
		String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

		String hql = " select ";
		hql += PK + " as " + PK + " , ";
		hql += " hottopicTitle as hottopicTitle, ";
		hql += " hottopicSummary as hottopicSummary, ";
		hql += " hottopicFull as hottopicFull, ";
		hql += " hottopicImage as hottopicImage ";
		hql += " from " + clazz.getSimpleName();
		hql += " where " + PK + " in ( ";
		hql += pkInHql();
		hql += " ) ";
		hql += " and isEnabled=:isEnabled ";
		hql += " and hottopicSysid in ( ";
		hql += hotTopicSysidInHql();
		hql += " ) ";

		if (StringUtils.isNotBlank(hottopicSysid)) {
			hql += " and " + PK + " in ( ";
			hql += pkInCpsHottopicInfoMenu();
			hql += " ) ";
		}

		hql += " order by " + DATA_ORDER;

		Query query = session.createQuery(hql);
		query.setParameter("sourceTable", clazz.getSimpleName());
		query.setParameter("keyWord", "%" + keyWord + "%");
		if (hql.contains(":isEnabled"))
			query.setParameter("isEnabled", true);

		if (StringUtils.isNotBlank(hottopicSysid)) {
			query.setParameterList("hottopicSysid", hottopicSysid.split(","));
		}

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();

		List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "HotTopic",
				getText("web.node.hotTopic"), "hottopicTitle", "hottopicSummary", "hottopicImage");
		request.setAttribute("cpsSearchResultsHottopicInfo", searchResultList);
		return searchResultList;
	}

	protected void mtsSearch() {
		String rule = request.getParameter("searchRule");
		String keyWord = request.getParameter("outerSearchBarInput");

		logger.debug("==keyWord=" + keyWord);
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(keyWord)) {
			keyWord = keyWord.trim();
			setOuterSearchKeepData(keyWord);
		}

		Session session = sf().openSession();

		mts_T_Search(session, keyWord, "");
		mts_S_Search(session, keyWord, "");
		mts_I_Search(session, keyWord, "");
		mts_H_Search(session, keyWord, "");
		// {
		// Class<?> clazz = MtsArticle.class;
		// String subUrl = pagesUrl.get("mtsTheArticle") + "?articleSysid=";
		// String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName()
		// + "/";
		//
		// String hql = " select ";
		// hql += PK + " as " + PK + " , ";
		// hql += " articleTitle as articleTitle, ";
		// hql += " articleSummary as articleSummary, ";
		// hql += " articleImageSummary as articleImageSummary ";
		// hql += " from " + clazz.getSimpleName();
		// hql += " where " + PK + " in ( ";
		// hql += pkInHql();
		// hql += " ) ";
		// hql += " order by " + DATA_ORDER;
		//
		// Query query = session.createQuery(hql);
		// query.setParameter("sourceTable", clazz.getSimpleName());
		// query.setParameter("keyWord", "%" + keyWord + "%");
		//
		// query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		// List<Map> results = query.list();
		//
		//
		// List<SearchResult> searchResultList = getSearchResults3(keyWord,
		// results, clazz, subUrl, imgSubUrl, "MTS",
		// getText("web.mts.bc.hmn"), "articleTitle", "articleSummary",
		// "articleImageSummary");
		// request.setAttribute("mtsSearchResultsArticle", searchResultList);
		// }

		{
			Class<?> clazz = MtsArticle.class;
			String subUrl = pagesUrl.get("mtsTheArticle") + "?articleSysid=";
			String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

			String hqlA = " select ";
			hqlA += PK + " as " + PK + " , ";
			hqlA += " articleTitle as articleTitle, ";
			hqlA += " articleSummary as articleSummary, ";
			hqlA += " articleImageSummary as articleImageSummary, ";
			hqlA += " mtsArticleType as mtsArticleType ";
			hqlA += " from " + clazz.getSimpleName();
			hqlA += " where " + PK + " in ( ";
			hqlA += pkInHql();
			hqlA += " ) ";
			hqlA += " and mtsArticleType = 'A' ";
			hqlA += " order by " + DATA_ORDER;

			Query query = session.createQuery(hqlA);
			query.setParameter("sourceTable", clazz.getSimpleName());
			query.setParameter("keyWord", "%" + keyWord + "%");

			query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			List<Map> results = query.list();

			List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
					getText("web.mts.breakingNews"), "articleTitle", "articleSummary", "articleImageSummary");
			request.setAttribute("mtsSearchResultBreakingNewsList", searchResultList);
		}

		{
			Class<?> clazz = MtsArticle.class;
			String subUrl = pagesUrl.get("mtsTheArticle") + "?articleSysid=";
			String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

			String hqlB = " select ";
			hqlB += PK + " as " + PK + " , ";
			hqlB += " articleTitle as articleTitle, ";
			hqlB += " articleSummary as articleSummary, ";
			hqlB += " articleImageSummary as articleImageSummary, ";
			hqlB += " mtsArticleType as mtsArticleType ";
			hqlB += " from " + clazz.getSimpleName();
			hqlB += " where " + PK + " in ( ";
			hqlB += pkInHql();
			hqlB += " ) ";
			hqlB += " and mtsArticleType = 'B' ";
			hqlB += " order by " + DATA_ORDER;

			Query query = session.createQuery(hqlB);
			query.setParameter("sourceTable", clazz.getSimpleName());
			query.setParameter("keyWord", "%" + keyWord + "%");

			query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			List<Map> results = query.list();

			List<SearchResult> searchResultList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "MTS",
					getText("web.mts.taiwanInsight"), "articleTitle", "articleSummary", "articleImageSummary");
			request.setAttribute("mtsSearchResultTaiwanInsightList", searchResultList);
		}

		session.close();
	}

	protected void bhsSearch() {
		String rule = request.getParameter("searchRule");
		String keyWord = request.getParameter("outerSearchBarInput");

		logger.debug("==keyWord=" + keyWord);
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(keyWord)) {
			keyWord = keyWord.trim();
			setOuterSearchKeepData(keyWord);
		}

		Session session = sf().openSession();

		bhs_B_Search(session, keyWord, "");
		bhs_P_Search(session, keyWord, "");
		bhs_T_Search(session, keyWord, "");
		bhs_H_Search(session, keyWord, "");

		// {
		// Class<?> clazz = BhsArticle.class;
		// String subUrl = pagesUrl.get("bhsTheArticle") + "?articleSysid=";
		// String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName()
		// + "/";
		//
		// String hql = " select ";
		// hql += PK + " as " + PK + " , ";
		// hql += " articleTitle as articleTitle, ";
		// hql += " articleSummary as articleSummary, ";
		// hql += " articleImageSummary as articleImageSummary, ";
		// hql += " bhsArticleType as bhsArticleType ";
		// hql += " from " + clazz.getSimpleName();
		// hql += " where " + PK + " in ( ";
		// hql += pkInHql();
		// hql += " ) ";
		// hql += " order by " + DATA_ORDER;
		//
		// Query query = session.createQuery(hql);
		// query.setParameter("sourceTable", clazz.getSimpleName());
		// query.setParameter("keyWord", "%" + keyWord + "%");
		//
		// query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		// List<Map> results = query.list();
		// logger.debug("results:"+results);
		//
		// List<SearchResult> searchResultList = getSearchResults3(keyWord,
		// results, clazz, subUrl, imgSubUrl, "BHS",
		// getText("web.bhs.bc.in"), "articleTitle", "articleSummary",
		// "articleImageSummary");
		//
		// logger.debug("###searchResultList:"+searchResultList.size());
		// request.setAttribute("bhsSearchResultsArticle", searchResultList);
		//
		// }

		{
			/** 取得A=BreakingNews數量 */
			Class<?> clazz = BhsArticle.class;
			String subUrl = pagesUrl.get("bhsTheArticle") + "?articleSysid=";
			String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

			String hqlA = " select ";
			hqlA += PK + " as " + PK + " , ";
			hqlA += " articleTitle as articleTitle, ";
			hqlA += " articleSummary as articleSummary, ";
			hqlA += " articleImageSummary as articleImageSummary, ";
			hqlA += " bhsArticleType as bhsArticleType ";
			hqlA += " from " + clazz.getSimpleName();
			hqlA += " where " + PK + " in ( ";
			hqlA += pkInHql();
			hqlA += " ) ";
			hqlA += " and bhsArticleType = 'A' ";
			hqlA += " order by " + DATA_ORDER;

			Query queryA = session.createQuery(hqlA);
			queryA.setParameter("sourceTable", clazz.getSimpleName());
			queryA.setParameter("keyWord", "%" + keyWord + "%");

			queryA.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			List<Map> results = queryA.list();

			List<SearchResult> searchResultAList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
					getText("web.bhs.breakingNews"), "articleTitle", "articleSummary", "articleImageSummary");

			request.setAttribute("bhsSearchResultBreakingNewsList", searchResultAList);
		}

		{
			/** 取得B=TaiwanInsight數量 */
			Class<?> clazz = BhsArticle.class;
			String subUrl = pagesUrl.get("bhsTheArticle") + "?articleSysid=";
			String imgSubUrl = "/" + getWebDfImg() + "/" + clazz.getSimpleName() + "/";

			String hqlB = " select ";
			hqlB += PK + " as " + PK + " , ";
			hqlB += " articleTitle as articleTitle, ";
			hqlB += " articleSummary as articleSummary, ";
			hqlB += " articleImageSummary as articleImageSummary, ";
			hqlB += " bhsArticleType as bhsArticleType ";
			hqlB += " from " + clazz.getSimpleName();
			hqlB += " where " + PK + " in ( ";
			hqlB += pkInHql();
			hqlB += " ) ";
			hqlB += " and bhsArticleType = 'B' ";
			hqlB += " order by " + DATA_ORDER;

			Query queryB = session.createQuery(hqlB);
			queryB.setParameter("sourceTable", clazz.getSimpleName());
			queryB.setParameter("keyWord", "%" + keyWord + "%");

			queryB.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			List<Map> results = queryB.list();

			List<SearchResult> searchResultBList = getSearchResults3(keyWord, results, clazz, subUrl, imgSubUrl, "BHS",
					getText("web.bhs.taiwanInsight"), "articleTitle", "articleSummary", "articleImageSummary");

			request.setAttribute("bhsSearchResultTaiwanInsightList", searchResultBList);
		}

		session.close();
	}

	// HotTopic
	protected void hotTopicSearch() {
		String rule = request.getParameter("searchRule");
		String keyWord = request.getParameter("outerSearchBarInput");

		logger.debug("==keyWord=" + keyWord);
		if (StringUtils.isNotBlank(rule))
			setOuterSearchKeepCondition(rule);
		if (StringUtils.isNotBlank(keyWord)) {
			keyWord = keyWord.trim();
			setOuterSearchKeepData(keyWord);
		}

		Session session = sf().openSession();

		hotTopic_I_Search(session, keyWord, "");

		session.close();
	}

	public String execute() {
		mtsSearch();
		bhsSearch();
		hotTopicSearch();

		genKeywordCount();
		return SUCCESS;
	}

	public String mtsExecute() {
		mtsSearch();

		genKeywordCount();
		return SUCCESS;
	}

	public String bhsExecute() {
		bhsSearch();

		genKeywordCount();
		return SUCCESS;
	}

	public String hotTopicExecute() {
		hotTopicSearch();

		genKeywordCount();
		return SUCCESS;
	}

	protected List<SearchResult> getSearchResults3(String keyWord, List<Map> results, Class<?> clazz, String subUrl,
			String imgSubUrl, String countType, String countLabel, String nameKey, String summaryKey, String imageKey) {

		if (results.size() > 0) {
			String countMsg = getText("web.dataCount", new String[] { countLabel, String.valueOf(results.size()) });
			if ("MTS".equalsIgnoreCase(countType)) {
				mtsContentSearchTop += countMsg + "；   ";
				mtsCount = mtsCount + results.size();
				logger.debug("顯示KEY:" + summaryKey);
				if (summaryKey.equals("vendorProfileSummary")) {
					mtsVendorProfileCount = mtsVendorProfileCount + results.size();
				} else if (summaryKey.equals("productsProfileSummary")) {
					mtsProductsProfileCount = mtsProductsProfileCount + results.size();
				}
				// 分成BreakingNews和TaiwanInsight
				else if (summaryKey.equals("articleSummary")) {
					mtsArticleCount = mtsArticleCount + results.size();
					for (Map map : results) {
						String mtsArticleType = (String) map.get("mtsArticleType");
						logger.debug("mtsArticleType:" + mtsArticleType);
						if (mtsArticleType.equals("A")) {
							mtsArticleForBreakingNews = mtsArticleForBreakingNews + results.size();
						} else if (mtsArticleType.equals("B")) {
							mtsArticleForTaiwanInsight = mtsArticleForTaiwanInsight + results.size();
						}
					}

				} else if (summaryKey.equals("highlightSummary")) {
					mtsHighlightCount = mtsHighlightCount + results.size();
				} else if (summaryKey.equals("cooperationSummary")) {
					mtscooperationCount = mtscooperationCount + results.size();
				}

			} else if ("BHS".equalsIgnoreCase(countType)) {
				bhsContentSearchTop += countMsg + "；  ";
				logger.debug("countMsg:" + countMsg);
				logger.debug("bhsContentSearchTop:" + bhsContentSearchTop);

				bhsCount = bhsCount + results.size();
				logger.debug("bhsCount:" + bhsCount);

				logger.debug("顯示KEY:" + summaryKey);
				if (summaryKey.equals("vendorProfileSummary")) {
					bhsVendorProfileCount = bhsVendorProfileCount + results.size();
				} else if (summaryKey.equals("productsProfileSummary")) {
					bhsProductsProfileCount = bhsProductsProfileCount + results.size();
				} else if (summaryKey.equals("technologySummary")) {
					bhsTechnologyCount = bhsTechnologyCount + results.size();
				} else if (summaryKey.equals("highlightSummary")) {
					bhsHighlightCount = bhsHighlightCount + results.size();
				}
				// 分成BreakingNews和TaiwanInsight
				else if (summaryKey.equals("articleSummary")) {
					bhsArticleCount = bhsArticleCount + results.size();
					for (Map map : results) {
						String bhsArticleType = (String) map.get("bhsArticleType");
						logger.debug("bhsArticleType:" + bhsArticleType);
						if (bhsArticleType.equals("A")) {
							bhsArticleForBreakingNews = bhsArticleForBreakingNews + results.size();
						} else if (bhsArticleType.equals("B")) {
							bhsArticleForTaiwanInsight = bhsArticleForTaiwanInsight + results.size();
						}
					}
				}
			} else if ("HotTopic".equalsIgnoreCase(countType)) {
				hotTopicSearchTop += countMsg + "；  ";
				hotTopicCount = hotTopicCount + results.size();
				logger.debug("顯示KEY:" + summaryKey);
				if (summaryKey.equals("hottopicSummary")) {
					cpsHottopicInfoCount = cpsHottopicInfoCount + results.size();
				}
			}
			logger.debug("countType" + countType);
		}
		allCount = mtsCount + bhsCount + hotTopicCount;
		request.setAttribute("allCount", allCount);
		request.setAttribute("mtsCount", mtsCount);
		request.setAttribute("bhsCount", bhsCount);
		request.setAttribute("hotTopicCount", hotTopicCount);

		request.setAttribute("mtsVendorProfileCount", mtsVendorProfileCount);
		request.setAttribute("mtsProductsProfileCount", mtsProductsProfileCount);
		request.setAttribute("mtsHighlightCount", mtsHighlightCount);
		request.setAttribute("mtscooperationCount", mtscooperationCount);
		request.setAttribute("mtsArticleCount", mtsArticleCount);
		request.setAttribute("mtsArticleForBreakingNews", mtsArticleForBreakingNews);
		request.setAttribute("mtsArticleForTaiwanInsight", mtsArticleForTaiwanInsight);

		request.setAttribute("bhsVendorProfileCount", bhsVendorProfileCount);
		request.setAttribute("bhsProductsProfileCount", bhsProductsProfileCount);
		request.setAttribute("bhsHighlightCount", bhsHighlightCount);
		request.setAttribute("bhsTechnologyCount", bhsTechnologyCount);
		request.setAttribute("bhsArticleCount", bhsArticleCount);
		request.setAttribute("bhsArticleForBreakingNews", bhsArticleForBreakingNews);
		request.setAttribute("bhsArticleForTaiwanInsight", bhsArticleForTaiwanInsight);

		request.setAttribute("cpsHottopicInfoCount", cpsHottopicInfoCount);

		addMultiLan(results, sf(), clazz);

		List<SearchResult> l = new ArrayList<SearchResult>();
		for (Map<String, String> aResultMap : results) {
			// logger.debug("aResultMap:" + aResultMap);
			SearchResult sr = new SearchResult();
			sr.setSysid(aResultMap.get(PK));
			sr.setImageSummary(aResultMap.get(imageKey));
			sr.setNameOri(aResultMap.get(nameKey));

			sr.setSummaryOri(aResultMap.get(summaryKey));

			//當關鍵字不在簡介時嘗試取其他欄位
			if (StringUtils.isNotBlank(keyWord) && !StringUtils.containsIgnoreCase(sr.getSummaryOri(), keyWord))
				for (String key : aResultMap.keySet()) {
					if (StringUtils.equals(key, nameKey))
						continue;
					String value = aResultMap.get(key);
					if (StringUtils.containsIgnoreCase(value, keyWord)) {
						// logger.debug("!!" + key + " " + value);
						sr.setSummaryOri(value);
						break;
					}
				}

			l.add(sr);
		}

		for (SearchResult sr : l) {
			// 標題字串
			String showTitleText = sr.getNameOri();

			// 取摘要呈現字串
			String summaryText = sr.getSummaryOri();
			String showSummaryString = getShowSearchResultText(summaryText, keyWord);

			String url = subUrl + sr.getSysid();
			String imgUrl = request.getContextPath() + "/images/blank.jpg";
			if (StringUtils.isNotBlank(sr.getImageSummary()))
				imgUrl = imgSubUrl + sr.getSysid() + "/" + sr.getImageSummary();

			sr.setName(showTitleText);
			sr.setSummary(showSummaryString);
			sr.setUrl(url);
			sr.setImgUrl(imgUrl);
		}
		return l;
	}

	/**
	 * 搜尋 tableName
	 * 
	 * @param keyWord
	 *            關鍵字
	 * @param searchSql
	 *            searchSql
	 * @return
	 */
	@Deprecated
	protected Collection<Map<String, String>> getSearchResults2(String keyWord, String searchSql, String subUrl,
			String imgSubUrl, String countType, String countLabel, String param) {
		Collection<Map<String, String>> target = new ArrayList<Map<String, String>>();

		// 先取得關鍵字位置
		Session session = sf().openSession();
		Query query = session.createSQLQuery(searchSql.toString());
		query.setParameter("keyWord", "%" + keyWord + "%");

		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map> results = query.list();
		session.close();

		if (results.size() > 0)
			if ("MTS".equalsIgnoreCase(countType)) {
				mtsContentSearchTop += " " + countLabel + results.size() + "筆 ";

			} else if ("BHS".equalsIgnoreCase(countType)) {
				bhsContentSearchTop += " " + countLabel + results.size() + "筆 ";

			} else if ("HotTopic".equalsIgnoreCase(countType)) {
				hotTopicSearchTop += " " + countLabel + results.size() + "筆 ";
			}
		for (Map aResultMap : results) {
			// 標題字串
			String showTitleText = aResultMap.get("name") == null ? "" : aResultMap.get("name").toString();
			// showTitleText = showTitleText.replaceAll(keyWord,
			// "<span style='color:red;font-weight:bold;'>" + keyWord
			// + "</span>");

			// 取摘要呈現字串
			String summaryText = aResultMap.get("summary") != null ? aResultMap.get("summary").toString() : "";
			String showSummaryString = getShowSearchResultText(summaryText, keyWord);

			// 取全文呈現字串
			// String fullText = aResultMap.get("full").toString();
			// String showFullString = getShowSearchResultText(fullText,
			// keyWord);

			// url
			String sysid = aResultMap.get("SYSID").toString();
			String imageSummary = aResultMap.get("imageSummary") == null ? "" : aResultMap.get("imageSummary")
					.toString();
			String url = subUrl + sysid;
			String imgUrl = imgSubUrl + sysid + "/" + imageSummary;
			// url第2組參數使用
			if (param != null) {
				url += "&" + param + "=" + aResultMap.get(param);
			}
			Map<String, String> aShowDataMap = new HashMap();
			aShowDataMap.put("name", showTitleText);// 名稱
			aShowDataMap.put("summary", showSummaryString);// 摘要
			// aShowDataMap.put("full", showFullString);// 全文
			aShowDataMap.put("sysid", aResultMap.get("SYSID").toString());// 全文
			aShowDataMap.put("url", url);// 超連結url
			aShowDataMap.put("imgUrl", imgUrl);// 摘要圖片路徑
			target.add(aShowDataMap);
		}
		return target;
	}

	/**
	 * <ul>
	 * <li>去tag</li>
	 * <li>拮取關鍵字前後一段文字作為呈現字串</li>
	 * <li>將關鍵字變紅色</li>
	 * </ul>
	 * 
	 * @param text
	 *            被搜尋的全文
	 * @param keyWord
	 *            關鍵字
	 * @return
	 */
	protected String getShowSearchResultText(String text, String keyWord) {
		String showText = "";
		text = Util.replaceAllTag(text);

		// logger.debug("===after replace===");

		// logger.debug("text="+text);
		// int keyWordNo = text.indexOf(keyWord);// 關鍵字位置
		int keyWordNo = StringUtils.indexOfIgnoreCase(text, keyWord);
		// logger.debug("keyWordNo=" + keyWordNo);

		if (keyWordNo < 0) {
			// 無關鍵字則從頭拮取片斷
			showText = text.length() > (showDataNums * 2) ? text.substring(0, showDataNums * 2) : text;
		} else {
			// 有關鍵字

			// 算出呈現字串起迄位置
			int startNo = 0;
			int endBufferNo = 0;// 前半段文字不足showDataNums時，把剩下的字額加在後半段文字
			if ((keyWordNo - showDataNums) > 0) {
				startNo = keyWordNo - showDataNums;
			} else {
				endBufferNo = showDataNums - keyWordNo;
			}

			int endNo = text.length();
			int startBufferNo = 0;// 後半段文字不足showDataNums時，把剩下的字額加在前半段文字
			if ((keyWordNo + showDataNums) > text.length()) {
				startBufferNo = (keyWordNo + showDataNums) - text.length();
			} else {
				endNo = keyWordNo + showDataNums;
			}

			// logger.debug("startNo=" + startNo);
			// logger.debug("startBufferNo=" + startBufferNo);
			// logger.debug("endNo=" + endNo);
			// logger.debug("endBufferNo=" + endBufferNo);

			// 處理加上buffer字額範圍
			int startIndex = (startNo - startBufferNo) < 0 ? 0 : (startNo - startBufferNo);
			int endIndex = (endNo + endBufferNo) > text.length() ? text.length() : (endNo + endBufferNo);

			// 平移至句子開始
			// logger.debug("text:" + text);
			while (startIndex > 0) {
				// logger.debug("showText:" + text.substring(startIndex,
				// endIndex));
				if (ArrayUtils.contains(wordSplitHead, text.charAt(startIndex - 1)))
					break;
				startIndex--;
				endIndex--;
			}
			// logger.debug("startIndex=" + startIndex);
			// logger.debug("endIndex=" + endIndex);

			// 延長至句子結束
			while (endIndex < text.length()) {
				// logger.debug("showText:" + text.substring(startIndex,
				// endIndex));
				if (ArrayUtils.contains(wordSplitTail, text.charAt(endIndex)))
					break;
				endIndex++;
			}

			logger.debug("endIndex=" + endIndex);
			showText = text.substring(startIndex, endIndex);

			int l = showText.length();

			if (StringUtils.isNotBlank(keyWord))
				showText = keywordRed(showText, keyWord);

			if (isNbsp) {
				// menu的時候 延長至showDataNums*2
				int max = showDataNums * 2;
				StringBuilder sb = new StringBuilder(showText);
				for (; l < max; l++) {
					int d = l % 10;
					if (d == 0)
						sb.append(" ");
					else
						sb.append("&nbsp;");
				}
				// logger.debug("max:" + max);
				showText = sb.toString();
			}
			// logger.debug("showText=" + showText);
		}
		return showText;
	}

	/**
	 * <ul>
	 * <li>將關鍵字變紅色(不區分大小寫)</li>
	 * </ul>
	 * 
	 * @param text
	 *            被搜尋的全文
	 * @param keyWord
	 *            關鍵字
	 * @return
	 */
	protected String keywordRed(String text, String keyWord) {
		Pattern p = Pattern.compile(keyWord, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find())
			m.appendReplacement(sb, "<span style='color:red;font-weight:bold;'>" + keyWord + "</span>");
		m.appendTail(sb);// 添加尾巴
		return sb.toString();
	}
}