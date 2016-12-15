package tw.com.mitac.thp.action;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsInfoLink;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsVendor;

import com.opensymphony.xwork2.ActionContext;

public class FrontBhsHighlightAction extends BasisTenancyAction {

	protected BhsHighlight bean;

	public BhsHighlight getBean() {
		return bean;
	}

	public void setBean(BhsHighlight bean) {
		this.bean = bean;
	}

	public String demo() {
		// logger.debug("bean:" + ReflectionToStringBuilder.toString(bean,
		// ToStringStyle.MULTI_LINE_STYLE));

		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		logger.debug("aa語系:" + cookieLan);

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);

			logger.debug("aa語系:" + cookieLan);
		}

		return execute(bean, false);
	}

	public String indexBhsHighLightPage() {
		String hlSysid = request.getParameter("hlSysid");
		
		if (StringUtils.isNotBlank(hlSysid)) {
			BhsHighlight bhsHighlight = null;
			List<BhsHighlight> bhsHightLightList = cloudDao.queryTable(sf(), BhsHighlight.class,
					new QueryGroup(new QueryRule(PK, hlSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER), new QueryOrder(PK) }, 0, 8);
			addMultiLan(bhsHightLightList, sf(), BhsHighlight.class);
			if (bhsHightLightList.size() > 0) {
				bhsHighlight = bhsHightLightList.get(0);
				return execute(bhsHighlight, true);
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

		
	}

	protected String execute(BhsHighlight bhsHighlight, boolean isClick) {

		try {
			List<BhsAdsC> bhsAdsCList = cloudDao.query(sf(), BhsAdsC.class, new QueryGroup(new QueryRule("parentSysid",
					bhsHighlight.getSysid()), new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(
					DATA_ORDER) }, null, null);
			addMultiLan(bhsAdsCList, sf(), BhsAdsC.class);

			List<BhsInfoLink> bhsInfoLinkList = cloudDao.query(sf(), BhsInfoLink.class, new QueryGroup(new QueryRule(
					FK, bhsHighlight.getSysid())), null, null, null);
			addMultiLan(bhsInfoLinkList, sf(), BhsInfoLink.class);

			List<BhsVendorProfile> vendorProfile = cloudDao.queryTable(sf(), BhsVendorProfile.class, new QueryGroup(
					new QueryRule("vendorSysid", bhsHighlight.getVendorSysid())), new QueryOrder[0], null, null);
			addMultiLan(vendorProfile, sf(), BhsVendorProfile.class);

			request.setAttribute("vendorProfileSysid", vendorProfile.get(0).getSysid());
			if (vendorProfile.size() > 0) {
				logger.debug("vendorProfileSysid:" + vendorProfile.get(0).getSysid());
			} else {
				logger.warn("vendorProfile:" + null);
			}

			List<CpsVendor> vendor = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(new QueryRule(PK,
					bhsHighlight.getVendorSysid())), new QueryOrder[0], null, null);
			addMultiLan(vendor, sf(), CpsVendor.class);

			request.setAttribute("vendorProfileName", vendor.get(0).getName());

			if (vendor.size() > 0) {
				logger.debug("vendorProfileName:" + vendor.get(0).getName());
			} else {
				logger.warn("vendorProfileName:" + null);
			}

			request.setAttribute("sysid", bhsHighlight.getSysid());
			request.setAttribute("vendorSysid", bhsHighlight.getVendorSysid());
			request.setAttribute("articleTypeShowIndex", bhsHighlight.getSysid());
			request.setAttribute("bhsHighlightSysid", bhsHighlight.getSysid());
			request.setAttribute("bhsHighlightName", bhsHighlight.getName());
			request.setAttribute("bhsHighlightSummary", bhsHighlight.getHighlightSummary());
			request.setAttribute("bhsHighlightProfileFull", bhsHighlight.getHighlightProfileFull());
			request.setAttribute("bhsHighlightText2", bhsHighlight.getHighlightText2());
			request.setAttribute("bhsInfoLinkList", bhsInfoLinkList);
			request.setAttribute("bhsAdsCList", bhsAdsCList);

			request.setAttribute("rankList",
					createRankList(BhsHighlight.class, "highlightSummaryImg", bhsHighlight.getSysid()));

			if (isClick)
				addClickHistory(BhsProducts.class, bhsHighlight.getVendorSysid(), bhsHighlight.getSysid());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return SUCCESS;
	}

	/**
	 * 首頁-標竿企業簡介
	 */
	public Map<String, Map<String, String>> getBhsVendorProfile() {
		LinkedHashMap<String, Map<String, String>> targetMap = new LinkedHashMap<String, Map<String, String>>();
		String vendorSysid = request.getParameter("vendorSysid");
		StringBuffer sb = new StringBuffer();
		// SELECT b.sysid,b.vendor_name
		// FROM bhs_vendor_profile a,cps_vendor b
		// where a.vendor_sysid = b.sysid
		sb.append("SELECT {a.*}, {b.*}");
		sb.append("  FROM bhs_vendor_profile a , cps_vendor b");
		sb.append(" where 1=1 ");
		sb.append("   and a.vendor_sysid = b.sysid");

		Session session = sf().openSession();
		Query query = session.createSQLQuery(sb.toString()).addEntity("a", BhsVendorProfile.class)
				.addEntity("b", CpsVendor.class);

		List<?> list = query.list();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {
				break;
			}
			// System.out.println("i=" + i);
			Object[] oArr = (Object[]) list.get(i);

			BhsVendorProfile h = (BhsVendorProfile) oArr[0];
			CpsVendor it = (CpsVendor) oArr[1];

			Map<String, String> vendorMap = new HashMap();
			vendorMap.put("vendorName", it.getName());
			vendorMap.put("vendorSysid", it.getSysid());
			vendorMap.put("vendorImageSummary", h.getVendorImageSummary());

			targetMap.put(h.getSysid(), vendorMap);
		}
		return targetMap;

	}
}