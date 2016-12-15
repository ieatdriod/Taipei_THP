package tw.com.mitac.thp.action;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsInfoLink;
import tw.com.mitac.thp.bean.MtsMenu;
import tw.com.mitac.thp.bean.MtsMenuLink;
import tw.com.mitac.thp.bean.MtsProducts;

/** MTS_FW_003_特色服務 */
public class IndexMtsPageSpecialAction extends BasisTenancyAction {
	public String execute() {
		String mtsProductsSysid = request.getParameter("mtsProductsSysid");
		String mtsType = request.getParameter("Type");
		if (StringUtils.isNotBlank(mtsProductsSysid)) {
			// --------------MTS 特色服務------------------------
			try {

				if ("S".equals(mtsType)) {
					List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
							new QueryRule(PK, mtsProductsSysid)), new QueryOrder[] { new QueryOrder(DATA_ORDER) },
							null, null);
					addMultiLan(mtsProductsList, sf(), MtsProducts.class);
					if(mtsProductsList.size()>0){
					MtsProducts mtsProducts = mtsProductsList.get(0);

					mtsProductsPage(mtsProducts, mtsType, true);
					}else{
						redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
						return REDIRECT_PAGE;
					}

				} else if ("I".equals(mtsType)) {
					List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
							new QueryGroup(new QueryRule(PK, mtsProductsSysid), new QueryRule(IS_ENABLED, true)),
							new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
					addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);
					if(mtsCooperationList.size()>0){
					MtsCooperation mtsCooperation = mtsCooperationList.get(0);

					mtsCooperationPage(mtsCooperation, mtsType, true);
					}else{
						redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
						return REDIRECT_PAGE;
					}
				}else{
					redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
					return REDIRECT_PAGE;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
			return REDIRECT_PAGE;
		}

		return SUCCESS;
	}

	protected void doMenu(String sysid) {
		List<String> menuSysidList = (List<String>) cloudDao.findProperty(sf(), MtsMenuLink.class, new QueryGroup(
				new QueryRule(FK, sysid)), new QueryOrder[0], false, "menuSysid");
		// 將被勾選(3,4層)的上層也查出來
		Set<String> menuSysidSet = new HashSet(menuSysidList);
		Collection<String> sub = menuSysidList;
		while (sub.size() > 0) {
			sub = (Collection<String>) cloudDao.findProperty(sf(), MtsMenu.class, new QueryGroup(new QueryRule(PK, IN,
					sub), new QueryRule("parentMtsMenuSysid", NN, null), new QueryRule("parentMtsMenuSysid", NE, "")),
					new QueryOrder[0], true, "parentMtsMenuSysid");
			menuSysidSet.addAll(sub);
		}
		request.setAttribute("menuSysidSet", menuSysidSet);
	}

	protected String mtsProductsPage(MtsProducts mtsProducts, String type, boolean isClick) {
		doMenu(mtsProducts.getSysid());

		request.setAttribute("mtsProductsSysid", mtsProducts.getSysid());
		request.setAttribute("vendorSysid", mtsProducts.getVendorSysid());
		request.setAttribute("mtsProductsType", type);
		request.setAttribute("displayName", mtsProducts.getName());
		request.setAttribute("displaySummary", mtsProducts.getProductsProfileSummary());
		request.setAttribute("mtsProductsProfileFull", mtsProducts.getProductsProfileFull());
		request.setAttribute("mtsProductsText2", mtsProducts.getProductsText2());
		request.setAttribute("mtsProductsText3", mtsProducts.getProductsText3());
		request.setAttribute("mtsProductsText4", mtsProducts.getProductsText4());
		request.setAttribute("mtsProductsText5", mtsProducts.getProductsText5());

		request.setAttribute("rankList",
				createRankList(MtsProducts.class, "productsImageSummary1", mtsProducts.getSysid()));

		List<MtsInfoLink> mtsInfoLinkList = cloudDao.query(sf(), MtsInfoLink.class, new QueryGroup(new QueryRule(FK,
				mtsProducts.getSysid())), null, null, null);
		addMultiLan(mtsInfoLinkList, sf(), MtsInfoLink.class);
		request.setAttribute("mtsInfoLinkList", mtsInfoLinkList);

		List<MtsAdsC> mtsAdsCList = cloudDao.query(sf(), MtsAdsC.class,
				new QueryGroup(new QueryRule(FK, mtsProducts.getSysid()), new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		addMultiLan(mtsAdsCList, sf(), MtsAdsC.class);
		request.setAttribute("mtsAdsCList", mtsAdsCList);

		if (isClick) {
			addClickHistory(MtsProducts.class, mtsProducts.getVendorSysid(), mtsProducts.getSysid());
		}

		String vendorSysid = (String) request.getAttribute("vendorSysid");
		CpsVendor cpsVendor = cloudDao.get(sf(), CpsVendor.class, vendorSysid);
		addMultiLan(new Object[] { cpsVendor }, sf(), CpsVendor.class);
		request.setAttribute("cpsVendor", cpsVendor);

		return SUCCESS;
	}

	protected String mtsCooperationPage(MtsCooperation mtsCooperation, String type, boolean isClick) {
		doMenu(mtsCooperation.getSysid());

		request.setAttribute("mtsProductsSysid", mtsCooperation.getSysid());
		request.setAttribute("vendorSysid", mtsCooperation.getVendorSysid());
		request.setAttribute("mtsProductsType", type);
		request.setAttribute("displayName", mtsCooperation.getCooperationName());
		request.setAttribute("displaySummary", mtsCooperation.getCooperationSummary());
		request.setAttribute("mtsCooperationProfileFull", mtsCooperation.getCooperationProfileFull());
		request.setAttribute("mtsCooperationText2", mtsCooperation.getCooperationText2());
		request.setAttribute("mtsCooperationText3", mtsCooperation.getCooperationText3());

		request.setAttribute("rankList",
				createRankList(MtsCooperation.class, "cooperationSummaryImg", mtsCooperation.getSysid()));

		List<MtsInfoLink> mtsInfoLinkList = cloudDao.query(sf(), MtsInfoLink.class, new QueryGroup(new QueryRule(FK,
				mtsCooperation.getSysid())), null, null, null);
		addMultiLan(mtsInfoLinkList, sf(), MtsInfoLink.class);
		request.setAttribute("mtsInfoLinkList", mtsInfoLinkList);

		List<MtsAdsC> mtsAdsCList = cloudDao.query(sf(), MtsAdsC.class,
				new QueryGroup(new QueryRule(FK, mtsCooperation.getSysid()), new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		addMultiLan(mtsAdsCList, sf(), MtsAdsC.class);
		request.setAttribute("mtsAdsCList", mtsAdsCList);

		if (isClick) {
			addClickHistory(MtsCooperation.class, mtsCooperation.getVendorSysid(), mtsCooperation.getSysid());
		}

		String vendorSysid = (String) request.getAttribute("vendorSysid");
		CpsVendor cpsVendor = cloudDao.get(sf(), CpsVendor.class, vendorSysid);
		addMultiLan(new Object[] { cpsVendor }, sf(), CpsVendor.class);
		request.setAttribute("cpsVendor", cpsVendor);

		return SUCCESS;
	}

}