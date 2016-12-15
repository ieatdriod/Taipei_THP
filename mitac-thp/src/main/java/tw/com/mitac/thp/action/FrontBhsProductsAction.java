package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsInfoLink;
import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.bean.BhsMenuLink;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsVendor;

/** BHS_FW_004_生技產品 */
public class FrontBhsProductsAction extends BasisTenancyAction {

	/**
	 * 傳入vendorSysid 特色產品維護bhs_products 核心技術維護bhs_technology
	 */
	public String outerItemSingle() {
		// 頁面傳入
		String dataPk = request.getParameter("vendorSysid");
		logger.debug("aaa查詢值:" + dataPk);

		if (StringUtils.isNotBlank(dataPk)) {

			// 產品P、技術T 根據字元值4位數有BHSP BHST
			if ("BHSP".equals(dataPk.substring(0, 4).toUpperCase())) {
				BhsProducts bhsProducts = null;
				bhsProducts = getDataBhsProductsTable().get(dataPk);

				if (bhsProducts == null) {
					redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
							+ getBhsPath();
					return REDIRECT_PAGE;
				} else {
					indexBhsProductsPage(bhsProducts, true);
				}
			} else if ("BHST".equals(dataPk.substring(0, 4).toUpperCase())) {
				BhsTechnology bhsTechnology = null;
				bhsTechnology = getDataBhsTechnologyTable().get(dataPk);
				if (bhsTechnology == null) {
					redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
							+ getBhsPath();
					return REDIRECT_PAGE;
				} else {
					indexBhsTechnologyPage(bhsTechnology, true);

				}

			} else {
				redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
						+ getBhsPath();
				return REDIRECT_PAGE;
			}

		} else {

			redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
					+ getBhsPath();
			return REDIRECT_PAGE;
		}
		return SUCCESS;
	}

	protected void doMenu(String sysid) {
		List<String> menuSysidList = (List<String>) cloudDao.findProperty(sf(), BhsMenuLink.class, new QueryGroup(
				new QueryRule(FK, sysid)), new QueryOrder[0], false, "menuSysid");
		// 將被勾選(3,4層)的上層也查出來
		Set<String> menuSysidSet = new HashSet(menuSysidList);
		Collection<String> sub = menuSysidList;
		while (sub.size() > 0) {
			sub = (Collection<String>) cloudDao.findProperty(sf(), BhsMenu.class, new QueryGroup(new QueryRule(PK, IN,
					sub), new QueryRule("parentBhsMenuSysid", NN, null), new QueryRule("parentBhsMenuSysid", NE, "")),
					new QueryOrder[0], true, "parentBhsMenuSysid");
			menuSysidSet.addAll(sub);
		}
		request.setAttribute("menuSysidSet", menuSysidSet);
	}

	protected String indexBhsTechnologyPage(BhsTechnology bhsTechnology, boolean isClick) {
		doMenu(bhsTechnology.getSysid());

		request.setAttribute("bhsTechnologyList", bhsTechnology);
		request.setAttribute("sysid", bhsTechnology.getSysid());
		request.setAttribute("vendorSysid", bhsTechnology.getVendorSysid());
		request.setAttribute("bhsBhsAdsCList", findBhsAdsCList(bhsTechnology.getSysid(), "T"));
		request.setAttribute("bhsBhsInfoLinkList", findBhsInfoLinkList(bhsTechnology.getSysid()));
		request.setAttribute("rankListType", "T");
		request.setAttribute("rankList",
				createRankList(BhsTechnology.class, "technologySummaryImg", bhsTechnology.getVendorSysid()));

		String vSysid = bhsTechnology.getVendorSysid();
		vendorNameKey(vSysid);

		if (isClick)
			addClickHistory(BhsTechnology.class, bhsTechnology.getVendorSysid(), bhsTechnology.getSysid());

		return SUCCESS;
	}

	protected String indexBhsProductsPage(BhsProducts bhsProducts, boolean isClick) {
		doMenu(bhsProducts.getSysid());

		request.setAttribute("bhsProductList", bhsProducts);
		request.setAttribute("sysid", bhsProducts.getSysid());
		request.setAttribute("vendorSysid", bhsProducts.getVendorSysid());
		request.setAttribute("bhsBhsAdsCList", findBhsAdsCList(bhsProducts.getSysid(), "P"));
		request.setAttribute("bhsBhsInfoLinkList", findBhsInfoLinkList(bhsProducts.getSysid()));
		request.setAttribute("rankListType", "P");
		request.setAttribute("rankList",
				createRankList(BhsProducts.class, "productsImageSummary1", bhsProducts.getVendorSysid()));

		String vSysid = bhsProducts.getVendorSysid();
		vendorNameKey(vSysid);

		if (isClick)
			addClickHistory(BhsProducts.class, bhsProducts.getVendorSysid(), bhsProducts.getSysid());

		return SUCCESS;
	}

	protected String vendorNameKey(String key) {
		// --------------treatment---------------------
		// Map<String, Map> bhsOrdersConstantMap =
		// getConstantMenu(BhsOrders.class);
		// orderTypeMap = bhsOrdersConstantMap.get("orderType");
		// request.setAttribute("vendorSysid", vendorSysid);

		// 原設計走cpsVendor名稱應是BhsVendorProfile名稱為主修正2016/8/1
		session.remove("vendorProfileSysid");
		session.remove("vendorProfileName");
		List<BhsVendorProfile> vendorProfile = cloudDao.queryTable(sf(), BhsVendorProfile.class, new QueryGroup(
				new QueryRule("vendorSysid", key)), new QueryOrder[0], null, null);
		addMultiLan(vendorProfile, sf(), BhsVendorProfile.class);
		if (vendorProfile.size() > 0) {
			logger.debug("vendorProfileSysid:" + vendorProfile.get(0).getSysid());
			request.setAttribute("vendorProfileSysid", vendorProfile.get(0).getSysid());
		} else {
			logger.warn("vendorProfile:" + null);
		}

		List<CpsVendor> vendor = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(new QueryRule(PK, key)),
				new QueryOrder[0], null, null);
		addMultiLan(vendor, sf(), CpsVendor.class);
		if (vendor.size() > 0) {
			logger.debug("vendorProfileName:" + vendor.get(0).getName());
			request.setAttribute("vendorProfileName", vendor.get(0).getName());
		} else {
			logger.warn("vendorProfileName:" + null);
		}

		return SUCCESS;
	}

	/**
	 * 特色產品維護bhs_products
	 */
	public List<BhsProducts> findBhsProductsList(String sysid) {
		List<BhsProducts> bhsProductsList = new ArrayList<BhsProducts>();
		bhsProductsList = cloudDao.queryTable(sf(), BhsProducts.class, new QueryGroup(new QueryRule(PK, sysid)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, 0, null);
		addMultiLan(bhsProductsList, sf(), BhsProducts.class);
		return bhsProductsList;
	}

	/**
	 * 核心技術維護bhs_technology
	 */
	public List<BhsTechnology> findBhsTechnologyList(String sysid) {
		List<BhsTechnology> bhsTechnologyList = new ArrayList<BhsTechnology>();
		bhsTechnologyList = cloudDao.queryTable(sf(), BhsTechnology.class, new QueryGroup(new QueryRule(PK, sysid)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, 0, null);
		addMultiLan(bhsTechnologyList, sf(), BhsTechnology.class);
		return bhsTechnologyList;
	}

	/**
	 * 廣告bhs_Ads_a
	 */
	public List<BhsAdsC> findBhsAdsCList(String sysid, String type) {
		List<BhsAdsC> bhsAdsCList = new ArrayList<BhsAdsC>();
		bhsAdsCList = cloudDao.queryTable(sf(), BhsAdsC.class, new QueryGroup(new QueryRule("parentSysid", sysid),
				new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER) }, 0, null);
		return bhsAdsCList;
	}

	/**
	 * 連結資料表bhs_Info_link
	 */
	public List<BhsInfoLink> findBhsInfoLinkList(String sysid) {
		session.remove("bhsBhsInfoLinkList");
		List<BhsInfoLink> bhsBhsInfoLinkList = new ArrayList<BhsInfoLink>();
		bhsBhsInfoLinkList = cloudDao.queryTable(sf(), BhsInfoLink.class, new QueryGroup(new QueryRule("parentSysid",
				sysid)), new QueryOrder[0], 0, null);
		addMultiLan(bhsBhsInfoLinkList, sf(), BhsInfoLink.class);
		return bhsBhsInfoLinkList;
	}
}