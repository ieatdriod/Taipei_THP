package tw.com.mitac.thp.action;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsExhibitionVendor;
import tw.com.mitac.thp.bean.CpsExhibitionVendorItem;

public class FrontCpsExhibitionVendorAction extends BasisTenancyAction {
	public String outerVendorMain() {
		String exhibitionVendorSysid = request.getParameter("exhibitionVendorSysid");

		List<CpsExhibitionVendorItem> cpsExhibitionVendorItemList = getCpsExhibitionVendorItem(exhibitionVendorSysid);

		request.setAttribute("exhibitionVendorSysid", exhibitionVendorSysid);
		request.setAttribute("cpsExhibitionVendorItemList", cpsExhibitionVendorItemList);

		List<CpsExhibitionVendor> l = cloudDao.queryTable(sf(), CpsExhibitionVendor.class, new QueryGroup(
				new QueryRule[] { new QueryRule(PK, exhibitionVendorSysid) }), null, null, null);
		CpsExhibitionVendor bean = l.get(0);
		addMultiLan(new Object[] { bean }, sf(), CpsExhibitionVendor.class);
		request.setAttribute("bean", bean);
		return SUCCESS;
	}

	/**
	 * 展覽廠商明細檔
	 * 
	 * @param exhibitionVendorSysid
	 * @return List<CpsExhibitionVendorItem>
	 */
	protected List<CpsExhibitionVendorItem> getCpsExhibitionVendorItem(String exhibitionVendorSysid) {
		List<CpsExhibitionVendorItem> cpsExhibitionVendorItemList = cloudDao.queryTable(sf(),
				CpsExhibitionVendorItem.class, new QueryGroup(new QueryRule(FK, exhibitionVendorSysid)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) }, 0, null);
		return cpsExhibitionVendorItemList;
	}
}