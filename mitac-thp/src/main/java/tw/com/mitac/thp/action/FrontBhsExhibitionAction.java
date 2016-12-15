package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsExhibitionList;
import tw.com.mitac.thp.bean.CpsExhibitionVendor;

/** BHS_FW_005_展覽館大廳 */
@SuppressWarnings("unchecked")
public class FrontBhsExhibitionAction extends BasisTenancyAction {
	public String exhibitionInit() {
		session.remove("cpsExhibitionListFront");
		List<CpsExhibitionList> cpsExhibitionListFront = (List<CpsExhibitionList>) session
				.get("cpsExhibitionListFront");
		if (cpsExhibitionListFront == null) {
			cpsExhibitionListFront = cloudDao.query(sf(), CpsExhibitionList.class, new QueryGroup(new QueryRule(
					"exhibitionStartDate", LE, systemDate), new QueryRule("exhibitionEndDate", GE, systemDate),
					new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(cpsExhibitionListFront, sf(), CpsExhibitionList.class);
			session.put("cpsExhibitionListFront", cpsExhibitionListFront);
		}

		String cpsExhibitionShowIndex = request.getParameter("cpsExhibitionShowIndex");
		if (StringUtils.isBlank(cpsExhibitionShowIndex) && cpsExhibitionListFront.size() > 0) {
			CpsExhibitionList firstArticleType = cpsExhibitionListFront.get(0);
			cpsExhibitionShowIndex = firstArticleType.getSysid();
		}
		request.setAttribute("cpsExhibitionShowIndex", cpsExhibitionShowIndex);

		List<CpsExhibitionVendor> cpsExhibitionVendorList = cloudDao.queryTable(sf(), CpsExhibitionVendor.class,
				new QueryGroup(new QueryRule("exhibitionSysid", cpsExhibitionShowIndex),
						new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER),
						new QueryOrder(PK) }, 0, 8);
		request.setAttribute("cpsExhibitionVendorList", cpsExhibitionVendorList);
		if (cpsExhibitionVendorList.size() > 0) {
			request.setAttribute("cpsExhibitionVendorListShowIndex", cpsExhibitionVendorList.get(0).getSysid());

		}

		System.out.println("=exhibitionEnterpriseEDM=");
		return SUCCESS;
	}

	public List<CpsExhibitionList> getCpsExhibitionFutureList() {
		List<CpsExhibitionList> cpsExhibitionFutureList = (List<CpsExhibitionList>) session
				.get("cpsExhibitionFutureList" + "_" + getCookieLan());
		if (cpsExhibitionFutureList == null) {
			cpsExhibitionFutureList = cloudDao.query(sf(), CpsExhibitionList.class, new QueryGroup(new QueryRule(
					"exhibitionStartDate", GT, systemDate), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(cpsExhibitionFutureList, sf(), CpsExhibitionList.class);
			// session.put("cpsExhibitionFutureList" + "_" + getCookieLan(),
			// cpsExhibitionFutureList);
		}
		return cpsExhibitionFutureList;
	}
}