package tw.com.mitac.thp.action;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsInfoLink;

import com.opensymphony.xwork2.ActionContext;

/** MTS_FW_008_Highlight介紹 */
public class IndexMtsPageHighLightAction extends BasisTenancyAction {

	protected MtsHighlight bean;

	public MtsHighlight getBean() {
		return bean;
	}

	public void setBean(MtsHighlight bean) {
		this.bean = bean;
	}

	public String demo() {
		logger.debug("bean:" + ReflectionToStringBuilder.toString(bean, ToStringStyle.MULTI_LINE_STYLE));

		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}

		return execute(bean, false);
	}

	public String indexMtsHighLightPage() {
		MtsHighlight mtsHightlight = null;
		String mtsHightlightSysid = request.getParameter("mtsHightlightSysid");

		if (mtsHightlight == null) {
			if(StringUtils.isNotBlank(mtsHightlightSysid) ){
			List<MtsHighlight> mtsHightlightList = cloudDao.query(sf(), MtsHighlight.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule(PK, mtsHightlightSysid)), null, null, null);
			addMultiLan(mtsHightlightList, sf(), MtsHighlight.class);
				if (mtsHightlightList.size() > 0) {
					mtsHightlight = mtsHightlightList.get(0);
				}else{
					redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
					return REDIRECT_PAGE;
				}
			}else{
				redirectPage = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()+ getMtsPath();
				return REDIRECT_PAGE;
			}
		}

		return execute(mtsHightlight, true);
	}

	protected String execute(MtsHighlight mtsHightlight, Boolean isClick) {

		try {
			List<MtsAdsC> mtsAdsCList = cloudDao.query(sf(), MtsAdsC.class, new QueryGroup(new QueryRule("parentSysid",
					mtsHightlight.getSysid()), new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(
					DATA_ORDER) }, null, null);
			addMultiLan(mtsAdsCList, sf(), MtsAdsC.class);
			request.setAttribute("mtsAdsCList", mtsAdsCList);

			List<MtsInfoLink> mtsInfoLinkList = cloudDao.query(sf(), MtsInfoLink.class, new QueryGroup(new QueryRule(
					"parentSysid", mtsHightlight.getSysid())), null, null, null);
			addMultiLan(mtsInfoLinkList, sf(), MtsInfoLink.class);
			request.setAttribute("mtsInfoLinkList", mtsInfoLinkList);

			List<CpsVendor> vendor = cloudDao.query(sf(), CpsVendor.class, new QueryGroup(new QueryRule(PK,
					mtsHightlight.getVendorSysid())), null, null, null);
			addMultiLan(vendor, sf(), CpsVendor.class);
			request.setAttribute("vendorName", vendor.get(0).getName());

			request.setAttribute("vendorSysid", mtsHightlight.getVendorSysid());
			request.setAttribute("mtsHighlightSysid", mtsHightlight.getSysid());
			request.setAttribute("mtsHighlightName", mtsHightlight.getName());
			request.setAttribute("mtsHighlightSummary", mtsHightlight.getHighlightSummary());
			request.setAttribute("mtsHighlightProfileFull", mtsHightlight.getHighlightProfileFull());
			request.setAttribute("mtsHighlightText2", mtsHightlight.getHighlightText2());
			request.setAttribute("articleTypeShowIndex", mtsHightlight);

			request.setAttribute("rankList",
					createRankList(MtsHighlight.class, "highlightSummaryImg", mtsHightlight.getSysid()));

			if (isClick)
				addClickHistory(MtsHighlight.class, mtsHightlight.getVendorSysid(), mtsHightlight.getSysid());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return SUCCESS;
	}
}