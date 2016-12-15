package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMarquee;
import tw.com.mitac.thp.bean.BhsOperate;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsVendor;

/** BHS_FW_003_標竿企業介紹 */
public class FrontBhsVendorProfileAction_BHS_FW_003 extends BasisTenancyAction {

	public String outerItemSingle() {

		BhsVendorProfile bhsVendorProfile = null;// 基本資料
		// BhsAdsC bhsAdsC=null;// 大廣告
		// BhsOperate bhsOperate=null;// 企業經營團隊維護
		// BhsOperateItem bhsOperateItem=null;// 企業經營團隊成員介紹
		// BhsTechnology bhsTechnology=null;// 核心技術
		// BhsProducts bhsProducts=null;// 特色產品
		// BhsMarquee bhsMarquee=null;// 最新消息

		String profileSysid = request.getParameter("vendorSysid");

		if (StringUtils.isNotBlank(profileSysid)) {
			
			// 從前一頁取得公司基本資料 >>
			List<BhsVendorProfile> l = cloudDao.queryTable(sf(), BhsVendorProfile.class,
					new QueryGroup(new QueryRule(PK, profileSysid), new QueryRule(IS_ENABLED, true)), null, null, null);
			if (l.size() > 0) {
				addMultiLan(l, sf(), BhsVendorProfile.class);
				bhsVendorProfile = l.get(0);

				execute(bhsVendorProfile, true);
			}else{
				redirectPage = request.getScheme() + "://" + request.getHeader("host") +request.getContextPath()+ getBhsPath();
				return REDIRECT_PAGE;
			}
			
		}else{
			redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath() + getBhsPath();
			return REDIRECT_PAGE;
		}

		return SUCCESS;
	}

	protected String execute(BhsVendorProfile bhsVendorProfile, Boolean isClick) {
		if (bhsVendorProfile != null) {
			List<CpsVendor> CpsVendorList = cloudDao.queryTable(sf(), CpsVendor.class,
					new QueryGroup(new QueryRule(PK, bhsVendorProfile.getVendorSysid())), null, null, null);
			addMultiLan(CpsVendorList, sf(), CpsVendor.class);
			request.setAttribute("isEnabledConversations", CpsVendorList.get(0).getIsEnabledConversations());

			String vendorSysid = bhsVendorProfile.getVendorSysid();// 基本資料 >>
			// 供應商系統代號(vendorSysid)
			request.setAttribute("bhsVendorProfile", bhsVendorProfile);

			CpsVendor cpsVendor = cloudDao.get(sf(), CpsVendor.class, vendorSysid);
			addMultiLan(new Object[] { cpsVendor }, sf(), CpsVendor.class);
			request.setAttribute("cpsVendor", cpsVendor);

			// 大廣告 >> bannerSysid
			List<BhsAdsC> bhsAdsCList = cloudDao.query(sf(), BhsAdsC.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true),
							new QueryRule("parentSysid", bhsVendorProfile.getSysid())),
					new QueryOrder[] {}, null, null);
			request.setAttribute("bhsAdsCList", bhsAdsCList);

			// 取得最新消息(代入供應商系統代號) >> marqueeText
			List<BhsMarquee> bhsMarqueeList = cloudDao.queryTable(sf(), BhsMarquee.class,
					new QueryGroup(new QueryRule("sourceId", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(bhsMarqueeList, sf(), BhsMarquee.class);
			request.setAttribute("bhsMarqueeList", bhsMarqueeList);

			// 企業經營團隊維護 >> name,operateImg,operateIntroduction
			Collection<Map<String, String>> bhsOperateListResults = new ArrayList<Map<String, String>>();

			session.remove("bhsOperateList");
			List<BhsOperate> bhsOperateList = cloudDao.queryTable(sf(), BhsOperate.class,
					new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(bhsOperateList, sf(), BhsOperate.class);
			request.setAttribute("bhsOperateList", bhsOperateList);

			for (BhsOperate bhsOperate : bhsOperateList) {

				/** 換行資源 */
				String lineFeed = new Character((char) 13).toString() + new Character((char) 10).toString();

				/** 名稱換行處理 */
				if (StringUtils.isNotBlank(bhsOperate.getOperateName())) {
					String ona = bhsOperate.getOperateName().replace(lineFeed, "<br/>");
					request.setAttribute("operateName" + bhsOperate.getSysid(), ona);
				} else {
					request.setAttribute("operateName" + bhsOperate.getSysid(), "");
				}

				/** 介紹換行處理 */
				if (StringUtils.isNotBlank(bhsOperate.getOperateIntroduction1())) {
					String oit = bhsOperate.getOperateIntroduction1().replace(lineFeed, "<br/>");
					request.setAttribute("operateIntroduction" + bhsOperate.getSysid(), oit);
				} else {
					request.setAttribute("operateIntroduction" + bhsOperate.getSysid(), "");
				}
			}

			// 核心技術 >> technologySummaryImg
			session.remove("bhsTechnologyList");
			List<BhsTechnology> bhsTechnologyList = cloudDao.queryTable(sf(), BhsTechnology.class,
					new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(bhsTechnologyList, sf(), BhsTechnology.class);
			request.setAttribute("bhsTechnologyList", bhsTechnologyList);

			// 特色產品 >> name,productsImageSummary1
			session.remove("bhsProductsList");
			List<BhsProducts> bhsProductsList = cloudDao.queryTable(sf(), BhsProducts.class,
					new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			request.setAttribute("bhsProductsList", bhsProductsList);
			addMultiLan(bhsProductsList, sf(), BhsProducts.class);

			// Hightlights
			List<BhsHighlight> highlightList = cloudDao.queryTable(sf(), BhsHighlight.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", vendorSysid)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(highlightList, sf(), BhsHighlight.class);
			request.setAttribute("highlightList", highlightList);

			request.setAttribute("rankList",
					createRankList(BhsVendorProfile.class, "vendorImageSummary", bhsVendorProfile.getSysid()));

			if (isClick)
				addClickHistory(BhsVendorProfile.class, bhsVendorProfile.getVendorSysid(), bhsVendorProfile.getSysid());
		}
		return SUCCESS;
	}

	/**
	 * 判定跟bhsVendorProfile做相反的事情 20160822 仿照團隊醫生Demo
	 */
	protected String bhsOperateDemo(BhsOperate bhsOperate, Boolean isClick) {

		request.setAttribute("isEnabledConversations", false);

		List<BhsVendorProfile> bhsVendorProfileList = cloudDao.queryTable(sf(), BhsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", bhsOperate.getVendorSysid())), new QueryOrder[0], null,
				null);
		addMultiLan(bhsVendorProfileList, sf(), BhsVendorProfile.class);

		if (bhsVendorProfileList.size() > 0) {
			String vendorSysid = bhsVendorProfileList.get(0).getVendorSysid();// 基本資料
																				// >>
			// 供應商系統代號(vendorSysid)
			request.setAttribute("bhsVendorProfile", bhsVendorProfileList.get(0));

			CpsVendor cpsVendor = cloudDao.get(sf(), CpsVendor.class, vendorSysid);
			addMultiLan(new Object[] { cpsVendor }, sf(), CpsVendor.class);
			request.setAttribute("cpsVendor", cpsVendor);

			// 大廣告 >> bannerSysid
			List<BhsAdsC> bhsAdsCList = cloudDao.query(sf(), BhsAdsC.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true),
							new QueryRule("parentSysid", bhsVendorProfileList.get(0).getSysid())),
					new QueryOrder[] {}, null, null);
			request.setAttribute("bhsAdsCList", bhsAdsCList);

			// 取得最新消息(代入供應商系統代號) >> marqueeText
			List<BhsMarquee> bhsMarqueeList = cloudDao.queryTable(sf(), BhsMarquee.class,
					new QueryGroup(new QueryRule("sourceId", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(bhsMarqueeList, sf(), BhsMarquee.class);
			request.setAttribute("bhsMarqueeList", bhsMarqueeList);

			// 企業經營團隊維護 >> name,operateImg,operateIntroduction
			request.setAttribute("bhsOperateList", bhsOperate);

			// 核心技術 >> technologySummaryImg
			session.remove("bhsTechnologyList");
			List<BhsTechnology> bhsTechnologyList = cloudDao.queryTable(sf(), BhsTechnology.class,
					new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] {}, null, null);
			addMultiLan(bhsTechnologyList, sf(), BhsTechnology.class);
			request.setAttribute("bhsTechnologyList", bhsTechnologyList);

			// 特色產品 >> name,productsImageSummary1
			session.remove("bhsProductsList");
			List<BhsProducts> bhsProductsList = cloudDao.queryTable(sf(), BhsProducts.class,
					new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)), null,
					null, null);
			request.setAttribute("bhsProductsList", bhsProductsList);
			addMultiLan(bhsProductsList, sf(), BhsProducts.class);

			// Hightlights
			List<BhsHighlight> highlightList = cloudDao.queryTable(sf(), BhsHighlight.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true), new QueryRule("vendorSysid", vendorSysid)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(highlightList, sf(), BhsHighlight.class);
			request.setAttribute("highlightList", highlightList);

			request.setAttribute("rankList", createRankList(BhsVendorProfile.class, "vendorImageSummary",
					bhsVendorProfileList.get(0).getSysid()));

			if (isClick)
				addClickHistory(BhsVendorProfile.class, bhsVendorProfileList.get(0).getVendorSysid(),
						bhsVendorProfileList.get(0).getSysid());
		}

		/** 換行資源 */
		String lineFeed = new Character((char) 13).toString() + new Character((char) 10).toString();

		/** 名稱換行處理 */

		if (StringUtils.isNotBlank(bhsOperate.getOperateName())) {
			String ona = bhsOperate.getOperateName().replace(lineFeed, "<br/>");
			request.setAttribute("operateName" + bhsOperate.getSysid(), ona);
		} else {
			request.setAttribute("operateName" + bhsOperate.getSysid(), "");
		}
		/** 介紹換行處理 */
		if (StringUtils.isNotBlank(bhsOperate.getOperateIntroduction1())) {
			String oit = bhsOperate.getOperateIntroduction1().replace(lineFeed, "<br/>");
			request.setAttribute("operateIntroduction" + bhsOperate.getSysid(), oit);
		} else {
			request.setAttribute("operateIntroduction" + bhsOperate.getSysid(), "");
		}

		return SUCCESS;
	}
}