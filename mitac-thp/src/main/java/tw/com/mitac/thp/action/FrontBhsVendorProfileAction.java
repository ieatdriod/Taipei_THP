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
import tw.com.mitac.thp.bean.BhsMenuLink;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsProducts;

public class FrontBhsVendorProfileAction extends BasisTenancyAction {
	protected BhsVendorProfile bean;

	public BhsVendorProfile getBean() {
		return bean;
	}

	public void setBean(BhsVendorProfile bean) {
		this.bean = bean;
	}

	protected Map<String, String> orderTypeMap;

	public Map<String, String> getOrderTypeMap() {
		return orderTypeMap;
	}

	private static final String ITEM_OUTER_MAIN_SHOW_NUMBER = "3";
	private String showItemPage = "1";

	public String getShowItemPage() {
		return showItemPage;
	}

	public void setShowItemPage(String showItemPage) {
		this.showItemPage = showItemPage;
	}

	/** 企業清單頁面 */
	/** 企業：B、產品：P、技術：T */
	public String outerMain() {
		String menuSysid = request.getParameter("menuSysid");
		String menuType = request.getParameter("menuType");

		// 取出vendor_sysid 或 product_sysid
		List<BhsMenuLink> bhsMenuLinkList = new ArrayList<BhsMenuLink>();

		if (StringUtils.isNotBlank(menuSysid)) {
			bhsMenuLinkList = cloudDao.queryTable(sf(), BhsMenuLink.class, new QueryGroup(new QueryRule("menuSysid",
					menuSysid)), new QueryOrder[0], null, null);
		}

		LinkedHashMap<String, Map<String, String>> targetMap = new LinkedHashMap<String, Map<String, String>>();

		switch (menuType) {
		case "B":

			String sourceSysid = "";
			for (BhsMenuLink bp : bhsMenuLinkList) {
				if (bp.getParentSysid().toString().indexOf("VendorProfile") > 0) {
					if (sourceSysid == "") {
						sourceSysid = "'" + bp.getParentSysid() + "'";
					} else {
						sourceSysid += ",'" + bp.getParentSysid() + "'";
					}
				}
			}

			if (!"".equals(sourceSysid)) {
				StringBuffer sb = new StringBuffer();
				Session session = sf().openSession();
				Query query = null;

				// 團隊
				sb.append("SELECT {a.*}, {b.*}").append(" FROM bhs_vendor_profile a , cps_vendor b")
						.append(" where 1=1").append(" and a.vendor_sysid = b.sysid").append(" and a.sysid in (")
						.append(sourceSysid).append(")");

				query = session.createSQLQuery(sb.toString()).addEntity("a", BhsVendorProfile.class)
						.addEntity("b", CpsVendor.class);

				List<?> list = query.list();
				session.close(); // 用完記得關閉

				if (list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == null) {
							break;
						}
						Object[] oArr = (Object[]) list.get(i);

						BhsVendorProfile bv = (BhsVendorProfile) oArr[0];
						CpsVendor cv = (CpsVendor) oArr[1];

						Map<String, String> vendorMap = new HashMap();
						vendorMap.put("name", cv.getName());
						vendorMap.put("vendorSysid", cv.getSysid());
						// 目前前台顯示資源沒有
						// vendorMap.put("summary",
						// bv.getVendorProfileSummary());
						vendorMap.put("summary", bv.getVendorProfileFull());
						String imageSummary = bv.getVendorImageSummary() == null ? "" : bv.getVendorImageSummary();
						vendorMap.put("imageSummary", imageSummary);
						vendorMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/BHS_FW_003?recommandType=B&vendorSysid=" + cv.getSysid();
						String imgSubUrl = "/" + getWebDfImg() + "/" + BhsVendorProfile.class.getSimpleName() + "/"
								+ bv.getSysid() + "/" + imageSummary;

						vendorMap.put("subUrl", subUrl);
						vendorMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(bv.getSysid(), vendorMap);
					}
				}
			}

			break;

		case "P":

			List<String> findProductsSysidList = new ArrayList<String>();
			for (BhsMenuLink bp : bhsMenuLinkList) {
				// logger.debug("@@@findint-P=" +
				// bp.getParentSysid().toString().indexOf("Products"));

				if (bp.getParentSysid().toString().indexOf("Products") > 0) {
					findProductsSysidList.add(bp.getParentSysid().toString());
				}
			}

			if (findProductsSysidList.size() > 0) {
				List<BhsProducts> bhsProductsList = cloudDao.queryTable(sf(), BhsProducts.class, new QueryGroup(
						new QueryRule(PK, IN, findProductsSysidList)), new QueryOrder[0], null, null);

				if (bhsProductsList.size() > 0) {
					for (int i = 0; i < bhsProductsList.size(); i++) {
						Map<String, String> productsMap = new HashMap();
						productsMap.put("name", bhsProductsList.get(i).getName());
						productsMap.put("vendorSysid", bhsProductsList.get(i).getSysid());
						String imageSummary = bhsProductsList.get(i).getProductsImageSummary1() == null ? ""
								: bhsProductsList.get(i).getProductsImageSummary1();
						productsMap.put("imageSummary", imageSummary);
						productsMap.put("summary", bhsProductsList.get(i).getProductsProfileSummary());
						productsMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/BHS_FW_004?vendorSysid=" + bhsProductsList.get(i).getSysid();

						String imgSubUrl = "/" + getWebDfImg() + "/" + MtsProducts.class.getSimpleName() + "/"
								+ bhsProductsList.get(i).getSysid() + "/" + imageSummary;

						productsMap.put("subUrl", subUrl);
						productsMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(bhsProductsList.get(i).getSysid(), productsMap);
					}
				}
			}

			break;
		case "T":
			List<String> findTechnologySysidList = new ArrayList<String>();
			for (BhsMenuLink bp : bhsMenuLinkList) {
				// logger.debug("@@@findint-T=" +
				// bp.getParentSysid().toString().indexOf("Technology"));

				if (bp.getParentSysid().toString().indexOf("Technology") > 0) {
					findTechnologySysidList.add(bp.getParentSysid().toString());
				}
			}

			if (findTechnologySysidList.size() > 0) {
				List<BhsTechnology> bhsTechnologyList = cloudDao.queryTable(sf(), BhsTechnology.class, new QueryGroup(
						new QueryRule(PK, IN, findTechnologySysidList)), new QueryOrder[0], null, null);

				if (bhsTechnologyList.size() > 0) {
					for (int i = 0; i < bhsTechnologyList.size(); i++) {
						Map<String, String> technologyMap = new HashMap();
						technologyMap.put("name", bhsTechnologyList.get(i).getName());
						technologyMap.put("vendorSysid", bhsTechnologyList.get(i).getSysid());
						String imageSummary = bhsTechnologyList.get(i).getTechnologySummaryImg() == null ? ""
								: bhsTechnologyList.get(i).getTechnologySummaryImg();
						technologyMap.put("imageSummary", imageSummary);
						technologyMap.put("summary", bhsTechnologyList.get(i).getTechnologySummary());
						technologyMap.put("menuType", menuType);

						// 組url
						String subUrl = "pages2/BHS_FW_004?vendorSysid=" + bhsTechnologyList.get(i).getSysid();

						String imgSubUrl = "/" + getWebDfImg() + "/" + BhsTechnology.class.getSimpleName() + "/"
								+ bhsTechnologyList.get(i).getSysid() + "/" + imageSummary;

						technologyMap.put("subUrl", subUrl);
						technologyMap.put("imgSubUrl", imgSubUrl);

						targetMap.put(bhsTechnologyList.get(i).getSysid(), technologyMap);
					}
				}
			}

			break;
		}

		sessionSet("showItemDataMap", targetMap);

		return "outerMain";

		// String vendorSysid = request.getParameter("vendorSysid");
		// List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
		// // 企業
		// // if (StringUtils.isNotBlank(vendorSysid))
		// // queryRuleList.add(new QueryRule("vendorSysid", "eq",
		// vendorSysid));
		// // QueryGroup groupItem = new QueryGroup(
		// // AND,queryRuleList.toArray(new
		// // QueryRule[0]),null);
		// // int dataCounter = cloudDao.queryCount(sessionFactory,
		// // BhsVendorProfile.class, groupItem);
		//
		// StringBuffer sb = new StringBuffer();
		// // SELECT b.sysid,b.vendor_name
		// // FROM bhs_vendor_profile a,cps_vendor b
		// // where a.vendor_sysid = b.sysid
		// sb.append("SELECT {a.*}, {b.*}");
		// sb.append("  FROM bhs_vendor_profile a , cps_vendor b");
		// sb.append(" where 1=1 ");
		// sb.append("   and a.vendor_sysid = b.sysid");
		//
		// Session session = sf().openSession();
		// Query query = session.createSQLQuery(sb.toString()).addEntity("a",
		// BhsVendorProfile.class)
		// .addEntity("b", CpsVendor.class);
		// List<?> list = query.list();
		// session.close();
		// int dataCounter = list.size();
		//
		// int length = new Integer(ITEM_OUTER_MAIN_SHOW_NUMBER);// 每頁幾筆ITEM
		// List<Integer> pagesNumList = new ArrayList<Integer>();
		// for (int i = 0; i < dataCounter / length; i++)
		// pagesNumList.add(i + 1);
		// if (pagesNumList.size() > 0 && dataCounter % length > 0)// 餘數加一
		// pagesNumList.add(pagesNumList.get(pagesNumList.size() - 1) + 1);
		//
		// sessionSet("showItemPagesList", pagesNumList);//
		// 先塞預設值，如此可處理總頁數小於限制頁數的狀況
		// // logger.debug("測試pagesNumList.size():" + pagesNumList.size());
		// int nowShowItemPage = Integer.parseInt(getShowItemPage());
		// Integer validatePagesNum = 1;
		// if (pagesNumList.size() > 0)
		// validatePagesNum = pagesNumList.get(pagesNumList.size() - 1);
		// if (nowShowItemPage > validatePagesNum)// 檢核超過總頁數的情況
		// nowShowItemPage = validatePagesNum;
		// else if (nowShowItemPage < 0)// 檢核超過總頁數的情況
		// nowShowItemPage = 1;
		// setShowItemPage("" + nowShowItemPage);
		// logger.debug("測試 nowShowItemPage:" + nowShowItemPage);
		// int pagesNumberListSize = 10;// 頁面上顯示的最大頁數數量
		// if (pagesNumList.size() > pagesNumberListSize) {// 當資料總頁數大於頁面可顯示總頁數時
		// Map<Integer, List<Integer>> numbersMap = new LinkedHashMap<Integer,
		// List<Integer>>();
		// int i = 1;
		// int key = 0;
		// List<Integer> tempNumList = new ArrayList<Integer>();
		// for (Integer num : pagesNumList) {
		// if (i <= pagesNumberListSize) {
		// tempNumList.add(num);
		// } else {
		// numbersMap.put(key, tempNumList);
		// key++;
		// tempNumList = new ArrayList<Integer>();
		// i = 1;
		// tempNumList.add(num);
		// }
		// i++;
		// }
		// numbersMap.put(key, tempNumList);
		// Integer targetKey = nowShowItemPage / pagesNumberListSize;
		// if (nowShowItemPage % pagesNumberListSize == 0)
		// targetKey -= 1;
		// sessionSet("showItemPagesList", numbersMap.get(targetKey));
		// }
		// if (pagesNumList.size() > 0)
		// sessionSet("latestPage", pagesNumList.get(pagesNumList.size() - 1));
		//
		// int from = length * (nowShowItemPage - 1);
		// LinkedHashMap<String, Object> orderMap = new LinkedHashMap<String,
		// Object>();
		// orderMap.put("sysid", "desc");
		//
		// // List<BhsVendorProfile> itemList =
		// // cloudDao.queryTable(sessionFactory, BhsVendorProfile.class,
		// // groupItem, orderMap, from,length);
		// LinkedHashMap<String, Map<String, String>> targetMap = new
		// LinkedHashMap<String, Map<String, String>>();
		//
		// for (int i = 0; i < list.size(); i++) {
		// if (list.get(i) == null) {
		// break;
		// }
		// // System.out.println("i=" + i);
		// Object[] oArr = (Object[]) list.get(i);
		//
		// BhsVendorProfile h = (BhsVendorProfile) oArr[0];
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
		// return "outerMain";
	}
}