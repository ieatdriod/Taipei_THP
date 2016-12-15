package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsConfigAd;
import tw.com.mitac.thp.bean.CpsMarquee;

/**
 * 
 * @author Adair
 * 
 *         2016-06-06:新增載入時，讀取廣告設定檔、大首頁公告訊息 by Adair <BR>
 *
 */
public class FrontIndexAction extends BasisTenancyAction {
	@Override
	public String execute() {

		/**
		 * 網站廣告設定檔
		 */
		// 大首頁顯示網頁or手機BANNER版本
		List<CpsConfigAd> cpsConfigAdList = null;
		if (getIsMobile()) {
			cpsConfigAdList = jspList("PHONE");
		} else {
			cpsConfigAdList = jspList("WEB");
		}
		if (cpsConfigAdList != null) {
			// logger.debug("TO HTML");
			request.setAttribute("cpsConfigAdHtml", jspListToHtml(cpsConfigAdList));
		}

		/**
		 * 大首頁公告訊息
		 */
		List<CpsMarquee> cpsMarqueeList = findCpsMarqueeList();
		request.setAttribute("cpsMarqueeList", cpsMarqueeList);
		return SUCCESS;
	}

	protected List<CpsConfigAd> jspList(String adsDisplaySource) {
		logger.info("adsDisplaySource:" + adsDisplaySource);
		request.setAttribute("adsDisplaySource", adsDisplaySource);
		String resourceKey = "data" + CpsConfigAd.class.getSimpleName() + adsDisplaySource + "JspList";
		List<CpsConfigAd> cpsConfigAdList = (List<CpsConfigAd>) appMap().get(resourceKey);
		if (cpsConfigAdList == null) {
			cpsConfigAdList = cloudDao.queryTable(sf(), CpsConfigAd.class, new QueryGroup(new QueryRule(IS_ENABLED,
					true), new QueryRule("adsDisplaySource", adsDisplaySource)), new QueryOrder[] { new QueryOrder(
					DATA_ORDER, ASC) }, null, null);
			appMap().put(resourceKey, cpsConfigAdList);
		}
		request.setAttribute("cpsConfigAdList", cpsConfigAdList);
		// logger.info("cpsConfigAdList:" + cpsConfigAdList.size());
		return cpsConfigAdList;
	}

	protected String jspListToHtml(List<CpsConfigAd> cpsConfigAdList) {
		StringBuilder sb = new StringBuilder();
		for (CpsConfigAd cpsConfigAd : cpsConfigAdList) {
			switch (cpsConfigAd.getBannerType()) {
			case "P":
				sb.append("<li>");
				String inLi = String.format("<img src='/%s/CpsConfigAd/%s/%s' />", getWebDfImg(),
						cpsConfigAd.getSysid(), cpsConfigAd.getBannerImg());
				if (StringUtils.isNotBlank(cpsConfigAd.getBannerUrl())) {
					sb.append(String.format("<a target='_blank' href='%s'>", cpsConfigAd.getBannerUrl()));
					sb.append(inLi);
					sb.append("</a>");
				} else {
					sb.append(inLi);
				}
				sb.append("</li>");
				break;
			case "V":
				String src = cpsConfigAd.getBannerUrl();
				if (StringUtils.containsIgnoreCase(src, "youtube")) {
					if (!StringUtils.containsIgnoreCase(src, "rel=0")) {
						if (StringUtils.contains(src, "?"))
							src += "&";
						else
							src += "?";
						src += "rel=0";
					}
				}
				sb.append("<li>");
				sb.append(String.format("<iframe frameborder='0' src='%s' /></iframe>", src));
				sb.append("</li>");
				break;
			}
			sb.append("\n");
		}
		// logger.debug("sb:" + sb);
		return sb.toString();
	}

	protected List<CpsMarquee> findCpsMarqueeList() {
		List<CpsMarquee> cpsMarqueeList = new ArrayList<CpsMarquee>(getDataCpsMarqueeETable().values());
		return cpsMarqueeList;
	}
}