package tw.com.mitac.thp.action;

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
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsDoctor;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsMarquee;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

@SuppressWarnings({ "unchecked", "rawtypes" })
/** MTS_FW_002_醫療團隊介紹 */
public class IndexMtsVendorAction extends BasisTenancyAction {
	/**
	 * @param k
	 *            MtsVendorProfile.sysid
	 * @param mtsVendorSysid
	 *            CpsVendor.sysid
	 */
	public String indexMtsVendorPage() {
		MtsVendorProfile mtsVendorProfile = null;
		String profileSysid = request.getParameter("k");
		String mtsVendorSysid = request.getParameter("mtsVendorSysid");

		if (StringUtils.isNotBlank(profileSysid)) {
			mtsVendorProfile = getDataMtsVendorProfileTable().get(profileSysid);
			if (mtsVendorProfile != null)
				mtsVendorSysid = mtsVendorProfile.getVendorSysid();
		}

		if (mtsVendorProfile == null) {
			if(StringUtils.isNotBlank(mtsVendorSysid)){
			List<MtsVendorProfile> mtsVendorProfileList = cloudDao.query(sf(), MtsVendorProfile.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", mtsVendorSysid)), null, null, null);
			addMultiLan(mtsVendorProfileList, sf(), MtsVendorProfile.class);
				if(mtsVendorProfileList.size()>0){
					mtsVendorProfile = mtsVendorProfileList.get(0);
				}else{
					redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
					return REDIRECT_PAGE;
				}
				
			}else{
				redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
				return REDIRECT_PAGE;
			}
			
			
		}

		return execute(mtsVendorSysid, mtsVendorProfile, true);
	}

	protected String execute(String cpsVendorSysid, MtsVendorProfile mtsVendorProfile, boolean isClick) {

		List<CpsVendor> cpsVendorList = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(new QueryRule(PK,
				cpsVendorSysid)), new QueryOrder[0], null, null);
		addMultiLan(cpsVendorList, sf(), CpsVendor.class);
		request.setAttribute("cpsVendor", cpsVendorList.get(0));

		request.setAttribute("isEnabledConversations", cpsVendorList.get(0).getIsEnabledConversations());

		request.setAttribute("mtsVendorSysid", mtsVendorProfile.getVendorSysid());
		request.setAttribute("mtsVendorProfile", mtsVendorProfile);
		// 20160706 大大直接取資源使用，為了圖片的SYSID
		request.setAttribute("mtsVendorSysidForImg", mtsVendorProfile.getSysid());

		// 團隊介紹
		Map<String, Map<String, String>> mtsAdsCProfile = findMtsAdsCProfile(mtsVendorProfile.getSysid());
		sessionSet("mtsAdsCList", mtsAdsCProfile);

		// 醫生介紹
		List<MtsDoctor> vendorProfileMap = findDoctorProfile(cpsVendorSysid);
		addMultiLan(vendorProfileMap, sf(), MtsDoctor.class);
		session.put("mtsDoctorAList", vendorProfileMap);

		for (MtsDoctor mtsDoctor : vendorProfileMap) {
			/** 換行資源 */
			String lineFeed = new Character((char) 13).toString() + new Character((char) 10).toString();

			/** 名稱換行處理 */
			if (StringUtils.isNotBlank(mtsDoctor.getDoctorName())) {
				String ona = mtsDoctor.getDoctorName().replace(lineFeed, "<br/>");
				request.setAttribute("doctorName" + mtsDoctor.getSysid(), ona);
			} else {
				request.setAttribute("doctorName" + mtsDoctor.getSysid(), "");
			}
			/** 介紹換行處理 */
			if (StringUtils.isNotBlank(mtsDoctor.getDoctorIntroduction1())) {
				String oit = mtsDoctor.getDoctorIntroduction1().replace(lineFeed, "<br/>");
				request.setAttribute("doctorIntroduction" + mtsDoctor.getSysid(), oit);
			} else {
				request.setAttribute("doctorIntroduction" + mtsDoctor.getSysid(), "");
			}
		}

		// 跑馬燈
		List<MtsMarquee> mtsMarqueeList = cloudDao.query(sf(), MtsMarquee.class, new QueryGroup(new QueryRule(
				IS_ENABLED, true), new QueryRule(SOURCE_ID, cpsVendorSysid)), new QueryOrder[] { new QueryOrder(
				DATA_ORDER) }, null, null);
		addMultiLan(mtsMarqueeList, sf(), MtsMarquee.class);
		session.put("mtsMarqueeList", mtsMarqueeList);

		// International Collaboration Projects
		List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class, new QueryGroup(
				new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", cpsVendorSysid)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);
		session.put("mtsCooperationList", mtsCooperationList);

		// Medical Services
		List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(new QueryRule(
				IS_ENABLED, true), new QueryRule("vendorSysid", cpsVendorSysid)), new QueryOrder[] { new QueryOrder(
				DATA_ORDER) }, null, null);
		addMultiLan(mtsProductsList, sf(), MtsProducts.class);
		session.put("mtsProductsList", mtsProductsList);

		// Hightlights
		List<MtsHighlight> mtsHighlightList = cloudDao.queryTable(sf(), MtsHighlight.class, new QueryGroup(
				new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", cpsVendorSysid)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		addMultiLan(mtsHighlightList, sf(), MtsHighlight.class);
		session.put("mtsHighlightList", mtsHighlightList);

		request.setAttribute("rankList",
				createRankList(MtsVendorProfile.class, "vendorImageSummary", mtsVendorProfile.getSysid()));
		if (isClick)
			addClickHistory(MtsVendorProfile.class, mtsVendorProfile.getVendorSysid(), mtsVendorProfile.getSysid());

		return SUCCESS;
	}

	/** 團隊介紹圖片 */
	protected Map<String, Map<String, String>> findMtsAdsCProfile(String vendorProfileSysid) {
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT {a.*}");
		sb.append("  FROM mts_ads_c a");
		sb.append(" where 1=1 ");
		// sb.append(" and a.banner_sysid=:vendorProfileSysid");
		sb.append("   and a.source_sysid=:vendorProfileSysid");
		// sb.append(" and a.ads_type=:ads_type");
		// sb.append(" and a.source_sysid = :vendor_sysid");

		Session session = sf().openSession();
		Query query = session.createSQLQuery(sb.toString()).addEntity("a", MtsAdsC.class);
		// query.setParameter("vendor_sysid", vendorSysid);
		query.setParameter("vendorProfileSysid", vendorProfileSysid);
		// query.setParameter("ads_type", "T");
		List<?> list = query.list();
		session.close();

		Map<String, Map<String, String>> targetMap = new LinkedHashMap<String, Map<String, String>>();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {
				break;
			}
			MtsAdsC ma = (MtsAdsC) list.get(i);

			Map<String, String> vendorMap = new HashMap();
			vendorMap.put("vendorBannerType", ma.getBannerType());
			vendorMap.put("vendorImg", ma.getAdsImage());
			vendorMap.put("adsUrl", ma.getAdsUrl());
			vendorMap.put("sysid", ma.getSysid());
			vendorMap.put("adsTw", ma.getAdsTw().toString());
			vendorMap.put("adsCn", ma.getAdsCn().toString());
			vendorMap.put("adsUs", ma.getAdsUs().toString());

			targetMap.put(ma.getSysid(), vendorMap);
		}
		return targetMap;
	}

	/** 團隊醫生 */
	protected List<MtsDoctor> findDoctorProfile(String vendorSysid) {
		List<MtsDoctor> MtsDoctorItemList = cloudDao.query(sf(), MtsDoctor.class, new QueryGroup(new QueryRule(
				"vendorSysid", vendorSysid)), new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
		return MtsDoctorItemList;
	}

	/**
	 * 判定跟MtsVendorProfile做相反的事情 20160822 團隊醫生Demo
	 */
	protected String mtsDoctorDemo(MtsDoctor mtsDoctor, boolean isClick) {

		request.setAttribute("isEnabledConversations", false);

		List<MtsVendorProfile> mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class, new QueryGroup(
				new QueryRule("vendorSysid", mtsDoctor.getVendorSysid())), new QueryOrder[0], null, null);
		addMultiLan(mtsVendorProfileList, sf(), MtsVendorProfile.class);

		List<CpsVendor> cpsVendorList = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(new QueryRule(PK,
				mtsDoctor.getVendorSysid())), new QueryOrder[0], null, null);
		addMultiLan(cpsVendorList, sf(), CpsVendor.class);

		request.setAttribute("cpsVendor", cpsVendorList.get(0));
		if (mtsVendorProfileList.size() > 0) {
			request.setAttribute("mtsVendorSysid", mtsVendorProfileList.get(0).getVendorSysid());
			request.setAttribute("mtsVendorProfile", mtsVendorProfileList.get(0));

			// 20160706 大大直接取資源使用，為了圖片的SYSID
			request.setAttribute("mtsVendorSysidForImg", mtsVendorProfileList.get(0).getSysid());

			// 團隊介紹
			Map<String, Map<String, String>> mtsAdsCProfile = findMtsAdsCProfile(mtsVendorProfileList.get(0).getSysid());
			sessionSet("mtsAdsCList", mtsAdsCProfile);

			// 跑馬燈
			List<MtsMarquee> mtsMarqueeList = cloudDao.query(sf(), MtsMarquee.class, new QueryGroup(new QueryRule(
					IS_ENABLED, true), new QueryRule(SOURCE_ID, mtsDoctor.getVendorSysid())),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsMarqueeList, sf(), MtsMarquee.class);
			session.put("mtsMarqueeList", mtsMarqueeList);

			// International Collaboration Projects
			List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", mtsDoctor.getVendorSysid())),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);
			session.put("mtsCooperationList", mtsCooperationList);

			// Medical Services
			List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", mtsDoctor.getVendorSysid())),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsProductsList, sf(), MtsProducts.class);
			session.put("mtsProductsList", mtsProductsList);

			// Hightlights
			List<MtsHighlight> mtsHighlightList = cloudDao.queryTable(sf(), MtsHighlight.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", mtsDoctor.getVendorSysid())),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsHighlightList, sf(), MtsHighlight.class);
			session.put("mtsHighlightList", mtsHighlightList);

			request.setAttribute(
					"rankList",
					createRankList(MtsVendorProfile.class, "vendorImageSummary", mtsVendorProfileList.get(0).getSysid()));
			if (isClick)
				addClickHistory(MtsVendorProfile.class, mtsVendorProfileList.get(0).getVendorSysid(),
						mtsVendorProfileList.get(0).getSysid());

			// 醫生介紹
			session.put("mtsDoctorAList", mtsDoctor);

			/** 換行資源 */
			String lineFeed = new Character((char) 13).toString() + new Character((char) 10).toString();

			/** 名稱換行處理 */
			if (StringUtils.isNotBlank(mtsDoctor.getDoctorName())) {
				String ona = mtsDoctor.getDoctorName().replace(lineFeed, "<br/>");
				request.setAttribute("doctorName" + mtsDoctor.getSysid(), ona);
			} else {
				request.setAttribute("doctorName" + mtsDoctor.getSysid(), "");
			}
			/** 介紹換行處理 */
			if (StringUtils.isNotBlank(mtsDoctor.getDoctorIntroduction1())) {
				String oit = mtsDoctor.getDoctorIntroduction1().replace(lineFeed, "<br/>");
				request.setAttribute("doctorIntroduction" + mtsDoctor.getSysid(), oit);
			} else {
				request.setAttribute("doctorIntroduction" + mtsDoctor.getSysid(), "");
			}

		}
		return SUCCESS;
	}
}