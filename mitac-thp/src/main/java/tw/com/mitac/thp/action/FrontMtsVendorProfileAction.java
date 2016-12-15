package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsMenu;
import tw.com.mitac.thp.bean.MtsMenuLink;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

public class FrontMtsVendorProfileAction extends BasisTenancyAction {
	private static final String ITEM_OUTER_MAIN_SHOW_NUMBER = "10";
	private String showItemPage = "1";
	private String recommandType = "";

	public String getRecommandType() {
		// System.out.println("recommandType2=" + recommandType);
		return recommandType;
	}

	public void setRecommandType(String recommandType) {
		// System.out.println("recommandType1=" + recommandType);
		this.recommandType = recommandType;
	}

	public String getShowItemPage() {
		return showItemPage;
	}

	public void setShowItemPage(String showItemPage) {
		this.showItemPage = showItemPage;
	}

	protected MtsVendorProfile bean;

	public MtsVendorProfile getBean() {
		return bean;
	}

	public void setBean(MtsVendorProfile bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	/** 從選單進入 */
	/** 醫療團隊：T、特色服務：S、國際合作：I */
	public String outerMain() {
		String menuSysid = request.getParameter("menuSysid");
		String menuType = request.getParameter("menuType");

		// 取出vendor_sysid 或 product_sysid
		List<MtsMenuLink> mtsMenuLinkList = new ArrayList<MtsMenuLink>();

		if (StringUtils.isNotBlank(menuSysid)) {
			mtsMenuLinkList = cloudDao.queryTable(sf(), MtsMenuLink.class, new QueryGroup(new QueryRule("menuSysid",
					menuSysid)), new QueryOrder[0], null, null);
			addMultiLan(mtsMenuLinkList, sf(), MtsMenuLink.class);
		}
		String keySysid = menuSysid;
		// int idx = 0;
		List<String> mtsMenuList = new ArrayList<String>();
		while (StringUtils.isNotBlank(keySysid)) {
			MtsMenu mtsMenu = cloudDao.get(sf(), MtsMenu.class, keySysid);
			addMultiLan(new Object[] { mtsMenu }, sf(), MtsMenu.class);
			if (mtsMenu == null)
				keySysid = "";
			else {
				keySysid = mtsMenu.getParentMtsMenuSysid();
				mtsMenuList.add(0, mtsMenu.getName());
			}
			// idx++;
		}
		request.setAttribute("mtsMenuList", mtsMenuList);

		LinkedHashMap<String, Map<String, String>> targetMap = new LinkedHashMap<String, Map<String, String>>();

		switch (menuType) {
		case "S":

			List<String> findProductsSysidList = new ArrayList<String>();
			for (MtsMenuLink mp : mtsMenuLinkList) {
				logger.debug("@@@findint-S=" + mp.getParentSysid().toString().indexOf("Products"));

				if (mp.getParentSysid().toString().indexOf("Products") > 0) {
					findProductsSysidList.add(mp.getParentSysid().toString());
				}
			}

			if (findProductsSysidList.size() > 0) {
				List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
						new QueryRule(PK, IN, findProductsSysidList)), new QueryOrder[0], null, null);
				addMultiLan(mtsProductsList, sf(), MtsProducts.class);

				if (mtsProductsList.size() > 0) {
					for (int i = 0; i < mtsProductsList.size(); i++) {
						Map<String, String> productsMap = new HashMap();
						productsMap.put("name", mtsProductsList.get(i).getName());
						productsMap.put("vendorSysid", mtsProductsList.get(i).getSysid()); // vendorSysid需要調整
						String imageSummary = mtsProductsList.get(i).getProductsImageSummary1() == null ? ""
								: mtsProductsList.get(i).getProductsImageSummary1();
						productsMap.put("imageSummary", imageSummary);
						productsMap.put("summary", mtsProductsList.get(i).getProductsProfileSummary());
						productsMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/MTS_FW_003?Type=S&mtsProductsSysid="
								+ mtsProductsList.get(i).getSysid();

						String imgSubUrl = "/" + getWebDfImg() + "/" + MtsProducts.class.getSimpleName() + "/"
								+ mtsProductsList.get(i).getSysid() + "/" + imageSummary;

						productsMap.put("subUrl", subUrl);
						productsMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(mtsProductsList.get(i).getSysid(), productsMap);
					}
				}
			}

			break;

		case "T":

			String sourceSysid = "";
			for (MtsMenuLink mp : mtsMenuLinkList) {
				if (mp.getParentSysid().toString().indexOf("VendorProfile") > 0) {
					if (sourceSysid == "") {
						sourceSysid = "'" + mp.getParentSysid() + "'";
					} else {
						sourceSysid += ",'" + mp.getParentSysid() + "'";
					}
				}
			}

			if (!"".equals(sourceSysid)) {
				StringBuffer sb = new StringBuffer();
				Session session = sf().openSession();
				Query query = null;

				// 團隊
				sb.append("SELECT {a.*}, {b.*}").append(" FROM mts_vendor_profile a , cps_vendor b")
						.append(" where 1=1").append(" and a.vendor_sysid = b.sysid").append(" and a.sysid in (")
						.append(sourceSysid).append(")");

				query = session.createSQLQuery(sb.toString()).addEntity("a", MtsVendorProfile.class)
						.addEntity("b", CpsVendor.class);

				List<?> list = query.list();
				session.close(); // 用完記得關閉

				if (list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == null) {
							break;
						}
						Object[] oArr = (Object[]) list.get(i);

						MtsVendorProfile mv = (MtsVendorProfile) oArr[0];
						CpsVendor cv = (CpsVendor) oArr[1];
						addMultiLan(new Object[] { mv }, sf(), MtsVendorProfile.class);
						addMultiLan(new Object[] { cv }, sf(), CpsVendor.class);

						Map<String, String> vendorMap = new HashMap();
						vendorMap.put("name", cv.getName());
						vendorMap.put("vendorSysid", cv.getSysid());
						//vendorMap.put("summary", mv.getVendorProfileSummary());						
						vendorMap.put("summary", mv.getVendorProfileFull());
						String imageSummary = mv.getVendorImageSummary() == null ? "" : mv.getVendorImageSummary();
						vendorMap.put("imageSummary", imageSummary);
						vendorMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/MTS_FW_002?mtsVendorSysid=" + cv.getSysid();
						String imgSubUrl = "/" + getWebDfImg() + "/" + MtsVendorProfile.class.getSimpleName() + "/"
								+ mv.getSysid() + "/" + imageSummary;

						vendorMap.put("subUrl", subUrl);
						vendorMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(mv.getSysid(), vendorMap);
					}
				}

			}

			// List<String> findVendorSysidList = new ArrayList<String>();
			// for (MtsMenuLink mp : mtsMenuLinkList) {
			// logger.debug("@@@findint-T=" +
			// mp.getParentSysid().toString().indexOf("VendorProfile"));
			//
			// if (mp.getParentSysid().toString().indexOf("VendorProfile") > 0)
			// {
			// findVendorSysidList.add(mp.getParentSysid().toString());
			// }
			// }

			// if (findVendorSysidList.size() > 0) {
			// List<MtsVendorProfile> mtsVendorProfileList =
			// cloudDao.queryTable(sf(),
			// MtsVendorProfile.class,
			// new QueryGroup(new QueryRule(PK, IN, findVendorSysidList)),
			// new QueryOrder[0], null, null);
			//
			// if (mtsVendorProfileList.size() > 0) {
			// for(int i=0;i<mtsVendorProfileList.size();i++) {
			// Map<String, String> vendorMap = new HashMap();
			// vendorMap.put("name",
			// mtsVendorProfileList.get(i).getVendorName());
			// vendorMap.put("vendorSysid",
			// mtsVendorProfileList.get(i).getSysid());
			// String imageSummary =
			// mtsVendorProfileList.get(i).getVendorImageSummary() == null ? ""
			// : mtsVendorProfileList.get(i).getVendorImageSummary();
			// vendorMap.put("imageSummary",
			// mtsVendorProfileList.get(i).getVendorImageSummary() == null ? ""
			// : mtsVendorProfileList.get(i).getVendorImageSummary());
			// vendorMap.put("summary",
			// mtsVendorProfileList.get(i).getVendorProfileSummary());
			// vendorMap.put("menuType", menuType);
			//
			// //組url
			// String subUrl = "pages2/MTS_FW_002?mtsVendorSysid=" +
			// mtsVendorProfileList.get(i).getSysid();
			// String imgSubUrl = "/" + getWebDfImg() + "/" +
			// MtsVendorProfile.class.getSimpleName() + "/"
			// + mtsVendorProfileList.get(i).getSysid() + "/" + imageSummary;
			//
			// vendorMap.put("subUrl", subUrl);
			// vendorMap.put("imgSubUrl", imgSubUrl);
			//
			// targetMap.put(mtsVendorProfileList.get(i).getSysid(), vendorMap);
			// }
			// }
			// }

			break;

		case "I":

			List<String> findCooperationSysidList = new ArrayList<String>();
			for (MtsMenuLink mp : mtsMenuLinkList) {
				logger.debug("@@@findint-I=" + mp.getParentSysid().toString().indexOf("Cooperation"));

				if (mp.getParentSysid().toString().indexOf("Cooperation") > 0) {
					findCooperationSysidList.add(mp.getParentSysid().toString());
				}
			}

			if (findCooperationSysidList.size() > 0) {
				List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
						new QueryGroup(new QueryRule(PK, IN, findCooperationSysidList)), new QueryOrder[0], null, null);
				addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);

				if (mtsCooperationList.size() > 0) {
					for (int i = 0; i < mtsCooperationList.size(); i++) {
						Map<String, String> cooperationMap = new HashMap();
						cooperationMap.put("name", mtsCooperationList.get(i).getCooperationName());
						cooperationMap.put("vendorSysid", mtsCooperationList.get(i).getSysid());
						String imageSummary = mtsCooperationList.get(i).getCooperationSummaryImg() == null ? ""
								: mtsCooperationList.get(i).getCooperationSummaryImg();
						cooperationMap.put("imageSummary", imageSummary);
						cooperationMap.put("summary", mtsCooperationList.get(i).getCooperationSummary());
						cooperationMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/MTS_FW_003?Type=I&mtsProductsSysid="
								+ mtsCooperationList.get(i).getSysid();
						String imgSubUrl = "/" + getWebDfImg() + "/" + MtsCooperation.class.getSimpleName() + "/"
								+ mtsCooperationList.get(i).getSysid() + "/" + imageSummary;

						cooperationMap.put("subUrl", subUrl);
						cooperationMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(mtsCooperationList.get(i).getSysid(), cooperationMap);
					}
				}
			}

			break;
		}

		sessionSet("showItemDataMap", targetMap);

		// String sourceSysid = "";

		// for (MtsMenuLink mp : mtsMenuLinkList) {
		// if (sourceSysid == "") {
		// sourceSysid = "'" + mp.getParentSysid() + "'";
		// } else {
		// sourceSysid += ",'" + mp.getParentSysid() + "'";
		// }
		// }

		// LinkedHashMap<String, Map<String, String>> targetMap = new
		// LinkedHashMap<String, Map<String, String>>();
		// if (StringUtils.isNotBlank(sourceSysid)) {

		// StringBuffer sb = new StringBuffer();
		// Session session = sf().openSession();
		// Query query = null;

		// if ("V".equals(menuType)) {
		// // 團隊
		// sb.append("SELECT {a.*}, {b.*}").append("  FROM mts_vendor_profile a , cps_vendor b")
		// .append(" where 1=1 ").append("   and a.vendor_sysid = b.sysid").append("   and a.sysid in (")
		// .append(sourceSysid).append(")");
		//
		// query = session.createSQLQuery(sb.toString()).addEntity("a",
		// MtsVendorProfile.class)
		// .addEntity("b", CpsVendor.class);
		//
		// } else {
		// // 特色、案例、醫生
		// sb.append("SELECT *").append("  FROM mts_products").append(" where 1=1 ").append("   and sysid in (")
		// .append(sourceSysid).append(")");
		//
		// query = session.createSQLQuery(sb.toString());
		// }
		//
		// List<?> list = query.list();
		// session.close();
		// int dataCounter = list.size();
		//
		// int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		// int nowShowItemPage = Integer.parseInt(getShowItemPage());
		//
		// int from = length * (nowShowItemPage - 1);
		// int to = (from + length) > dataCounter ? dataCounter : (from +
		// length);
		//
		// for (int i = from; i < to; i++) {
		// if (list.get(i) == null) {
		// break;
		// }
		// Object[] oArr = (Object[]) list.get(i);
		//
		// if ("V".equals(menuType)) {
		// MtsVendorProfile h = (MtsVendorProfile) oArr[0];
		// CpsVendor it = (CpsVendor) oArr[1];
		//
		// Map<String, String> vendorMap = new HashMap();
		// vendorMap.put("vendorName", it.getName());
		// vendorMap.put("vendorSysid", h.getSysid());
		// vendorMap.put("vendorImageSummary",
		// h.getVendorImageSummary() == null ? "" : h.getVendorImageSummary());
		// vendorMap.put("menuType", menuType);
		//
		// targetMap.put(h.getSysid(), vendorMap);
		//
		// } else {
		//
		// Map<String, String> vendorMap = new HashMap();
		// //配合 Mts_products table schema 變更調整
		// vendorMap.put("vendorName", oArr[6].toString()); //PRODUCTS_NAME
		// vendorMap.put("vendorSysid", oArr[0].toString()); //Mts_products
		// sysid
		// vendorMap.put("vendorImageSummary", oArr[15] == null ? "" :
		// oArr[15].toString()); //PRODUCTS_IMAGE_SUMMARY_1
		// // vendorMap.put("vendorName", oArr[7].toString());
		// // vendorMap.put("vendorSysid", oArr[0].toString());
		// // vendorMap.put("vendorImageSummary", oArr[15] == null ? "" :
		// oArr[15].toString());
		// vendorMap.put("menuType", menuType);
		//
		// targetMap.put(oArr[0].toString(), vendorMap);
		// }
		// }
		//
		// // 總頁數
		// int pageSize = 0;
		// pageSize = dataCounter / length;
		// pageSize = dataCounter % length > 0 ? pageSize + 1 : pageSize;//
		// 有餘數就+1
		// logger.debug("pageSize=" + pageSize);
		// // mts分頁元件
		// FrontMtsCommonAction frontMtsCommonAction = new
		// FrontMtsCommonAction();
		// String pages = frontMtsCommonAction.subPageCss2(nowShowItemPage,
		// pageSize,
		// "showMtsVendorProfileMain?showItemPage=");
		//
		// sessionSet("pages", pages);
		//
		// }
		// sessionSet("showItemDataMap", targetMap);

		// StringBuffer sb = new StringBuffer();
		// Session session = sf().openSession();
		// Query query = null;
		//
		//
		// if ("V".equals(menuType)) {
		// //團隊
		// sb.append("SELECT {a.*}, {b.*}")
		// .append("  FROM mts_vendor_profile a , cps_vendor b")
		// .append(" where 1=1 ")
		// .append("   and a.vendor_sysid = b.sysid")
		// .append("   and a.sysid in (").append(sourceSysid).append(")");
		//
		// query = session.createSQLQuery(sb.toString()).addEntity("a",
		// MtsVendorProfile.class)
		// .addEntity("b", CpsVendor.class);
		//
		// } else {
		// //特色、案例、醫生
		// sb.append("SELECT *")
		// .append("  FROM mts_products")
		// .append(" where 1=1 ")
		// .append("   and sysid in (").append(sourceSysid).append(")");
		//
		// query = session.createSQLQuery(sb.toString());
		// }
		//
		// System.out.println("###-sb[" + sb + "]");
		//
		// List<?> list = query.list();
		// session.close();
		// int dataCounter = list.size();
		//
		// int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		// int nowShowItemPage = Integer.parseInt(getShowItemPage());
		//
		// //LinkedHashMap<String, Map<String, String>> targetMap = new
		// LinkedHashMap<String, Map<String, String>>();
		// int from = length * (nowShowItemPage - 1);
		// int to = (from + length) > dataCounter ? dataCounter : (from +
		// length);
		//
		// System.out.println("###-from[" + from + "],to[" + to + "]");
		//
		// for (int i = from; i < to; i++) {
		// if (list.get(i) == null) {
		// break;
		// }
		// Object[] oArr = (Object[]) list.get(i);
		//
		// if ("V".equals(menuType)) {
		// MtsVendorProfile h = (MtsVendorProfile) oArr[0];
		// CpsVendor it = (CpsVendor) oArr[1];
		//
		// Map<String, String> vendorMap = new HashMap();
		// vendorMap.put("vendorName", it.getName());
		// vendorMap.put("vendorSysid", h.getSysid());
		// vendorMap.put("vendorImageSummary", h.getVendorImageSummary() == null
		// ? "":h.getVendorImageSummary());
		//
		// targetMap.put(h.getSysid(), vendorMap);
		//
		// } else {
		//
		// // if (oArr[15] == null) {
		// //System.out.println("###-oArr[15] = null");
		// // oArr[15] = "";
		// // }
		// // if (oArr[10] == null) {
		// //System.out.println("###-oArr[10] = null");
		// // oArr[10] = "";
		// // }
		//
		//
		// Map<String, String> vendorMap = new HashMap();
		// vendorMap.put("vendorName", oArr[7].toString());
		// vendorMap.put("vendorSysid", oArr[0].toString());
		// vendorMap.put("vendorImageSummary", oArr[15] == null ?
		// "":oArr[15].toString());
		//
		// targetMap.put(oArr[0].toString(), vendorMap);
		//
		// }
		//
		//
		// }
		// sessionSet("showItemDataMap", targetMap);

		// // 總頁數
		// int pageSize = 0;
		// pageSize = dataCounter / length;
		// pageSize = dataCounter % length > 0 ? pageSize + 1 : pageSize;//
		// 有餘數就+1
		// logger.debug("pageSize=" + pageSize);
		// // mts分頁元件
		// FrontMtsCommonAction frontMtsCommonAction = new
		// FrontMtsCommonAction();
		// String pages = frontMtsCommonAction.subPageCss2(nowShowItemPage,
		// pageSize,
		// "showMtsVendorProfileMain?showItemPage=");
		//
		// sessionSet("pages", pages);
		return "outerMain";
	}

	// /** org */
	// /** 醫療團隊清單頁面 */
	// public String outerMain() {
	// String vendorSysid = request.getParameter("vendorSysid");
	//
	// String menuType = request.getParameter("menuType");
	//
	// List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
	//
	// StringBuffer sb = new StringBuffer();
	//
	// sb.append("SELECT {a.*}, {b.*}");
	// sb.append("  FROM mts_vendor_profile a , cps_vendor b");
	// sb.append(" where 1=1 ");
	// sb.append("   and a.vendor_sysid = b.sysid");
	//
	// Session session = sf().openSession();
	// Query query = session.createSQLQuery(sb.toString()).addEntity("a",
	// MtsVendorProfile.class)
	// .addEntity("b", CpsVendor.class);
	// List<?> list = query.list();
	// session.close();
	// int dataCounter = list.size();
	//
	// int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
	// int nowShowItemPage = Integer.parseInt(getShowItemPage());
	//
	// LinkedHashMap<String, Map<String, String>> targetMap = new
	// LinkedHashMap<String, Map<String, String>>();
	// int from = length * (nowShowItemPage - 1);
	// int to = (from + length) > dataCounter ? dataCounter : (from + length);
	// for (int i = from; i < to; i++) {
	// if (list.get(i) == null) {
	// break;
	// }
	// Object[] oArr = (Object[]) list.get(i);
	//
	// MtsVendorProfile h = (MtsVendorProfile) oArr[0];
	// CpsVendor it = (CpsVendor) oArr[1];
	//
	// Map<String, String> vendorMap = new HashMap();
	// vendorMap.put("vendorName", it.getName());
	// vendorMap.put("vendorSysid", h.getSysid());
	// vendorMap.put("vendorImageSummary", h.getVendorImageSummary());
	//
	// targetMap.put(h.getSysid(), vendorMap);
	// }
	// sessionSet("showItemDataMap", targetMap);
	//
	// // 總頁數
	// int pageSize = 0;
	// pageSize = dataCounter / length;
	// pageSize = dataCounter % length > 0 ? pageSize + 1 : pageSize;// 有餘數就+1
	// logger.debug("pageSize=" + pageSize);
	// // mts分頁元件
	// FrontMtsCommonAction frontMtsCommonAction = new FrontMtsCommonAction();
	// String pages = frontMtsCommonAction.subPageCss2(nowShowItemPage,
	// pageSize,
	// "showMtsVendorProfileMain?showItemPage=");
	//
	// sessionSet("pages", pages);
	// return "outerMain";
	// }

	/** 醫療團隊介紹頁面 */
	@Deprecated
	public String outerItemSingle() {
		// 從醫療新知頁面傳入
		String mtsVendorSysid = request.getParameter("cpsVendorSysid");
		// 醫療團隊清單頁面傳入
		String vendorSysid = request.getParameter("vendorSysid");

		// 從醫療新知頁面傳入的團隊代碼是cpsVendorSysid,
		// 故先撈出該cpsVendorSysid於mts_vendor_profile的sysid
		if (StringUtils.isNotBlank(mtsVendorSysid)) {

			QueryGroup groupItem = new QueryGroup(new QueryRule("vendorSysid", CN, mtsVendorSysid));

			List<MtsVendorProfile> itemList = cloudDao.queryTable(sf(), MtsVendorProfile.class, groupItem,
					new QueryOrder[] { new QueryOrder(PK, DESC) }, 0, 1);
			for (MtsVendorProfile itemBean : itemList) {
				vendorSysid = itemBean.getSysid();
			}

		}

		if (StringUtils.isBlank(vendorSysid)) {
			addActionError(getText("msg.itemLost"));
			return ERROR;
		} else {
			bean = cloudDao.get(sf(), MtsVendorProfile.class, vendorSysid);
			// 特色S/案例C/醫生D

			// --------------特色服務簡介區------------------------
			List<MtsProducts> mtsProductsListS = findMtsProductsList(bean, "S");// 特色S
			request.setAttribute("mtsProductsListS", mtsProductsListS);

			// --------------案例簡介區------------------------
			List<MtsProducts> mtsProductsListC = findMtsProductsList(bean, "C");// 案例C
			request.setAttribute("mtsProductsListC", mtsProductsListC);

			// --------------醫生簡介區------------------------
			List<MtsProducts> mtsProductsListD = findMtsProductsList(bean, "D");// 醫生D
			request.setAttribute("mtsProductsListD", mtsProductsListD);

			// --------------make appoint---------------------
			List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
					new QueryRule("vendorSysid", bean.getVendorSysid()), new QueryRule("productsType", "S")),
					new QueryOrder[0], null, null);
			Map<String, String> map = new LinkedHashMap<String, String>();
			for (MtsProducts mp : mtsProductsList)
				map.put(mp.getSysid(), mp.getName());
			treatmentMap = map;

			// --------------推薦團隊區------------------------
			LinkedHashMap<String, Map<String, String>> vendorProfileMap = findVendorProfile(bean);
			request.setAttribute("showItemDataMap", vendorProfileMap);

		}
		return SUCCESS;
	}

	// 其他人也關注(醫療團隊)
	public LinkedHashMap<String, Map<String, String>> findVendorProfile(MtsVendorProfile itemBean) {

		String sysid = itemBean.getSysid();
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT {a.*}, {b.*}");
		sb.append("  FROM mts_vendor_profile a , cps_vendor b");
		sb.append(" where 1=1 ");
		sb.append("   and a.vendor_sysid = b.sysid");
		sb.append("   and a.sysid <> :sysid");

		Session session = sf().openSession();
		Query query = session.createSQLQuery(sb.toString()).addEntity("a", MtsVendorProfile.class)
				.addEntity("b", CpsVendor.class);
		query.setParameter("sysid", sysid);
		List<?> list = query.list();
		session.close();

		LinkedHashMap<String, Map<String, String>> targetMap = new LinkedHashMap<String, Map<String, String>>();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {
				break;
			}
			Object[] oArr = (Object[]) list.get(i);

			MtsVendorProfile h = (MtsVendorProfile) oArr[0];
			CpsVendor it = (CpsVendor) oArr[1];

			Map<String, String> vendorMap = new HashMap();
			vendorMap.put("vendorName", it.getName());
			vendorMap.put("vendorSysid", h.getSysid());
			vendorMap.put("vendorImageSummary", h.getVendorImageSummary());

			targetMap.put(h.getSysid(), vendorMap);
		}
		// sessionSet("showItemDataMap", targetMap);

		return targetMap;
	}

	/**
	 * 團隊介紹頁面-特色服務/案例/醫生區
	 */
	public List<MtsProducts> findMtsProductsList(MtsVendorProfile itemBean, String productsType) {
		String vendorSysid = itemBean.getVendorSysid();
		List<MtsProducts> mtsProductsList = new ArrayList<MtsProducts>();// 將同一個團隊的特色服務找出來
		if (StringUtils.isNotBlank(itemBean.getVendorSysid())) {
			mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(new QueryRule("vendorSysid",
					vendorSysid), new QueryRule("productsType", productsType)), new QueryOrder[0], null, null);
		}

		return mtsProductsList;
	}
}