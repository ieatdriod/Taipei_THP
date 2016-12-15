package tw.com.mitac.thp.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsArticle;

/** MTS_FW_007_醫療新知列表 */
public class FrontMtsArticleAction extends BasisTenancyAction {
	protected MtsArticle bean;

	public MtsArticle getBean() {
		return bean;
	}

	public void setBean(MtsArticle bean) {
		this.bean = bean;
	}

	protected String articleTypeSysid;
	protected String articleSysid;

	public final String getArticleTypeSysid() {
		return articleTypeSysid;
	}

	public final void setArticleTypeSysid(String articleTypeSysid) {
		this.articleTypeSysid = articleTypeSysid;
	}

	public final String getArticleSysid() {
		return articleSysid;
	}

	public final void setArticleSysid(String articleSysid) {
		this.articleSysid = articleSysid;
	}

	private static final String ITEM_OUTER_MAIN_SHOW_NUMBER = "10"; // ResourceBundle.getBundle("OuterRuleSetting").getString("itemOuterMainShowNumber");
	private String showItemPage = "1";

	public String getShowItemPage() {
		return showItemPage;
	}

	public void setShowItemPage(String showItemPage) {
		this.showItemPage = showItemPage;
	}

	/** 醫療新知頁面 */
	public String outerMain() {
		QueryGroup groupItem = new QueryGroup(AND,
				new QueryRule[] { new QueryRule("articleTypeSysid", articleTypeSysid) }, null);
		int dataCounter = cloudDao.queryCount(sf(), MtsArticle.class, groupItem);
		System.out.println("==dataCounter=" + dataCounter);
		int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		List<Integer> pagesNumList = new ArrayList<Integer>();
		for (int i = 0; i < dataCounter / length; i++)
			pagesNumList.add(i + 1);
		if (pagesNumList.size() > 0 && dataCounter % length > 0)// 餘數加一
			pagesNumList.add(pagesNumList.get(pagesNumList.size() - 1) + 1);

		sessionSet("showItemPagesList", pagesNumList);// 先塞預設值，如此可處理總頁數小於限制頁數的狀況
		// logger.debug("測試pagesNumList.size():" + pagesNumList.size());
		int nowShowItemPage = Integer.parseInt(getShowItemPage());
		Integer validatePagesNum = 1;
		if (pagesNumList.size() > 0)
			validatePagesNum = pagesNumList.get(pagesNumList.size() - 1);
		if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
			nowShowItemPage = validatePagesNum;
		else if (nowShowItemPage < 0)// 檢核超過總頁數的情況
			nowShowItemPage = 1;
		setShowItemPage("" + nowShowItemPage);
		logger.debug("測試 nowShowItemPage:" + nowShowItemPage);
		int pagesNumberListSize = 10;// 頁面上顯示的最大頁數數量
		if (pagesNumList.size() > pagesNumberListSize) {// 當資料總頁數大於頁面可顯示總頁數時
			Map<Integer, List<Integer>> numbersMap = new LinkedHashMap<Integer, List<Integer>>();
			int i = 1;
			int key = 0;
			List<Integer> tempNumList = new ArrayList<Integer>();
			for (Integer num : pagesNumList) {
				if (i <= pagesNumberListSize) {
					tempNumList.add(num);
				} else {
					numbersMap.put(key, tempNumList);
					key++;
					tempNumList = new ArrayList<Integer>();
					i = 1;
					tempNumList.add(num);
				}
				i++;
			}
			numbersMap.put(key, tempNumList);
			Integer targetKey = nowShowItemPage / pagesNumberListSize;
			if (nowShowItemPage % pagesNumberListSize == 0)
				targetKey -= 1;
			sessionSet("showItemPagesList", numbersMap.get(targetKey));
			System.out.println("==showItemPagesList=" + numbersMap.get(targetKey));
		}
		if (pagesNumList.size() > 0)
			sessionSet("latestPage", pagesNumList.get(pagesNumList.size() - 1));

		int from = length * (nowShowItemPage - 1);

		List<MtsArticle> itemList = cloudDao.queryTable(sf(), MtsArticle.class, groupItem,
				new QueryOrder[] { new QueryOrder(PK, DESC) }, from, length);
		Map<String, MtsArticle> targetMap = new LinkedHashMap<String, MtsArticle>();
		Map<String, BigDecimal> comboQuMap = new LinkedHashMap<String, BigDecimal>();
		for (MtsArticle itemBean : itemList) {
			targetMap.put(itemBean.getSysid(), itemBean);
		}
		sessionSet("showItemDataMap", targetMap);
		System.out.println("==targetMap=" + targetMap.toString());

		return "outerMain";
	}

	/**
	 * @param articleSysid
	 * @param articleTypeSysid
	 * @return
	 */
	public String outerItemSingle() {
		if (StringUtils.isNotBlank(articleSysid)) {
			bean = cloudDao.get(sf(), MtsArticle.class, articleSysid);
			if (bean != null) {
				addMultiLan(new Object[] { bean }, sf(), MtsArticle.class);
				articleTypeSysid = bean.getMtsArticleType();
			}
		}

		if (bean == null) {
			// 無指定articleSysid，則取該分類最新一篇文章
			QueryGroup queryGroup = QueryGroup.DEFAULT;
			if (StringUtils.isNotBlank(articleTypeSysid))
				queryGroup = new QueryGroup(new QueryRule("mtsArticleType", articleTypeSysid));
			List<MtsArticle> articleList = cloudDao.queryTable(sf(), MtsArticle.class, queryGroup,
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, 0, 1);
			addMultiLan(articleList, sf(), MtsArticle.class);

			if (articleList.size() > 0)
				bean = articleList.get(0);
		}
		return execute(bean);
	}

	protected String execute(MtsArticle bean) {
		if (bean != null) {
			articleTypeSysid = StringUtils.defaultString(bean.getMtsArticleType());
			articleSysid = bean.getSysid();
		}

		// --------------同分類5篇簡介區------------------------
		List<MtsArticle> mtsArticleList = findMtsArticleList(articleTypeSysid, articleSysid);
		request.setAttribute("mtsArticleList", mtsArticleList);

		request.setAttribute("mtsArticleTypeName", getConstantMenu().get("mtsArticleType").get(articleTypeSysid));

		List<String> rankList = createRankList(MtsArticle.class, "", articleSysid);
		// request.setAttribute("rankList", rankList);
		List<MtsArticle> otherDataList = cloudDao.queryTable(sf(), MtsArticle.class, new QueryGroup(new QueryRule(PK,
				IN, rankList)), null, null, null);
		addMultiLan(otherDataList, sf(), MtsArticle.class);
		List<MtsArticle> rankDataList = new ArrayList<MtsArticle>();
		for (String pk : rankList) {
			for (int i = 0; i < otherDataList.size(); i++) {
				if (StringUtils.equals(pk, otherDataList.get(i).getSysid())) {
					rankDataList.add(otherDataList.remove(i));
					break;
				}
			}
		}
		request.setAttribute("rankDataList", rankDataList);
		return SUCCESS;
	}

	/**
	 * 醫療新知分類預覽畫面用
	 */
	public String viewItemSingle() {
		return execute(bean);
	}

	/**
	 * <pre>
	 * 醫療新知簡介
	 * </pre>
	 * 
	 * @param articleTypeSysid
	 * @param NE_articleSysid
	 * @return
	 */
	public List<MtsArticle> findMtsArticleList(String articleTypeSysid, String NE_articleSysid) {
		List<MtsArticle> mtsArticleList = cloudDao.queryTable(sf(), MtsArticle.class, new QueryGroup(new QueryRule[] {
				new QueryRule("mtsArticleType", articleTypeSysid), new QueryRule(PK, NE, NE_articleSysid) }),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		addMultiLan(mtsArticleList, sf(), MtsArticle.class);
		return mtsArticleList;
	}

	public String newsBoxInfo() {
		resultList = new ArrayList();
		if (StringUtils.isBlank(articleTypeSysid)) {
			articleTypeSysid = "A";
		}

		if (StringUtils.isNotBlank(articleTypeSysid)) {
			// --------------醫療新知簡介區------------------------
			try {
				int from = 0, length = 8;
				try {
					String rows = request.getParameter("rows");
					String pageStr = request.getParameter("page");
					length = Integer.parseInt(rows);
					int page = Integer.parseInt(pageStr);
					from = length * (page - 1);
				} catch (Exception e) {
					from = 0;
					length = 8;
				}

				List<MtsArticle> mtsArticleList = cloudDao.queryTable(sf(), MtsArticle.class, new QueryGroup(
						new QueryRule("mtsArticleType", articleTypeSysid), new QueryRule(IS_ENABLED, true)),
						new QueryOrder[] { new QueryOrder(DATA_ORDER), new QueryOrder(PK) }, from, length);
				addMultiLan(mtsArticleList, sf(), MtsArticle.class);
				resultList = mtsArticleList;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JSON_RESULT;
	}
}