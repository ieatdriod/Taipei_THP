package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

public class FrontMtsProductsAction extends BasisTenancyAction {

	protected MtsProducts bean;

	public MtsProducts getBean() {
		return bean;
	}

	public void setBean(MtsProducts bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	// 特色S/案例C/醫生D介紹頁面
	public String outerItemSingle() {
		// 醫療團隊介紹頁面傳入
		String sysid = request.getParameter("sysid");

		if (StringUtils.isBlank(sysid)) {
			addActionError(getText("msg.itemLost"));
			return ERROR;
		} else {
			bean = cloudDao.get(sf(), MtsProducts.class, sysid);
			// System.out.println("bean.text2="+bean.getProductsText2());

			// --------------make appoint---------------------
			List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
					new QueryRule("vendorSysid", bean.getVendorSysid()), new QueryRule("productsType", "S")),
					new QueryOrder[0], null, null);
			Map<String, String> map = new LinkedHashMap<String, String>();
			for (MtsProducts mp : mtsProductsList)
				map.put(mp.getSysid(), mp.getName());
			treatmentMap = map;
			// System.out.println("treatmentMap="+treatmentMap.values());

			// mts_products.vendorSysid取mts_vendor_profile.sysid
			String mtsProductsVendorSysid = bean.getVendorSysid();// CpsVendor20160125114836059125968
			String mtsVendorProfileSysid = "";// MtsVendorProfile20160125114954130322473

			QueryGroup groupItem = new QueryGroup(new QueryRule("vendorSysid", CN, mtsProductsVendorSysid));

			List<MtsVendorProfile> itemList = cloudDao.queryTable(sf(), MtsVendorProfile.class, groupItem,
					new QueryOrder[] { new QueryOrder(PK, DESC) }, 0, 1);
			for (MtsVendorProfile itemBean : itemList) {
				mtsVendorProfileSysid = itemBean.getSysid();
			}
			request.setAttribute("mtsVendorProfileSysid", mtsVendorProfileSysid);
			// System.out.println("mtsVendorProfileSysid="+mtsVendorProfileSysid);

		}
		return SUCCESS;
	}

	// 特色S介紹頁面
	public String viewItemSingle() {
		// 醫療團隊介紹頁面傳入
		// String sysid = request.getParameter("sysid");

		// if (StringUtils.isBlank(sysid)) {
		// addActionError(getText("msg.itemLost"));
		// return ERROR;
		// } else {
		// bean = cloudDao.get(sf(), MtsProducts.class, sysid);
		// System.out.println("bean.text2="+bean.getProductsText2());

		// --------------make appoint---------------------
		List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(new QueryRule(
				"vendorSysid", bean.getVendorSysid()), new QueryRule("productsType", "S")), new QueryOrder[0], null,
				null);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (MtsProducts mp : mtsProductsList)
			map.put(mp.getSysid(), mp.getName());
		treatmentMap = map;
		// System.out.println("treatmentMap="+treatmentMap.values());

		// mts_products.vendorSysid取mts_vendor_profile.sysid
		String mtsProductsVendorSysid = bean.getVendorSysid();
		String mtsVendorProfileSysid = "";

		QueryGroup groupItem = new QueryGroup(new QueryRule("vendorSysid", CN, mtsProductsVendorSysid));

		List<MtsVendorProfile> itemList = cloudDao.queryTable(sf(), MtsVendorProfile.class, groupItem,
				new QueryOrder[] { new QueryOrder(PK, DESC) }, 0, 1);
		for (MtsVendorProfile itemBean : itemList) {
			mtsVendorProfileSysid = itemBean.getSysid();
		}
		request.setAttribute("mtsVendorProfileSysid", mtsVendorProfileSysid);
		// System.out.println("mtsVendorProfileSysid="+mtsVendorProfileSysid);

		// }
		return SUCCESS;
	}

	// 案例C 頁面
	public String outerItemTypeCSingle() {
		// 案例 頁面傳入
		String sysid = request.getParameter("sysid");

		if (StringUtils.isBlank(sysid)) {
			addActionError(getText("msg.itemLost"));
			return ERROR;
		} else {
			// 繼承bean,取後端資料
			bean = cloudDao.get(sf(), MtsProducts.class, sysid);
			// System.out.println("bean.text2="+bean.getProductsText2());

		}
		return SUCCESS;
	} // end outerItemTypeCSingle

	// 案例C 頁面預覽
	public String viewItemTypeCSingle() {
		// 案例 頁面傳入
		// String sysid = request.getParameter("sysid");

		// if (StringUtils.isBlank(sysid)) {
		// addActionError(getText("msg.itemLost"));
		// return ERROR;
		// } else {
		// 繼承bean,取後端資料
		// bean = cloudDao.get(sf(), MtsProducts.class, sysid);
		// System.out.println("bean.text2="+bean.getProductsText2());

		// }
		return SUCCESS;
	} // end outerItemTypeCSingle
}