package tw.com.mitac.thp.action;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsExhibitionList;

public class FrontCpsExhibitionListAction extends BasisTenancyAction {
	/**
	 * 預告展覽介紹EDM
	 */
	public String execute() {
		String sysid = request.getParameter("sysid");

		List<CpsExhibitionList> l = cloudDao.query(sf(), CpsExhibitionList.class, new QueryGroup(new QueryRule(PK,
				sysid)), null, null, null);
		request.setAttribute("item", l.get(0));
		addMultiLan(l, sf(), CpsExhibitionList.class);
		return SUCCESS;
	}
}